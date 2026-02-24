package com.dnsspeedchecker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsspeedchecker.model.DnsServer
import com.dnsspeedchecker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
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
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DNS Speed Checker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // VPN Status Card
        VpnStatusCard(
            isVpnConnected = isVpnConnected,
            currentDnsServer = currentDnsServer,
            onVpnToggle = onVpnToggle
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Auto Switch Toggle
        AutoSwitchToggle(
            isEnabled = isAutoSwitchEnabled,
            onToggle = onAutoSwitchToggle
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Latency Graph (Simplified - would use a proper charting library)
        LatencyGraphCard(latencyHistory)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // DNS Server List
        DnsServerList(
            dnsServers = DnsServer.DEFAULT_SERVERS,
            dnsLatencies = dnsLatencies,
            currentDnsServer = currentDnsServer
        )
    }
}

@Composable
fun VpnStatusCard(
    isVpnConnected: Boolean,
    currentDnsServer: DnsServer,
    onVpnToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVpnConnected) DnsFast.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        if (isVpnConnected) DnsFast else DnsUnknown
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVpnConnected) Icons.Default.VpnKey else Icons.Default.VpnKeyOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isVpnConnected) "VPN Connected" else "VPN Disconnected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isVpnConnected) DnsFast else DnsUnknown
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Current DNS: ${currentDnsServer.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onVpnToggle,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVpnConnected) Color.Red else DnsFast
                )
            ) {
                Text(
                    text = if (isVpnConnected) "Stop VPN" else "Start VPN",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AutoSwitchToggle(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
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
                    text = "Auto Switch",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Automatically switch to faster DNS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
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
fun LatencyGraphCard(latencyHistory: List<Long>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Latency History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simplified graph visualization
            if (latencyHistory.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    latencyHistory.takeLast(10).forEach { latency ->
                        val height = (latency / 200f).coerceAtMost(1f) // Normalize to 0-1
                        val animatedHeight by animateFloatAsState(
                            targetValue = height,
                            label = "latency_bar"
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(animatedHeight * 100.dp)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        latency < 50 -> DnsFast
                                        latency < 100 -> DnsMedium
                                        else -> DnsSlow
                                    }
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Last ${latencyHistory.size} measurements",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun DnsServerList(
    dnsServers: List<DnsServer>,
    dnsLatencies: Map<String, Long>,
    currentDnsServer: DnsServer
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "DNS Servers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dnsServers) { server ->
                    DnsServerItem(
                        server = server,
                        latency = dnsLatencies[server.id] ?: -1,
                        isCurrent = server.id == currentDnsServer.id
                    )
                }
            }
        }
    }
}

@Composable
fun DnsServerItem(
    server: DnsServer,
    latency: Long,
    isCurrent: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Current",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Text(
                    text = server.primaryIp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            LatencyBadge(latency = latency)
        }
    }
}

@Composable
fun LatencyBadge(latency: Long) {
    val (color, text) = when {
        latency < 0 -> DnsUnknown to "N/A"
        latency < 50 -> DnsFast to "${latency}ms"
        latency < 100 -> DnsMedium to "${latency}ms"
        else -> DnsSlow to "${latency}ms"
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
