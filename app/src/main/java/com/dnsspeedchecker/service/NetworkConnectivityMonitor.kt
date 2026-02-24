package com.dnsspeedchecker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Network connectivity monitor for handling internet disconnection scenarios
 */
class NetworkConnectivityMonitor(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkConnectivityMonitor"
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _networkType = MutableStateFlow(NetworkType.NONE)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()
    
    private val _connectionQuality = MutableStateFlow(ConnectionQuality.UNKNOWN)
    val connectionQuality: StateFlow<ConnectionQuality> = _connectionQuality.asStateFlow()
    
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    
    enum class NetworkType {
        NONE, WIFI, MOBILE, ETHERNET, VPN, OTHER
    }
    
    enum class ConnectionQuality {
        UNKNOWN, POOR, FAIR, GOOD, EXCELLENT
    }
    
    /**
     * Starts monitoring network connectivity
     */
    fun startMonitoring() {
        Log.i(TAG, "🌐 Starting network connectivity monitoring")
        
        // Check current state first
        updateNetworkStatus()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerNetworkCallback()
        } else {
            registerBroadcastReceiver()
        }
    }
    
    /**
     * Stops monitoring network connectivity
     */
    fun stopMonitoring() {
        Log.i(TAG, "🛑 Stopping network connectivity monitoring")
        
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
            networkCallback = null
        }
        
        broadcastReceiver?.let {
            context.unregisterReceiver(it)
            broadcastReceiver = null
        }
    }
    
    /**
     * Registers network callback for modern Android versions
     */
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.i(TAG, "📶 Network available: $network")
                updateNetworkStatus()
            }
            
            override fun onLost(network: Network) {
                super.onLost(network)
                Log.w(TAG, "📶 Network lost: $network")
                updateNetworkStatus()
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                Log.d(TAG, "📶 Network capabilities changed: $network")
                updateNetworkStatus()
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }
    
    /**
     * Registers broadcast receiver for older Android versions
     */
    private fun registerBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                    Log.d(TAG, "📶 Connectivity change broadcast received")
                    updateNetworkStatus()
                }
            }
        }
        
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(broadcastReceiver, intentFilter)
    }
    
    /**
     * Updates network status and notifies listeners
     */
    private fun updateNetworkStatus() {
        try {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
            
            val wasConnected = _isConnected.value
            val isConnected = capabilities?.let {
                it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } ?: false
            
            val networkType = determineNetworkType(capabilities)
            val connectionQuality = assessConnectionQuality(capabilities)
            
            _isConnected.value = isConnected
            _networkType.value = networkType
            _connectionQuality.value = connectionQuality
            
            // Log significant changes
            if (wasConnected != isConnected) {
                if (isConnected) {
                    Log.i(TAG, "✅ Network connected: $networkType ($connectionQuality)")
                } else {
                    Log.w(TAG, "❌ Network disconnected")
                }
            } else {
                Log.d(TAG, "📶 Network status: $networkType ($connectionQuality), Connected: $isConnected")
            }
            
            // Broadcast network status change
            val intent = Intent("com.dnsspeedchecker.NETWORK_STATUS_CHANGE").apply {
                putExtra("is_connected", isConnected)
                putExtra("network_type", networkType.name)
                putExtra("connection_quality", connectionQuality.name)
            }
            context.sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating network status", e)
        }
    }
    
    /**
     * Determines the type of network connection
     */
    private fun determineNetworkType(capabilities: NetworkCapabilities?): NetworkType {
        if (capabilities == null) return NetworkType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.VPN
            else -> NetworkType.OTHER
        }
    }
    
    /**
     * Assesses connection quality based on network capabilities
     */
    private fun assessConnectionQuality(capabilities: NetworkCapabilities?): ConnectionQuality {
        if (capabilities == null) return ConnectionQuality.UNKNOWN
        
        return when {
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> ConnectionQuality.POOR
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> ConnectionQuality.POOR
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) -> {
                // Unmetered connection (usually WiFi) is typically better
                when {
                    capabilities.linkDownstreamBandwidthKbps > 20000 -> ConnectionQuality.EXCELLENT // > 20 Mbps
                    capabilities.linkDownstreamBandwidthKbps > 10000 -> ConnectionQuality.GOOD // > 10 Mbps
                    capabilities.linkDownstreamBandwidthKbps > 5000 -> ConnectionQuality.FAIR // > 5 Mbps
                    else -> ConnectionQuality.POOR
                }
            }
            
            else -> {
                // Metered connection (usually mobile)
                when {
                    capabilities.linkDownstreamBandwidthKbps > 10000 -> ConnectionQuality.GOOD // > 10 Mbps
                    capabilities.linkDownstreamBandwidthKbps > 5000 -> ConnectionQuality.FAIR // > 5 Mbps
                    capabilities.linkDownstreamBandwidthKbps > 1000 -> ConnectionQuality.POOR // > 1 Mbps
                    else -> ConnectionQuality.POOR
                }
            }
        }
    }
    
    /**
     * Checks if network is suitable for DNS testing
     */
    fun isNetworkSuitableForDnsTesting(): Boolean {
        return _isConnected.value && _connectionQuality.value != ConnectionQuality.POOR
    }
    
    /**
     * Gets current network information as a string
     */
    fun getNetworkInfo(): String {
        return "Network: ${_networkType.value}, Quality: ${_connectionQuality.value}, Connected: ${_isConnected.value}"
    }
    
    /**
     * Performs a quick network connectivity test
     */
    suspend fun testConnectivity(): Boolean {
        return try {
            // Test basic connectivity by attempting to resolve a domain
            val testResult = DnsLatencyChecker().measureDnsLatency("8.8.8.8")
            val isConnected = testResult.success
            
            Log.d(TAG, "🔍 Connectivity test result: $isConnected")
            isConnected
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Connectivity test failed", e)
            false
        }
    }
}
