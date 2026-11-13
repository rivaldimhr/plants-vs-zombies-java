#!/bin/sh
# Menyusun versi browser di dist/web/: halaman pemutar (web/) + JAR game.
# Uji lokal: python3 -m http.server -d dist/web 8000  lalu buka http://localhost:8000/
set -e
cd "$(dirname "$0")"
sh ./build-jar.sh
rm -rf dist/web
mkdir -p dist/web
cp web/* dist/web/
cp dist/plants-vs-zombies.jar dist/web/
touch dist/web/.nojekyll
echo "Selesai: dist/web/"
