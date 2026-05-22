package com.photondns.app.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DNSLatencyChecker @Inject constructor() {

    companion object {
        private const val DNS_PORT = 53
        private const val TEST_DOMAIN = "google.com"
        private const val TIMEOUT_MS = 5000
        private const val MAX_RETRIES = 3
        private const val TAG = "DNSLatencyChecker"
    }

    suspend fun checkLatency(dnsServer: String): Int {
        return withContext(Dispatchers.IO) {
            var totalLatency = 0L
            var successfulChecks = 0

            repeat(MAX_RETRIES) { attempt ->
                val latency = performDnsQuery(dnsServer, TEST_DOMAIN)
                if (latency > 0) {
                    totalLatency += latency
                    successfulChecks++
                } else {
                    Log.w(TAG, "DNS query failed for $dnsServer (attempt ${attempt + 1})")
                }
            }

            if (successfulChecks > 0) {
                (totalLatency / successfulChecks).toInt()
            } else {
                -1
            }
        }
    }

    suspend fun performDnsQuery(server: String, domain: String): Long {
        return withContext(Dispatchers.IO) {
            if (server.startsWith("https://")) {
                performDohQuery(server, domain)
            } else {
                performUdpDnsQuery(server, domain)
            }
        }
    }

    private fun normalizeDohUrl(url: String): String {
        return try {
            val httpUrl = url.toHttpUrl()
            if (httpUrl.pathSegments.isEmpty() || (httpUrl.pathSegments.size == 1 && httpUrl.pathSegments[0] == "")) {
                httpUrl.newBuilder().addPathSegment("dns-query").build().toString()
            } else {
                url
            }
        } catch (e: Exception) {
            url
        }
    }

    private fun performDohQuery(server: String, domain: String): Long {
        return try {
            val normalizedServer = normalizeDohUrl(server)
            val client = OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
                .build()

            val dohClient = DnsOverHttps.Builder()
                .client(client)
                .url(normalizedServer.toHttpUrl())
                .post(true)
                .build()

            val startTime = System.currentTimeMillis()
            val addresses = dohClient.lookup(domain)
            if (addresses.isNotEmpty()) {
                System.currentTimeMillis() - startTime
            } else {
                -1L
            }
        } catch (e: Exception) {
            Log.e(TAG, "DoH query error for $server", e)
            -1L
        }
    }

    private fun performUdpDnsQuery(serverIp: String, domain: String): Long {
        return try {
            DatagramSocket().use { socket ->
                socket.soTimeout = TIMEOUT_MS
                socket.connect(InetSocketAddress(serverIp, DNS_PORT))

                val query = createDnsQuery(domain)
                val request = DatagramPacket(query, query.size)

                val responseBuffer = ByteArray(512)
                val response = DatagramPacket(responseBuffer, responseBuffer.size)

                val startTime = System.currentTimeMillis()
                socket.send(request)
                socket.receive(response)
                val latency = System.currentTimeMillis() - startTime

                if (response.length > 0) latency else -1L
            }
        } catch (e: Exception) {
            Log.e(TAG, "UDP DNS query error for $serverIp", e)
            -1L
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
            performDnsQuery(server, TEST_DOMAIN) > 0
        }
    }

    private fun createDnsQuery(domain: String): ByteArray {
        val buffer = ByteBuffer.allocate(512)

        buffer.putShort(0x1234.toShort())
        buffer.putShort(0x0100.toShort())
        buffer.putShort(0x0001.toShort())
        buffer.putShort(0x0000.toShort())
        buffer.putShort(0x0000.toShort())
        buffer.putShort(0x0000.toShort())

        domain.split(".").forEach { label ->
            buffer.put(label.length.toByte())
            buffer.put(label.toByteArray())
        }
        buffer.put(0)

        buffer.putShort(0x0001.toShort())
        buffer.putShort(0x0001.toShort())

        return buffer.array().copyOf(buffer.position())
    }

    suspend fun getDnsServerInfo(server: String): DnsServerInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val host = if (server.startsWith("https://")) {
                    runCatching { server.toHttpUrl().host }.getOrDefault(server)
                } else {
                    server
                }
                val address = InetAddress.getByName(host)
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
                Log.e(TAG, "Failed to get DNS server info for $server", e)
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
