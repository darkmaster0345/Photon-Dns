package com.dnsspeedchecker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DnsServer(
    val id: String,
    val name: String,
    val primaryIp: String,
    val secondaryIp: String,
    val isEnabled: Boolean = true
) : Parcelable {
    
    companion object {
        val DEFAULT_SERVERS = listOf(
            DnsServer(
                id = "google",
                name = "Google DNS",
                primaryIp = "8.8.8.8",
                secondaryIp = "8.8.4.4"
            ),
            DnsServer(
                id = "cloudflare",
                name = "Cloudflare DNS",
                primaryIp = "1.1.1.1",
                secondaryIp = "1.0.0.1"
            ),
            DnsServer(
                id = "quad9",
                name = "Quad9 DNS",
                primaryIp = "9.9.9.9",
                secondaryIp = "149.112.112.112"
            ),
            DnsServer(
                id = "opendns",
                name = "OpenDNS",
                primaryIp = "208.67.222.222",
                secondaryIp = "208.67.220.220"
            )
        )
    }
}

data class DnsLatencyResult(
    val serverId: String,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean
)

data class DnsHistory(
    val serverId: String,
    val recentLatencies: MutableList<Long> = mutableListOf(),
    val consecutiveBetterCount: Int = 0
)
