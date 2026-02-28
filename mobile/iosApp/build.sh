#!/bin/sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}/.."
FRAMEWORK_SRC="${PROJECT_DIR}/shared/build/XCFrameworks/debug/shared.xcframework"
FRAMEWORK_DST="${SCRIPT_DIR}/Frameworks/shared.xcframework"

"${PROJECT_DIR}/gradlew" -p "${PROJECT_DIR}" :shared:assembleXCFramework

rm -rf "${FRAMEWORK_DST}"
mkdir -p "${SCRIPT_DIR}/Frameworks"
cp -R "${FRAMEWORK_SRC}" "${FRAMEWORK_DST}"
