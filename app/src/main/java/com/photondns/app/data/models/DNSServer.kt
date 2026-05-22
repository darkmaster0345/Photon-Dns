package com.photondns.app.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

enum class DNSProtocol {
    UDP, DOH, DOT
}

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
    val protocol: DNSProtocol = DNSProtocol.UDP,
    val dohUrl: String? = null,
    val dotHostname: String? = null,
    val addedTimestamp: Long = System.currentTimeMillis()
) : Parcelable {
    
    companion object {
        fun getPredefinedServers(): List<DNSServer> {
            return listOf(
                DNSServer("quad9_primary", "Quad9 DNS", "9.9.9.9", "CH", protocol = DNSProtocol.UDP),
                DNSServer("quad9_doh", "Quad9 DoH", "9.9.9.9", "CH", protocol = DNSProtocol.DOH, dohUrl = "https://dns.quad9.net/dns-query"),
                DNSServer("mullvad_doh", "Mullvad DoH", "194.242.2.2", "SE", protocol = DNSProtocol.DOH, dohUrl = "https://dns.mullvad.net/dns-query"),
                DNSServer("mullvad_udp", "Mullvad DNS", "194.242.2.2", "SE", protocol = DNSProtocol.UDP),
                DNSServer("adguard_doh", "AdGuard DoH", "94.140.14.14", "CY", protocol = DNSProtocol.DOH, dohUrl = "https://dns.adguard-dns.com/dns-query"),
                DNSServer("cloudflare_dns", "Cloudflare DNS", "1.1.1.1", "US", protocol = DNSProtocol.UDP),
                DNSServer("cloudflare_doh", "Cloudflare DoH", "1.1.1.1", "US", protocol = DNSProtocol.DOH, dohUrl = "https://cloudflare-dns.com/dns-query"),
                DNSServer("google_dns", "Google DNS", "8.8.8.8", "US", protocol = DNSProtocol.UDP),
                DNSServer("google_doh", "Google DoH", "8.8.8.8", "US", protocol = DNSProtocol.DOH, dohUrl = "https://dns.google/dns-query"),
                DNSServer("libredns_doh", "LibreDNS DoH", "116.202.176.26", "DE", protocol = DNSProtocol.DOH, dohUrl = "https://magical.libredns.gr/dns-query"),
                DNSServer("ahadns_doh", "AhaDNS DoH", "45.141.215.114", "NL", protocol = DNSProtocol.DOH, dohUrl = "https://doh.la.ahadns.net/dns-query")
            )
        }
    }
}
