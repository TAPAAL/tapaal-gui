#!/usr/bin/env bash
# Build the TAPAAL GUI with the given release version and copy tapaal.jar plus all
# runtime dependency jars to <output-dir>.
#
# Usage: build-gui.sh <version> <output-dir>   (run from the repository root)
set -euo pipefail

version="$1"
out="$2"
src=src/main/java/net/tapaal/TAPAAL.java

# Portable in-place edit (GNU and BSD sed)
sed 's/VERSION = "DEV";/VERSION = "'"$version"'";/' "$src" > "$src.tmp" && mv "$src.tmp" "$src"
grep -q "VERSION = \"$version\";" "$src" || { echo "::error::Failed to set version in $src"; exit 1; }

chmod +x gradlew
./gradlew installDist -x test

mkdir -p "$out"
cp build/install/TAPAAL/lib/*.jar "$out/"
mv "$out"/TAPAAL-*.jar "$out/tapaal.jar"
ls -l "$out"
