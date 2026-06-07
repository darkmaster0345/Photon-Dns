package com.photondns.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.photondns.app.presentation.ui.components.ErrorBanner
import com.photondns.app.presentation.ui.components.SpeedometerGauge
import com.photondns.app.presentation.viewmodel.SpeedTestViewModel

@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val progress by viewModel.testProgress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ErrorBanner(
            error = uiState.error,
            onDismiss = { viewModel.clearError() },
            onRetry = { viewModel.startSpeedTest() }
        )

        Text(
            text = "FOSS SPEED TEST",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF00E5CC)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SpeedometerGauge(
                speed = if (uiState.isTestRunning) (progress * 500).toDouble() else uiState.currentResult?.downloadSpeed ?: 0.0,
                animationsEnabled = uiState.animationsEnabled,
                modifier = Modifier.size(240.dp)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (uiState.isTestRunning) "POWERED BY FOSS" else "READY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00D9A3).copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("%.1f", if (uiState.isTestRunning) progress * 500 else uiState.currentResult?.downloadSpeed ?: 0.0),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5CC)
                )
                Text(
                    text = "Mbps",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Grid 1
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TestMetricCard("Ping", "${uiState.currentResult?.ping ?: 0}ms", Modifier.weight(1f))
            TestMetricCard("Jitter", "${uiState.currentResult?.jitter ?: 0}ms", Modifier.weight(1f))
            TestMetricCard("Loss", "${uiState.currentResult?.packetLoss ?: 0.0}%", Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Grid 2
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TestMetricCard("Bufferbloat", "+${uiState.currentResult?.bufferbloat ?: 0}ms", Modifier.weight(1f))
            TestMetricCard("Privacy", "${uiState.currentResult?.privacyScore ?: 100}%", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { if (uiState.isTestRunning) viewModel.cancelTest() else viewModel.startSpeedTest() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isTestRunning) Color(0xFF1A1A1A) else Color(0xFF00E5CC)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (uiState.isTestRunning) "ABORT" else "BEGIN FOSS TEST",
                fontWeight = FontWeight.Black,
                color = if (uiState.isTestRunning) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun TestMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
        }
    }
}
