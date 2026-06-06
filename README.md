# Photon DNS

**DNS at the speed of light** ⚡

A comprehensive Android application that monitors DNS server performance and automatically switches to the fastest DNS server using VPN technology for DNS interception.

## 🚀 Features

### Core Functionality
- **VPN Service for DNS Interception**: Local VPN service that handles DNS queries without requiring root access
- **Real-time DNS Speed Testing**: Continuously monitors latency of multiple DNS servers
- **Smart Auto-Switching**: Automatically switches to faster DNS servers based on performance history
- **Network Speed Testing**: Complete speed test with download, upload, ping, jitter, and packet loss metrics
- **Background Monitoring**: Foreground service that survives app closure with battery optimization

### Supported DNS Servers
- Google DNS (8.8.8.8, 8.8.4.4)
- Cloudflare DNS (1.1.1.1, 1.0.0.1)
- Quad9 DNS (9.9.9.9, 149.112.112.112)
- OpenDNS (208.67.222.222, 208.67.220.220)

### User Interface (5 Tabs)
- **🏠 Home**: Large latency display, glowing orb, current DNS card, quick metrics
- **📊 Monitor**: Real-time latency graph, DNS switch history, server performance stats
- **⚡ Speed Test**: Speedometer gauge, comprehensive metrics, test history, share results
- **🌐 Servers**: Searchable server list, fastest servers, custom server support
- **⚙️ Settings**: Auto-switch toggle, strategy selector, advanced options

### Smart Switching Logic
- Three preset strategies: Conservative, Balanced, Aggressive
- Custom strategy with configurable parameters
- Hysteresis to prevent flip-flopping
- Configurable minimum improvement threshold
- Consecutive checks requirement
- Stability period enforcement for reliable switching
- Error retry UI with automatic recovery
- Boot auto-reconnect to restore VPN on device restart
- Room database migrations for persistent data
- Custom strategy editor with full parameter control

## 🛠 Technical Specifications

### Project Setup
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34
- **Build System**: Gradle with Kotlin DSL
- **License**: MIT (FOSS)

### Architecture
- **MVVM Architecture**: Model-View-ViewModel pattern
- **Repository Pattern**: Clean separation of data sources
- **Room Database**: Local data persistence
- **DataStore**: Settings and preferences storage
- **Coroutines + Flow**: Asynchronous operations
- **Dependency Injection**: Hilt
- **Navigation**: Jetpack Navigation Compose

### Key Components
- `DNSVpnService`: VPN service for DNS interception
- `DNSLatencyChecker`: DNS performance monitoring
- `SpeedTestManager`: Comprehensive speed testing
- `DNSSwitchManager`: Smart auto-switching algorithm
- `PhotonDatabase`: Room database with DAOs
- Custom UI Components: GlowingOrb, SpeedometerGauge, LatencyGraph

