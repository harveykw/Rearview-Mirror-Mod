#!/usr/bin/env sh
set -eu

GRADLE_VERSION="${GRADLE_VERSION:-9.2.1}"
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DIST_DIR="$APP_HOME/.gradle/bootstrap"
GRADLE_HOME="$DIST_DIR/gradle-$GRADLE_VERSION"
ZIP_FILE="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"
DIST_URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if ! command -v java >/dev/null 2>&1; then
    echo "Java was not found on PATH. Minecraft 1.21.11/Fabric needs Java 21." >&2
    exit 1
fi

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
    mkdir -p "$DIST_DIR"
    echo "Downloading Gradle $GRADLE_VERSION..."
    if command -v curl >/dev/null 2>&1; then
        curl -fL "$DIST_URL" -o "$ZIP_FILE"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$ZIP_FILE" "$DIST_URL"
    else
        echo "Need curl or wget to download Gradle $GRADLE_VERSION." >&2
        exit 1
    fi

    echo "Unpacking Gradle $GRADLE_VERSION..."
    if command -v unzip >/dev/null 2>&1; then
        unzip -q "$ZIP_FILE" -d "$DIST_DIR"
    elif command -v python3 >/dev/null 2>&1; then
        python3 -m zipfile -e "$ZIP_FILE" "$DIST_DIR"
    else
        echo "Need unzip or python3 to unpack Gradle $GRADLE_VERSION." >&2
        exit 1
    fi
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
