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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsspeedchecker.model.DnsServer
import com.dnsspeedchecker.ui.theme.*
import kotlin.math.*
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMainScreen(
    isVpnConnected: Boolean,
    currentDnsServer: DnsServer,
    dnsLatencies: Map<String, Long>,
    isAutoSwitchEnabled: Boolean,
    latencyHistory: List<Long>,
    onVpnToggle: () -> Unit,
    onAutoSwitchToggle: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    onRefreshDns: () -> Unit,
    isPerformingHealthCheck: Boolean,
    detailedDnsResults: Map<String, com.dnsspeedchecker.service.DnsLatencyResult>
) {
    Log.d("EnhancedMainScreen", "Rendering EnhancedMainScreen - VPN: $isVpnConnected, AutoSwitch: $isAutoSwitchEnabled")
    
    val listState = rememberLazyListState()
    val scrollProgress = remember { derivedStateOf { 
        if (listState.firstVisibleItemIndex == 0) 0f 
        else 1f 
    } }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Animated Background
        AnimatedBackground(isVpnConnected)
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Enhanced Top Bar
                EnhancedTopBar(
                    scrollProgress = scrollProgress.value,
                    onNavigateToSettings = onNavigateToSettings,
                    onRefreshDns = onRefreshDns,
                    isPerformingHealthCheck = isPerformingHealthCheck
                )
            }
            
            item {
                // Enhanced VPN Status Card
                EnhancedVpnStatusCard(
                    isVpnConnected = isVpnConnected,
                    currentDnsServer = currentDnsServer,
                    onVpnToggle = onVpnToggle
                )
            }
            
            item {
                // Enhanced Auto Switch Card
                EnhancedAutoSwitchCard(
                    isEnabled = isAutoSwitchEnabled,
                    onToggle = onAutoSwitchToggle
                )
            }
            
            item {
                // Performance Overview Card
                PerformanceOverviewCard(
                    dnsLatencies = dnsLatencies,
                    latencyHistory = latencyHistory
                )
            }
            
            item {
                // Enhanced DNS Server List
                EnhancedDnsServerList(
                    dnsServers = DnsServer.DEFAULT_SERVERS,
                    dnsLatencies = dnsLatencies,
                    currentDnsServer = currentDnsServer,
                    detailedResults = detailedDnsResults
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Bottom padding
            }
        }
    }
}

