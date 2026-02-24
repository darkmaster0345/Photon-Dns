package com.dnsspeedchecker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsspeedchecker.model.DnsServer
import com.dnsspeedchecker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExactReplicaMainScreen(
    isVpnConnected: Boolean,
    currentDnsServer: DnsServer,
    dnsLatencies: Map<String, Long>,
    isAutoSwitchEnabled: Boolean,
    latencyHistory: List<Long>,
    onVpnToggle: () -> Unit,
    onAutoSwitchToggle: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Light gray background
            .padding(16.dp)
    ) {
        // Header with Settings and Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Settings Icon (left)
            IconButton(
                onClick = onNavigateToSettings
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF6B7280) // Dark blue
                )
            }
            
            // Title (center)
            Text(
                text = "DNS Speed Checker",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF6B7280), // Dark blue
                fontWeight = FontWeight.Bold
            )
            
            // Empty space (right)
            Box(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // VPN Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Status Icon and Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side - Icon and Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status Icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isVpnConnected) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isVpnConnected) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Status Text
                        Column {
                            Text(
                                text = if (isVpnConnected) "VPN Connected" else "VPN Disconnected",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isVpnConnected) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Current DNS: ${currentDnsServer.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                    
                    // Right side - Toggle Button
                    Button(
                        onClick = onVpnToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVpnConnected) Color(0xFFF44336) else Color(0xFF2196F3),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(
                            text = if (isVpnConnected) "Stop VPN" else "Start VPN",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Auto Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Text
                Column {
                    Text(
                        text = "Auto Switch",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF212121),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Automatically switch to faster DNS",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575)
                    )
                }
                
                // Right side - Toggle
                Switch(
                    checked = isAutoSwitchEnabled,
                    onCheckedChange = onAutoSwitchToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4CAF50),
                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.3f),
                        uncheckedThumbColor = Color(0xFF9E9E9E),
                        uncheckedTrackColor = Color(0xFF9E9E9E).copy(alpha = 0.3f)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Latency Graph Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Latency History",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF212121),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Simple Latency Graph Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFF5F5F5))
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📊 Latency Graph",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF9E9E9E)
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // DNS Server List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "DNS Servers",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF212121),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // DNS Server List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(DnsServer.DEFAULT_SERVERS) { server ->
                        DnsServerItem(
                            server = server,
                            latency = dnsLatencies[server.id],
                            isCurrent = server.id == currentDnsServer.id
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DnsServerItem(
    server: DnsServer,
    latency: Long?,
    isCurrent: Boolean
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isCurrent) 1.02f else 1f,
        animationSpec = tween(300),
        label = "serverScale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) Color(0xFFE3F2FD) else Color.White,
            contentColor = if (isCurrent) Color.White else Color(0xFF212121)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrent) 8.dp else 2.dp
        ),
        border = if (isCurrent) {
            BorderStroke(2.dp, Color(0xFF2196F3))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Server Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrent) Color.White else Color(0xFF212121),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${server.primaryIp} • ${server.secondaryIp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) Color.White.copy(alpha = 0.8f) else Color(0xFF757575)
                )
            }
            
            // Right side - Latency
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (latency != null) {
                    Text(
                        text = "${latency}ms",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isCurrent) Color.White else Color(0xFF212121),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    latency < 50 -> Color(0xFF4CAF50) // Green
                                    latency < 100 -> Color(0xFFFF9800) // Orange
                                    else -> Color(0xFFF44336) // Red
                                }
                            )
                    )
                }
            } else {
                Text(
                    text = "--",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrent) Color.White else Color(0xFF9E9E9E),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9E9E9E))
                )
            }
        }
    }
}
