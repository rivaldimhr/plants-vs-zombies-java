#!/bin/sh
# Compile lalu jalankan semua unit test (JUnit 5).
set -e
cd "$(dirname "$0")"
JUNIT=lib/junit-platform-console-standalone-1.10.2.jar
rm -rf bin-test
javac -encoding UTF-8 -cp "$JUNIT" -d bin-test $(find src test -name '*.java')
java -Djava.awt.headless=true -jar "$JUNIT" execute --class-path bin-test --scan-class-path --disable-banner
