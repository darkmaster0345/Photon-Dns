package com.photondns.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.photondns.app.service.DNSVpnService
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            "android.intent.action.BOOT_COMPLETED", "android.intent.action.MY_PACKAGE_REPLACED" -> {
                handleBootOrReplace(context)
            }
        }
    }

    private fun handleBootOrReplace(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsRepository = EntryPointAccessors.fromApplication(
                    context,
                    SettingsRepositoryEntryPoint::class.java
                ).settingsRepository()

                val wasVpnConnected = settingsRepository.vpnConnectedFlow.first()

                if (wasVpnConnected) {
                    Log.i(TAG, "VPN was previously connected, attempting auto-reconnect")

                    val connectIntent = Intent(context, DNSVpnService::class.java).apply {
                        action = DNSVpnService.ACTION_CONNECT
                    }
                    context.startService(connectIntent)
                } else {
                    Log.i(TAG, "VPN was not previously connected, skipping auto-reconnect")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle boot/replace", e)
            }
        }
    }
}