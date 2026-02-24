package com.dnsspeedchecker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dnsspeedchecker.service.DnsMonitoringService
import com.dnsspeedchecker.service.DnsVpnService

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                
                // Check if VPN was running before reboot/reinstall
                // This is a simplified approach - in production, you'd want to persist this state
                val prefs = context.getSharedPreferences("dns_prefs", Context.MODE_PRIVATE)
                val wasVpnRunning = prefs.getBoolean("vpn_running", false)
                
                if (wasVpnRunning) {
                    // Restart VPN service
                    context.startService(Intent(context, DnsVpnService::class.java).apply {
                        action = "START_VPN"
                    })
                    
                    // Restart monitoring service
                    context.startService(Intent(context, DnsMonitoringService::class.java).apply {
                        action = "START_MONITORING"
                    })
                }
            }
        }
    }
}
