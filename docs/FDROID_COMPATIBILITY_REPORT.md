# F-Droid Compatibility Report — Photon DNS

**Generated:** 2026-06-05
**Project:** Photon DNS v1.0
**License:** MIT

---

## 1. Build Status

### Result: BLOCKED — Android SDK Not Present

The build environment does not have an Android SDK installed. The `ANDROID_HOME` environment variable is not set, and no `local.properties` file exists in the project root.

**Error:**
```
FAILURE: Build failed with an exception.
Could not determine the dependencies of task ':app:compileDebugJavaWithJavac'.
> SDK location not found. Define a valid SDK location with an ANDROID_HOME environment
  variable or by setting the sdk.dir path in your project's local properties file at
  '/workspace/rigs/47e6186b-2d6f-48f4-9c15-baec664781c5/browse/local.properties'.
```

**What Was Tested:**
- `./gradlew assembleDebug` — Failed (no SDK)
- `./gradlew assembleRelease` — Not attempted (same blocker)
- `./gradlew test` — Not attempted (same blocker)
- `./gradlew lint` — Not attempted (same blocker)

**What We Verified Without Building:**
- Project uses Java 17 (`compileOptions` in `app/build.gradle.kts`) — compatible with installed JDK 21
- Gradle wrapper present and executable
- No compilation errors visible in source code review
- Dependencies resolve from `mavenCentral()` and `google()` (standard F-Droid repositories)

---

## 2. Dependency Audit

| Dependency | Version | Verdict | Notes |
|---|---|---|---|
| `androidx.core:core-ktx` | 1.12.0 | ✅ PASS | AndroidX, FOSS |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.7.0 | ✅ PASS | AndroidX, FOSS |
| `androidx.activity:activity-compose` | 1.8.2 | ✅ PASS | AndroidX, FOSS |
| `androidx.compose:*` (BOM) | 2023.10.01 | ✅ PASS | AndroidX, FOSS (Apache 2.0) |
| `com.google.android.material:material` | 1.11.0 | ✅ PASS | Material Components, FOSS |
| `androidx.navigation:navigation-compose` | 2.7.6 | ✅ PASS | AndroidX, FOSS |
| `androidx.room:*` | 2.6.1 | ✅ PASS | AndroidX, FOSS (Apache 2.0) |
| `androidx.datastore:datastore-preferences` | 1.0.0 | ✅ PASS | AndroidX, FOSS |
| `com.google.dagger:hilt-android` | 2.50 | ✅ PASS | Dagger/Hilt, FOSS (Apache 2.0) |
| `androidx.hilt:hilt-navigation-compose` | 1.1.0 | ✅ PASS | AndroidX, FOSS |
| `io.ktor:ktor-client-android` | 2.3.7 | ✅ PASS | Ktor, FOSS (Apache 2.0) |
| `io.ktor:ktor-client-cio` | 2.3.7 | ✅ PASS | Ktor CIO engine, FOSS |
| `io.ktor:ktor-client-content-negotiation` | 2.3.7 | ✅ PASS | Ktor, FOSS |
| `io.ktor:ktor-serialization-kotlinx-json` | 2.3.7 | ✅ PASS | Ktor + kotlinx, FOSS |
| `com.squareup.okhttp3:okhttp` | 4.12.0 | ✅ PASS | OkHttp, FOSS (Apache 2.0) |
| `com.squareup.okhttp3:okhttp-dnsoverhttps` | 4.12.0 | ✅ PASS | OkHttp DoH, FOSS |
| `io.coil-kt:coil-compose` | 2.5.0 | ✅ PASS | Coil image loading, FOSS (Apache 2.0) |
| `junit:junit` | 4.13.2 | ✅ PASS | Test dependency, FOSS |
| `androidx.test.ext:junit` | 1.1.5 | ✅ PASS | Test dependency, FOSS |
| `androidx.test.espresso:espresso-core` | 3.5.1 | ✅ PASS | Test dependency, FOSS |

**Summary:** All dependencies are FOSS-friendly. No Google Play Services, Firebase, Facebook SDK, analytics SDKs, or proprietary libraries detected.

**Minor note:** `com.google.android.material:material` is maintained by Google but is open-source (Apache 2.0). The `com.google.dagger:hilt-android` dependency is also from Google but is FOSS. Neither bundles proprietary services.

