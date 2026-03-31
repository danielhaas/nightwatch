package com.nightwatch

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.nightwatch.emergency.EmergencyMockServer
import com.nightwatch.model.AppSettings
import com.nightwatch.model.Strings
import com.nightwatch.service.KeepAliveService
import com.nightwatch.ui.screens.MainScreen
import com.nightwatch.ui.theme.NightWatchTheme
import com.nightwatch.viewmodel.MainViewModel
import com.nightwatch.voice.VoiceRecognitionService

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private var mockServer: EmergencyMockServer? = null

    private val emergencyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == VoiceRecognitionService.ACTION_EMERGENCY) {
                viewModel.triggerEmergency(Strings.get("voice_command_detected"))
            }
        }
    }

    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.onCalendarPermissionGranted()
    }

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val settings = AppSettings.load(this)
            if (settings.voiceDetectionEnabled) {
                VoiceRecognitionService.start(this)
                viewModel.setVoiceListening(true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load settings and apply language before anything else
        val settings = AppSettings.load(this)
        Strings.setLanguage(settings.language)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupFullscreen()
        setupWakeLock()

        // Mock server only for development/debugging
        // mockServer = EmergencyMockServer().also { it.start() }

        setContent {
            NightWatchTheme {
                MainScreen(viewModel = viewModel)
            }
        }

        KeepAliveService.start(this)
        requestPermissions()
        registerEmergencyReceiver()
    }

    private fun setupFullscreen() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
    }

    private fun setupWakeLock() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    private fun requestPermissions() {
        // Calendar permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
            == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.onCalendarPermissionGranted()
        } else {
            calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }

        // Audio permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val settings = AppSettings.load(this)
            if (settings.voiceDetectionEnabled) {
                VoiceRecognitionService.start(this)
                viewModel.setVoiceListening(true)
            }
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun registerEmergencyReceiver() {
        val filter = IntentFilter(VoiceRecognitionService.ACTION_EMERGENCY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(emergencyReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(emergencyReceiver, filter)
        }
    }

    override fun onResume() {
        super.onResume()
        setupFullscreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(emergencyReceiver) } catch (e: Exception) { /* ignore */ }
        // Don't stop VoiceRecognitionService or KeepAliveService here —
        // they must survive activity destruction to keep the process alive
        mockServer?.stop()
    }
}
