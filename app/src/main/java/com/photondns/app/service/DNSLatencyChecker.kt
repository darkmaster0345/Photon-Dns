package com.photondns.app.service

import android.util.Log
import com.photondns.app.data.models.DNSProtocol
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
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLParameters

@Singleton
class DNSLatencyChecker @Inject constructor() {

    companion object {
        private const val DNS_PORT = 53
        private const val DOT_PORT = 853
        private const val TEST_DOMAIN = "google.com"
        private const val TIMEOUT_MS = 5000
        private const val MAX_RETRIES = 2
        private const val TAG = "DNSLatencyChecker"
    }

    suspend fun checkLatency(serverIp: String, protocol: DNSProtocol = DNSProtocol.UDP, dohUrl: String? = null, dotHostname: String? = null): Int {
        return withContext(Dispatchers.IO) {
            var totalLatency = 0L
            var successfulChecks = 0

            repeat(MAX_RETRIES) { _ ->
                val latency = when (protocol) {
                    DNSProtocol.UDP -> performUdpDnsQuery(serverIp, TEST_DOMAIN)
                    DNSProtocol.DOH -> performDohQuery(dohUrl ?: "", TEST_DOMAIN)
                    DNSProtocol.DOT -> performDotQuery(serverIp, dotHostname ?: "", TEST_DOMAIN)
                }

                if (latency > 0) {
                    totalLatency += latency
                    successfulChecks++
                }
            }

            if (successfulChecks > 0) {
                (totalLatency / successfulChecks).toInt()
            } else {
                -1
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

    private fun performDotQuery(serverIp: String, hostname: String, domain: String): Long {
        var socket: SSLSocket? = null
        return try {
            if (hostname.isBlank()) {
                Log.e(TAG, "DoT query requires a non-blank hostname")
                return -1L
            }

            val startTime = System.currentTimeMillis()
            val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
            socket = factory.createSocket(serverIp, DOT_PORT) as SSLSocket
            socket.soTimeout = TIMEOUT_MS

            // Configure TLS with hostname verification
            val sslParams = SSLParameters()
            sslParams.endpointIdentificationAlgorithm = "HTTPS"
            sslParams.serverNames = listOf(SNIHostName(hostname))
            socket.sslParameters = sslParams
            socket.startHandshake()

            val out = socket.getOutputStream()
            val input = socket.getInputStream()

            val query = createDnsQuery(domain)
            val length = query.size
            out.write((length shr 8) and 0xFF)
            out.write(length and 0xFF)
            out.write(query)
            out.flush()

            val lenHi = input.read()
            val lenLo = input.read()
            if (lenHi == -1 || lenLo == -1) {
                socket.close()
                return -1L
            }

            val responseLen = (lenHi shl 8) or lenLo
            val response = ByteArray(responseLen)
            var read = 0
            while (read < responseLen) {
                val r = input.read(response, read, responseLen - read)
                if (r == -1) break
                read += r
            }
            socket.close()
            System.currentTimeMillis() - startTime
        } catch (e: Exception) {
            Log.e(TAG, "DoT query error for $serverIp", e)
            socket?.close()
            -1L
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
}
