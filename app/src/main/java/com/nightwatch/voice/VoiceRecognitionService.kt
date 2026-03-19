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
import kotlinx.coroutines.*

class VoiceRecognitionService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private val helpDetector = HelpDetector()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isListening = false
    private var audioManager: AudioManager? = null
    private var originalVolume: Int = 0

    companion object {
        const val CHANNEL_ID = "nightwatch_voice"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.nightwatch.START_LISTENING"
        const val ACTION_STOP = "com.nightwatch.STOP_LISTENING"
        const val ACTION_EMERGENCY = "com.nightwatch.EMERGENCY_DETECTED"

        fun start(context: Context) {
            val intent = Intent(context, VoiceRecognitionService::class.java).apply {
                action = ACTION_START
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
        }
        return START_STICKY
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
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            // Retry after delay
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
            // Restart listening on error (common with SpeechRecognizer)
            if (isListening) {
                restartListeningDelayed()
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.forEach { text ->
                helpDetector.processText(text)
            }
            // Restart to continue listening
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
            delay(500)
            if (isListening) {
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
        // Send broadcast to activity
        val intent = Intent(ACTION_EMERGENCY)
        sendBroadcast(intent)

        // Send emergency API call
        scope.launch(Dispatchers.IO) {
            EmergencyApiClient.sendEmergency()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NightWatch Spracherkennung",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Dauerhaftes Zuhoeren fuer Notruf-Erkennung"
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
            .setContentText("Spracherkennung aktiv")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopListening()
        super.onDestroy()
    }
}