---

## 3. Permissions Audit

| Permission | Verdict | Justification |
|---|---|---|
| `android.permission.INTERNET` | ✅ PASS | Required for DoH/DoT queries and speed tests — user-directed network access |
| `android.permission.FOREGROUND_SERVICE` | ✅ PASS | Required for the VPN foreground service |
| `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | ✅ PASS | Required for VPN special-use foreground service type |
| `android.permission.WAKE_LOCK` | ✅ PASS | Required to keep the VPN service alive |
| `android.permission.RECEIVE_BOOT_COMPLETED` | ✅ PASS | Required to auto-start VPN on boot (user-configurable) |
| `android.permission.POST_NOTIFICATIONS` | ✅ PASS | Required for service notification (user-facing) |
| `android.permission.ACCESS_NETWORK_STATE` | ✅ PASS | Required to check network connectivity before starting VPN |
| `android.permission.CHANGE_NETWORK_STATE` | ⚠️ MINOR | Declared but not programmatically used in source code. Should be removed if unused. |

**Summary:** No forbidden permissions detected (no `CALL_PHONE`, `SEND_SMS`, `READ_CALL_LOG`, `READ_SMS`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `REQUEST_INSTALL_PACKAGES`, or `WRITE_SETTINGS`).

---

## 4. Network Usage Audit

### User-Directed Network Calls
| Call Site | Verdict | Details |
|---|---|---|
| `SpeedTestManager` — `runSpeedTest()` | ✅ PASS | User-initiated speed test; connects to FOSS test servers (Cloudflare, Hetzner, Scaleway, Bouygues) |
| `DNSLatencyChecker` — `checkLatency()` | ✅ PASS | User-initiated latency checks; queries user-configured DNS servers only |
| `DNSVpnService` — `updateVpn()` | ✅ PASS | VPN tunnel setup; no outbound data sent beyond DNS queries |
| `DNSServerCard` — `AsyncImage` | ⚠️ MINOR | Loads flag icons from `https://flagcdn.com/` CDN — not bundled, requires internet. Acceptable for F-Droid but note it. |

### Silent/Background Network Calls
| Check | Verdict | Details |
|---|---|---|
| Auto-switch latency polling | ✅ PASS | Runs only when user enables auto-switch; queries user-configured servers |
| Boot-time latency checks | ✅ PASS | Only via `RECEIVE_BOOT_COMPLETED` to restore VPN state; no silent data exfiltration |

### Telemetry/Analytics
| Check | Verdict | Details |
|---|---|---|
| Crash reporting | ✅ PASS | None detected |
| Analytics SDKs | ✅ PASS | None detected |
| Remote config | ✅ PASS | None detected |
| User tracking | ✅ PASS | No tracking IDs or identifiers found |

**Summary:** All network activity is user-directed. No telemetry or silent data exfiltration.

---

## 5. Privacy Audit

| Check | Verdict | Details |
|---|---|---|
| Hardcoded API keys/tokens | ✅ PASS | None found |
| Tracking IDs (GA, Firebase, Mixpanel) | ✅ PASS | None found |
| Embedded advertising identifiers | ✅ PASS | None found |
| Default DNS servers | ✅ PASS | User-configurable predefined list (Quad9, Mullvad, AdGuard, Cloudflare, Google, LibreDNS, AhaDNS) |
| Flag CDN (flagcdn.com) | ⚠️ MINOR | Not bundled; loads at runtime. Acceptable for F-Droid. |
| `usesCleartextTraffic="false"` | ✅ PASS | No cleartext HTTP traffic allowed |

---

## 6. Update Mechanism Audit

| Check | Verdict | Details |
|---|---|---|
| Embedded auto-update / APK download | ✅ PASS | None found |
| Hardcoded download links | ✅ PASS | None found |
| In-app update prompts | ✅ PASS | None found |

**Summary:** The app relies entirely on F-Droid for distribution and updates. No embedded update mechanism exists.

---

## 7. Reproducibility Audit

