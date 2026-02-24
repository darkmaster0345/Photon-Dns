package com.dnsspeedchecker.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.dnsoverhttps.DnsOverHttps
import java.util.concurrent.TimeUnit

/**
 * Enhanced DNS latency checker with multiple test domains and robust error handling
 */
class DnsLatencyChecker {
    
    companion object {
        private const val TAG = "DnsLatencyChecker"
        private const val DNS_PORT = 53
        private const val TIMEOUT_MS = 3000
        private const val MAX_RETRIES = 2
        private const val CONCURRENT_TESTS = 3
        
        // Multiple test domains for more accurate measurements
        private val TEST_DOMAINS = listOf(
            "google.com",
            "cloudflare.com", 
            "github.com",
            "stackoverflow.com",
            "wikipedia.org"
        )
        
        // Well-known reliable domains for fallback testing
        private val FALLBACK_DOMAINS = listOf(
            "8.8.8.8.in-addr.arpa", // Reverse DNS for Google DNS
            "1.1.1.1.in-addr.arpa"  // Reverse DNS for Cloudflare DNS
        )
    }
    
    /**
     * Measures DNS latency with multiple concurrent tests for accuracy
     */
    suspend fun measureDnsLatency(dnsServer: String): DnsLatencyResult {
        return withContext(Dispatchers.IO) {
            val testStartTime = System.currentTimeMillis()
            Log.d(TAG, "🚀 Starting DNS latency measurement for $dnsServer")
            
            try {
                val measurements = mutableListOf<Long>()
                val detailedResults = mutableListOf<String>()
                
                // Perform concurrent tests to multiple domains
                val testDomains = TEST_DOMAINS.shuffled().take(CONCURRENT_TESTS)
                Log.d(TAG, "📋 Testing domains: ${testDomains.joinToString(", ")}")
                
                val testJobs = testDomains.map { domain ->
                    async {
                        val result = measureSingleDnsQuery(dnsServer, domain)
                        Triple(domain, result, System.currentTimeMillis())
                    }
                }
                
                val results = testJobs.awaitAll()
                
                // Process results with detailed logging
                results.forEach { (domain, latency, timestamp) ->
                    if (latency > 0) {
                        measurements.add(latency)
                        detailedResults.add("$domain: ${latency}ms")
                        Log.d(TAG, "✅ $domain -> ${latency}ms")
                    } else {
                        detailedResults.add("$domain: FAILED")
                        Log.w(TAG, "❌ $domain -> FAILED")
                    }
                }
                
                // Filter successful measurements
                val successfulMeasurements = results.filter { it.second > 0 }
                
                if (successfulMeasurements.isNotEmpty()) {
                    // Calculate statistics
                    val avgLatency = successfulMeasurements.map { it.second }.average().toLong()
                    val minLatency = successfulMeasurements.minOf { it.second }
                    val maxLatency = successfulMeasurements.maxOf { it.second }
                    val medianLatency = calculateMedian(successfulMeasurements.map { it.second })
                    val variance = calculateVariance(successfulMeasurements.map { it.second }, avgLatency)
                    
                    val totalTestTime = System.currentTimeMillis() - testStartTime
                    
                    Log.i(TAG, "📊 DNS Latency Results for $dnsServer (${totalTestTime}ms):")
                    Log.i(TAG, "   Average: ${avgLatency}ms")
                    Log.i(TAG, "   Median:  ${medianLatency}ms")
                    Log.i(TAG, "   Min:     ${minLatency}ms")
                    Log.i(TAG, "   Max:     ${maxLatency}ms")
                    Log.i(TAG, "   Variance: ${"%.2f".format(variance)}ms²")
                    Log.i(TAG, "   Success Rate: ${successfulMeasurements.size}/$CONCURRENT_TESTS (${(successfulMeasurements.size.toFloat() / CONCURRENT_TESTS * 100).toInt()}%)")
                    
                    // Log accuracy warnings
                    if (variance > 100) {
                        Log.w(TAG, "⚠️ High variance detected: ${"%.2f".format(variance)}ms² - measurements may be inconsistent")
                    }
                    if (maxLatency - minLatency > 200) {
                        Log.w(TAG, "⚠️ High latency range: ${maxLatency - minLatency}ms - network may be unstable")
                    }
                    
                    DnsLatencyResult(
                        serverIp = dnsServer,
                        avgLatencyMs = avgLatency,
                        minLatencyMs = minLatency,
                        maxLatencyMs = maxLatency,
                        medianLatencyMs = medianLatency,
                        successRate = successfulMeasurements.size.toFloat() / CONCURRENT_TESTS,
                        timestamp = System.currentTimeMillis(),
                        success = true,
                        variance = variance
                    )
                } else {
                    // All tests failed, try fallback
                    Log.w(TAG, "⚠️ All primary tests failed for $dnsServer, trying fallback")
                    val fallbackResult = tryFallbackTest(dnsServer)
                    if (fallbackResult.success) {
                        fallbackResult
                    } else {
                        Log.e(TAG, "❌ All DNS tests failed for $dnsServer")
                        DnsLatencyResult(
                            serverIp = dnsServer,
                            avgLatencyMs = -1,
                            minLatencyMs = -1,
                            maxLatencyMs = -1,
                            medianLatencyMs = -1,
                            successRate = 0f,
                            timestamp = System.currentTimeMillis(),
                            success = false,
                            error = "All DNS queries failed"
                        )
                    }
                }
                
            } catch (e: Exception) {
                val testTime = System.currentTimeMillis() - testStartTime
                Log.e(TAG, "❌ Exception during DNS latency measurement for $dnsServer (${testTime}ms)", e)
                DnsLatencyResult(
                    serverIp = dnsServer,
                    avgLatencyMs = -1,
                    minLatencyMs = -1,
                    maxLatencyMs = -1,
                    medianLatencyMs = -1,
                    successRate = 0f,
                    timestamp = System.currentTimeMillis(),
                    success = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    /**
     * Measures a single DNS query with retry logic
     */
    private suspend fun measureSingleDnsQuery(dnsServer: String, domain: String): Long {
        repeat(MAX_RETRIES) { attempt ->
            val attemptStartTime = System.nanoTime()
            Log.d(TAG, "🔍 Testing $dnsServer for $domain (attempt ${attempt + 1}/$MAX_RETRIES)")
            
            try {
                val bootstrapClient = OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
                    .readTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
                    .build()
                
                val dnsUrl = if (dnsServer.startsWith("https://")) dnsServer else "https://$dnsServer/dns-query"
                
                val dohClient = DnsOverHttps.Builder()
                    .client(bootstrapClient)
                    .url(dnsUrl.toHttpUrl())
                    .post(true)
                    .build()

                val startTime = System.nanoTime()
                val addresses = dohClient.lookup(domain)
                
                if (addresses.isNotEmpty()) {
                    val endTime = System.nanoTime()
                    val totalLatencyMs = (endTime - startTime) / 1_000_000 // Convert nanoseconds to milliseconds
                    
                    Log.d(TAG, "📥 Received response for $domain: total=${totalLatencyMs}ms")
                    
                    // Validate timing accuracy
                    if (totalLatencyMs < 1) {
                        Log.w(TAG, "⚠️ Suspiciously low latency: ${totalLatencyMs}ms - possible timing issue")
                    }
                    if (totalLatencyMs > 5000) {
                        Log.w(TAG, "⚠️ Very high latency: ${totalLatencyMs}ms - possible network issue")
                    }
                    
                    // Add small random delay to prevent network congestion
                    delay(Random.nextLong(10, 50))
                    
                    return totalLatencyMs
                } else {
                    val invalidTime = (System.nanoTime() - startTime) / 1_000_000
                    Log.w(TAG, "⚠️ Invalid DNS response from $dnsServer for $domain (${invalidTime}ms)")
                }
                
            } catch (e: SocketTimeoutException) {
                val timeoutTime = (System.nanoTime() - attemptStartTime) / 1_000_000
                Log.w(TAG, "⏰ DNS query timeout for $dnsServer ($domain) after ${timeoutTime}ms (attempt ${attempt + 1}/$MAX_RETRIES)")
                if (attempt < MAX_RETRIES - 1) {
                    delay(100) // Brief delay before retry
                }
            } catch (e: Exception) {
                val errorTime = (System.nanoTime() - attemptStartTime) / 1_000_000
                Log.w(TAG, "❌ DNS query failed for $dnsServer ($domain) after ${errorTime}ms (attempt ${attempt + 1}/$MAX_RETRIES): ${e.message}")
                if (attempt < MAX_RETRIES - 1) {
                    delay(100)
                }
            }
        }
        
        Log.w(TAG, "❌ All retries failed for $dnsServer ($domain)")
        return -1L // All retries failed
    }
    
    /**
     * Fallback test using reverse DNS queries
     */
    private suspend fun tryFallbackTest(dnsServer: String): DnsLatencyResult {
        return withContext(Dispatchers.IO) {
            try {
                val measurements = mutableListOf<Long>()
                
                for (domain in FALLBACK_DOMAINS) {
                    val latency = measureSingleDnsQuery(dnsServer, domain)
                    if (latency > 0) {
                        measurements.add(latency)
                    }
                }
                
                if (measurements.isNotEmpty()) {
                    val avgLatency = measurements.average().toLong()
                    val minLatency = measurements.minOrNull() ?: 0L
                    val maxLatency = measurements.maxOrNull() ?: 0L
                    val medianLatency = calculateMedian(measurements)
                    
                    Log.d(TAG, "Fallback DNS latency for $dnsServer: avg=${avgLatency}ms")
                    
                    DnsLatencyResult(
                        serverIp = dnsServer,
                        avgLatencyMs = avgLatency,
                        minLatencyMs = minLatency,
                        maxLatencyMs = maxLatency,
                        medianLatencyMs = medianLatency,
                        successRate = measurements.size.toFloat() / FALLBACK_DOMAINS.size,
                        timestamp = System.currentTimeMillis(),
                        success = true,
                        isFallback = true
                    )
                } else {
                    DnsLatencyResult(
                        serverIp = dnsServer,
                        avgLatencyMs = -1,
                        minLatencyMs = -1,
                        maxLatencyMs = -1,
                        medianLatencyMs = -1,
                        successRate = 0f,
                        timestamp = System.currentTimeMillis(),
                        success = false,
                        error = "Fallback DNS queries also failed"
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Fallback test failed for $dnsServer", e)
                DnsLatencyResult(
                    serverIp = dnsServer,
                    avgLatencyMs = -1,
                    minLatencyMs = -1,
                    maxLatencyMs = -1,
                    medianLatencyMs = -1,
                    successRate = 0f,
                    timestamp = System.currentTimeMillis(),
                    success = false,
                    error = "Fallback test exception: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Creates a proper DNS query packet
     */
    private fun createDnsQuery(domain: String): ByteArray {
        val buffer = ByteBuffer.allocate(512)
        
        // DNS Header
        val transactionId = Random.nextInt(0x1000, 0xFFFF) // Random transaction ID
        buffer.putShort(transactionId.toShort())
        buffer.putShort(0x0100) // Flags: Standard query, recursion desired
        buffer.putShort(0x0001) // Questions: 1
        buffer.putShort(0x0000) // Answer RRs: 0
        buffer.putShort(0x0000) // Authority RRs: 0
        buffer.putShort(0x0000) // Additional RRs: 0
        
        // Query section
        val parts = domain.split(".")
        for (part in parts) {
            if (part.isNotEmpty()) {
                buffer.put(part.length.toByte())
                buffer.put(part.toByteArray())
            }
        }
        buffer.put(0) // End of domain name
        
        buffer.putShort(0x0001) // Type: A record (or PTR for reverse DNS)
        buffer.putShort(0x0001) // Class: IN
        
        return buffer.array().copyOf(buffer.position())
    }
    
    /**
     * Validates DNS response packet
     */
    private fun isValidDnsResponse(data: ByteArray, length: Int): Boolean {
        if (length < 12) return false // Minimum DNS header size
        
        val buffer = ByteBuffer.wrap(data, length)
        
        // Check response code (last 4 bits of flags)
        buffer.position(2) // Skip transaction ID
        val flags = buffer.short
        val responseCode = flags.toInt() and 0x000F
        
        return responseCode == 0 // NOERROR
    }
    
    /**
     * Calculates variance from a list of longs
     */
    private fun calculateVariance(values: List<Long>, mean: Long): Double {
        if (values.isEmpty()) return 0.0
        
        val squaredDifferences = values.map { (it - mean).toDouble().pow(2) }
        return squaredDifferences.average()
    }
    
    /**
     * Calculates median value from a list of longs
     */
    private fun calculateMedian(values: List<Long>): Long {
        if (values.isEmpty()) return 0L
        
        val sorted = values.sorted()
        val middle = sorted.size / 2
        
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2
        } else {
            sorted[middle]
        }
    }
    
    /**
     * Performs connectivity test to check if DNS server is reachable
     */
    suspend fun testDnsConnectivity(dnsServer: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val bootstrapClient = OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.SECONDS)
                    .readTimeout(1, TimeUnit.SECONDS)
                    .build()
                
                val dnsUrl = if (dnsServer.startsWith("https://")) dnsServer else "https://$dnsServer/dns-query"
                
                val dohClient = DnsOverHttps.Builder()
                    .client(bootstrapClient)
                    .url(dnsUrl.toHttpUrl())
                    .post(true)
                    .build()
                
                dohClient.lookup("test.com").isNotEmpty()
                
            } catch (e: Exception) {
                Log.w(TAG, "Connectivity test failed for $dnsServer: ${e.message}")
                false
            }
        }
    }
}

/**
 * Data class to hold comprehensive DNS latency results
 */
data class DnsLatencyResult(
    val serverIp: String,
    val avgLatencyMs: Long,
    val minLatencyMs: Long,
    val maxLatencyMs: Long,
    val medianLatencyMs: Long,
    val successRate: Float, // 0.0 to 1.0
    val timestamp: Long,
    val success: Boolean,
    val error: String? = null,
    val isFallback: Boolean = false,
    val variance: Double = 0.0
) {
    /**
     * Returns the primary latency value to use for comparisons
     */
    fun getPrimaryLatency(): Long {
        return if (success) medianLatency else -1L
    }
    
    /**
     * Returns a human-readable status with variance information
     */
    fun getStatus(): String {
        return when {
            !success -> error ?: "Failed"
            isFallback -> "Fallback (${avgLatencyMs}ms)"
            successRate < 1.0f -> "Partial (${avgLatencyMs}ms, ${(successRate * 100).toInt()}%, σ²=${"%.1f".format(variance)})"
            else -> "Good (${avgLatencyMs}ms, σ²=${"%.1f".format(variance)})"
        }
    }
    
    /**
     * Returns quality assessment based on variance and latency
     */
    fun getQuality(): String {
        return when {
            !success -> "Failed"
            variance > 200 -> "Unstable"
            variance > 100 -> "Variable"
            avgLatencyMs > 200 -> "Slow"
            avgLatencyMs > 100 -> "Fair"
            else -> "Good"
        }
    }
}
