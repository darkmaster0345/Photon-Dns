# Testing Guide for Photon DNS

## Running Database Migration Tests

The migration tests verify that the Room database schema changes preserve user data correctly.

### Prerequisites
- Android SDK configured (ANDROID_HOME environment variable)
- Java 17 JDK

2. **Start the VPN Service**
   - Open the app
   - Tap the "Start VPN" button on the main screen
   - The VPN status will change to "VPN Connected"
   - A VPN key icon will appear in your status bar

3. **Enable DNS Monitoring**
   - Toggle the "Auto Switch" option to enable automatic DNS monitoring
   - The app will start measuring DNS latency in the background
   - DNS queries will be intercepted and routed through the selected DNS server

### What Happens When VPN Starts:
- Creates a local VPN interface (`10.0.0.2`)
- Intercepts all DNS queries (port 53)
- Routes queries through the selected DNS server
- Starts latency monitoring service
- Begins automatic performance tracking

## 2. Debug Logs Verification

### DNS Query Interception Logs:
```
D/DNSVpnService: Forwarded DNS query with transaction ID: 12345
D/DNSVpnService: Sent DNS query to 8.8.8.8:53
D/DNSVpnService: Sent DNS response to client for transaction ID: 12345
```

### Latency Measurement Logs:
```
D/DNSLatencyChecker: Measured latency for 8.8.8.8: 45ms
D/DNSLatencyChecker: Latency test completed for 1.1.1.1: 32ms
D/DNSSwitchManager: Performance update: Google=45ms, Cloudflare=32ms
```

### Auto-Switching Logic Logs:
```
D/DNSSwitchManager: Current DNS (8.8.8.8) latency: 150ms
D/DNSSwitchManager: Faster DNS found: 1.1.1.1 (35ms)
D/DNSSwitchManager: Switching to faster DNS: 1.1.1.1
D/DNSVpnService: Changing DNS server from 8.8.8.8:53 to 1.1.1.1:53
```

## 3. Testing Edge Cases

### Internet Disconnection Test:
1. Disconnect from WiFi/mobile data
2. Expected behavior:
   ```
D/DNSSwitchManager: Network connectivity lost
D/DNSSwitchManager: Pausing DNS monitoring
   D/DNSVpnService: Network unavailable, pausing query forwarding
   ```
3. Reconnect network:
   ```
D/DNSSwitchManager: Network connectivity restored
D/DNSSwitchManager: Resuming DNS monitoring
   ```

### All DNS Servers Slow Test:
1. Simulate slow network conditions (network throttling)
2. Expected behavior:
   ```
D/DNSSwitchManager: All DNS servers above threshold (>100ms)
D/DNSSwitchManager: Using best available server: 8.8.8.8 (150ms)
D/DNSSwitchManager: Performance warning: All DNS servers slow
   ```

### VPN Service Crash Handling:
1. Force-stop the VPN service
2. Expected behavior:
   ```
   D/DNSVpnService: VPN established with DNS
   D/DNSSwitchManager: VPN disconnected, pausing monitoring
   D/MainActivity: VPN service failure detected
   ```

## 4. Manual Testing Commands

### Enable Debug Logging:
```bash
adb shell setprop log.tag.DNSVpnService DEBUG
adb shell setprop log.tag.DNSSwitchManager DEBUG
adb shell setprop log.tag.DNSLatencyChecker DEBUG
```

### Run All Tests
```bash
adb logcat -s DNSVpnService:DNSSwitchManager DNSLatencyChecker
```

### Test DNS Resolution:
```bash
# Test basic DNS resolution
nslookup google.com

# Test with specific DNS server
nslookup google.com 8.8.8.8
nslookup google.com 1.1.1.1
```

## 5. Performance Testing

### Load Testing:
1. Open multiple apps that make DNS queries
2. Browse websites with many external resources
3. Monitor for:
   - Memory usage stability
   - No memory leaks
   - Consistent performance under load

### Battery Impact Testing:
1. Run VPN for extended periods (1+ hours)
2. Monitor battery consumption
3. Check for excessive wake locks

## 6. Automated Testing

### Unit Tests:
- DNS latency measurement accuracy
- Auto-switching logic correctness
- Network error handling