### Design System
- **Theme**: AMOLED dark (#0A0A0A background) — dark mode only, optimized for OLED displays
- **Primary**: Cyan (#00E5CC)
- **Accent**: Green (#00D9A3)
- **Material 3**: Modern design system
- **Light/Dark theme toggle**: Planned for a future release to complement the current dark-only theme

## 📱 Installation

### From Source

1. Clone the repository:
   ```bash
   git clone https://github.com/darkmaster0345/Photon-Dns.git
   cd Photon-Dns
   ```

2. Open in Android Studio

3. Build and run the application

4. Grant VPN permission when prompted

5. Grant notification permission for status updates

### From F-Droid

Photon DNS is also available on [F-Droid](https://f-droid.org) — the open-source app store for Android. The F-Droid build is fully reproducible and includes only open-source dependencies.

**Requirements for F-Droid build**:
- JDK 17
- Linux Gradle wrapper files (`gradlew`, `gradle/wrapper/gradle-wrapper.jar`)
- Increase `.fdroid.yml` `versionCode` before submitting a new release

**Local F-Droid validation**:
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
bash build.sh
```

See [`docs/FDROID_SUBMISSION.md`](docs/FDROID_SUBMISSION.md) for the full submission guide.

## 🎯 Usage

### Getting Started
1. **Start VPN**: Tap the VPN toggle on the Home screen to begin DNS monitoring
2. **Configure Settings**: Access Settings tab to customize switching strategies
3. **Monitor Performance**: View real-time latency data and automatic switching
4. **Run Speed Tests**: Use the Speed Test tab for comprehensive network analysis

### Auto-Switching Strategies
- **Conservative**: 10s checks, 30ms threshold, 5 consecutive, 3min stability
- **Balanced**: 5s checks, 20ms threshold, 4 consecutive, 2min stability
- **Aggressive**: 5s checks, 15ms threshold, 2 consecutive, 1min stability
- **Custom**: Fully configurable parameters

### Speed Test Features
- **Download/Upload Speed**: Multi-threaded testing up to 500 Mbps
- **Ping & Jitter**: Latency analysis with jitter calculation
- **Packet Loss**: Network reliability testing
- **History**: Graphical visualization of test results
- **Share**: Export and share test results

## 🔐 Permissions Required

- `INTERNET`: Network access for DNS queries and speed testing
- `BIND_VPN_SERVICE`: Create VPN service for DNS interception
- `FOREGROUND_SERVICE`: Run background monitoring service
- `POST_NOTIFICATIONS`: Show status notifications
- `ACCESS_NETWORK_STATE`: Monitor network connectivity

## ⚙️ Configuration Options

### Auto-Switching Settings
- **Check Interval**: 5-60 seconds
- **Minimum Improvement**: 10-100ms
- **Consecutive Checks**: 2-10 checks
- **Stability Period**: 1-10 minutes

### Advanced Options
- **Hysteresis**: Prevent rapid switching
- **Battery Saver**: Reduced monitoring frequency
- **Switch on Failure**: Immediate switch on DNS failure
- **Notifications**: Status update preferences

## 🔋 Battery Optimization

The app is designed with battery efficiency in mind:
- Efficient DNS query implementation
- Configurable check intervals
- Smart background processing
- Minimal resource usage
- Battery saver mode option

## 🔒 Security & Privacy

- No data collection or transmission
- Local processing only
- No internet access beyond DNS queries
- Secure settings storage with DataStore
- Open source and auditable

## 🎨 UI Components

### Custom Components
- **GlowingOrb**: Animated status indicator with pulse effect
- **SpeedometerGauge**: Analog speedometer for speed test results
- **LatencyGraph**: Real-time multi-line latency visualization
- **DNSServerCard**: Server information with flag icons and signal bars
- **SignalBars**: 5-bar signal strength indicator
- **MetricCard**: Reusable metric display component

### Visual Effects
- Glowing borders on active elements
- Glassmorphism cards with blur
- Pulse animations
- Smooth transitions
- Shimmer loading states

## 🚀 Future Enhancements

### Coming in v1.x
- [ ] Network condition awareness (WiFi vs cellular switching behavior)
- [ ] Integration with system DNS settings
- [ ] Multi-language support
- [ ] Custom DNS server support expanded (bulk upload)
- [ ] Profile-based auto-switch rules

### Planned for v2.0

- **Home screen widget**: Glowing orb widget showing current DNS server, latency, and VPN status. Quick toggle from home screen without opening the app.
- **Network profiles**: Preset configurations (Gaming/Low-Latency, Privacy/Encrypted-Only, Streaming/No-Buffer) that auto-adjust strategy + server filters.
- **DNS leak detection**: Built-in test that verifies all DNS queries actually route through the app's VPN vs bypassing to ISP.
- **Per-app DNS bypass**: Allow users to exclude specific apps from VPN routing (useful for banking apps that block VPNs).
- **Ad blocking at DNS level**: Optional blocklist integration (AdGuard, StevenBlack) to block ads/trackers via DNS.
- **DNS over QUIC (DoQ)**: Add DoQ as a third encrypted protocol alongside DoH and DoT, with automatic fallback.
- **Backup & restore**: Export/import all settings, custom servers, and history as a JSON file for device migration.
- **VPN uptime & routing stats**: Track total VPN uptime, bytes routed, queries intercepted — displayed on a dedicated stats screen.
- **Quick-switch notification action**: Directly switch servers from the notification shade without opening the app.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For issues and feature requests, please create an issue in the repository.

## 🌟 Acknowledgments

- Material Design 3 guidelines
- Jetpack Compose framework
- Android VPN Service API
- Open source DNS providers

---

**Photon DNS - DNS at the speed of light** ⚡

Made with ❤️ for better internet performance

## 📦 Distribution

- **GitHub Releases**: [latest release](https://github.com/darkmaster0345/Photon-Dns/releases)
- **F-Droid**: [available on F-Droid](https://f-droid.org) — builds are reproducible and verified with JDK 17
