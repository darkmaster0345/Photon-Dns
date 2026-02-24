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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsspeedchecker.model.DnsServer
import com.dnsspeedchecker.ui.theme.*
import kotlin.math.*

@Composable
fun EnhancedDnsServerList(
    dnsServers: List<DnsServer>,
    dnsLatencies: Map<String, Long>,
    currentDnsServer: DnsServer,
    detailedResults: Map<String, com.dnsspeedchecker.service.DnsLatencyResult>,
    onServerClick: ((DnsServer) -> Unit)? = null
) {
    val listState = rememberLazyListState()
    
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DNS Servers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "${dnsServers.size} Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Server List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = listState
            ) {
                items(dnsServers) { server ->
                    EnhancedDnsServerItem(
                        server = server,
                        latency = dnsLatencies[server.id],
                        isCurrent = server.id == currentDnsServer.id,
                        detailedResult = detailedResults[server.id],
                        onClick = onServerClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedDnsServerItem(
    server: DnsServer,
    latency: Long?,
    isCurrent: Boolean,
    detailedResult: com.dnsspeedchecker.service.DnsLatencyResult?,
    onClick: ((DnsServer) -> Unit)? = null
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isCurrent) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isCurrent) 2.dp else 1.dp,
        animationSpec = tween(300)
    )
    
    val latencyColor = when {
        latency == null -> DnsUnknown
        latency < 50 -> DnsFast
        latency < 100 -> DnsMedium
        else -> DnsSlow
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
            .let { if (onClick != null) it.clickable { onClick(server) } else it },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) {
                latencyColor.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isCurrent) {
            BorderStroke(animatedBorderWidth, latencyColor)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrent) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Server Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Server Info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Server Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        latencyColor.copy(alpha = 0.3f),
                                        latencyColor.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = latencyColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = server.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) latencyColor else MaterialTheme.colorScheme.onSurface
                            )
                            
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Current",
                                    tint = latencyColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "${server.primaryIp} • ${server.secondaryIp}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Latency Display
                LatencyDisplay(
                    latency = latency,
                    detailedResult = detailedResult,
                    color = latencyColor
                )
            }
            
            // Performance Details
            if (detailedResult != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                PerformanceDetailsRow(
                    detailedResult = detailedResult,
                    color = latencyColor
                )
            }
        }
    }
}

@Composable
private fun LatencyDisplay(
    latency: Long?,
    detailedResult: com.dnsspeedchecker.service.DnsLatencyResult?,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        when {
            latency != null -> {
                Text(
                    text = "${latency}ms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                // Latency Status
                val statusText = when {
                    latency < 50 -> "Excellent"
                    latency < 100 -> "Good"
                    latency < 200 -> "Fair"
                    else -> "Poor"
                }
                
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
            
            detailedResult != null -> {
                // Show detailed latency info
                Text(
                    text = "${detailedResult.getPrimaryLatency()}ms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                Text(
                    text = "P: ${detailedResult.getPrimaryLatency()}ms | S: ${detailedResult.getSecondaryLatency()}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.8f)
                )
            }
            
            else -> {
                Text(
                    text = "--",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DnsUnknown
                )
                
                Text(
                    text = "No data",
                    style = MaterialTheme.typography.bodySmall,
                    color = DnsUnknown.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun PerformanceDetailsRow(
    detailedResult: com.dnsspeedchecker.service.DnsLatencyResult,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Success Rate
        PerformanceMetric(
            label = "Success Rate",
            value = "${detailedResult.successRate}%",
            icon = Icons.Default.CheckCircle,
            color = if (detailedResult.successRate >= 95) DnsFast else color
        )
        
        // Reliability
        PerformanceMetric(
            label = "Reliability",
            value = when {
                detailedResult.successRate >= 99 -> "Excellent"
                detailedResult.successRate >= 95 -> "Good"
                detailedResult.successRate >= 90 -> "Fair"
                else -> "Poor"
            },
            icon = Icons.Default.Shield,
            color = color
        )
        
        // Response Time
        PerformanceMetric(
            label = "Avg Response",
            value = "${detailedResult.getAverageLatency()}ms",
            icon = Icons.Default.Schedule,
            color = color
        )
    }
}

@Composable
private fun PerformanceMetric(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.8f),
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AnimatedLatencyBar(
    currentLatency: Long,
    targetLatency: Long,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (currentLatency.toFloat() / targetLatency.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = EaseOutCubic)
    )
    
    val color = when {
        currentLatency < 50 -> DnsFast
        currentLatency < 100 -> DnsMedium
        else -> DnsSlow
    }
    
    Box(
        modifier = modifier
            .height(8.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(
                    color = color,
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
fun LatencyComparisonChart(
    latencies: Map<String, Long>,
    modifier: Modifier = Modifier
) {
    val sortedLatencies = latencies.entries.sortedBy { it.value }
    val maxLatency = sortedLatencies.maxOfOrNull { it.value }?.toFloat() ?: 1f
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Latency Comparison",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            sortedLatencies.forEach { (serverId, latency) ->
                val color = when {
                    latency < 50 -> DnsFast
                    latency < 100 -> DnsMedium
                    else -> DnsSlow
                }
                
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = serverId,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${latency}ms",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    AnimatedLatencyBar(
                        currentLatency = latency,
                        targetLatency = maxLatency.toLong(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
