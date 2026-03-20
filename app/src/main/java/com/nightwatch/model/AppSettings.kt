package com.nightwatch.model

import android.content.Context
import android.content.SharedPreferences

data class AppSettings(
    val language: Language = Language.DE,
    val useRealSunTimes: Boolean = false,
    val fixedSunrise: Int = 390,       // 06:30
    val fixedSunset: Int = 1080,       // 18:00
    val latitude: Double = 51.0,
    val longitude: Double = 10.0,
    val triggerWord: String = "hilfe",
    val triggerRepetitions: Int = 3,
    val apiEndpoint: String = "http://localhost:8080",
    val voiceDetectionEnabled: Boolean = true
) {
    fun toTimeConfig(): TimeConfig {
        return if (useRealSunTimes) {
            TimeConfig(
                useRealSunTimes = true,
                latitude = latitude,
                longitude = longitude
            )
        } else {
            TimeConfig(
                fixedSunrise = fixedSunrise,
                fixedSunset = fixedSunset
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "nightwatch_settings"

        fun load(context: Context): AppSettings {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return AppSettings(
                language = Language.fromCode(prefs.getString("language", "de") ?: "de"),
                useRealSunTimes = prefs.getBoolean("use_real_sun_times", false),
                fixedSunrise = prefs.getInt("fixed_sunrise", 390),
                fixedSunset = prefs.getInt("fixed_sunset", 1080),
                latitude = prefs.getFloat("latitude", 51.0f).toDouble(),
                longitude = prefs.getFloat("longitude", 10.0f).toDouble(),
                triggerWord = prefs.getString("trigger_word", "hilfe") ?: "hilfe",
                triggerRepetitions = prefs.getInt("trigger_repetitions", 3),
                apiEndpoint = prefs.getString("api_endpoint", "http://localhost:8080") ?: "http://localhost:8080",
                voiceDetectionEnabled = prefs.getBoolean("voice_detection_enabled", true)
            )
        }

        fun save(context: Context, settings: AppSettings) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putString("language", settings.language.code)
                putBoolean("use_real_sun_times", settings.useRealSunTimes)
                putInt("fixed_sunrise", settings.fixedSunrise)
                putInt("fixed_sunset", settings.fixedSunset)
                putFloat("latitude", settings.latitude.toFloat())
                putFloat("longitude", settings.longitude.toFloat())
                putString("trigger_word", settings.triggerWord)
                putInt("trigger_repetitions", settings.triggerRepetitions)
                putString("api_endpoint", settings.apiEndpoint)
                putBoolean("voice_detection_enabled", settings.voiceDetectionEnabled)
                apply()
            }
        }
    }
}
