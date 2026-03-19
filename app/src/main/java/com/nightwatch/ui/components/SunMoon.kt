package com.nightwatch.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nightwatch.model.DayNightState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunMoon(
    state: DayNightState,
    currentMinutes: Int,
    sunriseMinutes: Int,
    sunsetMinutes: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        when (state) {
            DayNightState.DAY, DayNightState.DAWN, DayNightState.DUSK -> {
                drawSun(currentMinutes, sunriseMinutes, sunsetMinutes)
            }
            DayNightState.NIGHT -> {
                drawMoon(currentMinutes, sunsetMinutes, sunriseMinutes)
            }
        }
    }
}

private fun DrawScope.drawSun(currentMinutes: Int, sunrise: Int, sunset: Int) {
    val dayDuration = sunset - sunrise
    if (dayDuration <= 0) return
    val dayProgress = ((currentMinutes - sunrise).toFloat() / dayDuration).coerceIn(0f, 1f)

    // Sun moves in an arc
    val angle = PI * dayProgress
    val centerX = size.width * dayProgress
    val arcHeight = size.height * 0.6f
    val centerY = size.height * 0.8f - sin(angle).toFloat() * arcHeight

    val sunRadius = size.minDimension * 0.05f

    // Glow
    drawCircle(
        color = Color(0x40FFD700),
        radius = sunRadius * 2.5f,
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = Color(0x80FFD700),
        radius = sunRadius * 1.5f,
        center = Offset(centerX, centerY)
    )
    // Sun body
    drawCircle(
        color = Color(0xFFFFD700),
        radius = sunRadius,
        center = Offset(centerX, centerY)
    )

    // Rays
    val rayCount = 12
    for (i in 0 until rayCount) {
        val rayAngle = (2 * PI * i / rayCount)
        val innerR = sunRadius * 1.3f
        val outerR = sunRadius * 1.8f
        val startX = centerX + cos(rayAngle).toFloat() * innerR
        val startY = centerY + sin(rayAngle).toFloat() * innerR
        val endX = centerX + cos(rayAngle).toFloat() * outerR
        val endY = centerY + sin(rayAngle).toFloat() * outerR
        drawLine(
            color = Color(0xCCFFD700),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawMoon(currentMinutes: Int, sunset: Int, sunrise: Int) {
    val nightDuration = if (sunrise > sunset) sunrise - sunset else 1440 - sunset + sunrise
    val nightProgress = if (currentMinutes >= sunset) {
        ((currentMinutes - sunset).toFloat() / nightDuration).coerceIn(0f, 1f)
    } else {
        ((currentMinutes + 1440 - sunset).toFloat() / nightDuration).coerceIn(0f, 1f)
    }

    val angle = PI * nightProgress
    val centerX = size.width * nightProgress
    val arcHeight = size.height * 0.5f
    val centerY = size.height * 0.7f - sin(angle).toFloat() * arcHeight

    val moonRadius = size.minDimension * 0.04f

    // Moon glow
    drawCircle(
        color = Color(0x30F5F5DC),
        radius = moonRadius * 2f,
        center = Offset(centerX, centerY)
    )
    // Moon body
    drawCircle(
        color = Color(0xFFF5F5DC),
        radius = moonRadius,
        center = Offset(centerX, centerY)
    )
    // Craters
    drawCircle(
        color = Color(0x40808080),
        radius = moonRadius * 0.2f,
        center = Offset(centerX - moonRadius * 0.3f, centerY - moonRadius * 0.2f)
    )
    drawCircle(
        color = Color(0x30808080),
        radius = moonRadius * 0.15f,
        center = Offset(centerX + moonRadius * 0.25f, centerY + moonRadius * 0.3f)
    )
    drawCircle(
        color = Color(0x25808080),
        radius = moonRadius * 0.12f,
        center = Offset(centerX + moonRadius * 0.1f, centerY - moonRadius * 0.35f)
    )
}
