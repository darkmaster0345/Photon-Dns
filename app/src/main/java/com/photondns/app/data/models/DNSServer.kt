package com.photondns.app.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "dns_servers")
data class DNSServer(
    @PrimaryKey val id: String,
    val name: String,
    val ip: String,
    val countryCode: String,
    var latency: Int = 0,
    var isActive: Boolean = false,
    val isCustom: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis()
) : Parcelable {
    
    companion object {
        fun getPredefinedServers(): List<DNSServer> {
            return listOf(
                DNSServer("google_primary", "Google Primary", "8.8.8.8", "US"),
                DNSServer("google_secondary", "Google Secondary", "8.8.4.4", "US"),
                DNSServer("cloudflare_primary", "Cloudflare Primary", "1.1.1.1", "US"),
                DNSServer("cloudflare_secondary", "Cloudflare Secondary", "1.0.0.1", "US"),
                DNSServer("quad9_primary", "Quad9 Primary", "9.9.9.9", "CH"),
                DNSServer("quad9_secondary", "Quad9 Secondary", "149.112.112.112", "CH"),
                DNSServer("opendns_primary", "OpenDNS Primary", "208.67.222.222", "US"),
                DNSServer("opendns_secondary", "OpenDNS Secondary", "208.67.220.220", "US")
            )
        }
    }
}
