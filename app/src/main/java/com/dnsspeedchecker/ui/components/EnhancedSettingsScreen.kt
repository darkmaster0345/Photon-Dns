package com.dnsspeedchecker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsspeedchecker.data.preferences.DNSSettings
import com.dnsspeedchecker.data.preferences.Strategy
import com.dnsspeedchecker.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSettingsScreen(
    settings: DNSSettings,
    onSettingsChange: (DNSSettings) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSwitchingSettings: () -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: (String) -> Unit,
    onResetToDefaults: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val scrollProgress = remember { derivedStateOf { 
        if (scrollState.firstVisibleItemIndex == 0) 0f 
        else 1f 
    } }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Animated Background
        AnimatedSettingsBackground()
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Enhanced Top Bar
                EnhancedSettingsTopBar(
                    scrollProgress = scrollProgress.value,
                    onNavigateBack = onNavigateBack
                )
            }
            
            item {
                // Quick Actions Card
                QuickActionsCard(
                    onNavigateToSwitchingSettings = onNavigateToSwitchingSettings,
                    onExportSettings = onExportSettings,
                    onImportSettings = onImportSettings,
                    onResetToDefaults = onResetToDefaults
                )
            }
            
            item {
                // Current Settings Overview
                CurrentSettingsCard(settings = settings)
            }
            
            item {
                // Strategy Status Card
                StrategyStatusCard(settings = settings)
            }
            
            item {
                // Performance Settings Card
                PerformanceSettingsCard(
                    settings = settings,
                    onSettingsChange = onSettingsChange
                )
            }
            
            item {
                // Advanced Settings Card
                AdvancedSettingsCard(
                    settings = settings,
                    onSettingsChange = onSettingsChange
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Bottom padding
            }
        }
    }
}

@Composable
private fun AnimatedSettingsBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        targetValue = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        animatedColor,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    )
}

@Composable
private fun EnhancedSettingsTopBar(
    scrollProgress: Float,
    onNavigateBack: () -> Unit
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (scrollProgress > 0.5f) 8.dp else 0.dp,
        animationSpec = tween(300)
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = animatedElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Customize your DNS experience",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Settings Icon
            IconButton(
                onClick = { /* TODO: Show help */ },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Help,
                    contentDescription = "Help",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onNavigateToSwitchingSettings: () -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: (String) -> Unit,
    onResetToDefaults: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Tune,
                    label = "Strategy",
                    onClick = onNavigateToSwitchingSettings,
                    modifier = Modifier.weight(1f)
                )
                
                QuickActionButton(
                    icon = Icons.Default.FileUpload,
                    label = "Export",
                    onClick = onExportSettings,
                    modifier = Modifier.weight(1f)
                )
                
                QuickActionButton(
                    icon = Icons.Default.FileDownload,
                    label = "Import",
                    onClick = { /* TODO: Import dialog */ },
                    modifier = Modifier.weight(1f)
                )
                
                QuickActionButton(
                    icon = Icons.Default.Restore,
                    label = "Reset",
                    onClick = onResetToDefaults,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Column(
        modifier = modifier
            .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
            .clickable {
                isPressed = true
                onClick()
                isPressed = false
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CurrentSettingsCard(settings: DNSSettings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Settings Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingItem(
                    label = "Strategy",
                    value = settings.strategy.getDisplayName(),
                    icon = Icons.Default.Tune,
                    color = MaterialTheme.colorScheme.primary
                )
                
                SettingItem(
                    label = "Check Interval",
                    value = "${settings.getEffectiveCheckInterval()}s",
                    icon = Icons.Default.Schedule,
                    color = MaterialTheme.colorScheme.primary
                )
                
                SettingItem(
                    label = "Min Improvement",
                    value = "${settings.getEffectiveMinImprovement()}ms",
                    icon = Icons.Default.TrendingUp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                SettingItem(
                    label = "Stability Period",
                    value = "${settings.getEffectiveStabilityPeriod()}min",
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StrategyStatusCard(settings: DNSSettings) {
    val strategyColor = when (settings.strategy) {
        Strategy.CONSERVATIVE -> DnsFast
        Strategy.BALANCED -> MaterialTheme.colorScheme.primary
        Strategy.AGGRESSIVE -> DnsMedium
        Strategy.CUSTOM -> DnsSlow
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = strategyColor.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, strategyColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Strategy Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = settings.strategy.getDisplayName(),
                        style = MaterialTheme.typography.titleSmall,
                        color = strategyColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Status Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(strategyColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (settings.strategy) {
                            Strategy.CONSERVATIVE -> Icons.Default.Security
                            Strategy.BALANCED -> Icons.Default.Balance
                            Strategy.AGGRESSIVE -> Icons.Default.Speed
                            Strategy.CUSTOM -> Icons.Default.Settings
                        },
                        contentDescription = null,
                        tint = strategyColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = settings.strategy.getDescription(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PerformanceSettingsCard(
    settings: DNSSettings,
    onSettingsChange: (DNSSettings) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Performance Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto-Switch Toggle
            EnhancedToggleItem(
                title = "Smart Auto-Switch",
                subtitle = "Automatically switch to faster DNS servers",
                icon = Icons.Default.SwapHoriz,
                isChecked = settings.autoSwitchEnabled,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(autoSwitchEnabled = enabled))
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Battery Saver Toggle
            EnhancedToggleItem(
                title = "Battery Saver Mode",
                subtitle = "Reduce check frequency to save battery",
                icon = Icons.Default.Eco,
                isChecked = settings.batterySaverMode,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(batterySaverMode = enabled))
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Switch on Failure Toggle
            EnhancedToggleItem(
                title = "Switch on Failure",
                subtitle = "Immediately switch if current DNS fails",
                icon = Icons.Default.ErrorOutline,
                isChecked = settings.switchOnFailure,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(switchOnFailure = enabled))
                }
            )
        }
    }
}

@Composable
private fun AdvancedSettingsCard(
    settings: DNSSettings,
    onSettingsChange: (DNSSettings) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Advanced Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Hysteresis Toggle
                    EnhancedToggleItem(
                        title = "Hysteresis",
                        subtitle = "Prevent rapid switching between similar servers",
                        icon = Icons.Default.Sync,
                        isChecked = settings.hysteresisEnabled,
                        onCheckedChange = { enabled ->
                            onSettingsChange(settings.copy(hysteresisEnabled = enabled))
                        }
                    )
                    
                    if (settings.hysteresisEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Hysteresis Margin Slider
                        EnhancedSliderItem(
                            title = "Hysteresis Margin",
                            subtitle = "Additional margin to prevent flip-flopping",
                            value = settings.hysteresisMargin.toFloat(),
                            valueRange = 5f..50f,
                            unit = "ms",
                            onValueChange = { value ->
                                onSettingsChange(settings.copy(hysteresisMargin = value.toInt()))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun EnhancedToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun EnhancedSliderItem(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${value.toInt()}$unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )
    }
}
