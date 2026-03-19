package com.nightwatch.util

import java.util.Calendar

object TimeProvider {

    /** Returns current time as minutes from midnight */
    fun currentMinutes(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    /** Returns formatted time string HH:MM */
    fun currentTimeFormatted(): String {
        val cal = Calendar.getInstance()
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        return "%02d:%02d".format(h, m)
    }

    /** Returns current time in millis */
    fun currentTimeMillis(): Long = System.currentTimeMillis()
}
