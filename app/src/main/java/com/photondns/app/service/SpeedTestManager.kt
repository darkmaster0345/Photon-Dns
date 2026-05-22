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
            maxConnectionsCount = 16
            endpoint {
                maxConnectionsPerRoute = 8
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
        private const val DOWNLOAD_TEST_SIZE = 20 * 1024 * 1024 // 20MB
        private const val UPLOAD_TEST_SIZE = 5 * 1024 * 1024 // 5MB
        private const val PING_COUNT = 15

        private val FOSS_TEST_SERVERS = listOf(
            TestServerProvider(
                name = "Cloudflare",
                downloadUrl = "https://speed.cloudflare.com/__down?bytes=20000000",
                uploadUrl = "https://speed.cloudflare.com/__up",
                selectionUrl = "https://speed.cloudflare.com"
            ),
            TestServerProvider(
                name = "Hetzner",
                downloadUrl = "https://speed.hetzner.de/100MB.bin",
                uploadUrl = "https://speed.hetzner.de/upload",
                selectionUrl = "https://speed.hetzner.de"
            ),
            TestServerProvider(
                name = "Bouygues",
                downloadUrl = "https://bouygues.testdebit.info/download",
                uploadUrl = "https://bouygues.testdebit.info/upload",
                selectionUrl = "https://bouygues.testdebit.info"
            ),
            TestServerProvider(
                name = "Scaleway",
                downloadUrl = "https://scaleway.testdebit.info/download",
                uploadUrl = "https://scaleway.testdebit.info/upload",
                selectionUrl = "https://scaleway.testdebit.info"
            )
        )
    }
    
    suspend fun runSpeedTest(testServer: String = "auto"): SpeedTestResult? {
        if (!isTestRunning.compareAndSet(false, true)) {
            return null
        }

        return try {
            val serverProvider = if (testServer == "auto") selectBestServer() else FOSS_TEST_SERVERS.first()
            _testProgress.value = 0f
            val startTime = System.currentTimeMillis()

            _testProgress.value = 0.1f
            val pingResult = measurePing(serverProvider.selectionUrl)
            if (!isTestRunning.get()) return null

            _testProgress.value = 0.3f
            val downloadSpeed = measureDownloadSpeed(serverProvider.downloadUrl)
            if (!isTestRunning.get()) return null

            _testProgress.value = 0.7f
            val uploadSpeed = measureUploadSpeed(serverProvider.uploadUrl)
            if (!isTestRunning.get()) return null

            _testProgress.value = 0.9f
            val jitter = calculateJitter(pingResult.pings)
            val packetLoss = calculatePacketLoss(pingResult.pings, pingResult.failedPings)
            val bufferbloat = (pingResult.pings.maxOrNull() ?: 0L) - (pingResult.pings.minOrNull() ?: 0L)

            _testProgress.value = 1.0f

            val result = SpeedTestResult(
                timestamp = System.currentTimeMillis(),
                downloadSpeed = downloadSpeed,
                uploadSpeed = uploadSpeed,
                ping = pingResult.ping,
                jitter = jitter,
                packetLoss = packetLoss,
                bufferbloat = bufferbloat.toInt(),
                testServer = serverProvider.name,
                dnsUsed = "Photon Optimized",
                privacyScore = 100,
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
    
    private suspend fun measureDownloadSpeed(server: String): Double = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            val response: HttpResponse = httpClient.get(server)
            val bytes = response.readBytes()
            val duration = (System.currentTimeMillis() - start) / 1000.0
            if (duration <= 0) 0.0 else (bytes.size * 8.0 / (1024 * 1024)) / duration
        } catch (e: Exception) { 0.0 }
    }
    
    private suspend fun measureUploadSpeed(uploadUrl: String): Double = withContext(Dispatchers.IO) {
        try {
            val testData = ByteArray(UPLOAD_TEST_SIZE) { 0 }
            val start = System.currentTimeMillis()
            httpClient.post(uploadUrl) { setBody(testData) }
            val duration = (System.currentTimeMillis() - start) / 1000.0
            if (duration <= 0) 0.0 else (UPLOAD_TEST_SIZE * 8.0 / (1024 * 1024)) / duration
        } catch (e: Exception) { 0.0 }
    }
    
    private suspend fun measurePing(host: String): PingResult = withContext(Dispatchers.IO) {
        val pings = mutableListOf<Long>()
        var failed = 0

        // Parse the URL to extract host, port, and scheme
        val (hostname, port) = try {
            val uri = URI(host)
            val extractedHost = uri.host ?: host.replace("https://", "").replace("http://", "").split("/")[0]
            val extractedPort = if (uri.port != -1) {
                uri.port
            } else {
                when (uri.scheme?.lowercase()) {
                    "https" -> 443
                    "http" -> 80
                    else -> 80
                }
            }
            Pair(extractedHost, extractedPort)
        } catch (e: Exception) {
            // Fallback for malformed URLs
            val cleanHost = host.replace("https://", "").replace("http://", "").split("/")[0]
            val defaultPort = if (host.startsWith("https://")) 443 else 80
            Pair(cleanHost, defaultPort)
        }

        repeat(PING_COUNT) {
            try {
                val start = System.currentTimeMillis()
                val address = InetAddress.getByName(hostname)
                val socket = Socket()
                socket.connect(InetSocketAddress(address, port), 2000)
                socket.close()
                pings.add(System.currentTimeMillis() - start)
            } catch (e: Exception) { failed++ }
        }

        PingResult(
            ping = if (pings.isNotEmpty()) pings.average().toInt() else 0,
            pings = pings,
            failedPings = failed,
            success = pings.isNotEmpty()
        )
    }
    
    private fun calculateJitter(pings: List<Long>): Int {
        if (pings.size < 2) return 0
        val mean = pings.average()
        return sqrt(pings.map { (it - mean).pow(2.0) }.average()).toInt()
    }
    
    private fun calculatePacketLoss(pings: List<Long>, failed: Int): Double {
        val total = pings.size + failed
        return if (total > 0) (failed.toDouble() / total) * 100 else 0.0
    }
    
    private suspend fun selectBestServer(): TestServerProvider = withContext(Dispatchers.IO) {
        FOSS_TEST_SERVERS.minByOrNull { provider ->
            try {
                val start = System.currentTimeMillis()
                httpClient.get(provider.selectionUrl)
                System.currentTimeMillis() - start
            } catch (e: Exception) { Long.MAX_VALUE }
        } ?: FOSS_TEST_SERVERS.first()
    }
    
    fun cancelTest() {
        isTestRunning.set(false)
    }
}

data class PingResult(
    val ping: Int,
    val pings: List<Long>,
    val failedPings: Int,
    val success: Boolean
)

data class TestServerProvider(
    val name: String,
    val downloadUrl: String,
    val uploadUrl: String,
    val selectionUrl: String
)

data class SpeedTestResult(
    val timestamp: Long,
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val ping: Int,
    val jitter: Int,
    val packetLoss: Double,
    val bufferbloat: Int,
    val testServer: String,
    val dnsUsed: String,
    val privacyScore: Int,
    val testDuration: Long
)
