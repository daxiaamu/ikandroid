#!/usr/bin/env sh
set -eu
VERSION=9.1.0
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
export GRADLE_USER_HOME="$ROOT/.gradle-user"
INSTALL_ROOT="$HOME/.gradle/ikan-dists"
GRADLE_HOME="$INSTALL_ROOT/gradle-$VERSION"
if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$INSTALL_ROOT"
  ZIP="${TMPDIR:-/tmp}/gradle-$VERSION-bin.zip"
  curl -fL "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$ZIP"
  unzip -q "$ZIP" -d "$INSTALL_ROOT"
  rm -f "$ZIP"
fi
exec "$GRADLE_HOME/bin/gradle" -p "$ROOT" "$@"
