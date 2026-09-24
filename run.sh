#!/bin/sh
# Compile dan jalankan game. Folder image/ dibaca relatif terhadap folder ini.
set -e
cd "$(dirname "$0")"
rm -rf bin
javac -encoding UTF-8 -d bin $(find src -name '*.java')
java -cp bin game.Game
