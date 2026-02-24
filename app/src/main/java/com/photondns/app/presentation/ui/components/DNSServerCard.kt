package com.photondns.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DNSServerCard(
    server: DNSServer,
    isActive: Boolean = false,
    isFastest: Boolean = false,
    onServerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onServerClick(server.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1A2A2A) else Color(0xFF1A1A1A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 8.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://flagcdn.com/w48/${server.countryCode.lowercase()}.png",
                    contentDescription = "${server.countryCode} flag",
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color(0xFF00E5CC) else MaterialTheme.colorScheme.onSurface
                    )
                    if (isFastest) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Fastest",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(16.dp)
                        )
                    }
                    if (isActive) {
                        Badge(containerColor = Color(0xFF00E5CC)) {
                            Text(
                                text = "ACTIVE",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = server.ip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SignalBars(latency = server.latency, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (server.latency > 0) "${server.latency}ms" else "No data",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = latencyColor(server.latency)
                    )
                }
            }
        }
    }
}

@Composable
fun SignalBars(
    latency: Int,
    modifier: Modifier = Modifier,
    barCount: Int = 5
) {
    val activeBars = when {
        latency <= 0 -> 0
        latency <= 20 -> 5
        latency <= 50 -> 4
        latency <= 100 -> 3
        latency <= 200 -> 2
        else -> 1
    }
    val color = latencyColor(latency)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { i ->
            val height = when (i) {
                0 -> 4.dp
                1 -> 8.dp
                2 -> 12.dp
                3 -> 16.dp
                else -> 20.dp
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height)
                    .background(if (i < activeBars) color else Color.Gray.copy(alpha = 0.3f))
            )
        }
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
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon?.let {
                Icon(imageVector = it, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 24.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
