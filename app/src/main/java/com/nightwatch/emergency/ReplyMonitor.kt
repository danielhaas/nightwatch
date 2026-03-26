package com.nightwatch.emergency

import android.content.Context
import com.nightwatch.audio.AudioFeedback
import com.nightwatch.model.AppSettings
import kotlinx.coroutines.*
import java.util.Properties
import javax.mail.*
import javax.mail.search.SubjectTerm

/**
 * Monitors the sender's IMAP inbox for replies to emergency emails.
 * When a reply containing the emergency code is found, announces via audio.
 */
class ReplyMonitor(private val context: Context) {

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var audioFeedback: AudioFeedback? = null
    private var monitoring = false

    fun startMonitoring(audio: AudioFeedback) {
        if (monitoring) return
        monitoring = true
        audioFeedback = audio

        job = scope.launch {
            // Check every 30 seconds for replies
            while (isActive) {
                delay(30_000)
                checkForReplies()
            }
        }
    }

    fun stopMonitoring() {
        monitoring = false
        job?.cancel()
        job = null
    }

    /**
     * Start monitoring after an emergency was triggered.
     * Monitors for a reply for up to 30 minutes.
     */
    fun monitorForReply(audio: AudioFeedback) {
        audioFeedback = audio

        job?.cancel()
        job = scope.launch {
            val startTime = System.currentTimeMillis()
            val timeout = 30 * 60 * 1000L // 30 minutes

            while (isActive && System.currentTimeMillis() - startTime < timeout) {
                delay(30_000)
                if (checkForReplies()) {
                    break // Reply found, stop monitoring
                }
            }
            monitoring = false
        }
        monitoring = true
    }

    private fun checkForReplies(): Boolean {
        val settings = AppSettings.load(context)
        if (!settings.emailEnabled || settings.emailSender.isBlank()) return false

        return try {
            val props = Properties().apply {
                put("mail.imap.host", imapHost(settings.smtpHost))
                put("mail.imap.port", "993")
                put("mail.imap.ssl.enable", "true")
                put("mail.imap.ssl.trust", imapHost(settings.smtpHost))
                put("mail.imap.connectiontimeout", "10000")
                put("mail.imap.timeout", "10000")
            }

            val session = Session.getInstance(props)
            val store = session.getStore("imap")
            store.connect(
                imapHost(settings.smtpHost),
                settings.emailSender,
                settings.emailPassword
            )

            val inbox = store.getFolder("INBOX")
            inbox.open(Folder.READ_ONLY)

            // Search for recent messages with the emergency code in subject
            // (replies typically have "Re: <original subject>")
            val searchTerm = SubjectTerm(settings.emergencyCode)
            val messages = inbox.search(searchTerm)

            // Check for messages received in the last 30 minutes that are FROM the recipient
            val thirtyMinAgo = System.currentTimeMillis() - 30 * 60 * 1000
            var foundReply = false

            for (msg in messages) {
                val receivedDate = msg.receivedDate ?: continue
                if (receivedDate.time < thirtyMinAgo) continue

                // Check if it's from the recipient (i.e., a reply)
                val from = msg.from?.firstOrNull()?.toString()?.lowercase() ?: continue
                if (from.contains(settings.emailRecipient.lowercase())) {
                    foundReply = true
                    break
                }
            }

            inbox.close(false)
            store.close()

            if (foundReply) {
                // Announce on main thread
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    audioFeedback?.announceReplyReceived()
                }
            }

            foundReply
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Derive IMAP host from SMTP host.
     * smtp.gmail.com -> imap.gmail.com
     */
    private fun imapHost(smtpHost: String): String {
        return smtpHost.replace("smtp.", "imap.")
    }
}
