package com.dnsspeedchecker.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.UnknownHostException
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.dnsoverhttps.DnsOverHttps
import java.util.concurrent.TimeUnit

/**
 * Utility class for comprehensive DNS testing and validation
 */
class DnsTestUtility {
    
    companion object {
        private const val TAG = "DnsTestUtility"
        
        // Test domains for different types of DNS queries
        val DOMAINS_FOR_TESTING = listOf(
            // Popular websites
            "google.com",
            "youtube.com",
            "facebook.com",
            "amazon.com",
            "twitter.com",
            
            // CDN services
            "cloudflare.com",
            "fastly.net",
            "akamai.com",
            "cloudfront.net",
            
            // Development sites
            "github.com",
            "stackoverflow.com",
            "medium.com",
            
            // News sites
            "cnn.com",
            "bbc.com",
            "reuters.com",
            
            // Social media
            "instagram.com",
            "linkedin.com",
            "reddit.com"
        )
        
        // Domains known to have good DNS infrastructure
        val RELIABLE_DOMAINS = listOf(
            "google.com",
            "cloudflare.com",
            "amazon.com",
            "microsoft.com",
            "apple.com"
        )
    }
    
    /**
     * Performs comprehensive DNS server validation
     */
    suspend fun validateDnsServer(dnsServer: String): DnsValidationResult {
        return withContext(Dispatchers.IO) {
            val testResults = mutableListOf<DnsTestResult>()
            
            try {
                // Test basic connectivity
                val connectivityTest = async { testConnectivity(dnsServer) }
                
                // Test DNS resolution for multiple domains
                val resolutionTests = RELIABLE_DOMAINS.take(5).map { domain ->
                    async { testDnsResolution(dnsServer, domain) }
                }
                
                // Test response time consistency
                val consistencyTest = async { testResponseConsistency(dnsServer) }
                
                // Wait for all tests to complete
                val connectivity = connectivityTest.await()
                val resolutions = resolutionTests.awaitAll()
                val consistency = consistencyTest.await()
                
                testResults.add(connectivity)
                testResults.addAll(resolutions)
                testResults.add(consistency)
                
                // Calculate overall validation metrics
                val successRate = testResults.count { it.success }.toFloat() / testResults.size
                val avgResponseTime = testResults.filter { it.success }.map { it.responseTimeMs }.average()
                
                DnsValidationResult(
                    dnsServer = dnsServer,
                    isValid = successRate >= 0.8 && avgResponseTime < 500,
                    successRate = successRate,
                    avgResponseTimeMs = avgResponseTime,
                    testResults = testResults,
                    issues = identifyIssues(testResults),
                    recommendations = generateRecommendations(testResults)
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "DNS validation failed for $dnsServer", e)
                DnsValidationResult(
                    dnsServer = dnsServer,
                    isValid = false,
                    successRate = 0f,
                    avgResponseTimeMs = -1.0,
                    testResults = testResults,
                    issues = listOf("Validation failed: ${e.message}"),
                    recommendations = listOf("Check network connection and DNS server availability")
                )
            }
        }
    }
    
