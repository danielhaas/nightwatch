package com.nightwatch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DayBlue = Color(0xFF87CEEB)
val DuskOrange = Color(0xFFFF7F50)
val NightDark = Color(0xFF0B1026)
val SunYellow = Color(0xFFFFD700)
val MoonWhite = Color(0xFFF5F5DC)
val StarWhite = Color(0xFFFFFFFF)
val CloudWhite = Color(0xFFF0F0F0)
val EmergencyRed = Color(0xFFFF0000)

private val NightWatchColors = darkColorScheme(
    primary = DayBlue,
    secondary = DuskOrange,
    background = NightDark,
    surface = NightDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun NightWatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightWatchColors,
        content = content
    )
}