### Integration Tests:
- VPN service lifecycle
- DNS query interception
- Service communication

## 7. Troubleshooting Common Issues

### VPN Won't Start:
- Check VPN permission is granted
- Verify Android version compatibility
- Check for conflicting VPN apps

### DNS Queries Not Intercepted:
- Verify VPN is connected
- Check DNS server configuration
- Monitor for packet processing errors

### High Latency Measurements:
- Check network connectivity
- Verify DNS server availability
- Monitor for network congestion

### Auto-Switching Not Working:
- Verify auto-switch is enabled
- Check switching threshold settings
- Monitor for performance comparison logic

## 8. Test Data Validation

### Expected DNS Servers:
- Google DNS: 8.8.8.8, 8.8.4.4
- Cloudflare DNS: 1.1.1.1, 1.0.0.1
- OpenDNS: 208.67.222.222, 208.67.220.220
- Quad9 DNS: 9.9.9.9, 149.112.112.112

### Expected Latency Ranges:
- Fast: < 50ms (green)
- Medium: 50-100ms (yellow)
- Slow: > 100ms (red)

## 8. Enhanced Auto-Switching Logic

Photon DNS now uses intelligent switching logic with specific thresholds:

### Switching Thresholds:
- **🚀 High Improvement (50ms+ faster)**: Switch after 2 consecutive checks (10 seconds)
- **⚡ Medium Improvement (20-49ms faster)**: Switch after 5 consecutive checks (25 seconds)  
- **📉 Low Improvement (<20ms faster)**: Don't switch (not worth disruption)

### Stability Period:
- **⏸️ 2-minute stability period** after any switch
- No new switches allowed during stability period
- Prevents rapid switching between similar-performing DNS servers

### Enhanced Switching Logic Flow:
```
DNS Performance Check → Calculate Improvement
    ↓
Is improvement ≥ 50ms? → HIGH_IMPROVEMENT → 2 consecutive checks → Switch
    ↓
Is improvement ≥ 20ms? → MEDIUM_IMPROVEMENT → 5 consecutive checks → Switch  
    ↓
Improvement < 20ms → LOW_IMPROVEMENT → No switch (reset counter)
    ↓
In stability period? → Skip all switch checks
```

### Debug Logs for Enhanced Switching:
```
D/DNSSwitchManager: 🔄 Enhanced Switch Analysis for cloudflare-dns:
D/DNSSwitchManager:    Current DNS (8.8.8.8): avg=125.3ms, median=120ms, samples=3
D/DNSSwitchManager:    Candidate DNS (cloudflare-dns): 65ms
D/DNSSwitchManager:    Improvement: 60.3ms
D/DNSSwitchManager: 🚀 HIGH IMPROVEMENT: 60.3ms >= 50ms
D/DNSSwitchManager:    Required consecutive checks: 2 (switch after 10s)
D/DNSSwitchManager: 📈 Consecutive better count for cloudflare-dns: 2/2
D/DNSSwitchManager: 🚀 SWITCH TRIGGERED: cloudflare-dns consistently better than 8.8.8.8
D/DNSSwitchManager: ✅ Enhanced DNS Switch completed: 8.8.8.8 -> cloudflare-dns
D/DNSSwitchManager:    Stability period activated: 120s
```

The `MIGRATION_1_2` handles upgrading from schema version 1 to version 2.

### Schema Changes in Version 2

**DNSServer table:**
- Added `protocol TEXT NOT NULL DEFAULT 'UDP'` - for UDP/DoH/DoT protocol support
- Added `dohUrl TEXT DEFAULT NULL` - DoH endpoint URL
- Added `dotHostname TEXT DEFAULT NULL` - DoT hostname

**SpeedTestResult table:**
- Added `bufferbloat INTEGER NOT NULL DEFAULT 0` - bufferbloat measurement
- Added `privacyScore INTEGER NOT NULL DEFAULT 100` - DNS privacy score

**SwitchReason enum:**
- Added PROTOCOL_UPGRADE, LATENCY_THRESHOLD, SECURITY_FILTER values
- No schema migration needed (stored as strings)

### Verifying Migrations

The migration test creates a version 1 database with sample data, runs the migration, and verifies:
1. All existing data is preserved
2. New columns exist with correct default values
3. The migration completes without errors