#!/bin/bash
files="$(find -name "*.l1")"
IFS=$'\n'
for file in $files; do
  "$COMPILER" "$file" "$(dirname "$file")/bin/$(basename "${file%.l1}")"
done