package com.nightwatch.audio

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import com.nightwatch.model.Strings
import java.util.*

/**
 * Provides audio feedback using Text-to-Speech.
 * Temporarily unmutes and raises volume for announcements.
 */
class AudioFeedback(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ready = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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

        // Ensure volume is audible
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (currentVol == 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol / 2, 0)
        }

        @Suppress("DEPRECATION")
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null)
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
