package com.nightwatch.calendar

data class NextEventModel(
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isAllDay: Boolean = false
) {
    fun countdownText(nowMillis: Long): String {
        val diffMillis = startTimeMillis - nowMillis
        if (diffMillis <= 0) return "jetzt"

        val totalMinutes = diffMillis / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val days = hours / 24
        val remainingHours = hours % 24

        return when {
            totalMinutes < 60 -> "in $totalMinutes Minuten"
            hours < 24 -> "in $hours Stunden $minutes Minuten"
            else -> "in $days Tagen $remainingHours Stunden"
        }
    }

    fun startTimeFormatted(): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = startTimeMillis
        val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val m = cal.get(java.util.Calendar.MINUTE)
        return "%02d:%02d".format(h, m)
    }
}
