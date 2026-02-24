package com.dnsspeedchecker.ui.theme

import androidx.compose.ui.graphics.Color

// Enhanced Color Palette
val EnhancedPrimary = Color(0xFF1976D2)
val EnhancedPrimaryDark = Color(0xFF1565C0)
val EnhancedPrimaryLight = Color(0xFF42A5F5)

val EnhancedSecondary = Color(0xFF7B1FA2)
val EnhancedSecondaryDark = Color(0xFF4A148C)
val EnhancedSecondaryLight = Color(0xFFAB47BC)

// DNS Performance Colors with Enhanced Gradients
val DnsUltraFast = Color(0xFF00C853) // Bright Green
val DnsFast = Color(0xFF4CAF50) // Green
val DnsMedium = Color(0xFFFF9800) // Orange
val DnsSlow = Color(0xFFFF5722) // Red-Orange
val DnsVerySlow = Color(0xFFD32F2F) // Deep Red
val DnsUnknown = Color(0xFF9E9E9E) // Gray

// Status Colors
val StatusConnected = Color(0xFF00C853)
val StatusDisconnected = Color(0xFF757575)
val StatusConnecting = Color(0xFF1976D2)
val StatusError = Color(0xFFD32F2F)

// Background Colors with Depth
val SurfaceLight = Color(0xFFFAFAFA)
val SurfaceDark = Color(0xFF121212)
val SurfaceElevated = Color(0xFFFFFFFF)
val SurfaceElevatedDark = Color(0xFF1E1E1E)

// Gradient Colors
val GradientPrimary = listOf(
    Color(0xFF1976D2),
    Color(0xFF42A5F5)
)

val GradientSecondary = listOf(
    Color(0xFF7B1FA2),
    Color(0xFFAB47BC)
)

val GradientSuccess = listOf(
    Color(0xFF00C853),
    Color(0xFF4CAF50)
)

val GradientWarning = listOf(
    Color(0xFFFF9800),
    Color(0xFFFF5722)
)

val GradientError = listOf(
    Color(0xFFD32F2F),
    Color(0xFFFF5252)
)

// Card Background Colors
val CardBackgroundLight = Color(0xFFFFFFFF)
val CardBackgroundDark = Color(0xFF1E1E1E)
val CardBackgroundSelected = Color(0xFFE3F2FD)
val CardBackgroundSelectedDark = Color(0xFF0D47A1)

// Interactive Colors
val InteractiveHover = Color(0xFFE3F2FD)
val InteractivePressed = Color(0xFFBBDEFB)
val InteractiveDisabled = Color(0xFFBDBDBD)

// Text Colors
val TextPrimaryLight = Color(0xFF212121)
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryLight = Color(0xFF757575)
val TextSecondaryDark = Color(0xFFBDBDBD)

// Border Colors
val BorderLight = Color(0xFFE0E0E0)
val BorderDark = Color(0xFF424242)
val BorderSelected = Color(0xFF1976D2)

// Shadow Colors
val ShadowLight = Color(0x1A000000)
val ShadowDark = Color(0x33000000)

// Animation Colors
val AnimationPrimary = Color(0xFF1976D2)
val AnimationSecondary = Color(0xFF7B1FA2)
val AnimationSuccess = Color(0xFF00C853)
val AnimationWarning = Color(0xFFFF9800)
val AnimationError = Color(0xFFD32F2F)

// Special Colors
val PulseColor = Color(0x401976D2)
val GlowColor = Color(0x601976D2)
val HighlightColor = Color(0x801976D2)

// Chart Colors
val ChartLine = Color(0xFF1976D2)
val ChartFill = Color(0x401976D2)
val ChartGrid = Color(0xFFE0E0E0)
val ChartGridDark = Color(0xFF424242)

// Performance Indicator Colors
val PerformanceExcellent = Color(0xFF00C853)
val PerformanceGood = Color(0xFF4CAF50)
val PerformanceFair = Color(0xFFFF9800)
val PerformancePoor = Color(0xFFFF5722)

// Battery Colors
val BatteryFull = Color(0xFF00C853)
val BatteryGood = Color(0xFF4CAF50)
val BatteryLow = Color(0xFFFF9800)
val BatteryCritical = Color(0xFFFF5722)

// Network Status Colors
val NetworkConnected = Color(0xFF00C853)
val NetworkConnecting = Color(0xFF1976D2)
val NetworkDisconnected = Color(0xFF757575)
val NetworkError = Color(0xFFD32F2F)

// Theme-specific Extensions
fun Color.getLightVariant(): Color {
    return when (this) {
        EnhancedPrimary -> EnhancedPrimaryLight
        EnhancedSecondary -> EnhancedSecondaryLight
        DnsFast -> DnsUltraFast
        DnsMedium -> DnsFast
        DnsSlow -> DnsMedium
        else -> this
    }
}

fun Color.getDarkVariant(): Color {
    return when (this) {
        EnhancedPrimary -> EnhancedPrimaryDark
        EnhancedSecondary -> EnhancedSecondaryDark
        DnsFast -> DnsMedium
        DnsMedium -> DnsSlow
        DnsSlow -> DnsVerySlow
        else -> this
    }
}

fun Color.getAlphaVariant(alpha: Float): Color {
    return this.copy(alpha = alpha)
}

fun Color.getGradientColors(): List<Color> {
    return when (this) {
        EnhancedPrimary -> GradientPrimary
        EnhancedSecondary -> GradientSecondary
        DnsFast -> GradientSuccess
        DnsMedium -> GradientWarning
        DnsSlow -> GradientError
        else -> listOf(this, this)
    }
}
