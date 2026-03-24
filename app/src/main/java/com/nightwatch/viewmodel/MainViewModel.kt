package com.nightwatch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nightwatch.calendar.CalendarRepository
import com.nightwatch.calendar.NextEventModel
import com.nightwatch.model.*
import com.nightwatch.util.SunCalculator
import com.nightwatch.util.TimeProvider
import com.nightwatch.voice.VoiceRecognitionService
import com.nightwatch.watchdog.WatchdogScheduler
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
    val voiceListening: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val showSettings: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    private val calendarRepository = CalendarRepository(application)
    private val watchdogScheduler = WatchdogScheduler(application)

    init {
        loadSettings()
        startTimeUpdates()
        watchdogScheduler.start()
    }

    private fun loadSettings() {
        val settings = AppSettings.load(getApplication())
        Strings.setLanguage(settings.language)
        _uiState.value = _uiState.value.copy(
            settings = settings,
            timeConfig = settings.toTimeConfig()
        )
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

    fun showSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true)
    }

    fun hideSettings() {
        _uiState.value = _uiState.value.copy(showSettings = false)
    }

    fun updateSettings(newSettings: AppSettings) {
        val app = getApplication<Application>()
        AppSettings.save(app, newSettings)
        Strings.setLanguage(newSettings.language)

        val timeConfig = if (newSettings.useRealSunTimes) {
            val sunTimes = SunCalculator.calculateForToday(newSettings.latitude, newSettings.longitude)
            TimeConfig.withSunTimes(sunTimes.sunriseMinutes, sunTimes.sunsetMinutes)
        } else {
            newSettings.toTimeConfig()
        }

        _uiState.value = _uiState.value.copy(
            settings = newSettings,
            timeConfig = timeConfig
        )
        updateTime()

        // Restart watchdog with new settings
        if (newSettings.watchdogEnabled) {
            watchdogScheduler.stop()
            watchdogScheduler.start()
        } else {
            watchdogScheduler.stop()
        }

        // Update voice service if running
        if (_uiState.value.voiceListening) {
            if (newSettings.voiceDetectionEnabled) {
                VoiceRecognitionService.updateSettings(app, newSettings)
            } else {
                VoiceRecognitionService.stop(app)
                setVoiceListening(false)
            }
        }
    }
}
