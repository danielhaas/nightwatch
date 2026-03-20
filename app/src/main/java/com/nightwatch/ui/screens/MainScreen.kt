package com.nightwatch.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightwatch.ui.components.*
import com.nightwatch.viewmodel.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Sky background gradient
        SkyBackground(
            state = uiState.dayNightState,
            progress = uiState.stateProgress
        )

        // Layer 2: Stars (behind everything, night only)
        StarsOverlay(
            state = uiState.dayNightState,
            progress = uiState.stateProgress
        )

        // Layer 3: Sun or Moon
        SunMoon(
            state = uiState.dayNightState,
            currentMinutes = uiState.currentMinutes,
            sunriseMinutes = uiState.timeConfig.sunriseEnd,
            sunsetMinutes = uiState.timeConfig.duskStart
        )

        // Layer 4: Clouds
        CloudsOverlay(
            state = uiState.dayNightState
        )

        // Layer 5: Clock display (bottom center)
        ClockDisplay(
            time = uiState.currentTime,
            state = uiState.dayNightState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Layer 6: Next calendar event (top left)
        NextEventDisplay(
            event = uiState.nextEvent,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Layer 7: Settings gear icon (bottom right, long-press to open)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { viewModel.showSettings() }
                )
                .padding(12.dp)
        ) {
            Text(
                text = "\u2699",
                fontSize = 24.sp,
                color = Color.White.copy(alpha = 0.25f)
            )
        }

        // Layer 8: Settings overlay
        if (uiState.showSettings) {
            SettingsScreen(
                settings = uiState.settings,
                onSettingsChanged = { viewModel.updateSettings(it) },
                onClose = { viewModel.hideSettings() }
            )
        }

        // Layer 9: Emergency overlay (on top of everything)
        if (uiState.emergencyActive) {
            EmergencyOverlay(
                message = uiState.emergencyMessage,
                onDismiss = { viewModel.dismissEmergency() }
            )
        }
    }
}
