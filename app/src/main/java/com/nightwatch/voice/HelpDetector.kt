package com.nightwatch.voice

/**
 * Detects the emergency trigger word repeated N times.
 * Configurable trigger word and repetition count.
 */
class HelpDetector(
    var triggerWord: String = "hilfe",
    var requiredCount: Int = 3
) {

    private val detections = mutableListOf<Long>()
    private val timeWindowMs = 10_000L // 10 second window

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
        val word = triggerWord.lowercase()

        val wordCount = word.toRegex(RegexOption.LITERAL).findAll(lower).count()

        if (wordCount >= requiredCount) {
            listener?.onHelpDetected()
            detections.clear()
            return true
        }

        if (wordCount > 0) {
            val now = System.currentTimeMillis()
            repeat(wordCount) {
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
