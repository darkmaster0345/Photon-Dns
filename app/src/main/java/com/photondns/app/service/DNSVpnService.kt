package com.photondns.app.service

import android.app.*
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import javax.inject.Inject

@AndroidEntryPoint
class DNSVpnService : VpnService() {
    
    @Inject
    lateinit var dnsLatencyChecker: DNSLatencyChecker
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        const val ACTION_CONNECT = "com.photondns.app.CONNECT"
        const val ACTION_DISCONNECT = "com.photondns.app.DISCONNECT"
        const val NOTIFICATION_CHANNEL_ID = "dns_vpn_channel"
        const val NOTIFICATION_ID = 1001
        
        // DNS packet constants
        private const val DNS_PORT = 53
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_CONNECT -> {
                startVpn()
                START_STICKY
            }
            ACTION_DISCONNECT -> {
                stopVpn()
                START_NOT_STICKY
            }
            else -> START_NOT_STICKY
        }
    }
    
    private fun startVpn() {
        if (isRunning) return
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        serviceScope.launch {
            try {
                establishVpn()
                isRunning = true
                handleVpnTraffic()
            } catch (e: Exception) {
                Log.e("DNSVpnService", "VPN error", e)
                stopVpn()
            }
        }
    }
    
    private fun stopVpn() {
        isRunning = false
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun establishVpn() {
        vpnInterface = Builder()
            .setSession("Photon DNS")
            .addAddress(VPN_ADDRESS, 24)
            .addRoute(VPN_ROUTE, 0)
            .addDnsServer("8.8.8.8") // Will be updated dynamically
            .establish()
    }
    
    private suspend fun handleVpnTraffic() {
        vpnInterface?.let { vpn ->
            val vpnInput = FileInputStream(vpn.fileDescriptor)
            val vpnOutput = FileOutputStream(vpn.fileDescriptor)
            val buffer = ByteArray(32767)
            
            while (isRunning) {
                try {
                    val bytesRead = vpnInput.read(buffer)
                    if (bytesRead > 0) {
                        processPacket(buffer, bytesRead, vpnOutput)
                    }
                } catch (e: Exception) {
                    Log.e("DNSVpnService", "Traffic handling error", e)
                    break
                }
            }
        }
    }
    
    private suspend fun processPacket(packet: ByteArray, length: Int, vpnOutput: FileOutputStream) {
        // Check if it's a DNS query (UDP port 53)
        if (isDnsQuery(packet, length)) {
            handleDnsQuery(packet, length, vpnOutput)
        } else {
            // Forward non-DNS traffic normally
            vpnOutput.write(packet, 0, length)
        }
    }
    
    private fun isDnsQuery(packet: ByteArray, length: Int): Boolean {
        // Basic DNS packet detection (simplified)
        return length >= 12 && // Minimum DNS packet size
                packet[9].toInt() and 0x11 == 0 // UDP and no fragmentation
    }
    
    private suspend fun handleDnsQuery(packet: ByteArray, length: Int, vpnOutput: FileOutputStream) {
        try {
            // Extract DNS query and forward to current DNS server
            val dnsServer = getCurrentDnsServer()
            val response = forwardDnsQuery(packet, length, dnsServer)
            vpnOutput.write(response)
        } catch (e: Exception) {
            Log.e("DNSVpnService", "DNS query handling error", e)
        }
    }
    
    private suspend fun getCurrentDnsServer(): String {
        // Get current active DNS server from repository
        return "8.8.8.8" // Default fallback
    }
    
    private suspend fun forwardDnsQuery(query: ByteArray, length: Int, dnsServer: String): ByteArray {
        return withContext(Dispatchers.IO) {
            val socket = DatagramSocket()
            try {
                val address = InetAddress.getByName(dnsServer)
                val packet = java.net.DatagramPacket(query, length, address, DNS_PORT)
                socket.send(packet)
                
                val responseBuffer = ByteArray(1024)
                val responsePacket = java.net.DatagramPacket(responseBuffer, responseBuffer.size)
                socket.receive(responsePacket)
                
                responseBuffer.copyOf(responsePacket.length)
            } finally {
                socket.close()
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "DNS VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Manages DNS traffic interception"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Photon DNS Active")
            .setContentText("DNS traffic is being monitored and optimized")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        serviceScope.cancel()
    }
}
