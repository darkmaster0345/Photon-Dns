# Testing Guide for Photon DNS

## Running Database Migration Tests

The migration tests verify that the Room database schema changes preserve user data correctly.

### Prerequisites
- Android SDK configured (ANDROID_HOME environment variable)
- Java 17 JDK

### Run Migration Tests
```bash
./gradlew :app:connectedAndroidTest --tests "com.photondns.app.data.database.MigrationTest"
```

### Run All Tests
```bash
./gradlew :app:test
./gradlew :app:connectedAndroidTest
```

## Migration Details

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