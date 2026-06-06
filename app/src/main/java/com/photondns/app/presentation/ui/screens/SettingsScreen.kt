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
import com.photondns.app.data.models.SwitchStrategy
import com.photondns.app.data.models.VpnMode
import com.photondns.app.presentation.viewmodel.SettingsViewModel

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
                SectionHeader("Custom Strategy")
                SettingsToggleCard(
                    title = "Use Custom Strategy",
                    description = "Override preset strategy with custom values.",
                    checked = uiState.currentStrategy.name == "Custom",
                    onCheckedChange = { useCustom ->
                        if (useCustom) viewModel.updateStrategy(uiState.appSettings.customStrategy)
                        else viewModel.selectPresetStrategy("Balanced")
                    }
                )
                if (uiState.currentStrategy.name == "Custom") {
                    Spacer(modifier = Modifier.height(12.dp))
                    CustomStrategyEditor(
                        strategy = uiState.currentStrategy,
                        onUpdate = { viewModel.updateCustomStrategy(
                            checkInterval = it.checkInterval,
                            minImprovement = it.minImprovement,
                            consecutiveChecks = it.consecutiveChecks,
                            stabilityPeriod = it.stabilityPeriod
                        ) }
                    )
                }
            }

            item {
                SectionHeader("Auto-Switch Options")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsToggleCard(
                        title = "Battery Saver",
                        description = "Reduce monitoring frequency to save power.",
                        checked = uiState.appSettings.batterySaverMode,
                        onCheckedChange = { viewModel.updateAppSettings(uiState.appSettings.copy(batterySaverMode = it)) }
                    )
                    SettingsToggleCard(
                        title = "Switch on Failure",
                        description = "Switch DNS immediately when a server fails.",
                        checked = uiState.appSettings.switchOnFailure,
                        onCheckedChange = { viewModel.updateAppSettings(uiState.appSettings.copy(switchOnFailure = it)) }
                    )
                    SettingsToggleCard(
                        title = "Notifications",
                        description = "Show status updates in notifications.",
                        checked = uiState.appSettings.notificationsEnabled,
                        onCheckedChange = { viewModel.updateAppSettings(uiState.appSettings.copy(notificationsEnabled = it)) }
                    )
                    SettingsToggleCard(
                        title = "Dark Mode",
                        description = "Use dark theme (default).",
                        checked = uiState.appSettings.darkMode,
                        onCheckedChange = { viewModel.updateAppSettings(uiState.appSettings.copy(darkMode = it)) }
                    )
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
        listOf("Conservative", "Balanced", "Aggressive").forEach { name ->
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

@Composable
fun CustomStrategyEditor(
    strategy: SwitchStrategy,
    onUpdate: (SwitchStrategy) -> Unit
) {
    var checkInterval by remember { mutableStateOf(strategy.checkInterval.toFloat()) }
    var minImprovement by remember { mutableStateOf(strategy.minImprovement.toFloat()) }
    var consecutiveChecks by remember { mutableStateOf(strategy.consecutiveChecks.toFloat()) }
    var stabilityPeriod by remember { mutableStateOf(strategy.stabilityPeriod.toFloat()) }

    LaunchedEffect(checkInterval, minImprovement, consecutiveChecks, stabilityPeriod) {
        onUpdate(
            SwitchStrategy(
                name = "Custom",
                checkInterval = checkInterval.toInt(),
                minImprovement = minImprovement.toInt(),
                consecutiveChecks = consecutiveChecks.toInt(),
                stabilityPeriod = stabilityPeriod.toInt()
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsSlider(
            title = "Check Interval",
            value = checkInterval,
            valueRange = 5f..60f,
            steps = 11,
            label = "${checkInterval.toInt()}s"
        ) { checkInterval = it }

        SettingsSlider(
            title = "Min Improvement",
            value = minImprovement,
            valueRange = 10f..100f,
            steps = 19,
            label = "${minImprovement.toInt()}ms"
        ) { minImprovement = it }

        SettingsSlider(
            title = "Consecutive Checks",
            value = consecutiveChecks,
            valueRange = 2f..10f,
            steps = 7,
            label = "${consecutiveChecks.toInt()}"
        ) { consecutiveChecks = it }

        SettingsSlider(
            title = "Stability Period",
            value = stabilityPeriod,
            valueRange = 1f..10f,
            steps = 9,
            label = "${stabilityPeriod.toInt()}min"
        ) { stabilityPeriod = it }
    }
}

@Composable
fun SettingsSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    label: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF00E5CC))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5CC), activeTrackColor = Color(0xFF00E5CC))
        )
    }
}
