package com.photondns.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.photondns.app.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCustomSettings by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5CC)
            )
            
            IconButton(
                onClick = { showExportDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export Settings",
                    tint = Color(0xFF00E5CC)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Auto-switch toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Switch DNS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Automatically switch to fastest DNS server",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Switch(
                    checked = uiState.autoSwitchEnabled,
                    onCheckedChange = { viewModel.toggleAutoSwitch() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5CC),
                        checkedTrackColor = Color(0xFF00E5CC).copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Strategy selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Switching Strategy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val strategies = viewModel.getAvailableStrategies()
                strategies.forEach { strategy ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.currentStrategy.name == strategy.name,
                            onClick = { 
                                viewModel.selectPresetStrategy(strategy.name)
                                if (strategy.name == "Custom") {
                                    showCustomSettings = true
                                }
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF00E5CC),
                                unselectedColor = Color.Gray
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strategy.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${strategy.checkInterval}s interval, ${strategy.minImprovement}ms improvement, ${strategy.consecutiveChecks} checks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Advanced options
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Advanced Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Hysteresis toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hysteresis",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Prevent rapid switching between servers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Switch(
                        checked = uiState.appSettings.hysteresisEnabled,
                        onCheckedChange = { 
                            val updatedSettings = uiState.appSettings.copy(hysteresisEnabled = it)
                            viewModel.updateAppSettings(updatedSettings)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5CC),
                            checkedTrackColor = Color(0xFF00E5CC).copy(alpha = 0.3f)
                        )
                    )
                }
                
                // Battery saver mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Battery Saver",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reduce monitoring frequency to save battery",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Switch(
                        checked = uiState.appSettings.batterySaverMode,
                        onCheckedChange = { 
                            val updatedSettings = uiState.appSettings.copy(batterySaverMode = it)
                            viewModel.updateAppSettings(updatedSettings)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5CC),
                            checkedTrackColor = Color(0xFF00E5CC).copy(alpha = 0.3f)
                        )
                    )
                }
                
                // Switch on failure
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Switch on Failure",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Immediately switch if current DNS server fails",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Switch(
                        checked = uiState.appSettings.switchOnFailure,
                        onCheckedChange = { 
                            val updatedSettings = uiState.appSettings.copy(switchOnFailure = it)
                            viewModel.updateAppSettings(updatedSettings)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5CC),
                            checkedTrackColor = Color(0xFF00E5CC).copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Speed test settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Speed Test Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Test Server: ${uiState.appSettings.speedTestServer}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // About section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Photon DNS",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5CC)
                )
                
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "DNS at the speed of light",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "(c) 2024 Photon DNS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Reset button
        Button(
            onClick = { viewModel.resetToDefaults() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF4444)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Reset to Defaults",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
    
    // Custom settings dialog
    if (showCustomSettings) {
        CustomSettingsDialog(
            currentStrategy = uiState.currentStrategy,
            onDismiss = { showCustomSettings = false },
            onSave = { interval, improvement, checks, stability ->
                viewModel.updateCustomStrategy(interval, improvement, checks, stability)
                showCustomSettings = false
            }
        )
    }
    
    // Export dialog
    if (showExportDialog) {
        ExportSettingsDialog(
            settingsText = viewModel.exportSettings(),
            onDismiss = { showExportDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSettingsDialog(
    currentStrategy: com.photondns.app.data.models.SwitchStrategy,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int) -> Unit
) {
    var checkInterval by remember { mutableStateOf(currentStrategy.checkInterval) }
    var minImprovement by remember { mutableStateOf(currentStrategy.minImprovement) }
    var consecutiveChecks by remember { mutableStateOf(currentStrategy.consecutiveChecks) }
    var stabilityPeriod by remember { mutableStateOf(currentStrategy.stabilityPeriod) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Custom Strategy Settings",
                color = Color(0xFF00E5CC)
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Check Interval: $checkInterval seconds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = checkInterval.toFloat(),
                        onValueChange = { checkInterval = it.toInt() },
                        valueRange = 5f..60f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF00E5CC),
                            thumbColor = Color(0xFF00E5CC)
                        )
                    )
                }
                
                item {
                    Text(
                        text = "Min Improvement: $minImprovement ms",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = minImprovement.toFloat(),
                        onValueChange = { minImprovement = it.toInt() },
                        valueRange = 10f..100f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF00E5CC),
                            thumbColor = Color(0xFF00E5CC)
                        )
                    )
                }
                
                item {
                    Text(
                        text = "Consecutive Checks: $consecutiveChecks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = consecutiveChecks.toFloat(),
                        onValueChange = { consecutiveChecks = it.toInt() },
                        valueRange = 2f..10f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF00E5CC),
                            thumbColor = Color(0xFF00E5CC)
                        )
                    )
                }
                
                item {
                    Text(
                        text = "Stability Period: $stabilityPeriod minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = stabilityPeriod.toFloat(),
                        onValueChange = { stabilityPeriod = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF00E5CC),
                            thumbColor = Color(0xFF00E5CC)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(checkInterval, minImprovement, consecutiveChecks, stabilityPeriod) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5CC)
                )
            ) {
                Text(
                    text = "Save",
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF00E5CC)
                )
            }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSettingsDialog(
    settingsText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Export Settings",
                color = Color(0xFF00E5CC)
            )
        },
        text = {
            Text(
                text = settingsText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5CC)
                )
            ) {
                Text(
                    text = "OK",
                    color = Color.Black
                )
            }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}
