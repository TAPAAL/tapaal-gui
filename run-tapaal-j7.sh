#!/bin/sh

(cd release-version; java -Dapple.laf.useScreenMenuBar=true -Dcom.apple.mrj.application.apple.menu.about.name="TAPAAL" -Xdock:name="TAPAAL" -Xdock:icon=../src/resources/Images/tapaal-icon.png -cp .:* TAPAAL)
