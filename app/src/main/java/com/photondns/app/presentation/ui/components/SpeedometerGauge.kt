package com.photondns.app.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SpeedometerGauge(
    speed: Double,
    animationsEnabled: Boolean,
    maxSpeed: Double = 500.0,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = speed.coerceIn(0.0, maxSpeed).toFloat(),
        animationSpec = if (animationsEnabled) tween(1000, easing = FastOutSlowInEasing) else snap(),
        label = "speed"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.2f
        val strokeWidth = 12.dp.toPx()

        // Background Track
        drawArc(
            color = Color.White.copy(alpha = 0.05f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Active Speed Arc
        val sweepAngle = (animatedSpeed / maxSpeed.toFloat()) * 270f
        drawArc(
            brush = Brush.sweepGradient(
                0.0f to Color(0xFF00E5CC),
                0.5f to Color(0xFF00D9A3),
                1.0f to Color(0xFF00E5CC)
            ),
            startAngle = 135f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Ticks
        for (i in 0..10) {
            val angle = 135f + (i * 27f)
            val start = Offset(
                center.x + (radius - 20.dp.toPx()) * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
                center.y + (radius - 20.dp.toPx()) * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
            )
            val end = Offset(
                center.x + radius * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(),
                center.y + radius * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
            )
            drawLine(
                color = if (angle <= 135f + sweepAngle) Color(0xFF00E5CC) else Color.White.copy(alpha = 0.1f),
                start = start,
                end = end,
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
