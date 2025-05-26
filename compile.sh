#!/usr/bin/env bash
project_root="$(realpath "$(dirname "$0")")"
compiler="$project_root/run.sh"

files=()
java_opts=""
for opt in "$@"; do
  if [ "$opt" == "--debug" ]; then
    java_opts="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
  else
    files+=("$opt")
  fi
done

for file in "${files[@]}"; do
  target_dir="$(dirname "$file")/bin"
  if [ ! -d "$target_dir" ]; then
    mkdir "$target_dir/bin"
  fi
  target_name=$(basename "$file")
  target="$target_dir/${target_name%.l*}"
  echo "Compiling $file > $target"
  JAVA_OPTS="$java_opts" "$compiler" "$file" "$target"
  error="$?"
  if [ "$error" != "0" ]; then
    echo "Compilation failed with error code $error."
  else
    echo Compilation successful.
  fi
  echo
done