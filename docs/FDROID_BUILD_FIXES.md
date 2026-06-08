# F-Droid Build Fixes

## Summary of Changes

This document describes the fixes applied to resolve F-Droid blocking issues.

### 1. Removed local.properties from version control

**File:** `local.properties` (deleted)
**Issue:** The `local.properties` file containing `sdk.dir=/opt/android-sdk` was tracked in git. This breaks reproducible builds on F-Droid servers as each build environment has different SDK paths.
**Fix:** Removed from git tracking and added to `.gitignore` to prevent future commits.

### 2. Updated .gitignore

**File:** `.gitignore`
**Change:** Added `local.properties` to ignore list.
**Reason:** Prevents SDK path files from being committed, ensuring reproducible builds across different environments.

### 3. Fixed GitHub Actions workflow for Android SDK

**File:** `.github/workflows/build.yml`
**Issue:** The workflow was missing Android SDK installation, causing build failures.
**Fix:** Added `android-actions/setup-android@v3` step with API level 34 to properly install the Android SDK before building.

### 4. Increased Gradle wrapper network timeout

**File:** `gradle/wrapper/gradle-wrapper.properties`
**Change:** Increased `networkTimeout` from `10000` to `60000` (60 seconds).
**Reason:** The shorter timeout was causing download failures on slower connections during CI builds.

### 5. Added unit test dependencies

**File:** `app/build.gradle.kts`
**Change:** Added `mockk` and `kotlinx-coroutines-test` to `testImplementation`.
**Reason:** Enables proper unit testing without requiring Android instrumentation.

### 6. Added minimal unit tests

**Files:**
- `app/src/test/java/com/photondns/app/service/DNSSwitchManagerTest.kt`
- `app/src/test/java/com/photondns/app/service/DNSLatencyCheckerTest.kt`
- `app/src/test/java/com/photondns/app/service/SpeedTestManagerTest.kt`

**Purpose:** Basic test structure to ensure test infrastructure works. Tests use mocking to avoid Android dependencies.

## F-Droid Compatibility Status

### Verified Compatible
- ✅ No signing configs in release build (clean build, no `storeFile`/`storePassword` in buildTypes)
- ✅ `usesCleartextTraffic="false"` set in AndroidManifest.xml for HTTPS-only network traffic
- ✅ Java 17 source/target compatibility matching AGP 8.x
- ✅ ProGuard rules present and minimal
- ✅ `.fdroid.yml` output path correctly points to unsigned APK

### Requirements for F-Droid Server
- JDK 17 (configurable in workflow)
- Android SDK build tools for API 34
- No additional signing configuration required (F-Droid signs automatically)