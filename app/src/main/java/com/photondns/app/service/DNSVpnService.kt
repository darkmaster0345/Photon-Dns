package com.photondns.app.service

import android.app.*
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.photondns.app.data.models.DNSProtocol
import com.photondns.app.data.models.DNSServer
import com.photondns.app.data.repository.DNSServerRepository
import com.photondns.app.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class DNSVpnService : VpnService() {
    
    @Inject
    lateinit var dnsServerRepository: DNSServerRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var activeServerId: String? = null
    private var observerJob: Job? = null
    private var packetPumpJob: Job? = null
    
    companion object {
        const val ACTION_CONNECT = "com.photondns.app.CONNECT"
        const val ACTION_DISCONNECT = "com.photondns.app.DISCONNECT"
        const val NOTIFICATION_CHANNEL_ID = "dns_vpn_channel"
        const val NOTIFICATION_ID = 1001
        
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ADDRESS6 = "fd00:1::2"
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
        startForeground(NOTIFICATION_ID, createNotification(getString(com.photondns.app.R.string.vpn_starting)))

        isRunning = true
        observerJob = serviceScope.launch {
            observeActiveServer()
        }
    }
    
    private suspend fun observeActiveServer() {
        dnsServerRepository.getAllServers().collectLatest { servers ->
            val activeServer = servers.find { it.isActive }
            if (activeServer != null && activeServer.id != activeServerId) {
                activeServerId = activeServer.id
                updateVpn(activeServer)
            }
        }
    }
    
    private fun updateVpn(server: DNSServer) {
        try {
            // Check if the protocol is encrypted (DoH or DoT)
            if (server.protocol != DNSProtocol.UDP) {
                val errorMsg = "Encrypted DNS (${server.protocol.name}) forwarding is not yet implemented. Please use UDP servers."
                Log.e("DNSVpnService", errorMsg)
                throw IllegalStateException(errorMsg)
            }

            vpnInterface?.close()

            val settings = runBlocking { settingsRepository.appSettingsFlow.first() }

            val builder = Builder()
                .setSession("Photon DNS")
                .addAddress(VPN_ADDRESS, 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer(server.ip)
                .setBlocking(true)

            if (settings.ipv6Enabled) {
                builder.addAddress(VPN_ADDRESS6, 128)
                builder.addRoute("::", 0)
            }

            vpnInterface = builder.establish()

            // Start packet pump
            startPacketPump()

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, createNotification(getString(com.photondns.app.R.string.active_server, server.name)))

            Log.i("DNSVpnService", "VPN established with DNS: ${server.ip} (${server.name})")
        } catch (e: Exception) {
            Log.e("DNSVpnService", "Failed to update VPN", e)
        }
    }
    
    private fun stopVpn() {
        isRunning = false
        observerJob?.cancel()
        observerJob = null
        packetPumpJob?.cancel()
        packetPumpJob = null
        vpnInterface?.close()
        vpnInterface = null
        activeServerId = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startPacketPump() {
        packetPumpJob?.cancel()
        val vpnFd = vpnInterface ?: return

        packetPumpJob = serviceScope.launch {
            try {
                val inputStream = FileInputStream(vpnFd.fileDescriptor)
                val outputStream = FileOutputStream(vpnFd.fileDescriptor)
                val buffer = ByteArray(32767)

                while (isRunning && isActive) {
                    try {
                        val length = inputStream.read(buffer)
                        if (length > 0) {
                            // TODO: Parse DNS packets, forward to server, and write responses
                            // For now, this is a placeholder that demonstrates the packet pump structure
                            Log.d("DNSVpnService", "Received packet of length $length")
                        }
                    } catch (e: Exception) {
                        if (isRunning) {
                            Log.e("DNSVpnService", "Packet pump error", e)
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e("DNSVpnService", "Failed to start packet pump", e)
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(com.photondns.app.R.string.dns_vpn_service),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(com.photondns.app.R.string.photon_dns_description)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(content: String): Notification {
        val intent = Intent(this, com.photondns.app.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(com.photondns.app.R.string.photon_dns_active))
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        serviceScope.cancel()
    }
}
