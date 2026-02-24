package com.dnsspeedchecker.service

import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import android.util.Log
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * Advanced VPN service for DNS interception and routing
 * 
 * This service creates a local VPN that intercepts all DNS queries and routes them
 * through the specified DNS server while allowing other traffic to pass through normally.
 */
class DnsVpnService : VpnService(), CoroutineScope {
    
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job
    
    // VPN components
    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnInput: FileInputStream? = null
    private var vpnOutput: FileOutputStream? = null
    private var isRunning = false
    
    // Crash detection and recovery
    private var lastHeartbeat = AtomicLong(System.currentTimeMillis())
    private var crashDetectionEnabled = AtomicBoolean(true)
    private val healthCheckInterval = 10000L // 10 seconds
    
    // DNS routing
    private var currentDnsServer: String = "8.8.8.8"
    private var dnsPort: Int = 53
    private val dnsSocketMap = ConcurrentHashMap<String, DatagramSocket>()
    
    // Packet routing
    private val pendingQueries = ConcurrentHashMap<Short, PendingQuery>()
    private var transactionId = 0x1000
    
    companion object {
        private const val TAG = "DnsVpnService"
        
        // VPN configuration
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"
        private const val VPN_ROUTE6 = "::" // IPv6 route
        private const val MTU = 1500
        private const val DNS_TIMEOUT_MS = 5000
        
        // Protocol constants
        private const val IPPROTO_UDP = 17
        private const val IPPROTO_TCP = 6
        private const val DNS_PORT = 53
        
        // IP header offsets
        private const val IP_VERSION_OFFSET = 0
        private const val IP_PROTOCOL_OFFSET = 9
        private const val IP_SOURCE_OFFSET = 12
        private const val IP_DEST_OFFSET = 16
        
        // UDP header offsets
        private const val UDP_SOURCE_OFFSET = 0
        private const val UDP_DEST_OFFSET = 2
        private const val UDP_LENGTH_OFFSET = 4
        private const val UDP_CHECKSUM_OFFSET = 6
        
        // DNS header offsets
        private const val DNS_ID_OFFSET = 0
        private const val DNS_FLAGS_OFFSET = 2
        private const val DNS_QDCOUNT_OFFSET = 4
    }
    
