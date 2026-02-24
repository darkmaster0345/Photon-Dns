package com.dnsspeedchecker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.dnsspeedchecker.service.DnsMonitoringService
import com.dnsspeedchecker.service.DnsVpnService

/**
 * Receiver for VPN status updates and network changes
 */
class VpnStatusReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "VpnStatusReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.dnsspeedchecker.VPN_STATUS_UPDATE" -> {
                handleVpnStatusUpdate(context, intent)
            }
            ConnectivityManager.CONNECTIVITY_ACTION -> {
                handleConnectivityChange(context)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                handleBootCompleted(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                handlePackageReplaced(context)
            }
        }
    }
    
    /**
     * Handles VPN status updates from the VPN service
     */
    private fun handleVpnStatusUpdate(context: Context, intent: Intent) {
        val isConnected = intent.getBooleanExtra("is_connected", false)
        val dnsServer = intent.getStringExtra("dns_server") ?: "Unknown"
        val error = intent.getStringExtra("error")
        
        Log.i(TAG, "VPN Status Update - Connected: $isConnected, DNS: $dnsServer, Error: $error")
        
        if (isConnected) {
            // VPN connected, start monitoring service
            startMonitoringService(context)
            Log.i(TAG, "VPN connected, started DNS monitoring")
        } else {
            // VPN disconnected, stop monitoring service
            stopMonitoringService(context)
            Log.i(TAG, "VPN disconnected, stopped DNS monitoring")
        }
        
        // Broadcast status to UI components
        broadcastStatusToUi(context, isConnected, dnsServer, error)
    }
    
    /**
     * Handles network connectivity changes
     */
    private fun handleConnectivityChange(context: Context) {
        Log.d(TAG, "Network connectivity changed")
        
        // Check if VPN is still running and restart if needed
        val vpnIntent = Intent(context, DnsVpnService::class.java).apply {
            action = "GET_STATUS"
        }
        
        // This would ideally get a response, but for now we'll just ensure services are in correct state
        try {
            context.startService(vpnIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check VPN status", e)
        }
    }
    
    /**
     * Handles device boot completion
     */
    private fun handleBootCompleted(context: Context) {
        Log.i(TAG, "Device boot completed")
        
        // Check if VPN should be auto-started
        val prefs = context.getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE)
        val autoStartVpn = prefs.getBoolean("auto_start_vpn", false)
        
        if (autoStartVpn) {
            Log.i(TAG, "Auto-starting VPN after boot")
            startVpnService(context)
        }
    }
    
    /**
     * Handles package replacement (app update)
     */
    private fun handlePackageReplaced(context: Context) {
        Log.i(TAG, "Package replaced (app updated)")
        
        // Restart services if they were running before update
        val prefs = context.getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE)
        val wasVpnRunning = prefs.getBoolean("vpn_running", false)
        
        if (wasVpnRunning) {
            Log.i(TAG, "Restarting VPN after app update")
            startVpnService(context)
        }
    }
    
    /**
     * Starts the VPN service
     */
    private fun startVpnService(context: Context) {
        try {
            val intent = Intent(context, DnsVpnService::class.java).apply {
                action = "START_VPN"
            }
            context.startService(intent)
            
            // Save state
            context.getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("vpn_running", true)
                .apply()
                
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VPN service", e)
        }
    }
    
    /**
     * Starts the DNS monitoring service
     */
    private fun startMonitoringService(context: Context) {
        try {
            val intent = Intent(context, DnsMonitoringService::class.java).apply {
                action = "START_MONITORING"
            }
            context.startService(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start monitoring service", e)
        }
    }
    
    /**
     * Stops the DNS monitoring service
     */
    private fun stopMonitoringService(context: Context) {
        try {
            val intent = Intent(context, DnsMonitoringService::class.java).apply {
                action = "STOP_MONITORING"
            }
            context.startService(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop monitoring service", e)
        }
    }
    
    /**
     * Broadcasts VPN status to UI components
     */
    private fun broadcastStatusToUi(
        context: Context,
        isConnected: Boolean,
        dnsServer: String,
        error: String?
    ) {
        val uiIntent = Intent("com.dnsspeedchecker.UI_VPN_STATUS").apply {
            putExtra("is_connected", isConnected)
            putExtra("dns_server", dnsServer)
            putExtra("error", error)
        }
        context.sendBroadcast(uiIntent)
    }
}

/**
 * Network callback for monitoring network changes
 */
class VpnNetworkCallback(
    private val onNetworkAvailable: () -> Unit,
    private val onNetworkLost: () -> Unit
) : ConnectivityManager.NetworkCallback() {
    
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Log.d("VpnNetworkCallback", "Network available: $network")
        onNetworkAvailable()
    }
    
    override fun onLost(network: Network) {
        super.onLost(network)
        Log.d("VpnNetworkCallback", "Network lost: $network")
        onNetworkLost()
    }
    
    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        
        val hasVpn = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        Log.d("VpnNetworkCallback", "Network $network VPN capability: $hasVpn")
    }
}

/**
 * Utility class for network monitoring
 */
object VpnNetworkMonitor {
    
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: VpnNetworkCallback? = null
    private var isMonitoring = false
    
    /**
     * Starts monitoring network changes
     */
    fun startMonitoring(
        context: Context,
        onNetworkAvailable: () -> Unit,
        onNetworkLost: () -> Unit
    ) {
        if (isMonitoring) return
        
        try {
            connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback = VpnNetworkCallback(onNetworkAvailable, onNetworkLost)
            
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .build()
            
            connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
            isMonitoring = true
            
            Log.d("VpnNetworkMonitor", "Started network monitoring")
            
        } catch (e: Exception) {
            Log.e("VpnNetworkMonitor", "Failed to start network monitoring", e)
        }
    }
    
    /**
     * Stops monitoring network changes
     */
    fun stopMonitoring() {
        if (!isMonitoring) return
        
        try {
            networkCallback?.let { callback ->
                connectivityManager?.unregisterNetworkCallback(callback)
            }
            
            networkCallback = null
            connectivityManager = null
            isMonitoring = false
            
            Log.d("VpnNetworkMonitor", "Stopped network monitoring")
            
        } catch (e: Exception) {
            Log.e("VpnNetworkMonitor", "Failed to stop network monitoring", e)
        }
    }
    
    /**
     * Checks if VPN is currently active
     */
    fun isVpnActive(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        } catch (e: Exception) {
            Log.e("VpnNetworkMonitor", "Failed to check VPN status", e)
            false
        }
    }
}
