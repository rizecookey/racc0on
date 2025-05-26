#!/usr/bin/env bash
JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005" ./run.sh "$@"