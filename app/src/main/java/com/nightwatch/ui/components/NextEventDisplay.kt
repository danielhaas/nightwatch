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
import com.nightwatch.model.Strings
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
            delay(30_000)
        }
    }

    // Scale factor: 1.0 at 24h+ away, up to 8.0 when imminent (fills screen)
    val hoursAway = ((event.startTimeMillis - nowMillis) / 3_600_000f).coerceAtLeast(0f)
    val scale = when {
        hoursAway >= 24f -> 1.0f
        hoursAway >= 6f  -> 1.0f + (24f - hoursAway) / 18f * 2.0f   // 1.0 -> 3.0
        hoursAway >= 1f  -> 3.0f + (6f - hoursAway) / 5f * 2.5f     // 3.0 -> 5.5
        else             -> 5.5f + (1f - hoursAway) * 2.5f           // 5.5 -> 8.0
    }

    val labelSize = (14f * scale).sp
    val titleSize = (24f * scale).sp
    val timeSize = (18f * scale).sp
    val detailSize = (16f * scale).sp
    val alpha = (0.7f + (scale - 1f) * 0.04f).coerceAtMost(1f)

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = Strings.get("next_event"),
            fontSize = labelSize,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = event.title,
            fontSize = titleSize,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = alpha),
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${event.weekdayName()}, ${event.startTimeFormatted()}",
            fontSize = timeSize,
            fontWeight = FontWeight.Light,
            color = Color.White.copy(alpha = alpha - 0.05f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = event.countdownText(nowMillis),
            fontSize = detailSize,
            color = Color.White.copy(alpha = alpha - 0.15f)
        )
    }
}
