package com.nightwatch.util

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * Simplified solar position algorithm to calculate sunrise/sunset times.
 * Based on NOAA solar calculator equations.
 */
object SunCalculator {

    data class SunTimes(
        val sunriseMinutes: Int,
        val sunsetMinutes: Int
    )

    fun calculate(year: Int, month: Int, day: Int, latitude: Double, longitude: Double, timezoneOffsetHours: Double = autoTimezoneOffset()): SunTimes {
        val jd = julianDay(year, month, day)
        val jc = (jd - 2451545.0) / 36525.0

        val geomMeanLongSun = (280.46646 + jc * (36000.76983 + 0.0003032 * jc)) % 360
        val geomMeanAnomSun = 357.52911 + jc * (35999.05029 - 0.0001537 * jc)
        val eccentEarthOrbit = 0.016708634 - jc * (0.000042037 + 0.0000001267 * jc)

        val sunEqOfCenter = sin(Math.toRadians(geomMeanAnomSun)) * (1.914602 - jc * (0.004817 + 0.000014 * jc)) +
                sin(Math.toRadians(2 * geomMeanAnomSun)) * (0.019993 - 0.000101 * jc) +
                sin(Math.toRadians(3 * geomMeanAnomSun)) * 0.000289

        val sunTrueLong = geomMeanLongSun + sunEqOfCenter
        val sunAppLong = sunTrueLong - 0.00569 - 0.00478 * sin(Math.toRadians(125.04 - 1934.136 * jc))

        val meanObliqEcliptic = 23.0 + (26.0 + (21.448 - jc * (46.815 + jc * (0.00059 - jc * 0.001813))) / 60.0) / 60.0
        val obliqCorr = meanObliqEcliptic + 0.00256 * cos(Math.toRadians(125.04 - 1934.136 * jc))

        val sunDeclin = Math.toDegrees(asin(sin(Math.toRadians(obliqCorr)) * sin(Math.toRadians(sunAppLong))))

        val varY = tan(Math.toRadians(obliqCorr / 2)) * tan(Math.toRadians(obliqCorr / 2))
        val eqOfTime = 4 * Math.toDegrees(
            varY * sin(2 * Math.toRadians(geomMeanLongSun)) -
                    2 * eccentEarthOrbit * sin(Math.toRadians(geomMeanAnomSun)) +
                    4 * eccentEarthOrbit * varY * sin(Math.toRadians(geomMeanAnomSun)) * cos(2 * Math.toRadians(geomMeanLongSun)) -
                    0.5 * varY * varY * sin(4 * Math.toRadians(geomMeanLongSun)) -
                    1.25 * eccentEarthOrbit * eccentEarthOrbit * sin(2 * Math.toRadians(geomMeanAnomSun))
        )

        val haSunrise = Math.toDegrees(
            acos(
                cos(Math.toRadians(90.833)) / (cos(Math.toRadians(latitude)) * cos(Math.toRadians(sunDeclin))) -
                        tan(Math.toRadians(latitude)) * tan(Math.toRadians(sunDeclin))
            )
        )

        val solarNoon = (720 - 4 * longitude - eqOfTime + timezoneOffsetHours * 60) / 1440
        val sunriseTime = solarNoon - haSunrise * 4 / 1440
        val sunsetTime = solarNoon + haSunrise * 4 / 1440

        val sunriseMinutes = (sunriseTime * 1440).toInt().coerceIn(0, 1439)
        val sunsetMinutes = (sunsetTime * 1440).toInt().coerceIn(0, 1439)

        return SunTimes(sunriseMinutes, sunsetMinutes)
    }

    fun calculateForToday(latitude: Double, longitude: Double): SunTimes {
        val cal = Calendar.getInstance()
        return calculate(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            latitude,
            longitude
        )
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + b - 1524.5
    }

    private fun autoTimezoneOffset(): Double {
        return TimeZone.getDefault().rawOffset / 3600000.0
    }
}
