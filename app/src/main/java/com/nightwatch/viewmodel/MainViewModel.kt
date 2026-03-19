package com.nightwatch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nightwatch.calendar.CalendarRepository
import com.nightwatch.calendar.NextEventModel
import com.nightwatch.model.DayNightState
import com.nightwatch.model.TimeConfig
import com.nightwatch.util.SunCalculator
import com.nightwatch.util.TimeProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val dayNightState: DayNightState = DayNightState.DAY,
    val stateProgress: Float = 0.5f,
    val currentTime: String = "00:00",
    val currentMinutes: Int = 720,
    val timeConfig: TimeConfig = TimeConfig(),
    val nextEvent: NextEventModel? = null,
    val emergencyActive: Boolean = false,
    val emergencyMessage: String = "",
    val calendarPermissionGranted: Boolean = false,
    val voiceListening: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    private val calendarRepository = CalendarRepository(application)

    init {
        startTimeUpdates()
    }

    private fun startTimeUpdates() {
        viewModelScope.launch {
            while (true) {
                updateTime()
                updateCalendar()
                delay(10_000) // Update every 10 seconds
            }
        }
    }

    private fun updateTime() {
        val minutes = TimeProvider.currentMinutes()
        val config = _uiState.value.timeConfig
        val state = DayNightState.fromTime(minutes, config)
        val progress = state.progress(minutes, config)

        _uiState.value = _uiState.value.copy(
            currentMinutes = minutes,
            currentTime = TimeProvider.currentTimeFormatted(),
            dayNightState = state,
            stateProgress = progress
        )
    }

    private fun updateCalendar() {
        if (!_uiState.value.calendarPermissionGranted) return
        val event = calendarRepository.getNextEvent()
        _uiState.value = _uiState.value.copy(nextEvent = event)
    }

    fun onCalendarPermissionGranted() {
        _uiState.value = _uiState.value.copy(calendarPermissionGranted = true)
        updateCalendar()
    }

    fun updateTimeConfig(config: TimeConfig) {
        val newConfig = if (config.useRealSunTimes) {
            val sunTimes = SunCalculator.calculateForToday(config.latitude, config.longitude)
            TimeConfig.withSunTimes(sunTimes.sunriseMinutes, sunTimes.sunsetMinutes)
        } else {
            config
        }
        _uiState.value = _uiState.value.copy(timeConfig = newConfig)
        updateTime()
    }

    fun triggerEmergency(message: String) {
        _uiState.value = _uiState.value.copy(
            emergencyActive = true,
            emergencyMessage = message
        )
        viewModelScope.launch {
            delay(10_000)
            dismissEmergency()
        }
    }

    fun dismissEmergency() {
        _uiState.value = _uiState.value.copy(
            emergencyActive = false,
            emergencyMessage = ""
        )
    }

    fun setVoiceListening(active: Boolean) {
        _uiState.value = _uiState.value.copy(voiceListening = active)
    }
}
