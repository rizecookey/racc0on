#!/bin/bash
project_root="$(realpath $(dirname "$0")/../../)"
demo_dir="$project_root/tests/sample"

if [ ! -d "$demo_dir/bin" ]; then
  mkdir "$demo_dir/bin"
fi

files="$(find "$demo_dir" -name "*.l1")"
IFS=$'\n'
for file in $files; do
  target="$(dirname "$file")/bin/$(basename "${file%.l1}")"
  echo "Compiling $file > $target"
  "$project_root/run.sh" "$file" "$target"
  echo
done