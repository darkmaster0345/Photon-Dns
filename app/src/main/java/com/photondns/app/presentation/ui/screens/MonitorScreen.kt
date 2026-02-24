package com.photondns.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.photondns.app.presentation.ui.components.*
import com.photondns.app.presentation.viewmodel.MonitorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
                text = "DNS Monitor",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5CC)
            )
            
            IconButton(
                onClick = { viewModel.refreshData() },
                enabled = !uiState.isRefreshing
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (uiState.isRefreshing) Color.Gray else Color(0xFF00E5CC)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Real-time latency graph
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Real-time Latency (60 seconds)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LatencyGraph(
                    data = viewModel.getLatencyDataForGraph(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Server performance stats
        val performanceStats = viewModel.getServerPerformanceStats()
        if (performanceStats.isNotEmpty()) {
            Text(
                text = "Server Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(performanceStats.values.toList()) { stats ->
                    ServerPerformanceCard(stats = stats)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // DNS switch history
        if (uiState.switchEvents.isNotEmpty()) {
            Text(
                text = "Recent DNS Switches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(uiState.switchEvents.take(10)) { event ->
                    SwitchEventCard(event = event)
                }
            }
        }
        
        // Error handling
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF4444).copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = Color(0xFFFF4444),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ServerPerformanceCard(
    stats: com.photondns.app.presentation.viewmodel.ServerPerformanceStats
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stats.serverName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${String.format("%.1f", stats.averageLatency)}ms",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = getLatencyColor(stats.averageLatency.toInt())
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Uptime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${String.format("%.1f", stats.uptime)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00D9A3)
                    )
                }
                
                Column {
                    Text(
                        text = "Success Rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${stats.successfulChecks}/${stats.totalChecks}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00E5CC)
                    )
                }
                
                Column {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (stats.uptime >= 95.0) "Excellent" else if (stats.uptime >= 90.0) "Good" else "Poor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (stats.uptime >= 95.0) Color(0xFF00D9A3) else if (stats.uptime >= 90.0) Color(0xFFFFD700) else Color(0xFFFF4444)
                    )
                }
            }
        }
    }
}

@Composable
fun SwitchEventCard(
    event: com.photondns.app.data.models.DNSSwitchEvent
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DNS Switch",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = formatTimestamp(event.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${event.fromDnsServerName} -> ${event.toDnsServerName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reason: ${event.reason.name.replace("_", " ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF00E5CC)
                )
                
                Text(
                    text = "Improvement: ${event.improvement}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (event.improvement > 0) Color(0xFF00D9A3) else Color(0xFFFF4444)
                )
            }
        }
    }
}

private fun getLatencyColor(latency: Int): Color {
    return when {
        latency <= 0 -> Color.Gray
        latency <= 20 -> Color(0xFF00D9A3) // Green
        latency <= 50 -> Color(0xFF00E5CC) // Cyan
        latency <= 100 -> Color(0xFFFFD700) // Yellow
        latency <= 200 -> Color(0xFFFF8C00) // Orange
        else -> Color(0xFFFF4444) // Red
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
    }
}
