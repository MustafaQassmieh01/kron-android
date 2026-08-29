#!/bin/sh
# Bootstrap wrapper for this generated port. It downloads the official Gradle
# 8.11.1 wrapper JAR only when the binary is not already present.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
URL="https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar"
SHA="2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046"

if [ ! -f "$JAR" ]; then
  mkdir -p "$(dirname "$JAR")"
  echo "Gradle wrapper JAR is missing; downloading Gradle 8.11.1 bootstrap..." >&2
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$URL" -o "$JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$JAR" "$URL"
  else
    echo "Install curl/wget or let Android Studio regenerate the Gradle wrapper." >&2
    exit 1
  fi

  if command -v sha256sum >/dev/null 2>&1; then
    ACTUAL=$(sha256sum "$JAR" | awk '{print $1}')
  elif command -v shasum >/dev/null 2>&1; then
    ACTUAL=$(shasum -a 256 "$JAR" | awk '{print $1}')
  else
    ACTUAL="$SHA"
  fi
  if [ "$ACTUAL" != "$SHA" ]; then
    rm -f "$JAR"
    echo "Gradle wrapper checksum verification failed." >&2
    exit 1
  fi
fi

exec java -Xmx64m -Xms64m -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
