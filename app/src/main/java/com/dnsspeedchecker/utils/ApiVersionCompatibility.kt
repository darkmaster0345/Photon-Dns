package com.dnsspeedchecker.utils

import android.os.Build
import android.util.Log
import java.lang.reflect.Method

/**
 * Utility class for handling API level compatibility across Android versions (API 26-34)
 */
object ApiVersionCompatibility {
    
    private const val TAG = "ApiVersionCompatibility"
    
    // API level constants
    const val API_26 = 26  // Android 8.0
    const val API_28 = 28  // Android 9.0
    const val API_29 = 29  // Android 10.0
    const val API_30 = 30  // Android 11.0
    const val API_31 = 31  // Android 12.0
    const val API_32 = 32  // Android 12L
    const val API_33 = 33  // Android 13.0
    const val API_34 = 34  // Android 14.0
    
    /**
     * Check if current API level is at least the specified level
     */
    fun isAtLeast(apiLevel: Int): Boolean {
        return Build.VERSION.SDK_INT >= apiLevel
    }
    
    /**
     * Check if current API level is in the specified range
     */
    fun isInRange(minApi: Int, maxApi: Int): Boolean {
        return Build.VERSION.SDK_INT in minApi..maxApi
    }
    
    /**
     * Get current API level as integer
     */
    fun getCurrentApiLevel(): Int {
        return Build.VERSION.SDK_INT
    }
    
    /**
     * Get Android version name
     */
    fun getAndroidVersionName(): String {
        return when (Build.VERSION.SDK_INT) {
            in API_26..API_27 -> "Oreo"
            in API_28..API_28 -> "Pie"
            in API_29..API_29 -> "Q"
            in API_30..API_30 -> "R"
            in API_31..API_31 -> "S"
            in API_32..API_32 -> "12L"
            in API_33..API_33 -> "13"
            in API_34..API_34 -> "14"
            else -> "Unknown"
        }
    }
    
    /**
     * Check if notification permission is required
     */
    fun isNotificationPermissionRequired(): Boolean {
        return isAtLeast(API_33) // Android 13+
    }
    
    /**
     * Check if foreground service type is supported
     */
    fun isForegroundServiceTypeSupported(): Boolean {
        return isAtLeast(API_31) // Android 12+
    }
    
    /**
     * Check if dynamic colors are supported
     */
    fun isDynamicColorSupported(): Boolean {
        return isAtLeast(API_31) // Android 12+
    }
    
    /**
     * Check if splash screen API is supported
     */
    fun isSplashScreenApiSupported(): Boolean {
        return isAtLeast(API_31) // Android 12+
    }
    
    /**
     * Check if scoped storage is required
     */
    fun isScopedStorageRequired(): Boolean {
        return isAtLeast(API_30) // Android 11+
    }
    
    /**
     * Check if adaptive icons are supported
     */
    fun isAdaptiveIconSupported(): Boolean {
        return isAtLeast(API_26) // Android 8.0+
    }
    
    /**
     * Check if work manager constraints are supported
     */
    fun isWorkManagerConstraintsSupported(): Boolean {
        return isAtLeast(API_28) // Android 9.0+
    }
    
    /**
     * Get appropriate notification channel importance
     */
    fun getNotificationChannelImportance(): String {
        return when {
            isAtLeast(API_26) -> "default" // Use default importance for Android 8+
            else -> "high"
        }
    }
    
    /**
     * Check if we can use modern VPN builder methods
     */
    fun canUseModernVpnBuilder(): Boolean {
        return isAtLeast(API_29) // Android 10+
    }
    
    /**
     * Check if we can use network callback with modern features
     */
    fun canUseModernNetworkCallback(): Boolean {
        return isAtLeast(API_31) // Android 12+
    }
    
    /**
     * Check if we can use modern notification builder
     */
    fun canUseModernNotificationBuilder(): Boolean {
        return isAtLeast(API_26) // Android 8.0+
    }
    
