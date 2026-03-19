package com.nightwatch.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.nightwatch.model.DayNightState
import kotlin.random.Random

private data class Star(val x: Float, val y: Float, val size: Float, val twinkleSpeed: Int)

@Composable
fun StarsOverlay(
    state: DayNightState,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val baseAlpha = when (state) {
        DayNightState.NIGHT -> 1f
        DayNightState.DUSK -> progress * 0.8f
        DayNightState.DAWN -> (1f - progress) * 0.8f
        DayNightState.DAY -> 0f
    }

    if (baseAlpha <= 0.01f) return

    val stars = remember {
        val rng = Random(42)
        List(80) {
            Star(
                x = rng.nextFloat(),
                y = rng.nextFloat() * 0.7f, // mostly upper sky
                size = 1f + rng.nextFloat() * 2.5f,
                twinkleSpeed = 2000 + rng.nextInt(4000)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "stars")

    // Use a few shared twinkle animations instead of per-star
    val twinkle1 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "twinkle1"
    )
    val twinkle2 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "twinkle2"
    )
    val twinkle3 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "twinkle3"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        stars.forEachIndexed { index, star ->
            val twinkle = when (index % 3) {
                0 -> twinkle1
                1 -> twinkle2
                else -> twinkle3
            }
            drawCircle(
                color = Color.White.copy(alpha = baseAlpha * twinkle),
                radius = star.size,
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }
    }
}
