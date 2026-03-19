package com.nightwatch.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightwatch.model.DayNightState

@Composable
fun ClockDisplay(
    time: String,
    state: DayNightState,
    modifier: Modifier = Modifier
) {
    val textColor = when (state) {
        DayNightState.DAY -> Color.White.copy(alpha = 0.9f)
        DayNightState.DAWN, DayNightState.DUSK -> Color.White.copy(alpha = 0.95f)
        DayNightState.NIGHT -> Color.White.copy(alpha = 0.85f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = time,
            fontSize = 72.sp,
            fontWeight = FontWeight.Thin,
            color = textColor,
            textAlign = TextAlign.Center,
            letterSpacing = 8.sp
        )
    }
}