@Composable
private fun AnimatedBackground(isVpnConnected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedColor by infiniteTransition.animateColor(
        initialValue = if (isVpnConnected) DnsFast else DnsUnknown,
        targetValue = if (isVpnConnected) DnsFast.copy(alpha = 0.3f) else DnsUnknown.copy(alpha = 0.3f),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
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
private fun EnhancedTopBar(
    scrollProgress: Float,
    onNavigateToSettings: () -> Unit,
    onRefreshDns: () -> Unit,
    isPerformingHealthCheck: Boolean
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
            Column {
                Text(
                    text = "DNS Speed Checker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                AnimatedText(
                    text = "Optimize your network performance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Row {
                // Refresh Button
                IconButton(
                    onClick = onRefreshDns,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    if (isPerformingHealthCheck) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh DNS Servers",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Settings Button
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Open Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedVpnStatusCard(
    isVpnConnected: Boolean,
    currentDnsServer: DnsServer,
    onVpnToggle: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isVpnConnected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    val animatedColor by animateColorAsState(
        targetValue = if (isVpnConnected) DnsFast else DnsUnknown,
        animationSpec = tween(500)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = animatedColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Status Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                animatedColor.copy(alpha = 0.3f),
                                animatedColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVpnConnected) Icons.Default.VpnKey else Icons.Default.VpnKeyOff,
                    contentDescription = if (isVpnConnected) "VPN Connected" else "VPN Disconnected",
                    tint = animatedColor,
                    modifier = Modifier.size(48.dp)
                )
                
                // Pulse Animation
                if (isVpnConnected) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                animatedColor.copy(alpha = 0.2f),
                                CircleShape
                            )
                            .scale(animateFloatAsState(
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = EaseInOutCubic),
                                    repeatMode = RepeatMode.Reverse
                                )
                            ).value)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Status Text with Animation
            AnimatedText(
                text = if (isVpnConnected) "VPN Connected" else "VPN Disconnected",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Current DNS Server
            Text(
                text = currentDnsServer.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "${currentDnsServer.primaryIp} • ${currentDnsServer.secondaryIp}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Enhanced Toggle Button
            EnhancedToggleButton(
                text = if (isVpnConnected) "Stop VPN" else "Start VPN",
                icon = if (isVpnConnected) Icons.Default.Stop else Icons.Default.PlayArrow,
                backgroundColor = if (isVpnConnected) Color.Red else animatedColor,
                onClick = onVpnToggle
            )
        }
    }
}

@Composable
private fun EnhancedAutoSwitchCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isEnabled) 2.dp else 1.dp,
        animationSpec = tween(300)
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isEnabled) {
            BorderStroke(animatedBorderWidth, MaterialTheme.colorScheme.primary)
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clickable { onToggle(!isEnabled) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Smart Auto-Switch",
                        tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Smart Auto-Switch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Automatically switch to fastest DNS servers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (isEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Auto-Switch Active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Enhanced Switch
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun PerformanceOverviewCard(
    dnsLatencies: Map<String, Long>,
    latencyHistory: List<Long>
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Performance Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Performance Overview",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mini Latency Graph
            MiniLatencyGraph(latencyHistory)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Performance Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PerformanceStat(
                    label = "Fastest",
                    value = dnsLatencies.values.minOrNull()?.toString() ?: "--",
                    unit = "ms",
                    color = DnsFast
                )
                
                PerformanceStat(
                    label = "Average",
                    value = if (dnsLatencies.isNotEmpty()) {
                        (dnsLatencies.values.average()).toInt().toString()
                    } else "--",
                    unit = "ms",
                    color = MaterialTheme.colorScheme.primary
                )
                
                PerformanceStat(
                    label = "Servers",
                    value = dnsLatencies.size.toString(),
                    unit = "",
                    color = DnsMedium
                )
            }
        }
    }
}

@Composable
private fun MiniLatencyGraph(latencyHistory: List<Long>) {
    val density = LocalDensity.current
    val graphHeight = 60.dp
    val graphWidth = with(density) { graphHeight * 8f } // 8:1 aspect ratio
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(graphHeight)
    ) {
        if (latencyHistory.isEmpty()) {
            Log.w("MiniLatencyGraph", "Latency history is empty")
            return@Canvas
        }
        
        val maxLatency = latencyHistory.maxOrNull()?.toFloat() ?: 1f
        val minLatency = latencyHistory.minOrNull()?.toFloat() ?: 0f
        val range = maxLatency - minLatency
        
        if (range <= 0f) {
            Log.w("MiniLatencyGraph", "Invalid latency range: $range")
            return@Canvas
        }
        
        val points = latencyHistory.mapIndexed { index, latency ->
            val x = (index.toFloat() / (latencyHistory.size - 1)) * size.width
            val y = size.height - ((latency - minLatency) / range) * size.height
            Offset(x, y)
        }
        
        // Draw gradient background
        drawPath(
            path = Path().apply {
                moveTo(points.first().x, size.height)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, size.height)
                close()
            },
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                )
            )
        )
        
        // Draw line
        drawPath(
            path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
            },
            color = MaterialTheme.colorScheme.primary,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw points
        points.forEach { point ->
            drawCircle(
                color = MaterialTheme.colorScheme.primary,
                radius = 3.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun PerformanceStat(
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun EnhancedToggleButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Button(
        onClick = {
            // Trigger scale animation
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale },
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AnimatedText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
) {
    var visibleText by remember { mutableStateOf("") }
    val targetText = text
    
    LaunchedEffect(targetText) {
        visibleText = ""
        for (i in targetText.indices) {
            delay(50)
            visibleText += targetText[i]
        }
    }
    
    Text(
        text = visibleText,
        style = style,
        color = color
    )
}
