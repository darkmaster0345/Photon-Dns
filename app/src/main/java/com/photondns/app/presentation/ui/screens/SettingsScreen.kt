package com.photondns.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.photondns.app.data.models.VpnMode
import com.photondns.app.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF00E5CC)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader("Switch Strategy")
                StrategySelector(
                    selected = uiState.currentStrategy.name,
                    onSelect = { viewModel.selectPresetStrategy(it) }
                )
            }

            item {
                SectionHeader("Network Protocol")
                SettingsToggleCard(
                    title = "IPv6 Support",
                    description = "Intercept and optimize IPv6 DNS queries.",
                    checked = uiState.appSettings.ipv6Enabled,
                    onCheckedChange = { viewModel.updateAppSettings(uiState.appSettings.copy(ipv6Enabled = it)) }
                )
            }

            item {
                SectionHeader("VPN Mode")
                VpnModeSelector(
                    selected = uiState.appSettings.vpnMode,
                    onSelect = { viewModel.updateAppSettings(uiState.appSettings.copy(vpnMode = it)) }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(bottom = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.5f)
    )
}

@Composable
fun StrategySelector(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Conservative", "Balanced", "Aggressive").forEach { name ->
            val isActive = name == selected
            Button(
                onClick = { onSelect(name) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color(0xFF00E5CC) else Color(0xFF131313)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.Black else Color.White
                )
            }
        }
    }
}

@Composable
fun VpnModeSelector(selected: VpnMode, onSelect: (VpnMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        VpnModeCard(
            mode = VpnMode.STANDARD,
            title = "Standard Mode",
            description = "High performance foreground service. Manual control.",
            isSelected = selected == VpnMode.STANDARD,
            onClick = { onSelect(VpnMode.STANDARD) }
        )
        VpnModeCard(
            mode = VpnMode.ALWAYS_ON,
            title = "Always-on Mode",
            description = "System-integrated VPN. Automatically reconnects. Most reliable.",
            isSelected = selected == VpnMode.ALWAYS_ON,
            onClick = { onSelect(VpnMode.ALWAYS_ON) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnModeCard(mode: VpnMode, title: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF00E5CC).copy(alpha = 0.05f) else Color(0xFF131313)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5CC)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF00E5CC) else Color.White)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SettingsToggleCard(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5CC))
            )
        }
    }
}
