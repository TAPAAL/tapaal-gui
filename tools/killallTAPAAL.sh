#!/bin/sh

ps axu | grep TAPAAL | cut -d\  -f5 | xargs kill
