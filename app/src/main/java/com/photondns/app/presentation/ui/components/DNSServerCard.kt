package com.photondns.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.photondns.app.data.models.DNSServer
import com.photondns.app.data.models.DNSProtocol

@Composable
fun DNSServerCard(
    server: DNSServer,
    isActive: Boolean = false,
    onServerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onServerClick(server.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF00E5CC).copy(alpha = 0.05f) else Color(0xFF131313)
        ),
        border = if (isActive) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5CC).copy(alpha = 0.3f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://flagcdn.com/w48/${server.countryCode.lowercase()}.png",
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) Color(0xFF00E5CC) else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ProtocolBadge(protocol = server.protocol)
                }
                Text(
                    text = server.ip,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (server.latency > 0) "${server.latency}ms" else "--",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = latencyColor(server.latency)
                )
                SignalBars(latency = server.latency, modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ProtocolBadge(protocol: DNSProtocol) {
    Surface(
        color = Color(0xFF00E5CC).copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = protocol.name,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF00E5CC),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SignalBars(latency: Int, modifier: Modifier = Modifier) {
    val activeBars = when {
        latency <= 0 -> 0
        latency <= 20 -> 5
        latency <= 50 -> 4
        latency <= 100 -> 3
        latency <= 200 -> 2
        else -> 1
    }
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        repeat(5) { i ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .width(3.dp)
                    .height((4 + (i * 2)).dp)
                    .background(if (i < activeBars) Color(0xFF00D9A3) else Color.White.copy(alpha = 0.1f))
            )
        }
    }
}

private fun latencyColor(latency: Int): Color {
    return when {
        latency <= 0 -> Color.Gray
        latency <= 20 -> Color(0xFF00D9A3)
        latency <= 50 -> Color(0xFF00E5CC)
        latency <= 100 -> Color(0xFFFFD700)
        else -> Color(0xFFFF4444)
    }
}

@Composable
fun QuickMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    color: Color = Color(0xFF00E5CC),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1.4f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            icon?.let { Icon(it, null, tint = color, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
        }
    }
}
