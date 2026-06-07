package com.photondns.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.photondns.app.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        Log.d(TAG, "Received broadcast: $action")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val vpnConnected = settingsRepository.appSettingsFlow
                    .map { it.vpnConnected }
                    .first()
                
                if (vpnConnected) {
                    Log.i(TAG, "VPN was previously connected, reconnecting...")
                    reconnectVpn(context)
                } else {
                    Log.d(TAG, "VPN was not connected, skipping reconnection")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check VPN state on boot", e)
            }
        }
    }

    private suspend fun reconnectVpn(context: Context) {
        try {
            val connectIntent = Intent(context, DNSVpnService::class.java).apply {
                action = DNSVpnService.ACTION_CONNECT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(connectIntent)
            } else {
                context.startService(connectIntent)
            }
            Log.i(TAG, "VPN reconnection initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reconnect VPN", e)
        }
    }
}