package com.nightwatch.model

enum class DayNightState {
    NIGHT,
    DAWN,
    DAY,
    DUSK;

    fun progress(currentMinutes: Int, config: TimeConfig): Float {
        val (start, end) = when (this) {
            DAWN -> config.dawnStart to config.sunriseEnd
            DAY -> config.sunriseEnd to config.duskStart
            DUSK -> config.duskStart to config.sunsetEnd
            NIGHT -> config.sunsetEnd to (config.dawnStart + 1440)
        }
        val adjusted = if (currentMinutes < start) currentMinutes + 1440 else currentMinutes
        val duration = if (end > start) end - start else end + 1440 - start
        return ((adjusted - start).toFloat() / duration).coerceIn(0f, 1f)
    }

    companion object {
        fun fromTime(currentMinutes: Int, config: TimeConfig): DayNightState {
            return when {
                currentMinutes in config.dawnStart until config.sunriseEnd -> DAWN
                currentMinutes in config.sunriseEnd until config.duskStart -> DAY
                currentMinutes in config.duskStart until config.sunsetEnd -> DUSK
                else -> NIGHT
            }
        }
    }
}
