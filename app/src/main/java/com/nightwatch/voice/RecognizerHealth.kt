package com.nightwatch.voice

import android.content.Context
import android.content.SharedPreferences

/**
 * Detects silent failure of the speech recognizer: audio is being captured
 * (onBeginningOfSpeech fires) but transcriptions come back empty.
 *
 * Counter resets whenever a non-empty transcription arrives, so a healthy
 * recognizer keeps the counter near zero. A broken recognizer accumulates
 * speech events without any corresponding transcription.
 */
object RecognizerHealth {
    private const val PREFS = "nightwatch_recognizer_health"
    private const val KEY_SPEECH_SINCE_TRANSCRIPTION = "speech_since_transcription"
    private const val KEY_LAST_TRANSCRIPTION_AT = "last_transcription_at"
    private const val KEY_LAST_ALARM_AT = "last_alarm_at"
    private const val KEY_TOTAL_SPEECH_EVENTS = "total_speech_events"
    private const val KEY_TOTAL_TRANSCRIPTIONS = "total_transcriptions"
    private const val KEY_FIRST_USE_AT = "first_use_at"

    // Speech events without any transcription before we sound the alarm.
    const val ALARM_THRESHOLD = 5

    // Don't spam: at most one alarm every 6h until something transcribes again.
    private const val ALARM_DEDUP_MS = 6L * 3600 * 1000

    data class Stats(
        val speechSinceLastTranscription: Int,
        val lastTranscriptionAt: Long,
        val totalSpeechEvents: Int,
        val totalTranscriptions: Int,
        val firstUseAt: Long
    )

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @Synchronized
    fun onSpeechEvent(context: Context) {
        val p = prefs(context)
        val e = p.edit()
            .putInt(KEY_SPEECH_SINCE_TRANSCRIPTION, p.getInt(KEY_SPEECH_SINCE_TRANSCRIPTION, 0) + 1)
            .putInt(KEY_TOTAL_SPEECH_EVENTS, p.getInt(KEY_TOTAL_SPEECH_EVENTS, 0) + 1)
        if (!p.contains(KEY_FIRST_USE_AT)) e.putLong(KEY_FIRST_USE_AT, System.currentTimeMillis())
        e.apply()
    }

    @Synchronized
    fun onTranscription(context: Context, text: String) {
        if (text.isBlank()) return
        val p = prefs(context)
        p.edit()
            .putInt(KEY_SPEECH_SINCE_TRANSCRIPTION, 0)
            .putLong(KEY_LAST_TRANSCRIPTION_AT, System.currentTimeMillis())
            .putInt(KEY_TOTAL_TRANSCRIPTIONS, p.getInt(KEY_TOTAL_TRANSCRIPTIONS, 0) + 1)
            .apply()
    }

    @Synchronized
    fun shouldRaiseAlarm(context: Context): Boolean {
        val p = prefs(context)
        if (p.getInt(KEY_SPEECH_SINCE_TRANSCRIPTION, 0) < ALARM_THRESHOLD) return false
        val now = System.currentTimeMillis()
        val lastAlarm = p.getLong(KEY_LAST_ALARM_AT, 0)
        return (now - lastAlarm) > ALARM_DEDUP_MS
    }

    @Synchronized
    fun markAlarmSent(context: Context) {
        prefs(context).edit().putLong(KEY_LAST_ALARM_AT, System.currentTimeMillis()).apply()
    }

    fun getStats(context: Context): Stats {
        val p = prefs(context)
        return Stats(
            speechSinceLastTranscription = p.getInt(KEY_SPEECH_SINCE_TRANSCRIPTION, 0),
            lastTranscriptionAt = p.getLong(KEY_LAST_TRANSCRIPTION_AT, 0),
            totalSpeechEvents = p.getInt(KEY_TOTAL_SPEECH_EVENTS, 0),
            totalTranscriptions = p.getInt(KEY_TOTAL_TRANSCRIPTIONS, 0),
            firstUseAt = p.getLong(KEY_FIRST_USE_AT, 0)
        )
    }
}
