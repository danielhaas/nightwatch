package com.nightwatch.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nightwatch.model.DayNightState

@Composable
fun SkyBackground(
    state: DayNightState,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val (topColor, bottomColor) = skyColors(state, progress)

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(topColor, bottomColor),
                startY = 0f,
                endY = size.height
            )
        )

        // Horizon glow during dawn/dusk
        if (state == DayNightState.DAWN || state == DayNightState.DUSK) {
            val glowAlpha = if (state == DayNightState.DAWN) {
                (1f - progress) * 0.6f
            } else {
                progress * 0.6f
            }
            drawCircle(
                color = Color(0xFFFF8C00).copy(alpha = glowAlpha),
                radius = size.width * 0.5f,
                center = Offset(size.width / 2f, size.height * 0.95f)
            )
        }
    }
}

private fun skyColors(state: DayNightState, progress: Float): Pair<Color, Color> {
    return when (state) {
        DayNightState.NIGHT -> {
            Color(0xFF0B1026) to Color(0xFF1A1A3E)
        }
        DayNightState.DAWN -> {
            val top = lerpColor(Color(0xFF0B1026), Color(0xFF87CEEB), progress)
            val bottom = lerpColor(Color(0xFF1A1A3E), Color(0xFFB0E0E6), progress)
            top to bottom
        }
        DayNightState.DAY -> {
            Color(0xFF4A90D9) to Color(0xFF87CEEB)
        }
        DayNightState.DUSK -> {
            val top = lerpColor(Color(0xFF4A90D9), Color(0xFF0B1026), progress)
            val bottom = lerpColor(Color(0xFF87CEEB), Color(0xFF1A1A3E), progress)
            top to bottom
        }
    }
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = 1f
    )
}
