package com.dnsspeedchecker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsspeedchecker.model.DnsServer
import com.dnsspeedchecker.ui.theme.*

/**
 * Optimized DNS server list with lazy loading and performance optimizations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizedDnsServerList(
    dnsServers: List<DnsServer>,
    dnsLatencies: Map<String, Long>,
    currentDnsServer: DnsServer,
    detailedResults: Map<String, com.dnsspeedchecker.service.DnsLatencyResult>,
    modifier: Modifier = Modifier,
    onServerClick: (DnsServer) -> Unit = {}
) {
    val listState = rememberLazyListState()
    
    // Optimized recomposition - only calculate when data changes
    val sortedServers = remember(dnsServers) {
        dnsServers.sortedWithComparator { a, b ->
            val latencyA = dnsLatencies[a.id] ?: Long.MAX_VALUE
            val latencyB = dnsLatencies[b.id] ?: Long.MAX_VALUE
            
            when {
                latencyA == Long.MAX_VALUE && latencyB == Long.MAX_VALUE -> 0
                latencyA == Long.MAX_VALUE -> 1
                latencyB == Long.MAX_VALUE -> -1
                else -> latencyA.compareTo(latencyB)
            }
        }
    }
    
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(
            items = sortedServers,
            key = { it.id }
        ) { server ->
            OptimizedDnsServerItem(
                server = server,
                latency = dnsLatencies[server.id] ?: -1,
                isCurrent = server.id == currentDnsServer.id,
                detailedResult = detailedResults[server.id],
                onClick = { onServerClick(server) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizedDnsServerItem(
    server: DnsServer,
    latency: Long,
    isCurrent: Boolean,
    detailedResult: com.dnsspeedchecker.service.DnsLatencyResult?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Optimized animations - only animate when actually needed
    val animatedProgress by animateFloatAsState(
        targetValue = if (latency > 0) 1f else 0f,
        animationSpec = tween(300),
        label = "progress_animation"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "background_animation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrent) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Server info column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCurrent) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Current",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${server.primaryIp} • ${server.secondaryIp}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrent) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
                
                // Detailed metrics (only show if available and not current)
                if (!isCurrent && detailedResult != null && detailedResult.success) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailedMetric(
                            label = "Success",
                            value = "${(detailedResult.successRate * 100).toInt()}%",
                            color = if (detailedResult.successRate > 0.8f) DnsFast else DnsMedium
                        )
                        
                        if (!detailedResult.isFallback) {
                            DetailedMetric(
                                label = "Range",
                                value = "${detailedResult.minLatencyMs}-${detailedResult.maxLatencyMs}ms",
                                color = DnsMedium
                            )
                        }
                    }
                }
            }
            
            // Latency display with optimized animation
            when {
                latency > 0 -> {
                    OptimizedLatencyDisplay(
                        latency = latency,
                        animatedProgress = animatedProgress,
                        isCurrent = isCurrent
                    )
                }
                
                latency == -1L -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "Error",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                else -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "No Data",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedMetric(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = "$label: $value",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun OptimizedLatencyDisplay(
    latency: Long,
    animatedProgress: Float,
    isCurrent: Boolean
) {
    val (color, text) = when {
        latency < 50 -> DnsFast to "${latency}ms"
        latency < 100 -> DnsMedium to "${latency}ms"
        else -> DnsSlow to "${latency}ms"
    }
    
    Box(
        contentAlignment = Alignment.Center
    ) {
        // Background progress indicator
        if (animatedProgress < 1f) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        color = color.copy(alpha = 0.2f)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            color = color.copy(alpha = animatedProgress * 0.3f)
                        )
                ) {
                    // This creates a filling animation effect
                }
            }
        }
        
        // Latency text
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Memory-optimized empty state component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizedEmptyState(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {}
) {
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No DNS Data",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start the VPN to begin monitoring DNS performance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh")
            }
        }
    }
}
