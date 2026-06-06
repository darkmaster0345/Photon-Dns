# F-Droid Submission Guide

This document describes how to submit a new release of Photon DNS to F-Droid.

## Prerequisites

- An approved F-Droid app metadata record for this app (the maintainer does not have to be you).
- A git tag at the release you want to publish, matching the commit range accepted by the app metadata.
- Internet access to fetch dependencies during build.

## Workflow

1. Update metadata in `.fdroid.yml` if needed (categories, URLs, description, changelog).
2. Ensure the release tag exists and points to the desired commit.
3. Build locally to verify the F-Droid build and command are valid.
   - Use the local helper: `./build.sh`
   - Or run Gradle directly with the environment described below.
4. Open or update the F-Droid track entry, reference the `.fdroid.yml` paths, and point it to your repository.
5. Trigger the F-Droid inclusion/update build.
6. Respond to F-Droid reviewer feedback and repeat until build and metadata checks pass.

## Build environment

- JDK 17 (required for AGP 8.2 compatibility)
- Android SDK as specified in the project build config (compileSdk 34, minSdk 26)
- Gradle 8.4 (configured in `gradle/wrapper/gradle-wrapper.properties`)

## Versioning notes

- `.fdroid.yml` versionCode must be increased for every accepted F-Droid release.
- Match versionCode in `.fdroid.yml` with the value in `app/build.gradle.kts` before submission.
- Current versionCode: 1 (increment for future releases)

## Testing on F-Droid infrastructure

If the project has F-Droid client access, you can validate metadata without affecting users with the `f-droid checkupdates` / build pipeline features provided by the maintainer's F-Droid app submission channel.

## Local build verification

```bash
# Set JDK 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk

# Run build script
./build.sh

# Or run Gradle directly
./gradlew assembleRelease
```

Expected output: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Compatibility checklist

- [x] All dependencies are open source (no proprietary libraries)
- [x] Signing config is disabled in release build (uses F-Droid signing)
- [x] Java 17 compatibility confirmed (compileOptions targetCompatibility = 17)
- [x] No Google Play services dependencies
- [x] No Firebase dependencies
