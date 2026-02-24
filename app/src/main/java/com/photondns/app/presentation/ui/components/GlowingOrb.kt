package com.photondns.app.presentation.ui.components

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlowingOrb(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    glowColor: Color = Color(0xFF00E5CC),
    pulseColor: Color = Color(0xFF00D9A3)
) {
    val density = LocalDensity.current
    val paddingPx = with(density) { 8.dp.toPx() }

    val transition = rememberInfiniteTransition(label = "orb")
    val ringColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 10000 else 1, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = (this.size.minDimension / 2f) - paddingPx

        if (isActive) {
            for (i in 4 downTo 1) {
                drawCircle(
                    color = glowColor.copy(alpha = (glowAlpha / i) * 0.25f),
                    radius = radius + (i * 10f),
                    center = center
                )
            }
        }

        drawCircle(
            color = if (isActive) glowColor else glowColor.copy(alpha = 0.35f),
            radius = radius * if (isActive) pulseScale else 1f,
            center = center
        )

        drawCircle(
            color = pulseColor.copy(alpha = if (isActive) 0.85f else 0.25f),
            radius = radius * 0.32f * if (isActive) pulseScale else 1f,
            center = center
        )

        if (isActive) {
            drawRotatingRing(
                center = center,
                radius = radius * 0.78f,
                rotation = rotation,
                color = ringColor
            )
        }
    }
}

private fun DrawScope.drawRotatingRing(
    center: Offset,
    radius: Float,
    rotation: Float,
    color: Color
) {
    val sweepAngle = 110f
    val strokeWidth = 3f
    repeat(3) { index ->
        val startAngle = rotation + (index * 120f)
        drawArc(
            color = color.copy(alpha = 0.65f - (index * 0.15f)),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun LatencyIndicator(
    latency: Int,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 8.dp.toPx() }
    val color = when {
        latency <= 0 -> Color.Gray
        latency <= 20 -> Color(0xFF00D9A3)
        latency <= 50 -> Color(0xFF00E5CC)
        latency <= 100 -> Color(0xFFFFD700)
        latency <= 200 -> Color(0xFFFF8C00)
        else -> Color(0xFFFF4444)
    }
    val progress = when {
        latency <= 0 -> 0f
        latency <= 20 -> 1f
        latency <= 50 -> 0.8f
        latency <= 100 -> 0.6f
        latency <= 200 -> 0.4f
        else -> 0.2f
    }

    Canvas(modifier = modifier.size(size)) {
        val radius = (this.size.minDimension - strokeWidth) / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        drawCircle(
            color = Color.DarkGray.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth)
        )
    }
}
