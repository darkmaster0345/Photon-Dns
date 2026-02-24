package com.dnsspeedchecker

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dnsspeedchecker.ui.screens.MainScreen
import com.dnsspeedchecker.ui.screens.ExactReplicaMainScreen
import com.dnsspeedchecker.ui.screens.SettingsScreen
import com.dnsspeedchecker.ui.screens.StrategyQuizScreen
import com.dnsspeedchecker.ui.screens.SwitchingSettingsScreen
import com.dnsspeedchecker.ui.components.EnhancedMainScreen
import com.dnsspeedchecker.ui.components.EnhancedSettingsScreen
import com.dnsspeedchecker.ui.screens.ExportLogsScreen
import com.dnsspeedchecker.ui.theme.DNSSpeedCheckerTheme
import com.dnsspeedchecker.ui.viewmodel.MainViewModel
import com.dnsspeedchecker.utils.LogExporter
import com.dnsspeedchecker.utils.PermissionManager
import com.dnsspeedchecker.utils.requestVpnPermission

class MainActivity : ComponentActivity() {
    
    private lateinit var vpnPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Clear old logs on app start
        LogExporter.clearOldLogs(this)
        
        setContent {
            DNSSpeedCheckerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DNSApp()
                }
            }
        }
    }
    
    @Composable
    fun DNSApp() {
        val navController = rememberNavController()
        val viewModel: MainViewModel = viewModel()
        
        val uiState by viewModel.uiState.collectAsState()
        val settingsState by viewModel.settingsState.collectAsState()
        
        // Setup permission launchers
        vpnPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.toggleVpn()
            } else {
                viewModel.clearError()
            }
        }
        
        notificationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Permission granted, proceed with VPN if needed
            } else {
                // Show rationale or disable notification features
            }
        }
        
        // Check and request permissions on first launch
        LaunchedEffect(Unit) {
            checkAndRequestPermissions()
        }
        
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                ExactReplicaMainScreen(
                    isVpnConnected = uiState.isVpnConnected,
                    currentDnsServer = uiState.currentDnsServer,
                    dnsLatencies = uiState.dnsLatencies,
                    isAutoSwitchEnabled = uiState.isAutoSwitchEnabled,
                    latencyHistory = uiState.latencyHistory,
                    onVpnToggle = { 
                        handleVpnToggle(uiState.isVpnConnected, viewModel)
                    },
                    onAutoSwitchToggle = { enabled -> viewModel.toggleAutoSwitch(enabled) },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            
            composable("settings") {
                EnhancedSettingsScreen(
                    checkInterval = settingsState.checkInterval,
                    switchingThreshold = settingsState.switchingThreshold,
                    enabledDnsServers = settingsState.enabledDnsServers,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSwitchingSettings = { navController.navigate("switching_settings") },
                    onCheckIntervalChange = { interval -> viewModel.updateCheckInterval(interval) },
                    onSwitchingThresholdChange = { threshold -> viewModel.updateSwitchingThreshold(threshold) },
                    onDnsServerToggle = { serverId, enabled -> 
                        viewModel.updateDnsServerEnabled(serverId, enabled) 
                    },
                    onSaveSettings = { navController.popBackStack() },
                    onExportSettings = { /* TODO: Handle export */ },
                    onImportSettings = { json -> 
                        viewModel.importDNSSettings(json)
                        // Handle result (show toast, etc.)
                    },
                    onResetToDefaults = { viewModel.resetDNSSettingsToDefaults() }
                )
            }
            
            composable("switching_settings") {
                SwitchingSettingsScreen(
                    settings = settingsState.dnsSettings,
                    onSettingsChange = { settings -> viewModel.updateDNSSettings(settings) },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToStrategyQuiz = { navController.navigate("strategy_quiz") },
                    onExportSettings = { /* TODO: Handle export */ },
                    onImportSettings = { json -> 
                        viewModel.importDNSSettings(json)
                        // Handle result (show toast, etc.)
                    },
                    onResetToDefaults = { viewModel.resetDNSSettingsToDefaults() }
                )
            }
            
            composable("strategy_quiz") {
                StrategyQuizScreen(
                    onStrategyRecommended = { strategy ->
                        viewModel.updateStrategy(strategy)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("export_logs") {
                ExportLogsScreen(
                    onExportLogs = { success, message ->
                        if (success) {
                            // Show success message
                        } else {
                            // Show error message
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
    
    /**
     * Checks and requests necessary permissions
     */
    private fun checkAndRequestPermissions() {
        // Check VPN permission
        if (!PermissionManager.isVpnPermissionGranted(this)) {
            PermissionManager.showFirstTimePermissionDialog(this) {
                requestVpnPermissionWithLauncher()
            }
        }
        
        // Check notification permission (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionManager.isNotificationPermissionGranted(this)) {
                PermissionManager.requestNotificationPermission(
                    context = this,
                    launcher = notificationPermissionLauncher,
                    onGranted = {
                        // Permission granted
                    },
                    onDenied = {
                        // Show rationale or disable notification features
                        PermissionManager.showNotificationPermissionRationale(this) {
                            // User can try again
                        }
                    }
                )
            }
        }
    }
    
    /**
     * Handles VPN toggle with permission check
     */
    private fun handleVpnToggle(isCurrentlyConnected: Boolean, viewModel: MainViewModel) {
        if (!isCurrentlyConnected) {
            // Starting VPN - check permission first
            if (PermissionManager.isVpnPermissionGranted(this)) {
                viewModel.toggleVpn()
            } else {
                PermissionManager.showVpnPermissionRationale(this) {
                    requestVpnPermissionWithLauncher()
                }
            }
        } else {
            // Stopping VPN - no permission needed
            viewModel.toggleVpn()
        }
    }
    
    /**
     * Requests VPN permission using the launcher
     */
    private fun requestVpnPermissionWithLauncher() {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            vpnPermissionLauncher.launch(vpnIntent)
        } else {
            // Permission already granted
            // This shouldn't happen if we checked properly
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check permissions again when app resumes
        checkAndRequestPermissions()
    }
}

@Composable
fun ExportLogsScreen(
    onExportLogs: (Boolean, String?) -> Unit,
    onNavigateBack: () -> Unit
) {
    var isExporting by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Export logs when screen opens
        isExporting = true
        LogExporter.exportLogs(this@MainActivity) { success, message ->
            isExporting = false
            onExportLogs(success, message)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Export Logs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isExporting) {
            DnsTestingLoader(
                message = "Collecting logs and debug information..."
            )
        }
    }
}
