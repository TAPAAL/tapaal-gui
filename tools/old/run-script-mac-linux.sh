#!/bin/sh

( cd `dirname "$0"`; cd lib; java -cp .:* TAPAAL)
