package com.nightwatch.voice

/**
 * Detects the emergency phrase "Hilfe, Hilfe, Hilfe" (3x "Hilfe").
 * Tracks occurrences within a time window.
 */
class HelpDetector {

    private val detections = mutableListOf<Long>()
    private val timeWindowMs = 10_000L // 10 second window
    private val requiredCount = 3

    interface Listener {
        fun onHelpDetected()
    }

    var listener: Listener? = null

    /**
     * Process recognized text from speech recognizer.
     * Returns true if emergency phrase detected.
     */
    fun processText(text: String): Boolean {
        val lower = text.lowercase()

        // Count "hilfe" occurrences in this recognition result
        val hilfeCount = "hilfe".toRegex().findAll(lower).count()

        if (hilfeCount >= 3) {
            // Three or more in a single utterance
            listener?.onHelpDetected()
            detections.clear()
            return true
        }

        if (hilfeCount > 0) {
            val now = System.currentTimeMillis()
            repeat(hilfeCount) {
                detections.add(now)
            }

            // Remove old detections
            detections.removeAll { now - it > timeWindowMs }

            if (detections.size >= requiredCount) {
                listener?.onHelpDetected()
                detections.clear()
                return true
            }
        }

        return false
    }

    fun reset() {
        detections.clear()
    }
}
