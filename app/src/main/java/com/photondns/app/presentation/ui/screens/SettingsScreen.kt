package com.photondns.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.photondns.app.data.models.VpnMode
import com.photondns.app.presentation.ui.components.ErrorBanner
import com.photondns.app.presentation.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ErrorBanner(
            error = uiState.error,
            onDismiss = { viewModel.clearError() },
            onRetry = { viewModel.updateAppSettings(uiState.appSettings) }
        )

        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF00E5CC)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader("Switch Strategy")
                StrategySelector(
                    selected = uiState.currentStrategy.name,
                    onSelect = { viewModel.selectPresetStrategy(it) }
                )
            }

            item {
                SectionHeader("App Behavior")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsToggleCard(
                        title = "Battery Saver",
                        description = "Reduces DNS polling frequency to preserve battery life.",
                        checked = uiState.appSettings.batterySaverMode,
                        onCheckedChange = { viewModel.updateAppSettings(uiState.appSettings.copy(batterySaverMode = it)) }
                    )
                    SettingsToggleCard(
                        title = "Switch on Failure",
                        description = "Automatically switch to next server on connection failure.",
                        checked = uiState.appSettings.switchOnFailure,
                        onCheckedChange = { viewModel.updateAppSettings(uiState.appSettings.copy(switchOnFailure = it)) }
                    )
                    SettingsToggleCard(
                        title = "Notifications",
                        description = "Receive alerts for DNS switches and status changes.",
                        checked = uiState.appSettings.notificationsEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(uiState.appSettings.copy(notificationsEnabled = it)) }
                    )
                }
            }

            item {
                SectionHeader("Appearance")
                SettingsToggleCard(
                    title = "Dark Mode",
                    description = "Use dark AMOLED theme with deep blacks and cyan accents.",
                    checked = uiState.appSettings.darkMode,
                    onCheckedChange = { viewModel.updateAppSettings(uiState.appSettings.copy(darkMode = it)) }
                )
            }

            if (uiState.currentStrategy.name == "Custom") {
                item {
                    SectionHeader("Custom Strategy")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            StrategySlider(
                                label = "Check Interval",
                                value = uiState.currentStrategy.checkInterval.toFloat(),
                                valueRange = 30f..300f,
                                steps = 270,
                                suffix = "s",
                                onValueChange = { viewModel.updateCustomStrategy(
                                    checkInterval = it.roundToInt(),
                                    minImprovement = uiState.currentStrategy.minImprovement,
                                    consecutiveChecks = uiState.currentStrategy.consecutiveChecks,
                                    stabilityPeriod = uiState.currentStrategy.stabilityPeriod
                                )}
                            )
                            StrategySlider(
                                label = "Min Improvement",
                                value = uiState.currentStrategy.minImprovement.toFloat(),
                                valueRange = 5f..50f,
                                steps = 45,
                                suffix = "%",
                                onValueChange = { viewModel.updateCustomStrategy(
                                    checkInterval = uiState.currentStrategy.checkInterval,
                                    minImprovement = it.roundToInt(),
                                    consecutiveChecks = uiState.currentStrategy.consecutiveChecks,
                                    stabilityPeriod = uiState.currentStrategy.stabilityPeriod
                                )}
                            )
                            StrategySlider(
                                label = "Consecutive Checks",
                                value = uiState.currentStrategy.consecutiveChecks.toFloat(),
                                valueRange = 1f..10f,
                                steps = 9,
                                suffix = "",
                                onValueChange = { viewModel.updateCustomStrategy(
                                    checkInterval = uiState.currentStrategy.checkInterval,
                                    minImprovement = uiState.currentStrategy.minImprovement,
                                    consecutiveChecks = it.roundToInt(),
                                    stabilityPeriod = uiState.currentStrategy.stabilityPeriod
                                )}
                            )
                            StrategySlider(
                                label = "Stability Period",
                                value = uiState.currentStrategy.stabilityPeriod.toFloat(),
                                valueRange = 1f..10f,
                                steps = 9,
                                suffix = "min",
                                onValueChange = { viewModel.updateCustomStrategy(
                                    checkInterval = uiState.currentStrategy.checkInterval,
                                    minImprovement = uiState.currentStrategy.minImprovement,
                                    consecutiveChecks = uiState.currentStrategy.consecutiveChecks,
                                    stabilityPeriod = it.roundToInt()
                                )}
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader("Network Protocol")
                SettingsToggleCard(
                    title = "IPv6 Support",
                    description = "Intercept and optimize IPv6 DNS queries.",
                    checked = uiState.appSettings.ipv6Enabled,
                    onCheckedChange = { viewModel.updateAppSettings(uiState.appSettings.copy(ipv6Enabled = it)) }
                )
            }

            item {
                SectionHeader("VPN Mode")
                VpnModeSelector(
                    selected = uiState.appSettings.vpnMode,
                    onSelect = { viewModel.updateAppSettings(uiState.appSettings.copy(vpnMode = it)) }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(bottom = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.5f)
    )
}

@Composable
fun StrategySelector(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Conservative", "Balanced", "Aggressive", "Custom").forEach { name ->
            val isActive = name == selected
            Button(
                onClick = { onSelect(name) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color(0xFF00E5CC) else Color(0xFF131313)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.Black else Color.White
                )
            }
        }
    }
}

@Composable
fun VpnModeSelector(selected: VpnMode, onSelect: (VpnMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        VpnModeCard(
            mode = VpnMode.STANDARD,
            title = "Standard Mode",
            description = "High performance foreground service. Manual control.",
            isSelected = selected == VpnMode.STANDARD,
            onClick = { onSelect(VpnMode.STANDARD) }
        )
        VpnModeCard(
            mode = VpnMode.ALWAYS_ON,
            title = "Always-on Mode",
            description = "System-integrated VPN. Automatically reconnects. Most reliable.",
            isSelected = selected == VpnMode.ALWAYS_ON,
            onClick = { onSelect(VpnMode.ALWAYS_ON) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnModeCard(mode: VpnMode, title: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF00E5CC).copy(alpha = 0.05f) else Color(0xFF131313)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5CC)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF00E5CC) else Color.White)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SettingsToggleCard(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5CC))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategySlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    suffix: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = "${value.roundToInt()}$suffix",
                color = Color(0xFF00E5CC),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00E5CC),
                activeTrackColor = Color(0xFF00E5CC).copy(alpha = 0.5f),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}