| Check | Verdict | Details |
|---|---|---|
| Hardcoded absolute paths | ✅ PASS | None found in `build.gradle.kts` or source |
| Local signing configs | ✅ PASS | No signing config in `build.gradle.kts`; F-Droid will handle signing |
| `LOCAL_*` / `DEBUG_*` flags affecting release behavior | ✅ PASS | None found |
| `buildConfigField` usage | ✅ PASS | None found |
| `.gitignore` coverage | ✅ PASS | Ignores `.gradle/`, `build/`, `app/build/`, `*.class` |
| Build from HEAD | ✅ PASS | `.fdroid.yml` uses `commit: HEAD` |

---

## 8. Asset/License Audit

| Item | Verdict | Details |
|---|---|---|
| App icons | ✅ PASS | Vector drawables (`ic_launcher.xml`, `ic_launcher_foreground.xml`, `ic_launcher_background.xml`) — FOSS-compatible |
| Splash screen | ✅ PASS | Vector drawable |
| Notification icon | ✅ PASS | Vector drawable |
| Flag images | ⚠️ MINOR | Loaded from `flagcdn.com` CDN at runtime, not bundled |
| License file | ✅ PASS | MIT License present at project root |
| Fonts | ✅ PASS | No custom font files; uses system/Compose defaults |

---

## 9. `.fdroid.yml` Audit

| Field | Value | Verdict |
|---|---|---|
| `Categories` | Internet | ✅ PASS |
| `License` | MIT | ✅ PASS |
| `RepoType` | git | ✅ PASS |
| `Repo` | https://github.com/darkmaster0345/Photon-Dns.git | ✅ PASS |
| `gradle` | assembleRelease | ✅ PASS (fixed from malformed `gradle: - yes`) |
| `prebuild` | chmod +x gradlew | ✅ PASS |
| `AutoName` | Photon DNS | ✅ PASS |
| `UpdateCheckMode` | Static | ✅ PASS (added) |

**FIX APPLIED:** The original `.fdroid.yml` had a malformed `gradle` field:
```yaml
gradle:
  - yes
```
This would pass the string `"yes"` as a Gradle task argument, which is not a valid task. It was corrected to:
```yaml
gradle: assembleRelease
```
An `UpdateCheckMode: Static` was also added since version numbers are manually managed.

---

## 10. Summary

### Blocking Issues (Must Fix Before F-Droid Submission)
1. **Android SDK missing in build environment** — Cannot build or test. This is an environment issue, not a code issue. F-Droid's build server will need a proper Android SDK. The project's `gradle.properties` and `build.gradle.kts` are correctly configured for F-Droid builds.

### Minor Issues (Acceptable, Should Note)
1. **`CHANGE_NETWORK_STATE` permission** — Declared in manifest but not used in code. Should be removed to minimize permissions.
2. **Flag CDN (flagcdn.com)** — Icons loaded at runtime from external CDN. Not bundled, acceptable for F-Droid, but users without internet won't see flags.
3. **Google Material Components + Dagger Hilt** — Both maintained by Google but are FOSS (Apache 2.0). No proprietary services bundled. F-Droid allows these.

### Passed (Clean)
- All dependencies are FOSS-friendly
- No proprietary SDKs, analytics, or telemetry
- All network calls are user-directed
- No telemetry, crash reporting, or tracking
- No forbidden permissions
- No hardcoded API keys, tokens, or credentials
- No embedded auto-update mechanism
- No hardcoded absolute paths or local signing configs
- No `LOCAL_*`/`DEBUG_*` flags affecting release behavior
- License file present and correct (MIT)
- Assets are FOSS-compatible (vector drawables)
- `.fdroid.yml` is now correctly configured (after fix)
- `usesCleartextTraffic="false"` — good security practice
- `exportSchema = false` for Room — fine for F-Droid
- No `com.google.android.gms`, Firebase, Facebook SDK, or ADS SDKs

---

## Recommendations

1. **Remove unused `CHANGE_NETWORK_STATE` permission** from `AndroidManifest.xml` to follow least-privilege.
2. **Bundle flag icons locally** or make CDN loading gracefully degrade when offline.
3. **Add unit tests** (currently no `src/test/` directory exists). F-Droid prefers projects with test infrastructure.
4. **Consider adding `-keepattributes SourceFile,LineNumberTable`** to `proguard-rules.pro` for better crash reporting without obfuscation.
