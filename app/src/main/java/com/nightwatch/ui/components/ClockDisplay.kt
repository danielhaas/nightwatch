package com.nightwatch.ui.components

import androidx.compose.foundation.layout.*
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
import com.nightwatch.model.Strings
import java.util.Calendar

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

    val weekday = Strings.weekday(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = "$weekday  $time",
            fontSize = 72.sp,
            fontWeight = FontWeight.Thin,
            color = textColor,
            textAlign = TextAlign.Center,
            letterSpacing = 4.sp
        )
    }
}
