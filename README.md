# DNS Speed Checker

A comprehensive Android application that monitors DNS server performance and automatically switches to the fastest DNS server using VPN technology for DNS interception.

## Features

### Core Functionality
- **VPN Service for DNS Interception**: Local VPN service that handles DNS queries without requiring root access
- **Real-time DNS Speed Testing**: Continuously monitors latency of multiple DNS servers
- **Smart Auto-Switching**: Automatically switches to faster DNS servers based on performance history
- **Background Monitoring**: Foreground service that survives app closure with battery optimization

### Supported DNS Servers
- Google DNS (8.8.8.8, 8.8.4.4)
- Cloudflare DNS (1.1.1.1, 1.0.0.1)
- Quad9 DNS (9.9.9.9, 149.112.112.112)
- OpenDNS (208.67.222.222, 208.67.220.220)

### User Interface
- **Main Screen**: Real-time display of current DNS server, latency graph, and server list
- **Settings Screen**: Configurable check intervals, switching thresholds, and server selection
- **Persistent Notification**: Shows current DNS status and latency in the notification bar

### Smart Switching Logic
- Tracks latency history (last 5 checks per server)
- Only switches if new server is consistently faster (3+ consecutive checks)
- Configurable minimum improvement threshold (10ms, 20ms, 50ms)
- User-configurable switching settings

## Technical Specifications

### Project Setup
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34
- **Build System**: Gradle with Kotlin DSL

### Architecture
- **MVVM Architecture**: Model-View-ViewModel pattern
- **Repository Pattern**: Clean separation of data sources
- **Room Database**: Local data persistence
- **DataStore**: Settings and preferences storage
- **Coroutines**: Asynchronous operations
- **Dependency Injection**: Manual DI with Application class

### Key Components
- `DnsVpnService`: VPN service for DNS interception
- `DnsMonitoringService`: Background service for continuous monitoring
- `MainViewModel`: UI state management
- `DnsRepository`: Data layer abstraction
- `SettingsRepository`: Settings persistence

## Installation

1. Clone the repository
2. Open in Android Studio
3. Build and run the application
4. Grant VPN permission when prompted
5. Grant notification permission for status updates

## Usage

1. **Start VPN**: Tap "Start VPN" to begin DNS monitoring
2. **Configure Settings**: Access settings to customize check intervals and thresholds
3. **Monitor Performance**: View real-time latency data and automatic switching
4. **Enable Auto-Switch**: Toggle automatic DNS switching based on performance

## Permissions Required

- `INTERNET`: Network access for DNS queries
- `BIND_VPN_SERVICE`: Create VPN service for DNS interception
- `FOREGROUND_SERVICE`: Run background monitoring service
- `POST_NOTIFICATIONS`: Show status notifications
- `ACCESS_NETWORK_STATE`: Monitor network connectivity

## Configuration Options

### Check Intervals
- 5 seconds (default)
- 10 seconds
- 30 seconds
- 60 seconds

### Switching Thresholds
- 10ms
- 20ms (default)
- 50ms

### DNS Server Selection
- Enable/disable specific DNS servers
- Custom server support (future enhancement)

## Battery Optimization

The app is designed with battery efficiency in mind:
- Efficient DNS query implementation
- Configurable check intervals
- Smart background processing
- Minimal resource usage

## Security & Privacy

- No data collection or transmission
- Local processing only
- No internet access beyond DNS queries
- Secure settings storage with DataStore

## Future Enhancements

- Custom DNS server support
- Historical data visualization
- Export performance reports
- Network condition awareness
- Integration with system DNS settings

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and feature requests, please create an issue in the repository.
