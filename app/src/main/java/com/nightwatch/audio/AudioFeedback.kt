package com.nightwatch.audio

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.nightwatch.model.Strings
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AudioFeedback(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ready = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    @Volatile
    var isSpeaking = false
        private set

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ready = true
            val locale = when (Strings.getLanguage().code) {
                "de" -> Locale.GERMAN
                "fr" -> Locale.FRENCH
                "it" -> Locale.ITALIAN
                else -> Locale.ENGLISH
            }
            tts?.language = locale
        }
    }

    fun speak(text: String) {
        if (!ready) return

        isSpeaking = true

        // Unmute and raise volume
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        try { audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol / 2, 0) } catch (_: Exception) { }
        try { audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL } catch (_: Exception) { }

        val latch = CountDownLatch(1)
        val utteranceId = "nw_${System.currentTimeMillis()}"

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                isSpeaking = false
                latch.countDown()
            }
            @Deprecated("Deprecated")
            override fun onError(id: String?) {
                isSpeaking = false
                latch.countDown()
            }
        })

        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        tts?.speak(text, TextToSpeech.QUEUE_ADD, params, utteranceId)

        // Wait for TTS to finish (max 15 seconds)
        latch.await(15, TimeUnit.SECONDS)
        isSpeaking = false
    }

    fun announceEmergencySending() {
        speak(Strings.get("audio_emergency_sending"))
    }

    fun announceEmergencySent() {
        speak(Strings.get("audio_emergency_sent"))
    }

    fun announceEmergencyFailed() {
        speak(Strings.get("audio_emergency_failed"))
    }

    fun announceReplyReceived() {
        speak(Strings.get("audio_reply_received"))
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
