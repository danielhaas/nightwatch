package com.nightwatch.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nightwatch.model.DayNightState

private data class CloudDef(
    val baseX: Float,
    val y: Float,
    val scale: Float,
    val speed: Float // duration in ms for full traverse
)

private val clouds = listOf(
    CloudDef(0.1f, 0.12f, 1.0f, 120_000f),
    CloudDef(0.4f, 0.08f, 0.7f, 90_000f),
    CloudDef(0.7f, 0.18f, 1.2f, 150_000f)
)

@Composable
fun CloudsOverlay(
    state: DayNightState,
    modifier: Modifier = Modifier
) {
    val alpha = when (state) {
        DayNightState.DAY -> 0.8f
        DayNightState.DAWN, DayNightState.DUSK -> 0.5f
        DayNightState.NIGHT -> 0.15f
    }

    val cloudColor = when (state) {
        DayNightState.DAY -> Color.White
        DayNightState.DAWN -> Color(0xFFFFE4C4)
        DayNightState.DUSK -> Color(0xFFDDA0DD)
        DayNightState.NIGHT -> Color(0xFF4A4A6A)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val offsets = clouds.map { cloud ->
        val anim by infiniteTransition.animateFloat(
            initialValue = -0.2f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = cloud.speed.toInt(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "cloud_${cloud.baseX}"
        )
        anim
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        offsets.forEachIndexed { index, offsetX ->
            val cloud = clouds[index]
            drawCloud(
                x = offsetX * size.width,
                y = cloud.y * size.height,
                scale = cloud.scale,
                color = cloudColor.copy(alpha = alpha),
            )
        }
    }
}

private fun DrawScope.drawCloud(x: Float, y: Float, scale: Float, color: Color) {
    val w = 120f * scale
    val h = 40f * scale
    val r = 20f * scale

    // Main body
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(r, r)
    )
    // Top bumps
    drawCircle(color = color, radius = r * 1.2f, center = Offset(x + w * 0.3f, y - r * 0.3f))
    drawCircle(color = color, radius = r * 0.9f, center = Offset(x + w * 0.6f, y - r * 0.5f))
    drawCircle(color = color, radius = r * 0.7f, center = Offset(x + w * 0.8f, y - r * 0.1f))
}
