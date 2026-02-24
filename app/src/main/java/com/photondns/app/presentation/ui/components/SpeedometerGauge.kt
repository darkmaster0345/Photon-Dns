package com.photondns.app.presentation.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    speed: Double,
    modifier: Modifier = Modifier,
    maxSpeed: Double = 500.0,
    size: Dp = 200.dp
) {
    val clamped = speed.coerceIn(0.0, maxSpeed)
    val targetAngle = ((clamped / maxSpeed) * 270.0 - 135.0).toFloat()
    val needleAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "needleAngle"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (this.size.minDimension / 2f) - 16.dp.toPx()
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            drawSpeedometerArc(center, radius)
            drawNeedle(center, radius * 0.8f, needleAngle)
            drawCenter(center)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.1f", clamped),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = speedColor(clamped)
            )
            Text(
                text = "Mbps",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun DrawScope.drawSpeedometerArc(center: Offset, radius: Float) {
    drawArc(
        color = Color.DarkGray.copy(alpha = 0.45f),
        startAngle = -135f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = Stroke(width = 12.dp.toPx())
    )
}

private fun DrawScope.drawNeedle(center: Offset, length: Float, angle: Float) {
    val radians = Math.toRadians(angle.toDouble())
    val end = Offset(
        x = center.x + (length * cos(radians)).toFloat(),
        y = center.y + (length * sin(radians)).toFloat()
    )
    drawLine(
        color = Color(0xFF00E5CC),
        start = center,
        end = end,
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawCenter(center: Offset) {
    drawCircle(color = Color(0xFF00E5CC), radius = 8.dp.toPx(), center = center)
    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = center)
}

private fun speedColor(speed: Double): Color {
    return when {
        speed < 10 -> Color(0xFFFF4444)
        speed < 50 -> Color(0xFFFFD700)
        speed < 100 -> Color(0xFF00E5CC)
        else -> Color(0xFF00D9A3)
    }
}
