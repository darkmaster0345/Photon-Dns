package com.dnsspeedchecker.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.net.*
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

/**
 * Comprehensive network error handling and retry logic
 */
class NetworkErrorHandler private constructor() {
    
    companion object {
        private const val TAG = "NetworkErrorHandler"
        private const val DEFAULT_TIMEOUT_MS = 5000
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000
        private const val EXPONENTIAL_BACKOFF_BASE = 2
        
        /**
         * Network error types for better handling
         */
        sealed class NetworkError(
            val type: String,
            val message: String,
            val isRetryable: Boolean,
            val suggestedAction: String? = null
        ) {
            class TimeoutError(message: String) : NetworkError(
                type = "TIMEOUT",
                message = message,
                isRetryable = true,
                suggestedAction = "Check your internet connection"
            )
            
            class ConnectionError(message: String) : NetworkError(
                type = "CONNECTION",
                message = message,
                isRetryable = true,
                suggestedAction = "Verify network connectivity"
            )
            
            class DnsError(message: String) : NetworkError(
                type = "DNS",
                message = message,
                isRetryable = true,
                suggestedAction = "Try a different DNS server"
            )
            
            class HostNotFoundError(message: String) : NetworkError(
                type = "HOST_NOT_FOUND",
                message = message,
                isRetryable = false,
                suggestedAction = "Check the DNS server address"
            )
            
            class AuthenticationError(message: String) : NetworkError(
                type = "AUTHENTICATION",
                message = message,
                isRetryable = false,
                suggestedAction = "Check authentication credentials"
            )
            
            class RateLimitError(message: String) : NetworkError(
                type = "RATE_LIMIT",
                message = message,
                isRetryable = true,
                suggestedAction = "Wait before trying again"
            )
            
            class UnknownError(message: String) : NetworkError(
                type = "UNKNOWN",
                message = message,
                isRetryable = false
            )
        }
        
        /**
         * Executes network operation with comprehensive error handling and retry logic
         */
        suspend fun <T> executeWithRetry(
            operation: suspend () -> T,
            operationName: String,
            maxRetries: Int = MAX_RETRIES,
            timeoutMs: Long = DEFAULT_TIMEOUT_MS,
            context: Context? = null
        ): Result<T> {
            return withContext(Dispatchers.IO) {
                var lastError: Exception? = null
                
                repeat(maxRetries) { attempt ->
                    try {
                        // Check network connectivity before operation
                        context?.let { checkNetworkConnectivity(it) }
                        
                        // Execute operation with timeout
                        val result = withTimeout(timeoutMs) {
                            operation()
                        }
                        
                        // Success - log and return
                        Log.d(TAG, "$operationName succeeded on attempt ${attempt + 1}")
                        return@withContext Result.success(result)
                        
                    } catch (e: TimeoutCancellationException) {
                        lastError = NetworkError.TimeoutError(
                            "Operation timed out after ${timeoutMs}ms"
                        )
                        
                    } catch (e: SocketTimeoutException) {
                        lastError = NetworkError.TimeoutError(
                            "Socket timeout after ${timeoutMs}ms"
                        )
                        
                    } catch (e: UnknownHostException) {
                        lastError = NetworkError.HostNotFoundError(
                            "Host not found: ${e.message}"
                        )
                        
                    } catch (e: ConnectException) {
                        lastError = NetworkError.ConnectionError(
                            "Connection failed: ${e.message}"
                        )
                        
                    } catch (e: NoRouteToHostException) {
                        lastError = NetworkError.ConnectionError(
                            "No route to host: ${e.message}"
                        )
                        
                    } catch (e: PortUnreachableException) {
                        lastError = NetworkError.ConnectionError(
                            "Port unreachable: ${e.message}"
                        )
                        
                    } catch (e: SSLException) {
                        lastError = NetworkError.AuthenticationError(
                            "SSL/TLS error: ${e.message}"
                        )
                        
                    } catch (e: IOException) {
                        // Check for specific error patterns
                        val errorMessage = e.message?.lowercase() ?: ""
                        lastError = when {
                            errorMessage.contains("connection refused") -> {
                                NetworkError.ConnectionError("Connection refused by server")
                            }
                            errorMessage.contains("network unreachable") -> {
                                NetworkError.ConnectionError("Network is unreachable")
                            }
                            errorMessage.contains("dns") -> {
                                NetworkError.DnsError("DNS resolution failed")
                            }
                            else -> {
                                NetworkError.UnknownError("Network error: ${e.message}")
                            }
                        }
                        
                    } catch (e: Exception) {
                        lastError = NetworkError.UnknownError("Unexpected error: ${e.message}")
                    }
                }
                    
                    // If this is not the last attempt, wait before retrying
                    if (attempt < maxRetries - 1 && lastError is NetworkError && lastError.isRetryable) {
                        val delayMs = calculateRetryDelay(attempt, lastError)
                        Log.w(TAG, "$operationName failed on attempt ${attempt + 1}, retrying in ${delayMs}ms: ${lastError.message}")
                        delay(delayMs)
                    }
                }
                
                // All retries exhausted
                Log.e(TAG, "$operationName failed after $maxRetries attempts. Last error: ${lastError?.message}")
                Result.failure(lastError ?: Exception("Unknown error"))
            }
        }
        
