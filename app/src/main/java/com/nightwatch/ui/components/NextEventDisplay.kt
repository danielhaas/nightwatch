package com.nightwatch.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightwatch.calendar.NextEventModel
import kotlinx.coroutines.delay

@Composable
fun NextEventDisplay(
    event: NextEventModel?,
    modifier: Modifier = Modifier
) {
    if (event == null) return

    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(30_000) // update countdown every 30s
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Naechster Termin",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = event.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!event.isAllDay) {
                Text(
                    text = event.startTimeFormatted(),
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "  \u2022  ",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
            Text(
                text = event.countdownText(nowMillis),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
