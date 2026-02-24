package com.photondns.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SwitchStrategy(
    val name: String,
    val checkInterval: Int, // seconds
    val minImprovement: Int, // milliseconds
    val consecutiveChecks: Int, // count
    val stabilityPeriod: Int // minutes
) : Parcelable {
    
    companion object {
        fun getPresets(): List<SwitchStrategy> {
            return listOf(
                SwitchStrategy(
                    name = "Conservative",
                    checkInterval = 10,
                    minImprovement = 30,
                    consecutiveChecks = 5,
                    stabilityPeriod = 3
                ),
                SwitchStrategy(
                    name = "Balanced",
                    checkInterval = 5,
                    minImprovement = 20,
                    consecutiveChecks = 4,
                    stabilityPeriod = 2
                ),
                SwitchStrategy(
                    name = "Aggressive",
                    checkInterval = 5,
                    minImprovement = 15,
                    consecutiveChecks = 2,
                    stabilityPeriod = 1
                )
            )
        }
    }
}

@Parcelize
data class AppSettings(
    val autoSwitchEnabled: Boolean = false,
    val currentStrategy: String = "Balanced",
    val customStrategy: SwitchStrategy = SwitchStrategy(
        name = "Custom",
        checkInterval = 5,
        minImprovement = 20,
        consecutiveChecks = 4,
        stabilityPeriod = 2
    ),
    val hysteresisEnabled: Boolean = true,
    val batterySaverMode: Boolean = false,
    val switchOnFailure: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val darkMode: Boolean = true,
    val speedTestServer: String = "auto"
) : Parcelable

data class PingResult(
    val ping: Int, // ms
    val jitter: Int, // ms
    val packetLoss: Double, // percentage
    val success: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class NetworkMetrics(
    val downloadSpeed: Double = 0.0, // Mbps
    val uploadSpeed: Double = 0.0, // Mbps
    val ping: Int = 0, // ms
    val jitter: Int = 0, // ms
    val packetLoss: Double = 0.0, // percentage
    val timestamp: Long = System.currentTimeMillis()
)
