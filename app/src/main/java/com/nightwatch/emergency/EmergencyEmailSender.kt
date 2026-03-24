package com.nightwatch.emergency

import com.nightwatch.model.Strings
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
        val emergencyCode: String = ""
    )

    fun sendEmergencyEmail(config: EmailConfig): Boolean {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", config.smtpHost)
            put("mail.smtp.port", config.smtpPort.toString())
            put("mail.smtp.ssl.trust", config.smtpHost)
            // Timeouts to avoid hanging
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.senderEmail, config.senderPassword)
            }
        })

        return try {
            val timestamp = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                .format(Date())

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.senderEmail, "NightWatch"))
                setRecipient(Message.RecipientType.TO, InternetAddress(config.recipientEmail))
                subject = "NightWatch - ${Strings.get("emergency")}! [${config.emergencyCode}]"
                setText(
                    "${Strings.get("emergency_email_body")}\n\n" +
                    "${Strings.get("emergency_email_time")}: $timestamp\n" +
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
}
