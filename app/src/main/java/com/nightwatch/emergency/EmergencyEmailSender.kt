package com.nightwatch.emergency

import com.nightwatch.model.Strings
import com.nightwatch.voice.RecognizerHealth
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmergencyEmailSender {

    data class EmailConfig(
        val smtpHost: String,
        val smtpPort: Int,
        val senderEmail: String,
        val senderPassword: String,
        val recipientEmail: String,
        val emergencyCode: String = "",
        val useSsl: Boolean = false
    )

    private fun createSession(config: EmailConfig): Session {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", config.smtpHost)
            put("mail.smtp.port", config.smtpPort.toString())
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
            if (config.useSsl) {
                // Direct SSL/TLS (typically port 465)
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.socketFactory.port", config.smtpPort.toString())
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            } else {
                // STARTTLS (typically port 587)
                put("mail.smtp.starttls.enable", "true")
            }
            put("mail.smtp.ssl.trust", config.smtpHost)
        }
        val password = config.senderPassword.replace(" ", "")
        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.senderEmail, password)
            }
        })
    }

    private fun timestamp(): String =
        SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())

    fun sendEmergencyEmail(config: EmailConfig): Boolean {
        return try {
            val session = createSession(config)
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.senderEmail, "NightWatch"))
                setRecipient(Message.RecipientType.TO, InternetAddress(config.recipientEmail))
                subject = config.emergencyCode
                setText(
                    "${Strings.get("emergency_email_body")}\n\n" +
                    "${Strings.get("emergency_email_time")}: ${timestamp()}\n" +
                    "${Strings.get("emergency_email_device")}: NightWatch\n" +
                    "${Strings.get("emergency_code_label")}: ${config.emergencyCode}\n\n" +
                    "${Strings.get("emergency_email_footer")}"
                )
            }
            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun sendWatchdogEmail(config: EmailConfig, stats: RecognizerHealth.Stats? = null): Boolean {
        return try {
            val session = createSession(config)
            val broken = stats != null && stats.speechSinceLastTranscription >= RecognizerHealth.ALARM_THRESHOLD
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.senderEmail, "NightWatch"))
                setRecipient(Message.RecipientType.TO, InternetAddress(config.recipientEmail))
                subject = if (broken) Strings.get("watchdog_email_alarm_subject") else config.emergencyCode
                setText(
                    "${Strings.get("watchdog_email_body")}\n\n" +
                    "${Strings.get("emergency_email_time")}: ${timestamp()}\n" +
                    "${Strings.get("emergency_email_device")}: NightWatch\n" +
                    "${Strings.get("emergency_code_label")}: ${config.emergencyCode}\n" +
                    (stats?.let { "\n" + formatHealthReport(it) } ?: "")
                )
            }
            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun sendRecognizerAlarmEmail(config: EmailConfig, stats: RecognizerHealth.Stats): Boolean {
        return try {
            val session = createSession(config)
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.senderEmail, "NightWatch"))
                setRecipient(Message.RecipientType.TO, InternetAddress(config.recipientEmail))
                subject = Strings.get("recognizer_alarm_subject")
                setText(
                    "${Strings.get("recognizer_alarm_body")}\n\n" +
                    "${Strings.get("emergency_email_time")}: ${timestamp()}\n" +
                    "${Strings.get("emergency_email_device")}: NightWatch\n\n" +
                    formatHealthReport(stats)
                )
            }
            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun formatHealthReport(s: RecognizerHealth.Stats): String {
        val lastTrans = if (s.lastTranscriptionAt > 0) {
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(s.lastTranscriptionAt))
        } else {
            Strings.get("health_never")
        }
        return Strings.get("health_header") + "\n" +
            "  " + Strings.get("health_last_transcription") + ": " + lastTrans + "\n" +
            "  " + Strings.get("health_speech_since") + ": " + s.speechSinceLastTranscription + "\n" +
            "  " + Strings.get("health_total_speech") + ": " + s.totalSpeechEvents + "\n" +
            "  " + Strings.get("health_total_transcriptions") + ": " + s.totalTranscriptions + "\n"
    }
}
