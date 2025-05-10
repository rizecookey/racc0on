#!/usr/bin/env sh
BIN_DIR="$(dirname "$0")/build/install/racc0on/bin"
$BIN_DIR/racc0on "$@.s"
gcc -Wl,--entry=_entry "$2.s" -o "$2"
