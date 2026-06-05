package com.photondns.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
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
import com.photondns.app.presentation.ui.components.ErrorBanner
import com.photondns.app.presentation.ui.components.GlowingOrb
import com.photondns.app.presentation.ui.components.QuickMetricCard
import com.photondns.app.presentation.viewmodel.HomeViewModel

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
                    text = "PHOTON DNS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
                    color = Color(0xFF00E5CC)
                )
                Text(
                    text = "System optimized",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00D9A3).copy(alpha = 0.7f)
                )
            }

IconButton(
                onClick = { viewModel.refreshLatency() },
                enabled = !uiState.isRefreshing
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (uiState.isRefreshing) Color.Gray else Color(0xFF00E5CC)
                )
            }
        }

        if (uiState.error != null) {
            ErrorBanner(
                error = uiState.error,
                onRetry = { viewModel.refreshLatency() },
                onDismiss = { viewModel.clearError() },
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            GlowingOrb(
                isActive = uiState.isVpnConnected,
                animationsEnabled = uiState.animationsEnabled,
                modifier = Modifier.size(200.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (uiState.isVpnConnected) "CONNECTED" else "DISCONNECTED",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isVpnConnected) Color(0xFF00D9A3) else Color.White.copy(alpha = 0.5f)
                )
                if (uiState.isVpnConnected) {
                    Surface(
                        color = Color(0xFF00D9A3).copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Text(
                            text = "SECURE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00D9A3),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        uiState.activeServer?.let { server ->
            DNSServerCard(
                server = server,
                isActive = true,
                onServerClick = { viewModel.switchToServer(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickMetricCard(
                title = "Avg Latency",
                value = "${averageLatency(uiState.fastestServers)}ms",
                icon = Icons.Default.ElectricBolt,
                color = Color(0xFF00E5CC),
                modifier = Modifier.weight(1f)
            )
            QuickMetricCard(
                title = "Uptime",
                value = "99.9%",
                icon = Icons.Default.Speed,
                color = Color(0xFF00D9A3),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Fastest Servers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.fastestServers.take(3)) { server ->
                DNSServerCard(
                    server = server,
                    isActive = server.id == uiState.activeServer?.id,
                    onServerClick = { viewModel.switchToServer(it) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.toggleVpn() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isVpnConnected) Color(0xFF1A1A1A) else Color(0xFF00E5CC)
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (uiState.isVpnConnected) "DISCONNECT" else "CONNECT",
                fontWeight = FontWeight.Black,
                color = if (uiState.isVpnConnected) Color.White else Color.Black
            )
        }
    }
}

private fun averageLatency(servers: List<DNSServer>): Int {
    val values = servers.filter { it.latency > 0 }.map { it.latency }
    return if (values.isEmpty()) 0 else values.average().toInt()
}
