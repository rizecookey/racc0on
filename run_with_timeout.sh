#!/bin/env bash
./run.sh "$@"
exitcode=$?
if [ $exitcode -eq 0 ]; then
  mv "$2" "$2.actual"
  gcc "-DBIN=\"$2.actual\"" tests/utils/timeout_wrapper.c -o "$2"
else
  exit $exitcode
fi