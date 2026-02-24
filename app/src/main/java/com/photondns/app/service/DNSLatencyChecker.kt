package com.photondns.app.service

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.dnsoverhttps.DnsOverHttps
import java.util.concurrent.TimeUnit

@Singleton
class DNSLatencyChecker @Inject constructor() {
    
    companion object {
        private const val DNS_PORT = 53
        private const val TEST_DOMAIN = "google.com"
        private const val TIMEOUT_MS = 5000
        private const val MAX_RETRIES = 3
    }
    
    suspend fun checkLatency(dnsServer: String): Int {
        return withContext(Dispatchers.IO) {
            var totalLatency = 0
            var successfulChecks = 0
            
            repeat(MAX_RETRIES) { attempt ->
                try {
                    val latency = performDnsQuery(dnsServer, TEST_DOMAIN)
                    if (latency > 0) {
                        totalLatency += latency.toInt()
                        successfulChecks++
                    }
                } catch (e: Exception) {
                    Log.w("DNSLatencyChecker", "DNS query failed for $dnsServer (attempt ${attempt + 1})", e)
                }
            }
            
            if (successfulChecks > 0) {
                totalLatency / successfulChecks
            } else {
                -1 // Indicates failure
            }
        }
    }
    
    suspend fun performDnsQuery(server: String, domain: String): Long {
        return withContext(Dispatchers.IO) {
            try {
                val bootstrapClient = OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
                    .readTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
                    .build()
                
                val dnsUrl = if (server.startsWith("https://")) server else "https://$server/dns-query"
                
                val dohClient = DnsOverHttps.Builder()
                    .client(bootstrapClient)
                    .url(dnsUrl.toHttpUrl())
                    .post(true)
                    .build()
                
                val startTime = System.currentTimeMillis()
                val addresses = dohClient.lookup(domain)
                
                if (addresses.isNotEmpty()) {
                    val endTime = System.currentTimeMillis()
                    endTime - startTime
                } else {
                    -1L
                }
            } catch (e: Exception) {
                Log.e("DNSLatencyChecker", "DNS query error for $server", e)
                -1L
            }
        }
    }
    
    suspend fun checkMultipleServers(servers: List<String>): Map<String, Int> {
        return coroutineScope {
            servers
                .map { server ->
                    async(Dispatchers.IO) {
                        server to checkLatency(server)
                    }
                }
                .awaitAll()
                .toMap()
        }
    }
    
    suspend fun isDnsServerResponsive(server: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val latency = performDnsQuery(server, TEST_DOMAIN)
                latency > 0
            } catch (e: Exception) {
                false
            }
        }
    }
    
    private fun createDnsQuery(domain: String): ByteArray {
        val buffer = ByteBuffer.allocate(512)
        
        // DNS Header
        buffer.putShort(0x1234.toShort()) // Transaction ID
        buffer.putShort(0x0100.toShort()) // Flags: Standard query
        buffer.putShort(0x0001.toShort()) // Questions: 1
        buffer.putShort(0x0000.toShort()) // Answer RRs: 0
        buffer.putShort(0x0000.toShort()) // Authority RRs: 0
        buffer.putShort(0x0000.toShort()) // Additional RRs: 0
        
        // Question section
        domain.split(".").forEach { label ->
            buffer.put(label.length.toByte())
            buffer.put(label.toByteArray())
        }
        buffer.put(0) // End of domain name
        
        buffer.putShort(0x0001.toShort()) // Type: A record
        buffer.putShort(0x0001.toShort()) // Class: IN
        
        return buffer.array().copyOf(buffer.position())
    }
    
    suspend fun getDnsServerInfo(server: String): DnsServerInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val address = InetAddress.getByName(server)
                val latency = checkLatency(server)
                val isResponsive = latency > 0
                
                DnsServerInfo(
                    server = server,
                    ipAddress = address.hostAddress,
                    hostname = address.hostName,
                    latency = latency,
                    isResponsive = isResponsive
                )
            } catch (e: Exception) {
                Log.e("DNSLatencyChecker", "Failed to get DNS server info for $server", e)
                null
            }
        }
    }
}

data class DnsServerInfo(
    val server: String,
    val ipAddress: String?,
    val hostname: String?,
    val latency: Int,
    val isResponsive: Boolean
)
