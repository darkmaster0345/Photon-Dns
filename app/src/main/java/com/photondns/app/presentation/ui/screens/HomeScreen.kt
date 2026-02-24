package com.photondns.app.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.photondns.app.data.models.DNSServer
import com.photondns.app.presentation.ui.components.DNSServerCard
import com.photondns.app.presentation.ui.components.GlowingOrb
import com.photondns.app.presentation.ui.components.QuickMetricCard
import com.photondns.app.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Photon DNS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5CC)
                )
                Text(
                    text = "DNS at the speed of light",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = { viewModel.refreshLatency() }, enabled = !uiState.isRefreshing) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (uiState.isRefreshing) Color.Gray else Color(0xFF00E5CC)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlowingOrb(isActive = uiState.isVpnConnected, modifier = Modifier.size(120.dp))
                Spacer(modifier = Modifier.height(16.dp))

                Badge(containerColor = if (uiState.isVpnConnected) Color(0xFF00D9A3) else Color.Gray) {
                    Text(
                        text = if (uiState.isVpnConnected) "Connected" else "Disconnected",
                        color = Color.Black,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                uiState.activeServer?.let { server ->
                    Text(
                        text = if (server.latency > 0) "${server.latency}ms" else "No data",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = latencyColor(server.latency)
                    )
                    Text(
                        text = "Current DNS Latency",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.toggleVpn() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isVpnConnected) Color(0xFFFF4444) else Color(0xFF00E5CC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isVpnConnected) "Disconnect VPN" else "Connect VPN",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        uiState.activeServer?.let { server ->
            DNSServerCard(
                server = server,
                isActive = true,
                isFastest = uiState.fastestServers.any { it.id == server.id },
                onServerClick = { viewModel.switchToServer(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickMetricCard(
                title = "Avg Latency",
                value = averageLatency(uiState.fastestServers).toString(),
                subtitle = "ms",
                color = Color(0xFF00E5CC),
                modifier = Modifier.weight(1f)
            )
            QuickMetricCard(
                title = "Uptime",
                value = "99.9",
                subtitle = "%",
                color = Color(0xFF00D9A3),
                modifier = Modifier.weight(1f)
            )
            QuickMetricCard(
                title = "Quick Test",
                value = "->",
                icon = Icons.Default.Speed,
                color = Color(0xFFFFD700),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.fastestServers.isNotEmpty()) {
            Text(
                text = "Fastest Servers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(200.dp)) {
                items(uiState.fastestServers.take(3)) { server ->
                    DNSServerCard(
                        server = server,
                        isActive = server.id == uiState.activeServer?.id,
                        isFastest = true,
                        onServerClick = { viewModel.switchToServer(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF4444).copy(alpha = 0.1f)),
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

private fun latencyColor(latency: Int): Color {
    return when {
        latency <= 0 -> Color.Gray
        latency <= 20 -> Color(0xFF00D9A3)
        latency <= 50 -> Color(0xFF00E5CC)
        latency <= 100 -> Color(0xFFFFD700)
        latency <= 200 -> Color(0xFFFF8C00)
        else -> Color(0xFFFF4444)
    }
}

private fun averageLatency(servers: List<DNSServer>): Int {
    val values = servers.filter { it.latency > 0 }.map { it.latency }
    return if (values.isEmpty()) 0 else values.average().toInt()
}
