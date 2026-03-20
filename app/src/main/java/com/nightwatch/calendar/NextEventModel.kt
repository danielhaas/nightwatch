package com.nightwatch.calendar

import com.nightwatch.model.Strings

data class NextEventModel(
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isAllDay: Boolean = false
) {
    fun countdownText(nowMillis: Long): String {
        val diffMillis = startTimeMillis - nowMillis
        if (diffMillis <= 0) return Strings.get("now")

        val totalMinutes = diffMillis / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val days = hours / 24
        val remainingHours = hours % 24

        return when {
            totalMinutes < 60 -> Strings.get("in_minutes", totalMinutes)
            hours < 24 -> Strings.get("in_hours_minutes", hours, minutes)
            else -> Strings.get("in_days_hours", days, remainingHours)
        }
    }

    fun weekdayName(): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = startTimeMillis
        return Strings.weekday(cal.get(java.util.Calendar.DAY_OF_WEEK))
    }

    fun startTimeFormatted(): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = startTimeMillis
        val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val m = cal.get(java.util.Calendar.MINUTE)
        return "%02d:%02d".format(h, m)
    }
}
