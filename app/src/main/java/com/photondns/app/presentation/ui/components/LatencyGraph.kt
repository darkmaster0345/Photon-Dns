package com.photondns.app.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.photondns.app.presentation.viewmodel.LatencyDataPoint

@Composable
fun LatencyGraph(
    data: List<LatencyDataPoint>,
    modifier: Modifier = Modifier,
    maxDataPoints: Int = 60
) {
    val recentData = data.takeLast(maxDataPoints)
    if (recentData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No latency data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }

    val maxLatency = (recentData.flatMap { it.latencyValues.values }.maxOrNull() ?: 100.0)
        .coerceAtLeast(100.0)

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        drawRect(color = Color(0xFF121212))
        drawGridLines()
        drawLatencyLines(recentData, maxLatency)
    }
}

private fun DrawScope.drawGridLines() {
    val gridColor = Color.White.copy(alpha = 0.1f)
    val horizontal = 5
    val vertical = 6

    for (i in 0..horizontal) {
        val y = (size.height / horizontal) * i
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
    for (i in 0..vertical) {
        val x = (size.width / vertical) * i
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawLatencyLines(
    data: List<LatencyDataPoint>,
    maxLatency: Double
) {
    val colors = listOf(
        Color(0xFF00E5CC),
        Color(0xFF00D9A3),
        Color(0xFFFFD700),
        Color(0xFFFF8C00),
        Color(0xFFE91E63)
    )

    val serverIds = data.flatMap { it.latencyValues.keys }.distinct()
    val stepX = if (data.size > 1) size.width / (data.size - 1) else size.width

    serverIds.forEachIndexed { serverIndex, serverId ->
        val path = Path()
        var hasPoint = false
        data.forEachIndexed { index, point ->
            val latency = point.latencyValues[serverId] ?: return@forEachIndexed
            if (latency <= 0) return@forEachIndexed
            val x = index * stepX
            val y = size.height - ((latency / maxLatency) * size.height).toFloat()
            if (!hasPoint) {
                path.moveTo(x, y)
                hasPoint = true
            } else {
                path.lineTo(x, y)
            }
        }
        if (hasPoint) {
            drawPath(
                path = path,
                color = colors[serverIndex % colors.size],
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(92.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