    /**
     * Tests basic connectivity to DNS server
     */
    private suspend fun testConnectivity(dnsServer: String): DnsTestResult {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Try to resolve the DNS server IP
                val address = InetAddress.getByName(dnsServer)
                val endTime = System.currentTimeMillis()
                
                val responseTime = endTime - startTime
                val success = address.hostAddress != null
                
                DnsTestResult(
                    testType = "Connectivity",
                    target = dnsServer,
                    success = success,
                    responseTimeMs = responseTime,
                    details = if (success) "Server reachable at ${address.hostAddress}" else "Server unreachable"
                )
                
            } catch (e: Exception) {
                DnsTestResult(
                    testType = "Connectivity",
                    target = dnsServer,
                    success = false,
                    responseTimeMs = -1,
                    details = "Connection failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Tests DNS resolution for a specific domain
     */
    private suspend fun testDnsResolution(dnsServer: String, domain: String): DnsTestResult {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Build a DoH client to query this specific server directly, bypassing OS cache
                val bootstrapClient = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build()
                
                val dnsUrl = if (dnsServer.startsWith("https://")) dnsServer else "https://$dnsServer/dns-query"
                
                val dohClient = DnsOverHttps.Builder()
                    .client(bootstrapClient)
                    .url(dnsUrl.toHttpUrl())
                    .post(true)
                    .build()

                val addresses = dohClient.lookup(domain)
                val endTime = System.currentTimeMillis()
                
                val responseTime = endTime - startTime
                val success = addresses.isNotEmpty()
                
                DnsTestResult(
                    testType = "DNS Resolution",
                    target = domain,
                    success = success,
                    responseTimeMs = responseTime,
                    details = if (success) {
                        "Resolved to ${addresses.size} address(es): ${addresses.joinToString(", ") { it.hostAddress }}"
                    } else {
                        "Failed to resolve domain"
                    }
                )
                
            } catch (e: UnknownHostException) {
                DnsTestResult(
                    testType = "DNS Resolution",
                    target = domain,
                    success = false,
                    responseTimeMs = -1,
                    details = "Unknown host: $domain"
                )
            } catch (e: Exception) {
                DnsTestResult(
                    testType = "DNS Resolution",
                    target = domain,
                    success = false,
                    responseTimeMs = -1,
                    details = "Resolution failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Tests response time consistency with multiple queries
     */
    private suspend fun testResponseConsistency(dnsServer: String): DnsTestResult {
        return withContext(Dispatchers.IO) {
            try {
                val testDomain = "google.com"
                val responseTimes = mutableListOf<Long>()
                
                // Perform multiple tests via DoH to bypass OS cache
                val bootstrapClient = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build()
                
                val dnsUrl = if (dnsServer.startsWith("https://")) dnsServer else "https://$dnsServer/dns-query"
                
                val dohClient = DnsOverHttps.Builder()
                    .client(bootstrapClient)
                    .url(dnsUrl.toHttpUrl())
                    .post(true)
                    .build()
                
                repeat(5) {
                    val startTime = System.currentTimeMillis()
                    try {
                        dohClient.lookup(testDomain)
                        val endTime = System.currentTimeMillis()
                        responseTimes.add(endTime - startTime)
                    } catch (e: Exception) {
                        responseTimes.add(-1L)
                    }
                }
                
                val validTimes = responseTimes.filter { it > 0 }
                val success = validTimes.isNotEmpty()
                val avgTime = if (validTimes.isNotEmpty()) validTimes.average() else -1.0
                
                // Calculate consistency (lower standard deviation = more consistent)
                val consistency = if (validTimes.size > 1) {
                    val mean = validTimes.average()
                    val variance = validTimes.map { (it - mean).pow(2) }.average()
                    val stdDev = kotlin.math.sqrt(variance)
                    // Convert to consistency score (0-100)
                    (100 * (1 - (stdDev / mean))).coerceIn(0.0, 100.0)
                } else {
                    0.0
                }
                
                DnsTestResult(
                    testType = "Response Consistency",
                    target = testDomain,
                    success = success,
                    responseTimeMs = avgTime.toLong(),
                    details = "Avg: ${avgTime.toInt()}ms, Consistency: ${consistency.toInt()}%, Tests: ${validTimes.size}/5"
                )
                
            } catch (e: Exception) {
                DnsTestResult(
                    testType = "Response Consistency",
                    target = "google.com",
                    success = false,
                    responseTimeMs = -1,
                    details = "Consistency test failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Identifies common issues from test results
     */
    private fun identifyIssues(testResults: List<DnsTestResult>): List<String> {
        val issues = mutableListOf<String>()
        
        val failedTests = testResults.filter { !it.success }
        val slowTests = testResults.filter { it.success && it.responseTimeMs > 200 }
        
        when {
            failedTests.isEmpty() -> {
                // No issues detected
            }
            failedTests.size == testResults.size -> {
                issues.add("All tests failed - DNS server may be down")
            }
            failedTests.size > testResults.size / 2 -> {
                issues.add("Most tests failed - DNS server experiencing issues")
            }
            else -> {
                issues.add("Some tests failed - intermittent connectivity issues")
            }
        }
        
        if (slowTests.isNotEmpty()) {
            issues.add("Slow response times detected (avg > 200ms)")
        }
        
        val connectivityIssues = testResults.filter { 
            it.testType == "Connectivity" && !it.success 
        }
        if (connectivityIssues.isNotEmpty()) {
            issues.add("Basic connectivity issues detected")
        }
        
        return issues
    }
    
    /**
     * Generates recommendations based on test results
     */
    private fun generateRecommendations(testResults: List<DnsTestResult>): List<String> {
        val recommendations = mutableListOf<String>()
        
        val successRate = testResults.count { it.success }.toFloat() / testResults.size
        
        when {
            successRate >= 0.9 -> {
                recommendations.add("Excellent DNS server performance")
            }
            successRate >= 0.7 -> {
                recommendations.add("Good DNS server performance with occasional issues")
            }
            successRate >= 0.5 -> {
                recommendations.add("Moderate DNS server performance - consider alternatives")
            }
            else -> {
                recommendations.add("Poor DNS server performance - recommend changing")
            }
        }
        
        val avgResponseTime = testResults.filter { it.success }.map { it.responseTimeMs }.average()
        when {
            avgResponseTime <= 50 -> {
                recommendations.add("Excellent response times")
            }
            avgResponseTime <= 100 -> {
                recommendations.add("Good response times")
            }
            avgResponseTime <= 200 -> {
                recommendations.add("Acceptable response times")
            }
            else -> {
                recommendations.add("Slow response times - consider faster DNS server")
            }
        }
        
        return recommendations
    }
    
    /**
     * Performs a quick health check on multiple DNS servers
     */
    suspend fun performHealthCheck(dnsServers: List<String>): List<DnsHealthStatus> {
        return withContext(Dispatchers.IO) {
            dnsServers.map { server ->
                async {
                    val validation = validateDnsServer(server)
                    DnsHealthStatus(
                        server = server,
                        isHealthy = validation.isValid,
                        score = calculateHealthScore(validation),
                        lastChecked = System.currentTimeMillis(),
                        issues = validation.issues
                    )
                }
            }.awaitAll()
        }
    }
    
    private fun calculateHealthScore(validation: DnsValidationResult): Double {
        val successRateScore = validation.successRate * 50
        val responseTimeScore = when {
            validation.avgResponseTimeMs <= 0 -> 0.0
            validation.avgResponseTimeMs <= 50 -> 50.0
            validation.avgResponseTimeMs <= 100 -> 40.0
            validation.avgResponseTimeMs <= 200 -> 25.0
            else -> 10.0
        }
        
        return (successRateScore + responseTimeScore).coerceIn(0.0, 100.0)
    }
}

/**
 * Data classes for DNS test results
 */
data class DnsTestResult(
    val testType: String,
    val target: String,
    val success: Boolean,
    val responseTimeMs: Long,
    val details: String
)

data class DnsValidationResult(
    val dnsServer: String,
    val isValid: Boolean,
    val successRate: Float,
    val avgResponseTimeMs: Double,
    val testResults: List<DnsTestResult>,
    val issues: List<String>,
    val recommendations: List<String>
)

data class DnsHealthStatus(
    val server: String,
    val isHealthy: Boolean,
    val score: Double, // 0-100
    val lastChecked: Long,
    val issues: List<String>
)
