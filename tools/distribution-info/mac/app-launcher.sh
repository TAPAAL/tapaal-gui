#!/bin/bash
# Launcher used as Tapaal.app/Contents/MacOS/tapaal
# The application files live in Tapaal.app/Contents/Resources/{lib,bin}

if ! java --version >/dev/null 2>&1 ; then
    osascript -e 'tell app "System Events" to display dialog "You need to install Java 11 or higher to run TAPAAL"'
    exit 1
fi

cd "$(dirname "$0")/../Resources/lib" || exit 1
exec java -Xmx6144M -cp ".:*" TAPAAL "$@"
