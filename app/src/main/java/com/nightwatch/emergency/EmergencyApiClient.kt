package com.nightwatch.emergency

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object EmergencyApiClient {

    // Default mock server URL - change for production
    var baseUrl = "http://localhost:8080"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    data class EmergencyResponse(
        val status: String,
        val emergencyId: String,
        val message: String
    )

    fun sendEmergency(): EmergencyResponse? {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        val json = JSONObject().apply {
            put("type", "voice_alert")
            put("trigger", "hilfe_hilfe_hilfe")
            put("timestamp", timestamp)
            put("device_id", "SM-T530-nightwatch")
            put("location", JSONObject().apply {
                put("lat", 0.0)
                put("lon", 0.0)
            })
        }

        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$baseUrl/api/emergency")
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                val responseJson = JSONObject(responseBody)
                EmergencyResponse(
                    status = responseJson.optString("status", "unknown"),
                    emergencyId = responseJson.optString("emergency_id", ""),
                    message = responseJson.optString("message", "")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
