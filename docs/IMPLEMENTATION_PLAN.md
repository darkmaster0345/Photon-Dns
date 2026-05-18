# Photon DNS Full Implementation Plan

## Goal
Transform the current demo-oriented app into a production-ready, F-Droid-submittable application.

## Current-state bug/risk analysis

### Build and release blockers
1. Missing Linux Gradle wrapper artifacts (`gradlew` missing originally; `gradle-wrapper.jar` still missing).
2. Build currently fails in restricted environments when Android/Google Maven cannot be reached.
3. Project previously depended on Java runtime auto-selection; AGP 8.2 requires Java 17 compatibility.

### Functional risks in DNS/VPN path
1. DNS query interception code needs instrumentation and stress testing for malformed packets.
2. VPN lifecycle handling should be tested against process death, boot restart, and connectivity changes.
3. Auto-switch logic should have unit tests for hysteresis and stability period edge cases.

### Data and persistence risks
1. Room migrations are not versioned for schema evolution.
2. No explicit retention policy for latency/speed history tables.
3. Settings defaults and validation boundaries should be enforced and tested.

### UI/UX risks
1. Screens are demo-oriented and need operational states (empty/error/loading) standardized.
2. Accessibility pass needed (content descriptions, contrast checks, tap target sizes).
3. Internationalization not implemented (single-language strings).

### F-Droid policy/readiness risks
1. F-Droid metadata was absent (added `.fdroid.yml` baseline in this change set).
2. Reproducible build proof path not documented in CI.
3. Privacy declaration in app and metadata should match implementation details.

## Execution plan

## Phase 1 — Build stabilization (critical path)
- Restore full wrapper (`gradlew`, `gradle-wrapper.jar`, properties).
- Pin Java 17 in local/CI/F-Droid build docs.
- Validate `assembleDebug`, `assembleRelease`, `testDebugUnitTest`, `lint`.

## Phase 2 — Correctness and safety
- Add unit tests for `DNSSwitchManager`, `DNSLatencyChecker`, parsing/pathological packet handling.
- Add integration tests for VPN startup/shutdown and foreground-service compliance.
- Add failure handling tests for no-network, all-server-timeout, and stale database data.

## Phase 3 — GUI hardening
- Improve state handling on Home/Monitor/Servers/SpeedTest screens.
- Add explicit error banners and retry actions.
- Add accessibility pass and semantic labels for graphs/cards/gauges.

## Phase 4 — Data lifecycle
- Add Room schema export and migration tests.
- Implement data retention settings and pruning worker.
- Add safeguards around DAO query sizes and index usage.

## Phase 5 — F-Droid submission completion
- Finalize `.fdroid.yml` maintainer fields.
- Verify no non-free blobs/dependencies.
- Add deterministic release instructions (versioning, changelog, tags).
- Produce signed tag and release notes for F-Droid inclusion request.

## Done criteria
- Clean build and test pass on Linux with Java 17.
- VPN monitoring verified on Android 8–14 device matrix.
- No critical crashes in 24h soak test.
- F-Droid metadata and reproducible build steps validated.
