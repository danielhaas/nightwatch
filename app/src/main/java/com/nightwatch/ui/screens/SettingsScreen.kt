package com.nightwatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightwatch.emergency.EmergencyEmailSender
import com.nightwatch.model.AppSettings
import com.nightwatch.model.Language
import com.nightwatch.model.Strings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var current by remember { mutableStateOf(settings) }
    var testEmailStatus by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A2E))
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("settings"),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
                Text(
                    text = Strings.get("close"),
                    fontSize = 18.sp,
                    color = Color(0xFF64B5F6),
                    modifier = Modifier
                        .clickable {
                            onSettingsChanged(current)
                            onClose()
                        }
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Language selection
            SectionHeader(Strings.get("language"))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Language.entries.forEach { lang ->
                    LanguageChip(
                        language = lang,
                        selected = current.language == lang,
                        onClick = {
                            current = current.copy(
                                language = lang,
                                triggerWord = if (current.triggerWord == Strings.defaultTriggerWord(current.language)) {
                                    Strings.defaultTriggerWord(lang)
                                } else {
                                    current.triggerWord
                                }
                            )
                            Strings.setLanguage(lang)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Time settings
            SectionHeader(Strings.get("time_settings"))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsRow(
                label = Strings.get("use_real_sun_times"),
                toggle = current.useRealSunTimes,
                onToggle = { current = current.copy(useRealSunTimes = it) }
            )

            if (current.useRealSunTimes) {
                Spacer(modifier = Modifier.height(8.dp))
                NumberInputRow(
                    label = Strings.get("latitude"),
                    value = current.latitude.toString(),
                    onValueChange = { current = current.copy(latitude = it.toDoubleOrNull() ?: current.latitude) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                NumberInputRow(
                    label = Strings.get("longitude"),
                    value = current.longitude.toString(),
                    onValueChange = { current = current.copy(longitude = it.toDoubleOrNull() ?: current.longitude) }
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                TimeInputRow(
                    label = Strings.get("sunrise"),
                    minutes = current.fixedSunrise,
                    onMinutesChange = { current = current.copy(fixedSunrise = it) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                TimeInputRow(
                    label = Strings.get("sunset"),
                    minutes = current.fixedSunset,
                    onMinutesChange = { current = current.copy(fixedSunset = it) }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Emergency settings
            SectionHeader(Strings.get("emergency_settings"))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsRow(
                label = Strings.get("voice_detection"),
                toggle = current.voiceDetectionEnabled,
                onToggle = { current = current.copy(voiceDetectionEnabled = it) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            NumberInputRow(
                label = Strings.get("trigger_word"),
                value = current.triggerWord,
                onValueChange = { current = current.copy(triggerWord = it) },
                keyboardType = KeyboardType.Text
            )
            Spacer(modifier = Modifier.height(4.dp))
            NumberInputRow(
                label = Strings.get("repetitions"),
                value = current.triggerRepetitions.toString(),
                onValueChange = { current = current.copy(triggerRepetitions = it.toIntOrNull() ?: current.triggerRepetitions) }
            )
            Spacer(modifier = Modifier.height(4.dp))
            NumberInputRow(
                label = Strings.get("api_endpoint"),
                value = current.apiEndpoint,
                onValueChange = { current = current.copy(apiEndpoint = it) },
                keyboardType = KeyboardType.Uri
            )

            Spacer(modifier = Modifier.height(4.dp))
            NumberInputRow(
                label = Strings.get("emergency_code"),
                value = current.emergencyCode,
                onValueChange = { current = current.copy(emergencyCode = it) },
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Watchdog settings
            SectionHeader(Strings.get("watchdog_settings"))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsRow(
                label = Strings.get("watchdog_enabled"),
                toggle = current.watchdogEnabled,
                onToggle = { current = current.copy(watchdogEnabled = it) }
            )

            if (current.watchdogEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                TimeInputRow(
                    label = Strings.get("watchdog_time"),
                    minutes = current.watchdogTimeMinutes,
                    onMinutesChange = { current = current.copy(watchdogTimeMinutes = it) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                NumberInputRow(
                    label = Strings.get("watchdog_code"),
                    value = current.watchdogCode,
                    onValueChange = { current = current.copy(watchdogCode = it) },
                    keyboardType = KeyboardType.Text
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Test watchdog button
                var testWatchdogStatus by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF4CAF50))
                            .clickable {
                                testWatchdogStatus = "..."
                                coroutineScope.launch(Dispatchers.IO) {
                                    val config = EmergencyEmailSender.EmailConfig(
                                        smtpHost = current.smtpHost,
                                        smtpPort = current.smtpPort,
                                        senderEmail = current.emailSender,
                                        senderPassword = current.emailPassword,
                                        recipientEmail = current.emailRecipient,
                                        emergencyCode = current.watchdogCode
                                    )
                                    val success = EmergencyEmailSender.sendWatchdogEmail(config)
                                    testWatchdogStatus = if (success) "\u2713" else "\u2717"
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Test Watchdog",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (testWatchdogStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = testWatchdogStatus,
                            fontSize = 18.sp,
                            color = if (testWatchdogStatus == "\u2713") Color(0xFF4CAF50) else if (testWatchdogStatus == "\u2717") Color.Red else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Email settings
            SectionHeader(Strings.get("email_settings"))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsRow(
                label = Strings.get("email_enabled"),
                toggle = current.emailEnabled,
                onToggle = { current = current.copy(emailEnabled = it) }
            )

            if (current.emailEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                NumberInputRow(
                    label = Strings.get("email_recipient"),
                    value = current.emailRecipient,
                    onValueChange = { current = current.copy(emailRecipient = it) },
                    keyboardType = KeyboardType.Email
                )
                Spacer(modifier = Modifier.height(4.dp))
                NumberInputRow(
                    label = Strings.get("email_sender"),
                    value = current.emailSender,
                    onValueChange = { current = current.copy(emailSender = it) },
                    keyboardType = KeyboardType.Email
                )
                Spacer(modifier = Modifier.height(4.dp))
                PasswordInputRow(
                    label = Strings.get("email_password"),
                    value = current.emailPassword,
                    onValueChange = { current = current.copy(emailPassword = it) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                NumberInputRow(
                    label = Strings.get("smtp_host"),
                    value = current.smtpHost,
                    onValueChange = { current = current.copy(smtpHost = it) },
                    keyboardType = KeyboardType.Text
                )
                Spacer(modifier = Modifier.height(4.dp))
                NumberInputRow(
                    label = Strings.get("smtp_port"),
                    value = current.smtpPort.toString(),
                    onValueChange = { current = current.copy(smtpPort = it.toIntOrNull() ?: current.smtpPort) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Test email button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF64B5F6))
                        .clickable {
                            testEmailStatus = "..."
                            coroutineScope.launch(Dispatchers.IO) {
                                val config = EmergencyEmailSender.EmailConfig(
                                    smtpHost = current.smtpHost,
                                    smtpPort = current.smtpPort,
                                    senderEmail = current.emailSender,
                                    senderPassword = current.emailPassword,
                                    recipientEmail = current.emailRecipient,
                                    emergencyCode = current.emergencyCode
                                )
                                val success = EmergencyEmailSender.sendEmergencyEmail(config)
                                testEmailStatus = if (success) "\u2713" else "\u2717"
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Test E-Mail",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (testEmailStatus.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = testEmailStatus,
                        fontSize = 18.sp,
                        color = if (testEmailStatus == "\u2713") Color(0xFF4CAF50) else if (testEmailStatus == "\u2717") Color.Red else Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64B5F6),
        letterSpacing = 2.sp
    )
}

@Composable
private fun LanguageChip(
    language: Language,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF64B5F6) else Color(0xFF2A2A4E))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = language.displayName,
            fontSize = 16.sp,
            color = if (selected) Color.Black else Color.White.copy(alpha = 0.7f),
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun SettingsRow(
    label: String,
    toggle: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.85f)
        )
        Switch(
            checked = toggle,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF64B5F6),
                checkedTrackColor = Color(0xFF64B5F6).copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun TimeInputRow(
    label: String,
    minutes: Int,
    onMinutesChange: (Int) -> Unit
) {
    val hours = minutes / 60
    val mins = minutes % 60
    var text by remember(minutes) { mutableStateOf("%02d:%02d".format(hours, mins)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.85f)
        )
        BasicTextField(
            value = text,
            onValueChange = { newVal ->
                text = newVal
                val parts = newVal.split(":")
                if (parts.size == 2) {
                    val h = parts[0].toIntOrNull()
                    val m = parts[1].toIntOrNull()
                    if (h != null && m != null && h in 0..23 && m in 0..59) {
                        onMinutesChange(h * 60 + m)
                    }
                }
            },
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = Color.White
            ),
            modifier = Modifier
                .width(100.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2A2A4E))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            singleLine = true
        )
    }
}

@Composable
private fun NumberInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .width(200.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2A2A4E))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            singleLine = true
        )
    }
}

@Composable
private fun PasswordInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = Color.White
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .width(200.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2A2A4E))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            singleLine = true
        )
    }
}
