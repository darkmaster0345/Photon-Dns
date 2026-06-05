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
   - Use the local helper: `bash build.sh`
   - Or run Gradle directly with the environment described below.
4. Open or update the F-Droid track entry, reference the `.fdroid.yml` paths, and point it to your repository.
5. Trigger the F-Droid inclusion/update build.
6. Respond to F-Droid reviewer feedback and repeat until build and metadata checks pass.

## Build environment

- JDK 17
- Android SDK as specified in the project build config
- `build.sh` handles setting the executable bit on the Gradle wrapper and invoking `assembleRelease`

## Versioning notes

- `.fdroid.yml` versionCode must be increased for every accepted F-Droid release.
- The `BuildVersion.merge` block and `output` path are configured so the local build artifacts match expected F-Droid behavior for validation before submission.

## Testing on F-Droid infrastructure

If the project has F-Droid client access, you can validate metadata without affecting users with the `f-droid checkupdates` / build pipeline features provided by the maintainer's F-Droid app submission channel.
