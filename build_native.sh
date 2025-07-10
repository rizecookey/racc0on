#!/usr/bin/env bash
set -e

java_version_major=24
jdk_version=24.0.1

project_dir=$(realpath $(dirname "$0"))
cd "$project_dir"

graalvm_dir="$project_dir/.graalvm"

if [ ! -d "$graalvm_dir/graalvm-jdk-$jdk_version" ]; then
  rm -rf "$graalvm_dir"
  workdir=$(mktemp --directory)
  curl "https://download.oracle.com/graalvm/${java_version_major}/archive/graalvm-jdk-${jdk_version}_linux-x64_bin.tar.gz" -o "$workdir/graalvm-jdk.tar.gz"
  tar -xvzf "$workdir/graalvm-jdk.tar.gz" -C "$workdir"
  mkdir -p "$graalvm_dir/graalvm-jdk-$jdk_version"
  mv -T "$(find "$workdir" -name "graalvm-jdk-*" -type d)/" "$graalvm_dir/graalvm-jdk-$jdk_version"
fi
JAVA_HOME="$graalvm_dir/graalvm-jdk-$jdk_version" ./gradlew --no-daemon nativeCompile
