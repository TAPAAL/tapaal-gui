#!/bin/sh

(cd release-version; java -Xmx6144M -cp .:* TAPAAL "$@")
