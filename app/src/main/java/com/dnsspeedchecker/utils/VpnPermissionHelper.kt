package com.dnsspeedchecker.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Utility class for handling VPN permissions and preparation
 */
class VpnPermissionHelper private constructor() {
    
    companion object {
        /**
         * Checks if VPN permission is already granted
         */
        fun isVpnPermissionGranted(context: Context): Boolean {
            return VpnService.prepare(context) == null
        }
        
        /**
         * Creates a VPN permission launcher for Activity
         */
        fun createVpnPermissionLauncher(
            activity: AppCompatActivity,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit
        ): ActivityResultLauncher<Intent> {
            return activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
        
        /**
         * Creates a VPN permission launcher for Fragment
         */
        fun createVpnPermissionLauncher(
            fragment: Fragment,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit
        ): ActivityResultLauncher<Intent> {
            return fragment.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
        
        /**
         * Requests VPN permission if not already granted
         */
        fun requestVpnPermission(
            context: Context,
            launcher: ActivityResultLauncher<Intent>
        ): Boolean {
            val vpnIntent = VpnService.prepare(context)
            return if (vpnIntent != null) {
                launcher.launch(vpnIntent)
                false // Permission not granted, request initiated
            } else {
                true // Permission already granted
            }
        }
        
        /**
         * Shows VPN permission dialog and handles the result
         */
        fun showVpnPermissionDialog(
            context: Context,
            onPositive: () -> Unit,
            onNegative: () -> Unit
        ) {
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("VPN Permission Required")
                .setMessage("This app needs VPN permission to intercept and monitor DNS queries. This allows us to measure DNS performance and automatically switch to faster DNS servers.")
                .setPositiveButton("Grant Permission") { _, _ ->
                    onPositive()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    onNegative()
                }
                .setCancelable(false)
                .show()
        }
        
        /**
         * Shows VPN education dialog for first-time users
         */
        fun showVpnEducationDialog(
            context: Context,
            onContinue: () -> Unit
        ) {
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("How DNS Speed Checker Works")
                .setMessage(
                    "This app creates a local VPN service to:\n\n" +
                    "• Intercept DNS queries from your device\n" +
                    "• Measure response times from different DNS servers\n" +
                    "• Automatically switch to the fastest DNS server\n\n" +
                    "Your internet traffic is NOT monitored or stored. Only DNS queries are processed for performance measurement."
                )
                .setPositiveButton("I Understand") { _, _ ->
                    onContinue()
                }
                .setNegativeButton("Learn More") { _, _ ->
                    // Could open a help page or documentation
                    onContinue()
                }
                .setCancelable(false)
                .show()
        }
    }
}

/**
 * Extension function for Activity to simplify VPN permission handling
 */
fun AppCompatActivity.requestVpnPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
): ActivityResultLauncher<Intent> {
    val launcher = VpnPermissionHelper.createVpnPermissionLauncher(
        this,
        onPermissionGranted,
        onPermissionDenied
    )
    
    // Check if permission is already granted
    if (VpnPermissionHelper.isVpnPermissionGranted(this)) {
        onPermissionGranted()
    } else {
        // Show education dialog first
        VpnPermissionHelper.showVpnEducationDialog(this) {
            VpnPermissionHelper.showVpnPermissionDialog(this) {
                VpnPermissionHelper.requestVpnPermission(this, launcher)
            }
        }
    }
    
    return launcher
}

/**
 * Extension function for Fragment to simplify VPN permission handling
 */
fun Fragment.requestVpnPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
): ActivityResultLauncher<Intent> {
    val launcher = VpnPermissionHelper.createVpnPermissionLauncher(
        this,
        onPermissionGranted,
        onPermissionDenied
    )
    
    // Check if permission is already granted
    if (VpnPermissionHelper.isVpnPermissionGranted(requireContext())) {
        onPermissionGranted()
    } else {
        // Show education dialog first
        VpnPermissionHelper.showVpnEducationDialog(requireContext()) {
            VpnPermissionHelper.showVpnPermissionDialog(requireContext()) {
                VpnPermissionHelper.requestVpnPermission(requireContext(), launcher)
            }
        }
    }
    
    return launcher
}
