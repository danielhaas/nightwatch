package com.nightwatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nightwatch.service.KeepAliveService
import com.nightwatch.voice.VoiceRecognitionService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON" &&
            action != "com.htc.intent.action.QUICKBOOT_POWERON"
        ) return

        android.util.Log.d("NightWatch", "BootReceiver: $action")

        try {
            KeepAliveService.start(context)
        } catch (e: Exception) {
            android.util.Log.e("NightWatch", "KeepAliveService.start failed", e)
        }
        try {
            VoiceRecognitionService.start(context)
        } catch (e: Exception) {
            android.util.Log.e("NightWatch", "VoiceRecognitionService.start failed", e)
        }
        try {
            val launch = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(launch)
        } catch (e: Exception) {
            android.util.Log.w("NightWatch", "Activity launch from boot blocked", e)
        }
    }
}
