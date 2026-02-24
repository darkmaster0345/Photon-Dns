package com.photondns.app.service

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class SpeedTestManager @Inject constructor() {
    
    private val httpClient = HttpClient(CIO) {
        engine {
            maxConnectionsCount = 8
            endpoint {
                maxConnectionsPerRoute = 4
            }
        }
    }
    
    private val isTestRunning = AtomicBoolean(false)
    private val _testProgress = MutableStateFlow(0f)
    val testProgress: StateFlow<Float> = _testProgress.asStateFlow()
    
    private val _currentTest = MutableStateFlow<SpeedTestResult?>(null)
    val currentTest: StateFlow<SpeedTestResult?> = _currentTest.asStateFlow()
    
    companion object {
        private const val TAG = "SpeedTestManager"
        private const val DOWNLOAD_TEST_SIZE = 10 * 1024 * 1024 // 10MB
        private const val UPLOAD_TEST_SIZE = 1024 * 1024 // 1MB
        private const val PING_COUNT = 10
        private const val TEST_TIMEOUT = 30000L // 30 seconds
        
        // Test servers
        private val TEST_SERVERS = listOf(
            "http://speedtest.net",
            "http://fast.com",
            "http://cloudflare.com",
            "http://google.com"
        )
    }
    
    suspend fun runSpeedTest(testServer: String = "auto"): SpeedTestResult? {
        if (!isTestRunning.compareAndSet(false, true)) {
            return null // Test already running
        }
        
        return try {
            val server = if (testServer == "auto") {
                selectBestServer()
            } else {
                testServer
            }
            
            _testProgress.value = 0f
            
            val startTime = System.currentTimeMillis()
            
            // Measure ping first
            _testProgress.value = 0.1f
            val pingResult = measurePing(server)
            
            // Measure download speed
            _testProgress.value = 0.4f
            val downloadSpeed = measureDownloadSpeed(server)
            
            // Measure upload speed
            _testProgress.value = 0.7f
            val uploadSpeed = measureUploadSpeed(server)
            
            // Calculate jitter and packet loss
            _testProgress.value = 0.9f
            val jitter = calculateJitter(pingResult.pings)
            val packetLoss = calculatePacketLoss(pingResult.pings, pingResult.failedPings)
            
            _testProgress.value = 1.0f
            
            val result = SpeedTestResult(
                timestamp = System.currentTimeMillis(),
                downloadSpeed = downloadSpeed,
                uploadSpeed = uploadSpeed,
                ping = pingResult.ping,
                jitter = jitter,
                packetLoss = packetLoss,
                testServer = server,
                dnsUsed = getCurrentDnsServer(),
                testDuration = System.currentTimeMillis() - startTime
            )
            
            _currentTest.value = result
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Speed test failed", e)
            null
        } finally {
            isTestRunning.set(false)
            _testProgress.value = 0f
        }
    }
    
    suspend fun measureDownloadSpeed(server: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                val baseUrl = normalizeServerBaseUrl(server)
                val candidates = listOf(
                    "$baseUrl/speedtest?size=$DOWNLOAD_TEST_SIZE",
                    baseUrl
                )
                val startTime = System.currentTimeMillis()
                var content = ByteArray(0)

                for (url in candidates) {
                    val response: HttpResponse = httpClient.get(url)
                    content = response.readBytes()
                    if (content.isNotEmpty()) break
                }

                val endTime = System.currentTimeMillis()
                val durationSeconds = (endTime - startTime) / 1000.0
                if (durationSeconds <= 0.0) return@withContext 0.0
                val bitsTransferred = content.size * 8L
                val speedBps = bitsTransferred / durationSeconds
                val speedMbps = speedBps / (1024 * 1024)
                
                speedMbps
            } catch (e: Exception) {
                Log.e(TAG, "Download speed test failed", e)
                0.0
            }
        }
    }
    
    suspend fun measureUploadSpeed(server: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                val baseUrl = normalizeServerBaseUrl(server)
                val testData = ByteArray(UPLOAD_TEST_SIZE) { it.toByte() }
                val startTime = System.currentTimeMillis()
                
                httpClient.post("$baseUrl/upload") {
                    setBody(testData)
                }
                
                val endTime = System.currentTimeMillis()
                val durationSeconds = (endTime - startTime) / 1000.0
                if (durationSeconds <= 0.0) return@withContext 0.0
                val bitsTransferred = testData.size * 8L
                val speedBps = bitsTransferred / durationSeconds
                val speedMbps = speedBps / (1024 * 1024)
                
                speedMbps
            } catch (e: Exception) {
                Log.e(TAG, "Upload speed test failed", e)
                0.0
            }
        }
    }
    
    suspend fun measurePing(host: String): PingResult {
        return withContext(Dispatchers.IO) {
            val pings = mutableListOf<Long>()
            var failedPings = 0
            val hostToPing = extractHost(host)
            
            repeat(PING_COUNT) { attempt ->
                try {
                    val address = InetAddress.getByName(hostToPing)
                    val startTime = System.currentTimeMillis()
                    
                    val socket = Socket()
                    socket.connect(InetSocketAddress(address, 80), 5000)
                    socket.close()
                    
                    val endTime = System.currentTimeMillis()
                    pings.add(endTime - startTime)
                } catch (e: Exception) {
                    failedPings++
                }
            }
            
            val successfulPings = pings.size
            val averagePing = if (successfulPings > 0) {
                pings.average().toInt()
            } else {
                0
            }
            
            PingResult(
                ping = averagePing,
                pings = pings,
                failedPings = failedPings,
                success = successfulPings > 0
            )
        }
    }
    
    fun calculateJitter(pings: List<Long>): Int {
        if (pings.size < 2) return 0
        
        val mean = pings.average()
        val variance = pings.map { (it - mean).pow(2.0) }.average()
        return sqrt(variance).toInt()
    }
    
    fun calculatePacketLoss(pings: List<Long>, failedPings: Int = 0): Double {
        val totalAttempts = pings.size + failedPings
        return if (totalAttempts > 0) {
            (failedPings.toDouble() / totalAttempts) * 100
        } else {
            0.0
        }
    }
    
    private suspend fun selectBestServer(): String {
        return withContext(Dispatchers.IO) {
            var bestServer = TEST_SERVERS.first()
            var bestLatency = Long.MAX_VALUE
            
            for (server in TEST_SERVERS) {
                try {
                    val startTime = System.currentTimeMillis()
                    val response: HttpResponse = httpClient.get(normalizeServerBaseUrl(server))
                    response.readBytes()
                    val latency = System.currentTimeMillis() - startTime
                    
                    if (latency < bestLatency) {
                        bestLatency = latency
                        bestServer = server
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Server $server not available", e)
                }
            }
            
            bestServer
        }
    }
    
    private fun getCurrentDnsServer(): String {
        // This should get the current DNS server from the VPN service or settings
        return "8.8.8.8" // Default fallback
    }

    private fun normalizeServerBaseUrl(server: String): String {
        return if (server.startsWith("http://") || server.startsWith("https://")) {
            server
        } else {
            "https://$server"
        }
    }

    private fun extractHost(server: String): String {
        val normalized = normalizeServerBaseUrl(server)
        return runCatching { URI(normalized).host }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: server
    }
    
    fun cancelTest() {
        isTestRunning.set(false)
        _testProgress.value = 0f
    }
    
    fun isTestRunning(): Boolean = isTestRunning.get()
    
    fun cleanup() {
        httpClient.close()
    }
}

data class PingResult(
    val ping: Int,
    val pings: List<Long>,
    val failedPings: Int,
    val success: Boolean
)

data class SpeedTestResult(
    val timestamp: Long,
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val ping: Int,
    val jitter: Int,
    val packetLoss: Double,
    val testServer: String,
    val dnsUsed: String,
    val testDuration: Long
)
