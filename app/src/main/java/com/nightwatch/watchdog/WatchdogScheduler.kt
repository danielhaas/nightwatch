package com.nightwatch.watchdog

import android.content.Context
import com.nightwatch.emergency.EmergencyEmailSender
import com.nightwatch.model.AppSettings
import com.nightwatch.model.Strings
import kotlinx.coroutines.*
import java.util.Calendar

/**
 * Sends a daily "still alive" email at a configured time.
 * Runs as a coroutine loop checking every minute if it's time to send.
 */
class WatchdogScheduler(private val context: Context) {

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var lastSentDay: Int = -1

    fun start() {
        if (job?.isActive == true) return

        job = scope.launch {
            while (isActive) {
                checkAndSend()
                delay(60_000) // Check every minute
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun checkAndSend() {
        val settings = AppSettings.load(context)
        if (!settings.watchdogEnabled || !settings.emailEnabled) return
        if (settings.emailRecipient.isBlank()) return

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val dayOfYear = now.get(Calendar.DAY_OF_YEAR)

        // Send if we're at or past the configured time and haven't sent today
        if (currentMinutes >= settings.watchdogTimeMinutes && dayOfYear != lastSentDay) {
            lastSentDay = dayOfYear

            withContext(Dispatchers.IO) {
                val config = EmergencyEmailSender.EmailConfig(
                    smtpHost = settings.smtpHost,
                    smtpPort = settings.smtpPort,
                    senderEmail = settings.emailSender,
                    senderPassword = settings.emailPassword,
                    recipientEmail = settings.emailRecipient,
                    emergencyCode = settings.watchdogCode,
                    useSsl = settings.smtpUseSsl
                )
                EmergencyEmailSender.sendWatchdogEmail(config)
            }
        }
    }
}
