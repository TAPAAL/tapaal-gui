#!/bin/sh

(cd release-version; /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java -cp .:* TAPAAL "$@")
