package com.dnsspeedchecker.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Utility class for handling app permissions with rationale dialogs
 */
class PermissionManager private constructor() {
    
    companion object {
        // Permission codes
        const val VPN_PERMISSION_CODE = 1001
        const val NOTIFICATION_PERMISSION_CODE = 1002
        const val POST_NOTIFICATIONS_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
        
        /**
         * Checks if a permission is granted
         */
        fun isPermissionGranted(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        /**
         * Checks if VPN permission is granted
         */
        fun isVpnPermissionGranted(context: Context): Boolean {
            return android.net.VpnService.prepare(context) == null
        }
        
        /**
         * Checks if notification permission is granted
         */
        fun isNotificationPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isPermissionGranted(context, POST_NOTIFICATIONS_PERMISSION)
            } else {
                true // Notifications are automatically granted on older versions
            }
        }
        
        /**
         * Shows permission rationale dialog
         */
        fun showPermissionRationale(
            context: Context,
            title: String,
            message: String,
            onPositive: () -> Unit,
            onNegative: () -> Unit = {}
        ) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
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
         * Shows VPN permission rationale
         */
        fun showVpnPermissionRationale(
            context: Context,
            onPositive: () -> Unit,
            onNegative: () -> Unit = {}
        ) {
            showPermissionRationale(
                context = context,
                title = "VPN Permission Required",
                message = "DNS Speed Checker needs VPN permission to intercept and monitor DNS queries. This allows us to:\n\n" +
                        "• Measure DNS server response times\n" +
                        "• Automatically switch to faster servers\n" +
                        "• Optimize your internet experience\n\n" +
                        "Your internet traffic is NOT monitored or stored. Only DNS queries are processed for performance measurement.",
                onPositive = onPositive,
                onNegative = onNegative
            )
        }
        
        /**
         * Shows notification permission rationale
         */
        fun showNotificationPermissionRationale(
            context: Context,
            onPositive: () -> Unit,
            onNegative: () -> Unit = {}
        ) {
            showPermissionRationale(
                context = context,
                title = "Notification Permission Required",
                message = "DNS Speed Checker needs notification permission to:\n\n" +
                        "• Show current DNS server status\n" +
                        "• Display latency information\n" +
                        "• Alert you to DNS changes\n" +
                        "• Provide quick access to controls\n\n" +
                        "You can disable notifications anytime in settings.",
                onPositive = onPositive,
                onNegative = onNegative
            )
        }
        
        /**
         * Shows settings dialog for permissions that can't be requested directly
         */
        fun showSettingsDialog(
            context: Context,
            title: String,
            message: String,
            onPositive: () -> Unit,
            onNegative: () -> Unit = {}
        ) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Open Settings") { _, _ ->
                    onPositive()
                    openAppSettings(context)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    onNegative()
                }
                .setCancelable(false)
                .show()
        }
        
        /**
         * Opens app settings
         */
        fun openAppSettings(context: Context) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
        
        /**
         * Creates a permission launcher for Activity
         */
        fun createPermissionLauncher(
            activity: AppCompatActivity,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit,
            onRationaleNeeded: (String) -> Unit = {}
        ): ActivityResultLauncher<String> {
            return activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
        
        /**
         * Creates a permission launcher for Fragment
         */
        fun createPermissionLauncher(
            fragment: Fragment,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit,
            onRationaleNeeded: (String) -> Unit = {}
        ): ActivityResultLauncher<String> {
            return fragment.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
        
        /**
         * Requests notification permission with proper handling
         */
        fun requestNotificationPermission(
            context: Context,
            launcher: ActivityResultLauncher<String>,
            onGranted: () -> Unit,
            onDenied: () -> Unit
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!isNotificationPermissionGranted(context)) {
                    // Check if we should show rationale
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            context as Activity, POST_NOTIFICATIONS_PERMISSION
                        )
                    ) {
                        showNotificationPermissionRationale(context) {
                            launcher.launch(POST_NOTIFICATIONS_PERMISSION)
                        }
                    } else {
                        launcher.launch(POST_NOTIFICATIONS_PERMISSION)
                    }
                } else {
                    onGranted()
                }
            } else {
                onGranted() // Permission automatically granted on older versions
            }
        }
        
        /**
         * Gets all required permissions for the app
         */
        fun getRequiredPermissions(): List<String> {
            return mutableListOf<String>().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(POST_NOTIFICATIONS_PERMISSION)
                }
            }
        }
        
        /**
         * Checks if all required permissions are granted
         */
        fun areAllPermissionsGranted(context: Context): Boolean {
            return getRequiredPermissions().all { permission ->
                isPermissionGranted(context, permission)
            }
        }
        
        /**
         * Shows comprehensive permission dialog for first-time users
         */
        fun showFirstTimePermissionDialog(
            context: Context,
            onContinue: () -> Unit
        ) {
            AlertDialog.Builder(context)
                .setTitle("Welcome to DNS Speed Checker")
                .setMessage(
                    "This app needs the following permissions to work properly:\n\n" +
                            "🔐 VPN Permission: To intercept DNS queries\n" +
                            "🔔 Notification Permission: To show status updates\n\n" +
                            "Your privacy is important:\n" +
                            "• No internet traffic monitoring\n" +
                            "• Only DNS queries are processed\n" +
                            "• All data stays on your device\n\n" +
                            "Ready to optimize your DNS experience?"
                )
                .setPositiveButton("Continue") { _, _ ->
                    onContinue()
                }
                .setNegativeButton("Learn More") { _, _ ->
                    // Could open help documentation
                    onContinue()
                }
                .setCancelable(false)
                .show()
        }
    }
}

/**
 * Extension function for Activity to simplify permission requests
 */
fun AppCompatActivity.requestPermissionWithRationale(
    permission: String,
    rationaleTitle: String,
    rationaleMessage: String,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    if (PermissionManager.isPermissionGranted(this, permission)) {
        onGranted()
        return
    }
    
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
        PermissionManager.showPermissionRationale(
            context = this,
            title = rationaleTitle,
            message = rationaleMessage
        ) {
            // Launch permission request
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                when (permission) {
                    PermissionManager.POST_NOTIFICATIONS_PERMISSION -> PermissionManager.NOTIFICATION_PERMISSION_CODE
                    else -> 0
                }
            )
        }
    } else {
        // Direct permission request
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            when (permission) {
                PermissionManager.POST_NOTIFICATIONS_PERMISSION -> PermissionManager.NOTIFICATION_PERMISSION_CODE
                else -> 0
            }
        )
    }
}

/**
 * Extension function for Fragment to simplify permission requests
 */
fun Fragment.requestPermissionWithRationale(
    permission: String,
    rationaleTitle: String,
    rationaleMessage: String,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    if (PermissionManager.isPermissionGranted(requireContext(), permission)) {
        onGranted()
        return
    }
    
    if (shouldShowRequestPermissionRationale(permission)) {
        PermissionManager.showPermissionRationale(
            context = requireContext(),
            title = rationaleTitle,
            message = rationaleMessage
        ) {
            // Launch permission request
            requestPermissions(
                arrayOf(permission),
                when (permission) {
                    PermissionManager.POST_NOTIFICATIONS_PERMISSION -> PermissionManager.NOTIFICATION_PERMISSION_CODE
                    else -> 0
                }
            )
        }
    } else {
        // Direct permission request
        requestPermissions(
            arrayOf(permission),
            when (permission) {
                PermissionManager.POST_NOTIFICATIONS_PERMISSION -> PermissionManager.NOTIFICATION_PERMISSION_CODE
                else -> 0
            }
        )
    }
}