    /**
     * Get appropriate VPN builder configuration based on API level
     */
    fun getVpnBuilderConfig(): VpnConfig {
        return when {
            isAtLeast(API_29) -> {
                // Modern VPN builder with all features
                VpnConfig(
                    canUseMetered = true,
                    canUseIpv6 = true,
                    canUseBypass = true,
                    canUseRouteList = true,
                    canUseDns = true,
                    canUseSession = true
                )
            }
            isAtLeast(API_26) -> {
                // Basic VPN builder
                VpnConfig(
                    canUseMetered = false,
                    canUseIpv6 = false,
                    canUseBypass = false,
                    canUseRouteList = false,
                    canUseDns = true,
                    canUseSession = false
                )
            }
            else -> {
                // Fallback configuration
                VpnConfig()
            }
        }
    }
    
    /**
     * VPN configuration based on API level
     */
    data class VpnConfig(
        val canUseMetered: Boolean = false,
        val canUseIpv6: Boolean = false,
        val canUseBypass: Boolean = false,
        val canUseRouteList: Boolean = false,
        val canUseDns: Boolean = false,
        val canUseSession: Boolean = false
    )
    
    /**
     * Get appropriate notification builder configuration
     */
    fun getNotificationBuilderConfig(): NotificationConfig {
        return when {
            isAtLeast(API_33) -> {
                // Android 13+ - full notification features
                NotificationConfig(
                    canUseFullScreenIntent = true,
                    canUseBubble = true,
                    canUseTimeout = true,
                    canUseCategory = true,
                    canUsePeople = true,
                    canUseStyle = true
                )
            }
            isAtLeast(API_31) -> {
                // Android 12+ - most notification features
                NotificationConfig(
                    canUseFullScreenIntent = true,
                    canUseBubble = false,
                    canUseTimeout = true,
                    canUseCategory = true,
                    canUsePeople = false,
                    canUseStyle = true
                )
            }
            isAtLeast(API_26) -> {
                // Android 8+ - basic notification features
                NotificationConfig(
                    canUseFullScreenIntent = true,
                    canUseBubble = false,
                    canUseTimeout = false,
                    canUseCategory = true,
                    canUsePeople = false,
                    canUseStyle = false
                )
            }
            else -> {
                // Fallback
                NotificationConfig()
            }
        }
    }
    
    /**
     * Notification configuration based on API level
     */
    data class NotificationConfig(
        val canUseFullScreenIntent: Boolean = false,
        val canUseBubble: Boolean = false,
        val canUseTimeout: Boolean = false,
        val canUseCategory: Boolean = false,
        val canUsePeople: Boolean = false,
        val canUseStyle: Boolean = false
    )
    
    /**
     * Check if we can use modern file provider
     */
    fun canUseModernFileProvider(): Boolean {
        return isAtLeast(API_24) // Android 7.0+
    }
    
    /**
     * Check if we can use modern alarm manager
     */
    fun canUseModernAlarmManager(): Boolean {
        return isAtLeast(API_26) // Android 8.0+
    }
    
    /**
     * Check if we can use modern job scheduler
     */
    fun canUseModernJobScheduler(): Boolean {
        return isAtLeast(API_26) // Android 8.0+
    }
    
    /**
     * Get appropriate foreground service type based on API level
     */
    fun getForegroundServiceType(): String {
        return when {
            isForegroundServiceTypeSupported() -> "specialUse"
            isAtLeast(API_29) -> "dataSync"
            else -> "location"
        }
    }
    
    /**
     * Check if we can use modern connectivity manager
     */
    fun canUseModernConnectivityManager(): Boolean {
        return isAtLeast(API_29) // Android 10+
    }
    
