package com.dnsspeedchecker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsspeedchecker.data.preferences.DNSSettings
import com.dnsspeedchecker.data.preferences.Strategy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchingSettingsScreen(
    settings: DNSSettings,
    onSettingsChange: (DNSSettings) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToStrategyQuiz: () -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: (String) -> Unit,
    onResetToDefaults: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var selectedStrategy by remember { mutableStateOf(settings.strategy) }
    var autoSwitchEnabled by remember { mutableStateOf(settings.autoSwitchEnabled) }
    var customCheckInterval by remember { mutableStateOf(settings.customCheckInterval) }
    var customMinImprovement by remember { mutableStateOf(settings.customMinImprovement) }
    var customConsecutiveChecks by remember { mutableStateOf(settings.customConsecutiveChecks) }
    var customStabilityPeriod by remember { mutableStateOf(settings.customStabilityPeriod) }
    var hysteresisEnabled by remember { mutableStateOf(settings.hysteresisEnabled) }
    var hysteresisMargin by remember { mutableStateOf(settings.hysteresisMargin) }
    var switchOnFailure by remember { mutableStateOf(settings.switchOnFailure) }
    var batterySaverMode by remember { mutableStateOf(settings.batterySaverMode) }
    
    // Update settings when any parameter changes
    LaunchedEffect(
        selectedStrategy,
        autoSwitchEnabled,
        customCheckInterval,
        customMinImprovement,
        customConsecutiveChecks,
        customStabilityPeriod,
        hysteresisEnabled,
        hysteresisMargin,
        switchOnFailure,
        batterySaverMode
    ) {
        val newSettings = DNSSettings(
            autoSwitchEnabled = autoSwitchEnabled,
            strategy = selectedStrategy,
            customCheckInterval = customCheckInterval,
            customMinImprovement = customMinImprovement,
            customConsecutiveChecks = customConsecutiveChecks,
            customStabilityPeriod = customStabilityPeriod,
            hysteresisEnabled = hysteresisEnabled,
            hysteresisMargin = hysteresisMargin,
            switchOnFailure = switchOnFailure,
            batterySaverMode = batterySaverMode
        )
        onSettingsChange(newSettings)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Switching Strategy",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* TODO: Show help */ }) {
                Icon(Icons.Default.Help, contentDescription = "Help")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Auto-Switch Toggle
        SettingsCard(title = "Auto-Switch") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Automatic DNS Switching",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Automatically switch to faster DNS servers based on performance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = autoSwitchEnabled,
                    onCheckedChange = { autoSwitchEnabled = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Strategy Selection
        SettingsCard(title = "Switching Strategy") {
            Column {
                Text(
                    text = "Choose your switching strategy:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Strategy.values().forEach { strategy ->
                    StrategyOption(
                        strategy = strategy,
                        isSelected = selectedStrategy == strategy,
                        onClick = { selectedStrategy = strategy }
                    )
                    
                    if (strategy != Strategy.values().last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Custom Configuration (only show when CUSTOM is selected)
        if (selectedStrategy == Strategy.CUSTOM) {
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsCard(title = "Custom Configuration") {
                Column {
                    // Check Interval Slider
                    SettingsSlider(
                        title = "Check Interval",
                        value = customCheckInterval,
                        onValueChange = { customCheckInterval = it },
                        valueRange = 5..60,
                        unit = "seconds",
                        description = "How often to check DNS performance"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Minimum Improvement Slider
                    SettingsSlider(
                        title = "Minimum Improvement",
                        value = customMinImprovement,
                        onValueChange = { customMinImprovement = it },
                        valueRange = 10..100,
                        unit = "ms",
                        description = "Minimum performance improvement to trigger a switch"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Consecutive Checks Slider
                    SettingsSlider(
                        title = "Consecutive Checks",
                        value = customConsecutiveChecks,
                        onValueChange = { customConsecutiveChecks = it },
                        valueRange = 2..10,
                        unit = "checks",
                        description = "Number of consistent better measurements before switching"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stability Period Slider
                    SettingsSlider(
                        title = "Stability Period",
                        value = customStabilityPeriod,
                        onValueChange = { customStabilityPeriod = it },
                        valueRange = 1..10,
                        unit = "minutes",
                        description = "No switching period after a DNS change"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Advanced Options
        SettingsCard(title = "Advanced Options") {
            Column {
                // Hysteresis Toggle
                SettingsToggle(
                    title = "Enable Hysteresis",
                    subtitle = "Prevents rapid switching between similar DNS servers",
                    checked = hysteresisEnabled,
                    onCheckedChange = { hysteresisEnabled = it }
                )
                
                if (hysteresisEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsSlider(
                        title = "Hysteresis Margin",
                        value = hysteresisMargin,
                        onValueChange = { hysteresisMargin = it },
                        valueRange = 5..50,
                        unit = "ms",
                        description = "Additional margin to prevent flip-flopping"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Switch on Failure Toggle
                SettingsToggle(
                    title = "Immediate Switch on DNS Failure",
                    subtitle = "Switch DNS servers immediately if current one fails",
                    checked = switchOnFailure,
                    onCheckedChange = { switchOnFailure = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Battery Saver Toggle
                SettingsToggle(
                    title = "Battery Saver Mode",
                    subtitle = "Reduces check frequency to save battery",
                    checked = batterySaverMode,
                    onCheckedChange = { batterySaverMode = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Real-time Preview
        SettingsCard(title = "Real-time Preview") {
            Column {
                Text(
                    text = "Current Configuration Preview:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                PreviewItem(
                    label = "Check Interval",
                    value = "${settings.getEffectiveCheckInterval()}s"
                )
                
                PreviewItem(
                    label = "Min Improvement",
                    value = "${settings.getEffectiveMinImprovement()}ms"
                )
                
                PreviewItem(
                    label = "Consecutive Checks",
                    value = settings.getEffectiveConsecutiveChecks().toString()
                )
                
                PreviewItem(
                    label = "Stability Period",
                    value = "${settings.getEffectiveStabilityPeriod()}min"
                )
                
                if (hysteresisEnabled) {
                    PreviewItem(
                        label = "Hysteresis Threshold",
                        value = "${settings.getHysteresisThreshold(settings.getEffectiveMinImprovement())}ms"
                    )
                }
                
                if (batterySaverMode) {
                    PreviewItem(
                        label = "Battery Saver Interval",
                        value = "${settings.getEffectiveCheckInterval()}s"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        SettingsCard(title = "Actions") {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetToDefaults,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset to Defaults")
                    }
                    
                    Button(
                        onClick = onNavigateToStrategyQuiz,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("What's Best for Me?")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export Settings")
                    }
                    
                    OutlinedButton(
                        onClick = { /* TODO: Show import dialog */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import Settings")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StrategyOption(
    strategy: Strategy,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.7f,
        animationSpec = tween(200),
        label = "alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .alpha(animatedAlpha),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strategy.getDisplayName(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = strategy.getDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = strategy.getRecommendedFor(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun SettingsSlider(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    unit: String,
    description: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat(),
            steps = valueRange.endInclusive - valueRange.start,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun PreviewItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
