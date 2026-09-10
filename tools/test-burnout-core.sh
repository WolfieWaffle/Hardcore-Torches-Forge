#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
out="$(mktemp -d)"
trap 'rm -rf "$out"' EXIT
pkg="com/github/wolfiewaffle/hardcore_torches/burnout"
javac --release 21 -Xlint:all -Werror -d "$out" \
    "src/main/java/$pkg/FuelClock.java" \
    "src/main/java/$pkg/FuelMath.java" \
    "src/main/java/$pkg/IndexedDeadlineQueue.java" \
    "src/test/java/$pkg/BurnoutCoreTest.java"
java -ea -cp "$out" com.github.wolfiewaffle.hardcore_torches.burnout.BurnoutCoreTest
