package com.nightwatch.model

import android.content.Context

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
    val voiceDetectionEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val emailRecipient: String = "daniel@haas.li",
    val emailSender: String = "renatehaasbeck@gmail.com",
    val emailPassword: String = "vygfbfynpsidyrck",
    val smtpHost: String = "smtp.gmail.com",
    val smtpPort: Int = 587,
    val emergencyCode: String = "SB00;300495;A 001"
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
                voiceDetectionEnabled = prefs.getBoolean("voice_detection_enabled", true),
                emailEnabled = prefs.getBoolean("email_enabled", true),
                emailRecipient = prefs.getString("email_recipient", "daniel@haas.li") ?: "daniel@haas.li",
                emailSender = prefs.getString("email_sender", "renatehaasbeck@gmail.com") ?: "renatehaasbeck@gmail.com",
                emailPassword = prefs.getString("email_password", "vygfbfynpsidyrck") ?: "vygfbfynpsidyrck",
                smtpHost = prefs.getString("smtp_host", "smtp.gmail.com") ?: "smtp.gmail.com",
                smtpPort = prefs.getInt("smtp_port", 587),
                emergencyCode = prefs.getString("emergency_code", "SB00;300495;A 001") ?: "SB00;300495;A 001"
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
                putBoolean("email_enabled", settings.emailEnabled)
                putString("email_recipient", settings.emailRecipient)
                putString("email_sender", settings.emailSender)
                putString("email_password", settings.emailPassword)
                putString("smtp_host", settings.smtpHost)
                putInt("smtp_port", settings.smtpPort)
                putString("emergency_code", settings.emergencyCode)
                apply()
            }
        }
    }
}
