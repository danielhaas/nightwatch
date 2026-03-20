# NightWatch ProGuard Rules

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep our model classes (used with SharedPreferences keys)
-keep class com.nightwatch.model.** { *; }

# Keep emergency API client (reflection-free but uses JSON)
-keep class com.nightwatch.emergency.** { *; }

# Keep voice service (started via Intent)
-keep class com.nightwatch.voice.VoiceRecognitionService { *; }
