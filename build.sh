#!/bin/bash
set -e

echo "=== Using cached Gradle 8.5 wrapper ==="
chmod +x gradlew 2>/dev/null || true

if [ ! -f "gradlew" ]; then
  echo "Downloading Gradle 8.5..."
  curl -L https://services.gradle.org/distributions/gradle-8.5-bin.zip -o /tmp/gradle.zip
  unzip -q /tmp/gradle.zip -d /opt/
  export PATH=$PATH:/opt/gradle-8.5/bin
  gradle wrapper
  chmod +x gradlew
fi

echo "=== Building APK ==="
./gradlew assembleDebug --no-daemon
