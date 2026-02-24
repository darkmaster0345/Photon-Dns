package com.photondns.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.photondns.app.presentation.ui.components.*
import com.photondns.app.presentation.viewmodel.SpeedTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Speed Test",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5CC)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Speedometer gauge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpeedometerGauge(
                    speed = uiState.currentTest?.downloadSpeed ?: 0.0,
                    modifier = Modifier.size(200.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Test control buttons
                if (uiState.isTestRunning) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.cancelSpeedTest() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF4444)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.startSpeedTest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5CC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "START SPEED TEST",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Progress indicator
                if (uiState.isTestRunning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = uiState.testProgress,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF00E5CC),
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Metrics cards
        uiState.currentTest?.let { result ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Download",
                    value = String.format("%.1f", result.downloadSpeed),
                    unit = "Mbps",
                    color = getDownloadColor(result.downloadSpeed),
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Upload",
                    value = String.format("%.1f", result.uploadSpeed),
                    unit = "Mbps",
                    color = getUploadColor(result.uploadSpeed),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Ping",
                    value = result.ping.toString(),
                    unit = "ms",
                    color = getPingColor(result.ping),
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Jitter",
                    value = result.jitter.toString(),
                    unit = "ms",
                    color = getJitterColor(result.jitter),
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Loss",
                    value = String.format("%.1f", result.packetLoss),
                    unit = "%",
                    color = getPacketLossColor(result.packetLoss),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Share button
            Button(
                onClick = { viewModel.shareResults(
                    com.photondns.app.data.models.SpeedTestResult(
                        timestamp = result.timestamp,
                        downloadSpeed = result.downloadSpeed,
                        uploadSpeed = result.uploadSpeed,
                        ping = result.ping,
                        jitter = result.jitter,
                        packetLoss = result.packetLoss,
                        testServer = result.testServer,
                        dnsUsed = result.dnsUsed,
                        testDuration = result.testDuration
                    )
                ) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00D9A3)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Results",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Test history
        if (uiState.testHistory.isNotEmpty()) {
            Text(
                text = "Test History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(uiState.testHistory.take(10)) { result ->
                    SpeedTestHistoryCard(result = result)
                }
            }
        }
        
        // Statistics
        viewModel.getSpeedTestStats()?.let { stats ->
            Spacer(modifier = Modifier.height(16.dp))
            
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
                        text = "Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Avg Download",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${String.format("%.1f", stats.averageDownloadSpeed)} Mbps",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF00E5CC)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Avg Upload",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${String.format("%.1f", stats.averageUploadSpeed)} Mbps",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF00D9A3)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Avg Ping",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${String.format("%.0f", stats.averagePing)} ms",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFD700)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Tests",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = stats.testCount.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
fun SpeedTestHistoryCard(
    result: com.photondns.app.data.models.SpeedTestResult
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
                    text = formatTimestamp(result.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = result.testServer,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF00E5CC)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Down ${String.format("%.1f", result.downloadSpeed)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = getDownloadColor(result.downloadSpeed)
                    )
                    Text(
                        text = "Up ${String.format("%.1f", result.uploadSpeed)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = getUploadColor(result.uploadSpeed)
                    )
                }
                
                Column {
                    Text(
                        text = "Ping: ${result.ping}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = getPingColor(result.ping)
                    )
                    Text(
                        text = "Loss: ${String.format("%.1f", result.packetLoss)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = getPacketLossColor(result.packetLoss)
                    )
                }
            }
        }
    }
}

private fun getDownloadColor(speed: Double): Color {
    return when {
        speed < 10 -> Color(0xFFFF4444) // Red
        speed < 50 -> Color(0xFFFFD700) // Yellow
        speed < 100 -> Color(0xFF00E5CC) // Cyan
        else -> Color(0xFF00D9A3) // Green
    }
}

private fun getUploadColor(speed: Double): Color {
    return when {
        speed < 5 -> Color(0xFFFF4444) // Red
        speed < 20 -> Color(0xFFFFD700) // Yellow
        speed < 50 -> Color(0xFF00E5CC) // Cyan
        else -> Color(0xFF00D9A3) // Green
    }
}

private fun getPingColor(ping: Int): Color {
    return when {
        ping < 20 -> Color(0xFF00D9A3) // Green
        ping < 50 -> Color(0xFF00E5CC) // Cyan
        ping < 100 -> Color(0xFFFFD700) // Yellow
        ping < 200 -> Color(0xFFFF8C00) // Orange
        else -> Color(0xFFFF4444) // Red
    }
}

private fun getJitterColor(jitter: Int): Color {
    return when {
        jitter < 5 -> Color(0xFF00D9A3) // Green
        jitter < 15 -> Color(0xFFFFD700) // Yellow
        else -> Color(0xFFFF4444) // Red
    }
}

private fun getPacketLossColor(loss: Double): Color {
    return when {
        loss == 0.0 -> Color(0xFF00D9A3) // Green
        loss < 1.0 -> Color(0xFFFFD700) // Yellow
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
