package com.nightwatch.model

data class TimeConfig(
    val useRealSunTimes: Boolean = false,
    val latitude: Double = 51.0,   // Default: ~Germany
    val longitude: Double = 10.0,
    // Fixed times in minutes from midnight
    val fixedSunrise: Int = 390,   // 06:30
    val fixedSunset: Int = 1080,   // 18:00
    val dawnDuration: Int = 45,    // minutes
    val duskDuration: Int = 45,    // minutes
    // Computed boundaries (in minutes from midnight)
    val dawnStart: Int = fixedSunrise - dawnDuration,
    val sunriseEnd: Int = fixedSunrise,
    val duskStart: Int = fixedSunset,
    val sunsetEnd: Int = fixedSunset + duskDuration
) {
    companion object {
        fun withSunTimes(
            sunriseMinutes: Int,
            sunsetMinutes: Int,
            dawnDuration: Int = 45,
            duskDuration: Int = 45
        ): TimeConfig {
            return TimeConfig(
                useRealSunTimes = true,
                fixedSunrise = sunriseMinutes,
                fixedSunset = sunsetMinutes,
                dawnDuration = dawnDuration,
                duskDuration = duskDuration,
                dawnStart = sunriseMinutes - dawnDuration,
                sunriseEnd = sunriseMinutes,
                duskStart = sunsetMinutes,
                sunsetEnd = sunsetMinutes + duskDuration
            )
        }
    }
}
