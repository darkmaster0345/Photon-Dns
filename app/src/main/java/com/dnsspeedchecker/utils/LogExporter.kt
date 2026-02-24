package com.dnsspeedchecker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.content.FileProvider
import com.dnsspeedchecker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Utility class for exporting app logs and debug information
 */
class LogExporter private constructor() {
    
    companion object {
        private const val TAG = "LogExporter"
        private const val MAX_LOG_SIZE = 10 * 1024 * 1024 // 10MB
        private const val LOG_FILE_PREFIX = "dns_speed_checker_logs"
        private const val DEBUG_INFO_FILE = "debug_info.txt"
        
        /**
         * Exports all available logs and debug information
         */
        suspend fun exportLogs(context: Context, onResult: (Boolean, String?) -> Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val logFiles = mutableListOf<File>()
                    
                    // Collect app logs
                    val appLogFile = collectAppLogs(context)
                    if (appLogFile != null) {
                        logFiles.add(appLogFile)
                    }
                    
                    // Collect system logs related to the app
                    val systemLogFile = collectSystemLogs(context)
                    if (systemLogFile != null) {
                        logFiles.add(systemLogFile)
                    }
                    
                    // Create debug information file
                    val debugInfoFile = createDebugInfoFile(context)
                    logFiles.add(debugInfoFile)
                    
                    // Create zip file
                    val zipFile = createZipFile(context, logFiles)
                    
                    // Share the zip file
                    shareLogFile(context, zipFile, onResult)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to export logs", e)
                    onResult(false, e.message)
                }
            }
        }
        
        /**
         * Collects app-specific logs
         */
        private fun collectAppLogs(context: Context): File? {
            return try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val logFile = File(context.cacheDir, "${LOG_FILE_PREFIX}_app_$timestamp.log")
                
                // Get logs from logcat
                val process = ProcessBuilder("logcat", "-d", "-v", "time", context.packageName)
                    .redirectErrorStream(true)
                    .start()
                
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val writer = FileWriter(logFile)
                
                var line: String?
                var totalSize = 0
                while (reader.readLine().also { line = it } != null && totalSize < MAX_LOG_SIZE) {
                    writer.write("$line\n")
                    totalSize += line?.length ?: 0
                }
                
                writer.close()
                reader.close()
                process.destroy()
                
                logFile
            } catch (e: Exception) {
                Log.e(TAG, "Failed to collect app logs", e)
                null
            }
        }
        
        /**
         * Collects system logs
         */
        private fun collectSystemLogs(context: Context): File? {
            return try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val logFile = File(context.cacheDir, "${LOG_FILE_PREFIX}_system_$timestamp.log")
                
                // Get recent system logs
                val process = ProcessBuilder("logcat", "-d", "-v", "time", "-t", "500")
                    .redirectErrorStream(true)
                    .start()
                
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val writer = FileWriter(logFile)
                
                var line: String?
                var totalSize = 0
                while (reader.readLine().also { line = it } != null && totalSize < MAX_LOG_SIZE) {
                    writer.write("$line\n")
                    totalSize += line?.length ?: 0
                }
                
                writer.close()
                reader.close()
                process.destroy()
                
                logFile
            } catch (e: Exception) {
                Log.e(TAG, "Failed to collect system logs", e)
                null
            }
        }
        
        /**
         * Creates debug information file
         */
        private fun createDebugInfoFile(context: Context): File {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val debugFile = File(context.cacheDir, "${LOG_FILE_PREFIX}_debug_$timestamp.txt")
            
            val writer = FileWriter(debugFile)
            
            try {
                // App information
                writer.write("=== DNS Speed Checker Debug Information ===\n\n")
                writer.write("Generated: ${Date()}\n\n")
                
                // App version
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                writer.write("App Information:\n")
                writer.write("Package: ${context.packageName}\n")
                writer.write("Version Name: ${packageInfo.versionName}\n")
                writer.write("Version Code: ${packageInfo.longVersionCode}\n")
                writer.write("Target SDK: ${packageInfo.applicationInfo.targetSdkVersion}\n\n")
                
                // Device information
                writer.write("Device Information:\n")
                writer.write("Manufacturer: ${Build.MANUFACTURER}\n")
                writer.write("Model: ${Build.MODEL}\n")
                writer.write("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
                writer.write("Build ID: ${Build.ID}\n")
                writer.write("Build Type: ${Build.TYPE}\n")
                writer.write("Build Tags: ${Build.TAGS}\n\n")
                
                // Memory information
                val runtime = Runtime.getRuntime()
                val maxMemory = runtime.maxMemory() / (1024 * 1024)
                val totalMemory = runtime.totalMemory() / (1024 * 1024)
                val freeMemory = runtime.freeMemory() / (1024 * 1024)
                val usedMemory = totalMemory - freeMemory
                
                writer.write("Memory Information:\n")
                writer.write("Max Memory: ${maxMemory}MB\n")
                writer.write("Total Memory: ${totalMemory}MB\n")
                writer.write("Used Memory: ${usedMemory}MB\n")
                writer.write("Free Memory: ${freeMemory}MB\n\n")
                
                // Storage information
                writer.write("Storage Information:\n")
                try {
                    val cacheDir = context.cacheDir
                    val cacheSize = getDirectorySize(cacheDir) / (1024 * 1024)
                    writer.write("Cache Directory: ${cacheDir.absolutePath}\n")
                    writer.write("Cache Size: ${cacheSize}MB\n")
                    
                    val filesDir = context.filesDir
                    val filesSize = getDirectorySize(filesDir) / (1024 * 1024)
                    writer.write("Files Directory: ${filesDir.absolutePath}\n")
                    writer.write("Files Size: ${filesSize}MB\n")
                } catch (e: Exception) {
                    writer.write("Error getting storage info: ${e.message}\n")
                }
                writer.write("\n")
                
                // Permission status
                writer.write("Permission Status:\n")
                writer.write("VPN Permission: ${if (PermissionManager.isVpnPermissionGranted(context)) "Granted" else "Not Granted"}\n")
                writer.write("Notification Permission: ${if (PermissionManager.isNotificationPermissionGranted(context)) "Granted" else "Not Granted"}\n")
                writer.write("All Permissions Granted: ${if (PermissionManager.areAllPermissionsGranted(context)) "Yes" else "No"}\n\n")
                
                // Network information
                writer.write("Network Information:\n")
                try {
                    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                    val activeNetwork = connectivityManager.activeNetwork
                    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                    
                    if (capabilities != null) {
                        writer.write("Network Available: Yes\n")
                        writer.write("VPN Active: ${capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN)}\n")
                        writer.write("WiFi: ${capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)}\n")
                        writer.write("Mobile: ${capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)}\n")
                    } else {
                        writer.write("Network Available: No\n")
                    }
                } catch (e: Exception) {
                    writer.write("Error getting network info: ${e.message}\n")
                }
                writer.write("\n")
                
                // System properties
                writer.write("System Properties:\n")
                writer.write("ABI: ${Build.SUPPORTED_ABIS.joinToString(", ")}\n")
                writer.write("Locale: ${Locale.getDefault().toString()}\n")
                writer.write("Timezone: ${TimeZone.getDefault().id}\n")
                writer.write("24-Hour Format: ${android.text.format.DateFormat.is24HourFormat(context)}\n\n")
                
                // Recent errors (from logcat)
                writer.write("Recent Errors (last 20):\n")
                try {
                    val process = ProcessBuilder("logcat", "-d", "-v", "brief", "*:E", context.packageName)
                        .redirectErrorStream(true)
                        .start()
                    
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var errorCount = 0
                    
                    while (reader.readLine()?.also { line ->
                        if (line != null && errorCount < 20) {
                            writer.write("$line\n")
                            errorCount++
                        }
                    } != null && errorCount < 20) {
                        // Continue reading
                    }
                    
                    reader.close()
                    process.destroy()
                } catch (e: Exception) {
                    writer.write("Error getting recent errors: ${e.message}\n")
                }
                
                writer.write("\n=== End of Debug Information ===\n")
                
            } catch (e: Exception) {
                writer.write("Error creating debug info: ${e.message}\n")
            } finally {
                writer.close()
            }
            
            return debugFile
        }
        
        /**
         * Creates a zip file containing all log files
         */
        private fun createZipFile(context: Context, files: List<File>): File {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val zipFile = File(context.cacheDir, "${LOG_FILE_PREFIX}_$timestamp.zip")
            
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                files.forEach { file ->
                    if (file.exists()) {
                        val entry = ZipEntry(file.name)
                        entry.time = file.lastModified()
                        zos.putNextEntry(entry)
                        
                        FileInputStream(file).use { fis ->
                            fis.copyTo(zos)
                        }
                        
                        zos.closeEntry()
                    }
                }
            }
            
            return zipFile
        }
        
        /**
         * Shares the log file via intent
         */
        private fun shareLogFile(context: Context, file: File, onResult: (Boolean, String?) -> Unit) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "DNS Speed Checker Logs")
                    putExtra(Intent.EXTRA_TEXT, "Debug logs and information from DNS Speed Checker app")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = Intent.createChooser(intent, "Share DNS Speed Checker Logs")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                
                onResult(true, null)
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to share log file", e)
                onResult(false, e.message)
            }
        }
        
        /**
         * Gets the size of a directory
         */
        private fun getDirectorySize(dir: File): Long {
            var size = 0L
            if (dir.exists()) {
                dir.listFiles()?.forEach { file ->
                    size += if (file.isDirectory) {
                        getDirectorySize(file)
                    } else {
                        file.length()
                    }
                }
            }
            return size
        }
        
        /**
         * Clears old log files
         */
        fun clearOldLogs(context: Context) {
            try {
                val cacheDir = context.cacheDir
                cacheDir.listFiles { file ->
                    file.name.startsWith(LOG_FILE_PREFIX) && 
                    (System.currentTimeMillis() - file.lastModified()) > 7 * 24 * 60 * 60 * 1000 // 7 days old
                }?.forEach { file ->
                    file.deleteRecursively()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear old logs", e)
            }
        }
        
        /**
         * Gets log file count
         */
        fun getLogFileCount(context: Context): Int {
            return try {
                context.cacheDir.listFiles { file ->
                    file.name.startsWith(LOG_FILE_PREFIX)
                }?.size ?: 0
            } catch (e: Exception) {
                0
            }
        }
    }
}
