package com.nightwatch.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.nightwatch.emergency.EmergencyApiClient
import com.nightwatch.emergency.EmergencyEmailSender
import com.nightwatch.model.AppSettings
import com.nightwatch.model.Strings
import kotlinx.coroutines.*

class VoiceRecognitionService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private val helpDetector = HelpDetector()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isListening = false
    private var audioManager: AudioManager? = null
    private var originalVolume: Int = 0
    private var speechLanguage: String = "de-DE"

    companion object {
        const val CHANNEL_ID = "nightwatch_voice"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.nightwatch.START_LISTENING"
        const val ACTION_STOP = "com.nightwatch.STOP_LISTENING"
        const val ACTION_EMERGENCY = "com.nightwatch.EMERGENCY_DETECTED"
        const val ACTION_UPDATE_SETTINGS = "com.nightwatch.UPDATE_VOICE_SETTINGS"
        const val EXTRA_TRIGGER_WORD = "trigger_word"
        const val EXTRA_TRIGGER_COUNT = "trigger_count"
        const val EXTRA_SPEECH_LANGUAGE = "speech_language"
        const val EXTRA_API_ENDPOINT = "api_endpoint"

        fun start(context: Context) {
            val settings = AppSettings.load(context)
            val intent = Intent(context, VoiceRecognitionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TRIGGER_WORD, settings.triggerWord)
                putExtra(EXTRA_TRIGGER_COUNT, settings.triggerRepetitions)
                putExtra(EXTRA_SPEECH_LANGUAGE, settings.language.speechCode)
                putExtra(EXTRA_API_ENDPOINT, settings.apiEndpoint)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, VoiceRecognitionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun updateSettings(context: Context, settings: AppSettings) {
            val intent = Intent(context, VoiceRecognitionService::class.java).apply {
                action = ACTION_UPDATE_SETTINGS
                putExtra(EXTRA_TRIGGER_WORD, settings.triggerWord)
                putExtra(EXTRA_TRIGGER_COUNT, settings.triggerRepetitions)
                putExtra(EXTRA_SPEECH_LANGUAGE, settings.language.speechCode)
                putExtra(EXTRA_API_ENDPOINT, settings.apiEndpoint)
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        muteBeep()
        helpDetector.listener = object : HelpDetector.Listener {
            override fun onHelpDetected() {
                onEmergencyDetected()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Apply settings from intent
        intent?.let { applySettings(it) }

        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, createNotification())
                startListening()
            }
            ACTION_STOP -> {
                stopListening()
                stopForeground(true)
                stopSelf()
            }
            ACTION_UPDATE_SETTINGS -> {
                // Restart listening with new language if needed
                if (isListening) {
                    speechRecognizer?.stopListening()
                    speechRecognizer?.destroy()
                    speechRecognizer = null
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                    startRecognition()
                }
            }
        }
        return START_STICKY
    }

    private fun applySettings(intent: Intent) {
        intent.getStringExtra(EXTRA_TRIGGER_WORD)?.let { helpDetector.triggerWord = it }
        val count = intent.getIntExtra(EXTRA_TRIGGER_COUNT, -1)
        if (count > 0) helpDetector.requiredCount = count
        intent.getStringExtra(EXTRA_SPEECH_LANGUAGE)?.let { speechLanguage = it }
        intent.getStringExtra(EXTRA_API_ENDPOINT)?.let { EmergencyApiClient.baseUrl = it }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startListening() {
        if (isListening) return
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        isListening = true
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(createRecognitionListener())
        }
        startRecognition()
    }

    private fun startRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLanguage)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            restartListeningDelayed()
        }
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            if (isListening) {
                restartListeningDelayed()
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.forEach { text ->
                helpDetector.processText(text)
            }
            if (isListening) {
                restartListeningDelayed()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.forEach { text ->
                helpDetector.processText(text)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun restartListeningDelayed() {
        scope.launch {
            // Destroy old recognizer to free memory before restarting
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (e: Exception) { /* ignore */ }
            speechRecognizer = null

            delay(1000)
            if (isListening) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@VoiceRecognitionService).apply {
                    setRecognitionListener(createRecognitionListener())
                }
                startRecognition()
            }
        }
    }

    private fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        unmuteBeep()
        scope.cancel()
    }

    private fun muteBeep() {
        audioManager?.let { am ->
            originalVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        }
    }

    private fun unmuteBeep() {
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
    }

    private fun onEmergencyDetected() {
        val intent = Intent(ACTION_EMERGENCY)
        sendBroadcast(intent)

        scope.launch(Dispatchers.IO) {
            EmergencyApiClient.sendEmergency()
        }

        // Send emergency email if configured
        val settings = AppSettings.load(this)
        if (settings.emailEnabled && settings.emailRecipient.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
                val config = EmergencyEmailSender.EmailConfig(
                    smtpHost = settings.smtpHost,
                    smtpPort = settings.smtpPort,
                    senderEmail = settings.emailSender,
                    senderPassword = settings.emailPassword,
                    recipientEmail = settings.emailRecipient,
                    emergencyCode = settings.emergencyCode
                )
                EmergencyEmailSender.sendEmergencyEmail(config)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                Strings.get("voice_recognition"),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = Strings.get("voice_listening")
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("NightWatch")
            .setContentText(Strings.get("voice_active"))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopListening()
        super.onDestroy()
    }
}