    data class PendingQuery(
        val transactionId: Short,
        val clientAddress: InetAddress,
        val clientPort: Int,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            "START_VPN" -> {
                startVpn()
                START_STICKY
            }
            "STOP_VPN" -> {
                stopVpn()
                START_NOT_STICKY
            }
            "CHANGE_DNS" -> {
                val newDns = intent.getStringExtra("dns_server")
                val newPort = intent.getIntExtra("dns_port", 53)
                if (newDns != null) {
                    changeDnsServer(newDns, newPort)
                }
                START_STICKY
            }
            "GET_STATUS" -> {
                broadcastVpnStatus()
                START_STICKY
            }
            else -> START_NOT_STICKY
        }
    }
    
    /**
     * Starts the VPN service and begins DNS interception
     */
    private fun startVpn() {
        if (isRunning) {
            Log.w(TAG, "VPN is already running")
            return
        }
        
        try {
            Log.i(TAG, "Starting DNS VPN service...")
            
            // Build VPN configuration
            val builder = Builder()
                .setSession("DNS Speed Checker")
                .addAddress(VPN_ADDRESS, 24)
                .addRoute(VPN_ROUTE, 0)
                .addDnsServer(currentDnsServer)
                .setMtu(MTU)
                .setBlocking(true)
                .allowFamily(OsConstants.AF_INET) // IPv4
                .allowFamily(OsConstants.AF_INET6) // IPv6
            
            // Add DNS server as route to ensure DNS queries go through VPN
            builder.addRoute(currentDnsServer, 32)
            
            // Establish VPN interface
            vpnInterface = builder.establish()
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface")
                return
            }
            
            // Initialize streams
            vpnInput = FileInputStream(vpnInterface!!.fileDescriptor)
            vpnOutput = FileOutputStream(vpnInterface!!.fileDescriptor)
            
            isRunning = true
            
            // Start packet processing
            launchPacketProcessing()
            
            // Start DNS response handler
            launchDnsResponseHandler()
            
            // Start cleanup routine
            launchCleanupRoutine()
            
            // Start health monitoring
            launchHealthMonitoring()
            
            Log.i(TAG, "VPN started successfully with DNS server: $currentDnsServer")
            broadcastVpnStatus(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VPN", e)
            stopVpn()
            broadcastVpnStatus(false, e.message)
        }
    }
    
    /**
     * Stops the VPN service
     */
    private fun stopVpn() {
        if (!isRunning) {
            return
        }
        
        Log.i(TAG, "Stopping VPN service...")
        isRunning = false
        
        // Cancel all coroutines
        job.cancelChildren()
        
        // Disable crash detection
        crashDetectionEnabled.set(false)
        
        // Close sockets
        dnsSocketMap.values.forEach { it.close() }
        dnsSocketMap.clear()
        
        // Close VPN interface
        try {
            vpnOutput?.close()
            vpnInput?.close()
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        } finally {
            vpnOutput = null
            vpnInput = null
            vpnInterface = null
        }
        
        // Clear pending queries
        pendingQueries.clear()
        
        Log.i(TAG, "VPN stopped")
        broadcastVpnStatus(false)
    }
    
    /**
     * Changes the DNS server used by the VPN
     */
    private fun changeDnsServer(newDnsServer: String, newPort: Int = 53) {
        if (currentDnsServer == newDnsServer && dnsPort == newPort) {
            return
        }
        
        Log.i(TAG, "Changing DNS server from $currentDnsServer:$dnsPort to $newDnsServer:$newPort")
        
        currentDnsServer = newDnsServer
        dnsPort = newPort
        
        // Close existing DNS sockets
        dnsSocketMap.values.forEach { it.close() }
        dnsSocketMap.clear()
        
        // If VPN is running, restart it to apply new DNS settings
        if (isRunning) {
            stopVpn()
            startVpn()
        }
    }
    
    /**
     * Main packet processing loop
     */
    private fun launchPacketProcessing() {
        launch {
            val buffer = ByteBuffer.allocate(MTU)
            
            while (isRunning) {
                try {
                    // Read packet from VPN interface
                    val bytesRead = vpnInput?.read(buffer.array()) ?: -1
                    
                    if (bytesRead > 0) {
                        processPacket(buffer.array(), bytesRead)
                        buffer.clear()
                    }
                    
                } catch (e: Exception) {
                    if (isRunning) {
                        Log.e(TAG, "Error in packet processing loop", e)
                        delay(100) // Brief delay before retrying
                    }
                }
            }
        }
    }
    
    /**
     * Processes incoming IP packets
     */
    private fun processPacket(packet: ByteArray, length: Int) {
        try {
            // Parse IP header
            if (length < 20) return // Minimum IP header size
            
            val ipVersion = (packet[IP_VERSION_OFFSET].toInt() ushr 4) and 0x0F
            val protocol = packet[IP_PROTOCOL_OFFSET].toInt() and 0xFF
            
            when (protocol) {
                IPPROTO_UDP -> processUdpPacket(packet, length)
                IPPROTO_TCP -> processTcpPacket(packet, length)
                else -> {
                    // For other protocols, we might want to allow them through
                    // or drop them depending on requirements
                    Log.d(TAG, "Dropping unsupported protocol: $protocol")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing packet", e)
        }
    }
    
    /**
     * Processes UDP packets (handles DNS queries)
     */
    private fun processUdpPacket(packet: ByteArray, length: Int) {
        try {
            // Skip IP header (variable length)
            val ipHeaderLength = ((packet[0].toInt() and 0x0F) * 4)
            if (length < ipHeaderLength + 8) return // Minimum UDP header
            
            // Parse UDP header
            val udpHeaderOffset = ipHeaderLength
            val srcPort = ((packet[udpHeaderOffset + UDP_SOURCE_OFFSET].toInt() and 0xFF) shl 8) or 
                          (packet[udpHeaderOffset + UDP_SOURCE_OFFSET + 1].toInt() and 0xFF)
            val destPort = ((packet[udpHeaderOffset + UDP_DEST_OFFSET].toInt() and 0xFF) shl 8) or 
                           (packet[udpHeaderOffset + UDP_DEST_OFFSET + 1].toInt() and 0xFF)
            
            when (destPort) {
                DNS_PORT -> {
                    // This is a DNS query, intercept and forward
                    handleDnsQuery(packet, length, ipHeaderLength, srcPort)
                }
                else -> {
                    // Non-DNS UDP traffic, allow through (or implement routing as needed)
                    Log.d(TAG, "Allowing UDP traffic to port $destPort")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing UDP packet", e)
        }
    }
    
    /**
     * Processes TCP packets
     */
    private fun processTcpPacket(packet: ByteArray, length: Int) {
        // For now, we'll allow TCP traffic to pass through
        // In a more complete implementation, you might want to handle TCP DNS queries
        Log.d(TAG, "Allowing TCP traffic")
    }
    
    /**
     * Handles DNS query interception and forwarding
     */
    private fun handleDnsQuery(packet: ByteArray, length: Int, ipHeaderLength: Int, srcPort: Int) {
        try {
            // Extract DNS payload
            val udpHeaderOffset = ipHeaderLength
            val udpHeaderLength = 8
            val dnsOffset = udpHeaderOffset + udpHeaderLength
            val dnsLength = length - dnsOffset
            
            if (dnsLength < 12) {
                Log.w(TAG, "DNS query too short: $dnsLength bytes")
                return
            }
            
            // Parse DNS header
            val dnsBuffer = ByteBuffer.wrap(packet, dnsOffset, dnsLength)
            val transactionId = dnsBuffer.getShort(DNS_ID_OFFSET)
            val flags = dnsBuffer.getShort(DNS_FLAGS_OFFSET)
            val questions = dnsBuffer.getShort(DNS_QDCOUNT_OFFSET).toInt() and 0xFFFF
            
            // Extract domain name for debugging
            val domainName = extractDomainName(packet, dnsOffset + 12)
            
            Log.d(TAG, "🔍 DNS Query Intercepted: ID=$transactionId, Domain=$domainName, Questions=$questions")
            
            // Only handle standard queries
            if ((flags.toInt() and 0x8000) != 0) {
                Log.d(TAG, "Ignoring DNS response packet: ID=$transactionId")
                return
            }
            
            if (questions == 0) {
                Log.w(TAG, "DNS packet with no questions: ID=$transactionId")
                return
            }
            
            // Store pending query information
            val clientAddress = getDestinationAddress(packet, ipHeaderLength)
            val pendingQuery = PendingQuery(transactionId, clientAddress, srcPort)
            pendingQueries[transactionId] = pendingQuery
            
            Log.d(TAG, "📝 Pending Query Stored: ID=$transactionId, Client=${clientAddress.hostAddress}:$srcPort")
            
            // Forward DNS query to configured DNS server
            forwardDnsQuery(packet, dnsOffset, dnsLength, transactionId)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling DNS query", e)
        }
    }
    
    /**
     * Forwards DNS query to the configured DNS server
     */
    private fun forwardDnsQuery(packet: ByteArray, dnsOffset: Int, dnsLength: Int, transactionId: Short) {
        launch {
            val startTime = System.currentTimeMillis()
            try {
                // Get or create DNS socket
                val dnsSocket = getDnsSocket()
                
                // Extract DNS query
                val dnsQuery = packet.copyOfRange(dnsOffset, dnsOffset + dnsLength)
                
                // Send to DNS server
                val dnsAddress = InetAddress.getByName(currentDnsServer)
                val dnsPacket = DatagramPacket(
                    dnsQuery, dnsQuery.size, dnsAddress, dnsPort
                )
                dnsSocket.send(dnsPacket)
                
                val sendTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "📤 DNS Query Forwarded: ID=$transactionId -> $currentDnsServer:$dnsPort (${sendTime}ms)")
                
                // Track forwarding time for performance analysis
                trackQueryForwardingTime(transactionId, sendTime)
                
            } catch (e: Exception) {
                val errorTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "❌ Failed to forward DNS query: ID=$transactionId (${errorTime}ms)", e)
                pendingQueries.remove(transactionId)
            }
        }
    }
    
    /**
     * Handles DNS responses from the DNS server
     */
    private fun launchDnsResponseHandler() {
        launch {
            while (isRunning) {
                try {
                    dnsSocketMap.values.forEach { socket ->
                        try {
                            val buffer = ByteArray(1024)
                            val packet = DatagramPacket(buffer, buffer.size)
                            
                            socket.soTimeout = 1000 // 1 second timeout
                            socket.receive(packet)
                            
                            handleDnsResponse(packet.data, packet.length, packet.address, packet.port)
                            
                        } catch (e: SocketTimeoutException) {
                            // Timeout is expected, continue
                        } catch (e: Exception) {
                            Log.e(TAG, "Error receiving DNS response", e)
                        }
                    }
                    
                    delay(100) // Brief delay between checks
                    
                } catch (e: Exception) {
                    if (isRunning) {
                        Log.e(TAG, "Error in DNS response handler", e)
                        delay(1000)
                    }
                }
            }
        }
    }
    
    /**
     * Handles DNS responses and routes them back to the client
     */
    private fun handleDnsResponse(data: ByteArray, length: Int, fromAddress: InetAddress, fromPort: Int) {
        try {
            if (length < 12) {
                Log.w(TAG, "DNS response too short: $length bytes")
                return
            }
            
            // Parse DNS response header
            val dnsBuffer = ByteBuffer.wrap(data, 0, length)
            val transactionId = dnsBuffer.getShort(DNS_ID_OFFSET)
            val flags = dnsBuffer.getShort(DNS_FLAGS_OFFSET)
            val answerCount = dnsBuffer.getShort(6).toInt() and 0xFFFF
            
            // Find pending query
            val pendingQuery = pendingQueries.remove(transactionId)
            if (pendingQuery == null) {
                Log.w(TAG, "⚠️ Received DNS response for unknown query: ID=$transactionId")
                return
            }
            
            // Calculate total response time
            val totalTime = System.currentTimeMillis() - pendingQuery.timestamp
            val isSuccess = (flags.toInt() and 0x8000) != 0 && (flags.toInt() and 0x000F) == 0
            
            Log.d(TAG, "📥 DNS Response Received: ID=$transactionId, Success=$isSuccess, Answers=$answerCount, TotalTime=${totalTime}ms")
            
            // Create IP packet for response
            val responsePacket = createDnsResponsePacket(data, length, pendingQuery)
            
            // Send response back to client
            vpnOutput?.write(responsePacket)
            vpnOutput?.flush()
            
            Log.d(TAG, "✅ DNS Response Sent: ID=$transactionId -> ${pendingQuery.clientAddress.hostAddress}:${pendingQuery.clientPort}")
            
            // Track performance metrics
            trackDnsResponsePerformance(transactionId, totalTime, isSuccess, answerCount)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling DNS response", e)
        }
    }
    
    /**
     * Creates a DNS response packet to send back to the client
     */
    private fun createDnsResponsePacket(dnsResponse: ByteArray, dnsLength: Int, pendingQuery: PendingQuery): ByteArray {
        // Create IP header
        val ipHeader = createIpHeader(
            sourceAddress = currentDnsServer,
            destAddress = pendingQuery.clientAddress.hostAddress,
            protocol = IPPROTO_UDP,
            payloadLength = dnsLength + 8 // UDP header + DNS payload
        )
        
        // Create UDP header
        val udpHeader = createUdpHeader(
            srcPort = dnsPort,
            destPort = pendingQuery.clientPort,
            payloadLength = dnsLength
        )
        
        // Combine headers and payload
        return ipHeader + udpHeader + dnsResponse.copyOf(dnsLength)
    }
    
    /**
     * Creates an IP header for the response packet
     */
    private fun createIpHeader(
        sourceAddress: String,
        destAddress: String,
        protocol: Int,
        payloadLength: Int
    ): ByteArray {
        val buffer = ByteBuffer.allocate(20) // IPv4 header without options
        
        // Version (4 bits) + IHL (4 bits)
        buffer.put((0x45).toByte())
        
        // Type of Service
        buffer.put(0)
        
        // Total Length
        buffer.putShort((20 + payloadLength).toShort())
        
        // Identification
        buffer.putShort(0)
        
        // Flags and Fragment Offset
        buffer.putShort(0x4000) // Don't fragment
        
        // TTL
        buffer.put(64)
        
        // Protocol
        buffer.put(protocol.toByte())
        
        // Header Checksum (will be calculated later)
        buffer.putShort(0)
        
        // Source Address
        val srcAddr = InetAddress.getByName(sourceAddress).address
        buffer.put(srcAddr)
        
        // Destination Address
        val destAddr = InetAddress.getByName(destAddress).address
        buffer.put(destAddr)
        
        // Calculate and set checksum
        val checksum = calculateIpHeaderChecksum(buffer.array())
        buffer.putShort(10, checksum.toShort())
        
        return buffer.array()
    }
    
    /**
     * Creates a UDP header for the response packet
     */
    private fun createUdpHeader(srcPort: Int, destPort: Int, payloadLength: Int): ByteArray {
        val buffer = ByteBuffer.allocate(8)
        
        // Source Port
        buffer.putShort(srcPort.toShort())
        
        // Destination Port
        buffer.putShort(destPort.toShort())
        
        // Length
        buffer.putShort((8 + payloadLength).toShort())
        
        // Checksum (optional for UDP, set to 0)
        buffer.putShort(0)
        
        return buffer.array()
    }
    
    /**
     * Calculates IP header checksum
     */
    private fun calculateIpHeaderChecksum(header: ByteArray): Short {
        var sum = 0
        
        // Sum all 16-bit words
        for (i in header.indices step 2) {
            val word = ((header[i].toInt() and 0xFF) shl 8) or (header[i + 1].toInt() and 0xFF)
            sum += word
        }
        
        // Add carry
        sum = (sum and 0xFFFF) + (sum ushr 16)
        
        // One's complement
        return (sum.inv() and 0xFFFF).toShort()
    }
    
    /**
     * Gets or creates a DNS socket for the current DNS server
     */
    private fun getDnsSocket(): DatagramSocket {
        return dnsSocketMap.getOrPut(currentDnsServer) {
            val socket = DatagramSocket()
            socket.soTimeout = DNS_TIMEOUT_MS
            socket
        }
    }
    
    /**
     * Extracts destination address from IP packet
     */
    private fun getDestinationAddress(packet: ByteArray, ipHeaderLength: Int): InetAddress {
        val destOffset = ipHeaderLength + IP_DEST_OFFSET
        val destBytes = packet.copyOfRange(destOffset, destOffset + 4)
        return InetAddress.getByAddress(destBytes)
    }
    
    /**
     * Cleanup routine for expired pending queries
     */
    private fun launchCleanupRoutine() {
        launch {
            while (isRunning) {
                try {
                    val currentTime = System.currentTimeMillis()
                    val expiredQueries = pendingQueries.filter { (_, query) ->
                        currentTime - query.timestamp > DNS_TIMEOUT_MS
                    }
                    
                    expiredQueries.forEach { (transactionId, _) ->
                        pendingQueries.remove(transactionId)
                        Log.d(TAG, "Removed expired DNS query: $transactionId")
                    }
                    
                    delay(5000) // Check every 5 seconds
                    
                } catch (e: Exception) {
                    if (isRunning) {
                        Log.e(TAG, "Error in cleanup routine", e)
                    }
                }
            }
        }
    }
    
    /**
     * Extracts domain name from DNS query packet
     */
    private fun extractDomainName(packet: ByteArray, offset: Int): String {
        return try {
            val domainParts = mutableListOf<String>()
            var currentOffset = offset
            var length: Int
            
            while (true) {
                length = packet[currentOffset].toInt() and 0xFF
                if (length == 0) break
                
                if (length > 63) {
                    // Compression pointer, not handling in this simple implementation
                    return "[compressed]"
                }
                
                val part = String(packet, currentOffset + 1, length)
                domainParts.add(part)
                currentOffset += length + 1
                
                if (currentOffset >= packet.size) break
            }
            
            domainParts.joinToString(".")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract domain name", e)
            "[unknown]"
        }
    }
    
    /**
     * Tracks DNS query forwarding performance
     */
    private fun trackQueryForwardingTime(transactionId: Short, forwardTime: Long) {
        // Log forwarding performance for analysis
        if (forwardTime > 100) {
            Log.w(TAG, "⚠️ Slow DNS forwarding: ID=$transactionId took ${forwardTime}ms")
        }
    }
    
    /**
     * Tracks DNS response performance metrics
     */
    private fun trackDnsResponsePerformance(transactionId: Short, totalTime: Long, isSuccess: Boolean, answerCount: Int) {
        // Broadcast performance data for monitoring service
        val intent = Intent("com.dnsspeedchecker.DNS_PERFORMANCE_UPDATE").apply {
            putExtra("transaction_id", transactionId.toInt())
            putExtra("total_time", totalTime)
            putExtra("success", isSuccess)
            putExtra("answer_count", answerCount)
            putExtra("dns_server", currentDnsServer)
        }
        sendBroadcast(intent)
        
        // Log performance warnings
        if (!isSuccess) {
            Log.w(TAG, "⚠️ DNS query failed: ID=$transactionId")
        }
        if (totalTime > 1000) {
            Log.w(TAG, "⚠️ Slow DNS response: ID=$transactionId took ${totalTime}ms")
        }
    }
    
    /**
     * Launches health monitoring for crash detection
     */
    private fun launchHealthMonitoring() {
        launch {
            while (isRunning && crashDetectionEnabled.get()) {
                try {
                    delay(healthCheckInterval)
                    
                    // Update heartbeat
                    lastHeartbeat.set(System.currentTimeMillis())
                    
                    // Perform health checks
                    val isHealthy = performHealthCheck()
                    
                    if (!isHealthy) {
                        Log.w(TAG, "⚠️ VPN service health check failed")
                        broadcastVpnStatus(false, "Health check failed")
                    } else {
                        Log.d(TAG, "✅ VPN service health check passed")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error in health monitoring", e)
                    // Don't stop the service, just log the error
                }
            }
        }
    }
    
    /**
     * Performs basic health checks on the VPN service
     */
    private fun performHealthCheck(): Boolean {
        return try {
            // Check VPN interface status
            val interfaceValid = vpnInterface?.fileDescriptor?.valid() ?: false
            
            // Check socket connectivity
            val socketsHealthy = dnsSocketMap.values.all { socket ->
                socket.isClosed.not() && socket.isBound
            }
            
            // Check pending queries count (shouldn't be too high)
            val pendingQueriesCount = pendingQueries.size
            val queriesHealthy = pendingQueriesCount < 100 // Arbitrary threshold
            
            // Check memory usage (basic check)
            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
            val memoryHealthy = usedMemory < 200 // Less than 200MB
            
            val overallHealth = interfaceValid && socketsHealthy && queriesHealthy && memoryHealthy
            
            Log.d(TAG, "🏥 Health Check Results:")
            Log.d(TAG, "   Interface: $interfaceValid")
            Log.d(TAG, "   Sockets: $socketsHealthy (${dnsSocketMap.size} active)")
            Log.d(TAG, "   Pending Queries: $queriesHealthy ($pendingQueriesCount pending)")
            Log.d(TAG, "   Memory: $memoryHealthy (${usedMemory}MB)")
            Log.d(TAG, "   Overall: $overallHealth")
            
            overallHealth
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Health check exception", e)
            false
        }
    }
    
    /**
     * Broadcasts VPN status to interested components
     */
    private fun broadcastVpnStatus(isConnected: Boolean = isRunning, error: String? = null) {
        val intent = Intent("com.dnsspeedchecker.VPN_STATUS_UPDATE").apply {
            putExtra("is_connected", isConnected)
            putExtra("dns_server", currentDnsServer)
            putExtra("error", error)
        }
        sendBroadcast(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        job.cancel()
    }
}
