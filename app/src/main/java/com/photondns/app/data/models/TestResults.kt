package com.photondns.app.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "speed_test_results")
data class SpeedTestResult(
    @PrimaryKey val id: String = "speed_test_${System.currentTimeMillis()}",
    val timestamp: Long = System.currentTimeMillis(),
    val downloadSpeed: Double, // Mbps
    val uploadSpeed: Double, // Mbps
    val ping: Int, // ms
    val jitter: Int, // ms
    val packetLoss: Double, // percentage
    val testServer: String,
    val dnsUsed: String,
    val testDuration: Long // ms
) : Parcelable

@Parcelize
@Entity(tableName = "latency_records")
data class LatencyRecord(
    @PrimaryKey val id: String = "latency_${System.currentTimeMillis()}_${(0..1000).random()}",
    val timestamp: Long = System.currentTimeMillis(),
    val dnsServerId: String,
    val dnsServerName: String,
    val dnsServerIp: String,
    val latency: Int, // ms
    val success: Boolean
) : Parcelable

@Parcelize
@Entity(tableName = "dns_switch_events")
data class DNSSwitchEvent(
    @PrimaryKey val id: String = "switch_${System.currentTimeMillis()}",
    val timestamp: Long = System.currentTimeMillis(),
    val fromDnsServerId: String,
    val fromDnsServerName: String,
    val toDnsServerId: String,
    val toDnsServerName: String,
    val reason: SwitchReason,
    val previousLatency: Int,
    val newLatency: Int,
    val improvement: Int
) : Parcelable

enum class SwitchReason {
    AUTO_SWITCH,
    MANUAL_SWITCH,
    FAILURE_DETECTED,
    IMPROVEMENT_THRESHOLD,
    USER_INITIATED
}
