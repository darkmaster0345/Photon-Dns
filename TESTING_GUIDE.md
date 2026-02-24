# DNS Speed Checker - Testing Guide

## 1. Enabling VPN Service and Starting DNS Monitoring

### Step-by-Step Instructions:

1. **Grant VPN Permission**
   - When you first tap "Start VPN", Android will show a system dialog
   - Tap "OK" to grant VPN permission
   - This is a one-time permission required for all VPN apps

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
D/DnsVpnService: Forwarded DNS query with transaction ID: 12345
D/DnsVpnService: Sent DNS query to 8.8.8.8:53
D/DnsVpnService: Sent DNS response to client for transaction ID: 12345
```

### Latency Measurement Logs:
```
D/DnsLatencyChecker: Measured latency for 8.8.8.8: 45ms
D/DnsLatencyChecker: Latency test completed for 1.1.1.1: 32ms
D/DnsMonitoringService: Performance update: Google=45ms, Cloudflare=32ms
```

### Auto-Switching Logic Logs:
```
D/DnsMonitoringService: Current DNS (8.8.8.8) latency: 150ms
D/DnsMonitoringService: Faster DNS found: 1.1.1.1 (35ms)
D/DnsMonitoringService: Switching to faster DNS: 1.1.1.1
D/DnsVpnService: Changing DNS server from 8.8.8.8:53 to 1.1.1.1:53
```

## 3. Testing Edge Cases

### Internet Disconnection Test:
1. Disconnect from WiFi/mobile data
2. Expected behavior:
   ```
   D/DnsMonitoringService: Network connectivity lost
   D/DnsMonitoringService: Pausing DNS monitoring
   D/DnsVpnService: Network unavailable, pausing query forwarding
   ```
3. Reconnect network:
   ```
   D/DnsMonitoringService: Network connectivity restored
   D/DnsMonitoringService: Resuming DNS monitoring
   ```

### All DNS Servers Slow Test:
1. Simulate slow network conditions (network throttling)
2. Expected behavior:
   ```
   D/DnsMonitoringService: All DNS servers above threshold (>100ms)
   D/DnsMonitoringService: Using best available server: 8.8.8.8 (150ms)
   D/DnsMonitoringService: Performance warning: All DNS servers slow
   ```

### VPN Service Crash Handling:
1. Force-stop the VPN service
2. Expected behavior:
   ```
   D/DnsVpnService: VPN service crashed, attempting restart
   D/DnsMonitoringService: VPN disconnected, pausing monitoring
   D/MainActivity: VPN service failure detected
   ```

## 4. Manual Testing Commands

### Enable Debug Logging:
```bash
adb shell setprop log.tag.DnsVpnService DEBUG
adb shell setprop log.tag.DnsMonitoringService DEBUG
adb shell setprop log.tag.DnsLatencyChecker DEBUG
```

### Monitor Logs:
```bash
adb logcat -s DnsVpnService:DnsMonitoringService:DnsLatencyChecker
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

The DNS speed checker now uses intelligent switching logic with specific thresholds:

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
D/OptimizedDnsMonitoringService: 🔄 Enhanced Switch Analysis for cloudflare-dns:
D/OptimizedDnsMonitoringService:    Current DNS (8.8.8.8): avg=125.3ms, median=120ms, samples=3
D/OptimizedDnsMonitoringService:    Candidate DNS (cloudflare-dns): 65ms
D/OptimizedDnsMonitoringService:    Improvement: 60.3ms
D/OptimizedDnsMonitoringService: 🚀 HIGH IMPROVEMENT: 60.3ms >= 50ms
D/OptimizedDnsMonitoringService:    Required consecutive checks: 2 (switch after 10s)
D/OptimizedDnsMonitoringService: 📈 Consecutive better count for cloudflare-dns: 2/2
D/OptimizedDnsMonitoringService: 🚀 SWITCH TRIGGERED: cloudflare-dns consistently better than 8.8.8.8
D/OptimizedDnsMonitoringService: ✅ Enhanced DNS Switch completed: 8.8.8.8 -> cloudflare-dns
D/OptimizedDnsMonitoringService:    Stability period activated: 120s
```

### Testing Enhanced Switching:

1. **Test High Improvement Switch**:
   - Use network throttling to make one DNS server 50ms+ slower
   - Verify switch after 2 consecutive checks (10 seconds)
   - Check logs for "HIGH_IMPROVEMENT" and "SWITCH TRIGGERED"

2. **Test Medium Improvement Switch**:
   - Make one DNS server 20-49ms slower
   - Verify switch after 5 consecutive checks (25 seconds)
   - Check logs for "MEDIUM_IMPROVEMENT"

3. **Test Low Improvement No Switch**:
   - Make one DNS server <20ms slower
   - Verify no switch occurs
   - Check logs for "LOW_IMPROVEMENT" and "Not worth disruption"

4. **Test Stability Period**:
   - Trigger a switch
   - Immediately make another DNS server much faster
   - Verify no switch occurs for 2 minutes
   - Check logs for "In stability period"

5. **Test Stability Period Expiry**:
   - Wait 2+ minutes after a switch
   - Make another DNS server significantly faster
   - Verify switching resumes normally
   - Check logs for stability period ending

### Expected Broadcast Events:
- `DNS_SWITCH_EVENT` with enhanced metadata:
  - `stability_period_activated`: true
  - `stability_period_duration`: 120000 (2 minutes)
  - `switch_decision`: "HIGH_IMPROVEMENT" or "MEDIUM_IMPROVEMENT"
