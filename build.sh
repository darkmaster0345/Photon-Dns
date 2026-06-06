#!/bin/bash
set -eu

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

chmod +x ./gradlew

./gradlew assembleRelease
rc=$?;
if [ $rc -ne 0 ]; then
    echo "F-Droid build failed (assembleRelease)"; exit $rc;
fi
