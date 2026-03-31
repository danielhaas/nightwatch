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
import com.nightwatch.audio.AudioFeedback
import com.nightwatch.emergency.EmergencyApiClient
import com.nightwatch.emergency.EmergencyEmailSender
import com.nightwatch.emergency.ReplyMonitor
import com.nightwatch.model.AppSettings
import com.nightwatch.model.Strings
import kotlinx.coroutines.*

class VoiceRecognitionService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private val helpDetector = HelpDetector().apply {
        alternateWords = listOf("hülfe", "helfe", "hilf", "ilfe", "gilfe", "kilfe", "hife", "hilfä", "helft")
    }
    private val checkDetector = HelpDetector(triggerWord = "check", requiredCount = 3).apply {
        alternateWords = listOf("jack", "chuck", "zack", "tschek", "tscheck", "scheck", "schick", "schek", "tcheck", "czech", "jeck", "jäck", "tschäck", "shack", "zeck", "tschak")
    }
    private var scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isListening = false
    private var audioManager: AudioManager? = null
    private var originalMusicVolume: Int = 0
    private var originalNotifVolume: Int = 0
    private var originalSystemVolume: Int = 0
    private var speechLanguage: String = "de-DE"
    private var audioFeedback: AudioFeedback? = null
    private var replyMonitor: ReplyMonitor? = null
    private var restartJob: Job? = null
    private var emergencyCooldown = false
    private var checkCooldown = false
    private var consecutiveErrors = 0

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
        audioFeedback = AudioFeedback(this)
        replyMonitor = ReplyMonitor(this)
        muteBeep()
        helpDetector.listener = object : HelpDetector.Listener {
            override fun onHelpDetected() {
                onEmergencyDetected()
            }
        }
        checkDetector.listener = object : HelpDetector.Listener {
            override fun onHelpDetected() {
                onCheckDetected()
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
            // Suppress beep/vibration
            putExtra("android.speech.extra.DICTATION_MODE", true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 30000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
        }
        // Don't mute while TTS is speaking
        if (audioFeedback?.isSpeaking != true) {
            muteBeep()
            try {
                audioManager?.ringerMode = AudioManager.RINGER_MODE_SILENT
            } catch (_: Exception) { }
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
            consecutiveErrors++
            if (isListening) {
                restartListeningDelayed()
            }
        }

        override fun onResults(results: Bundle?) {
            consecutiveErrors = 0
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.forEach { text ->
                android.util.Log.d("NightWatch", "Heard: $text")
                helpDetector.processText(text)
                checkDetector.processText(text)
            }
            if (isListening) {
                restartListeningDelayed()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            consecutiveErrors = 0
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.forEach { text ->
                android.util.Log.d("NightWatch", "Partial: $text")
                helpDetector.processText(text)
                checkDetector.processText(text)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun restartListeningDelayed() {
        // Cancel any pending restart to prevent coroutine pile-up
        restartJob?.cancel()
        restartJob = scope.launch {
            // Destroy old recognizer to free memory before restarting
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (e: Exception) { /* ignore */ }
            speechRecognizer = null

            // After many consecutive failures, the recognition service is dead.
            // Restart our entire service to force a fresh connection.
            if (consecutiveErrors > 10) {
                android.util.Log.d("NightWatch", "Recognition service dead, restarting service (errors: $consecutiveErrors)")
                consecutiveErrors = 0
                delay(5_000)
                restartService()
                return@launch
            }

            // Back off when recognition service is failing repeatedly
            val delayMs = when {
                consecutiveErrors > 5 -> 5_000L
                else -> 1_000L
            }
            delay(delayMs)

            if (isListening) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@VoiceRecognitionService).apply {
                    setRecognitionListener(createRecognitionListener())
                }
                startRecognition()
            }
        }
    }

    private fun restartService() {
        val context = applicationContext
        val intent = Intent(context, VoiceRecognitionService::class.java).apply {
            action = ACTION_START
            val settings = AppSettings.load(context)
            putExtra(EXTRA_TRIGGER_WORD, settings.triggerWord)
            putExtra(EXTRA_TRIGGER_COUNT, settings.triggerRepetitions)
            putExtra(EXTRA_SPEECH_LANGUAGE, settings.language.speechCode)
            putExtra(EXTRA_API_ENDPOINT, settings.apiEndpoint)
        }
        stopListening()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun onCheckDetected() {
        if (checkCooldown) return
        checkCooldown = true
        scope.launch {
            delay(60_000)
            checkCooldown = false
        }

        // Send watchdog email if configured
        val settings = AppSettings.load(this)
        if (settings.emailEnabled && settings.emailRecipient.isNotBlank()) {
            audioFeedback?.speak(Strings.get("audio_watchdog_sending"))

            scope.launch(Dispatchers.IO) {
                val config = EmergencyEmailSender.EmailConfig(
                    smtpHost = settings.smtpHost,
                    smtpPort = settings.smtpPort,
                    senderEmail = settings.emailSender,
                    senderPassword = settings.emailPassword,
                    recipientEmail = settings.emailRecipient,
                    emergencyCode = settings.watchdogCode,
                    useSsl = settings.smtpUseSsl
                )
                val success = EmergencyEmailSender.sendWatchdogEmail(config)

                delay(5000)

                withContext(Dispatchers.Main) {
                    if (success) {
                        audioFeedback?.speak(Strings.get("audio_watchdog_sent"))
                    } else {
                        audioFeedback?.speak(Strings.get("audio_watchdog_failed"))
                    }
                }
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
        scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    }

    private val streamsToMute = intArrayOf(
        AudioManager.STREAM_MUSIC,
        AudioManager.STREAM_NOTIFICATION,
        AudioManager.STREAM_SYSTEM,
        AudioManager.STREAM_RING,
        AudioManager.STREAM_ALARM,
        5 // STREAM_ACCESSIBILITY (hidden constant)
    )
    private val originalVolumes = IntArray(streamsToMute.size)

    private fun muteBeep() {
        audioManager?.let { am ->
            for (i in streamsToMute.indices) {
                try {
                    originalVolumes[i] = am.getStreamVolume(streamsToMute[i])
                    am.setStreamVolume(streamsToMute[i], 0, 0)
                } catch (e: SecurityException) {
                    // DND restriction, try adjust method
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        try { am.adjustStreamVolume(streamsToMute[i], AudioManager.ADJUST_MUTE, 0) } catch (_: Exception) { }
                    }
                }
            }
        }
    }

    private fun unmuteBeep() {
        audioManager?.let { am ->
            for (i in streamsToMute.indices) {
                try {
                    am.setStreamVolume(streamsToMute[i], originalVolumes[i], 0)
                } catch (e: SecurityException) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        try { am.adjustStreamVolume(streamsToMute[i], AudioManager.ADJUST_UNMUTE, 0) } catch (_: Exception) { }
                    }
                }
            }
        }
    }

    private fun onEmergencyDetected() {
        if (emergencyCooldown) return
        emergencyCooldown = true
        // Reset cooldown after 60 seconds
        scope.launch {
            delay(60_000)
            emergencyCooldown = false
        }

        val intent = Intent(ACTION_EMERGENCY)
        intent.setPackage(packageName)
        sendBroadcast(intent)

        scope.launch(Dispatchers.IO) {
            EmergencyApiClient.sendEmergency()
        }

        // Announce sending
        audioFeedback?.announceEmergencySending()

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
                    emergencyCode = settings.emergencyCode,
                    useSsl = settings.smtpUseSsl
                )
                val success = EmergencyEmailSender.sendEmergencyEmail(config)

                // Wait for the "sending" announcement to finish before "sent"
                delay(5000)

                // Audio feedback on main thread
                withContext(Dispatchers.Main) {
                    if (success) {
                        audioFeedback?.announceEmergencySent()
                        // Start monitoring for reply
                        audioFeedback?.let { replyMonitor?.monitorForReply(it) }
                    } else {
                        audioFeedback?.announceEmergencyFailed()
                    }
                }
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
        replyMonitor?.stopMonitoring()
        audioFeedback?.shutdown()
        super.onDestroy()
    }
}