    /**
     * Get appropriate network request based on API level
     */
    fun getNetworkRequest(): NetworkRequest.Builder {
        return if (isAtLeast(API_31)) {
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
    
    /**
     * Check if we can use modern power manager
     */
    fun canUseModernPowerManager(): Boolean {
        return isAtLeast(API_28) // Android 9.0+
    }
    
    /**
     * Log API compatibility information
     */
    fun logCompatibilityInfo() {
        Log.i(TAG, "=== API Compatibility Info ===")
        Log.i(TAG, "Current API Level: ${getCurrentApiLevel()}")
        Log.i(TAG, "Android Version: ${getAndroidVersionName()}")
        Log.i(TAG, "Notification Permission Required: ${isNotificationPermissionRequired()}")
        Log.i(TAG, "Foreground Service Type Supported: ${isForegroundServiceTypeSupported()}")
        Log.i(TAG, "Dynamic Color Supported: ${isDynamicColorSupported()}")
        Log.i(TAG, "Splash Screen API Supported: ${isSplashScreenApiSupported()}")
        Log.i(TAG, "VPN Builder Config: ${getVpnBuilderConfig()}")
        Log.i(TAG, "Notification Builder Config: ${getNotificationBuilderConfig()}")
        Log.i(TAG, "Modern File Provider: ${canUseModernFileProvider()}")
        Log.i(TAG, "Modern Job Scheduler: ${canUseModernJobScheduler()}")
        Log.i(TAG, "Modern Connectivity Manager: ${canUseModernConnectivityManager()}")
        Log.i(TAG, "Modern Power Manager: ${canUseModernPowerManager()}")
        Log.i(TAG, "================================")
    }
    
    /**
     * Check if specific feature is available and log appropriate warnings
     */
    fun checkFeatureAvailability(featureName: String, isAvailable: Boolean, minApi: Int) {
        if (isAvailable) {
            Log.d(TAG, "✓ $featureName is available (API $minApi+)")
        } else {
            Log.w(TAG, "✗ $featureName is not available (requires API $minApi+, current: ${getCurrentApiLevel()})")
        }
    }
    
    /**
     * Get safe method invocation with fallback
     */
    inline fun <T> invokeSafely(
        methodName: String,
        minApi: Int,
        fallback: () -> T,
        crossinline block: () -> T?
    ): T {
        return if (isAtLeast(minApi)) {
            try {
                block() ?: fallback()
            } catch (e: Exception) {
                Log.w(TAG, "Error invoking $methodName (API $minApi+)", e)
                fallback()
            }
        } else {
            Log.w(TAG, "$methodName not available (requires API $minApi+, current: ${getCurrentApiLevel()})")
            fallback()
        }
    }
    
    /**
     * Check if we're running on a specific Android version or newer
     */
    fun isAndroidVersionOrNewer(version: String): Boolean {
        return try {
            val versionParts = version.split(".")
            val majorVersion = versionParts[0].toInt()
            Build.VERSION.SDK_INT >= majorVersion
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Android version", e)
            false
        }
    }
    
    /**
     * Get compatibility warnings for debugging
     */
    fun getCompatibilityWarnings(): List<String> {
        val warnings = mutableListOf<String>()
        
        if (!isAdaptiveIconSupported()) {
            warnings.add("Adaptive icons not supported (requires API 26+)")
        }
        
        if (!isDynamicColorSupported()) {
            warnings.add("Dynamic colors not supported (requires API 31+)")
        }
        
        if (!isSplashScreenApiSupported()) {
            warnings.add("Splash screen API not supported (requires API 31+)")
        }
        
        if (!isForegroundServiceTypeSupported()) {
            warnings.add("Foreground service type not supported (requires API 31+)")
        }
        
        if (!canUseModernVpnBuilder()) {
            warnings.add("Modern VPN builder not supported (requires API 29+)")
        }
        
        if (!canUseModernConnectivityManager()) {
            warnings.add("Modern connectivity manager not supported (requires API 29+)")
        }
        
        return warnings
    }
}