        /**
         * Calculate retry delay with exponential backoff
         */
        private fun calculateRetryDelay(attempt: Int, error: NetworkError?): Long {
            return when (error?.type) {
                "RATE_LIMIT" -> {
                    // Longer delay for rate limiting
                    (RETRY_DELAY_MS * EXPONENTIAL_BACKOFF_BASE.pow(attempt)).toLong()
                }
                "TIMEOUT" -> {
                    // Exponential backoff for timeouts
                    (RETRY_DELAY_MS * EXPONENTIAL_BACKOFF_BASE.pow(attempt)).toLong()
                }
                else -> {
                    // Standard delay for other errors
                    RETRY_DELAY_MS.toLong()
                }
            }
        }
        
        /**
         * Check network connectivity with API level compatibility
         */
        private fun checkNetworkConnectivity(context: Context) {
            try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = connectivityManager.activeNetwork
                    if (network != null) {
                        val capabilities = connectivityManager.getNetworkCapabilities(network)
                        if (capabilities != null) {
                            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            val hasValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                            
                            if (!hasInternet) {
                                Log.w(TAG, "No internet connectivity")
                                throw IOException("No internet connectivity")
                            }
                            
                            if (!hasValidated) {
                                Log.w(TAG, "Network not validated")
                            }
                        }
                    } else {
                        Log.w(TAG, "No active network")
                        throw IOException("No active network")
                    }
                } else {
                    // Legacy method for older API levels
                    val activeNetworkInfo = connectivityManager.activeNetworkInfo
                    if (activeNetworkInfo == null || activeNetworkInfo.isConnected) {
                        Log.w(TAG, "No active network connection")
                        throw IOException("No active network connection")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error checking network connectivity", e)
                // Don't throw here, let the operation proceed and fail naturally
            }
        }
        
        /**
         * Check if VPN is active
         */
        fun isVpnActive(context: Context): Boolean {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val network = connectivityManager.activeNetwork
                    if (network != null) {
                        val capabilities = connectivityManager.getNetworkCapabilities(network)
                        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
                    } else {
                        false
                    }
                } else {
                    false // VPN detection not reliable on older versions
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking VPN status", e)
                false
            }
        }
        
        /**
         * Get network type for debugging
         */
        fun getNetworkType(context: Context): String {
            return try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = connectivityManager.activeNetwork
                    if (network != null) {
                        val capabilities = connectivityManager.getNetworkCapabilities(network)
                        capabilities?.let { caps ->
                            return when {
                                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
                                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                                else -> "Unknown"
                            }
                        }
                    }
                } else {
                    "No Connection"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting network type", e)
                "Unknown"
            }
        }
        
        /**
         * Test network connectivity with a simple ping
         */
        suspend fun testNetworkConnectivity(
            host: String = "8.8.8.8",
            port: Int = 53,
            timeoutMs: Long = 3000
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val address = InetAddress.getByName(host)
                    val socket = Socket()
                    socket.soTimeout = timeoutMs.toInt()
                    
                    val startTime = System.currentTimeMillis()
                    socket.connect(InetSocketAddress(address, port))
                    val endTime = System.currentTimeMillis()
                    
                    val connected = socket.isConnected && (endTime - startTime) < timeoutMs
                    socket.close()
                    
                    Log.d(TAG, "Network connectivity test to $host:$port: ${if (connected) "Success" else "Failed"} (${endTime - startTime}ms)")
                    connected
                    
                } catch (e: Exception) {
                    Log.w(TAG, "Network connectivity test failed: ${e.message}")
                    false
                }
            }
        }
        
        /**
         * Get detailed network information for debugging
         */
        fun getNetworkInfo(context: Context): Map<String, Any> {
            val info = mutableMapOf<String, Any>()
            
            try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = connectivityManager.activeNetwork
                    if (network != null) {
                        val capabilities = connectivityManager.getNetworkCapabilities(network)
                        val linkProperties = connectivityManager.getLinkProperties(network)
                        
                        info["network_type"] = getNetworkType(context)
                        info["has_internet"] = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
                        info["has_validated"] = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
                        info["is_vpn"] = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
                        info["downstream_bandwidth"] = capabilities?.linkDownstreamBandwidthKbps ?: 0
                        info["upstream_bandwidth"] = capabilities?.linkUpstreamBandwidthKbps ?: 0
                        
                        linkProperties?.let { props ->
                            info["mtu"] = props.mtu
                        }
                    }
                } else {
                    info["network_type"] = "No Connection"
                }
                
                // Add system info
                info["android_version"] = Build.VERSION.RELEASE
                info["api_level"] = Build.VERSION.SDK_INT
                info["manufacturer"] = Build.MANUFACTURER
                info["model"] = Build.MODEL
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting network info", e)
                info["error"] = e.message
            }
            
            return info
        }
    }
}

/**
 * Extension function for better error handling
 */
fun <T> Result<T>.logError(operation: String): T? {
    return if (isFailure) {
        Log.e("NetworkErrorHandler", "$operation failed: ${exceptionOrNull()?.message}")
        null
    } else {
        getOrNull()
    }
}
