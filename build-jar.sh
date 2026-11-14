#!/bin/sh
# Membuat dist/plants-vs-zombies.jar: satu file yang berisi kode + semua aset.
# Jalankan: java -jar dist/plants-vs-zombies.jar
set -e
cd "$(dirname "$0")"
rm -rf build dist
mkdir -p build/classes build/classes/image/IMAGE dist
javac -encoding UTF-8 -d build/classes $(find src -name '*.java')
# hanya aset yang dipakai game
for f in MENU.png HELP.png "INVENTORY (7).png" "BACKGROUND DAY.png" "BACKGROUND NIGHT.png" WIN.png; do
    cp "image/IMAGE/$f" build/classes/image/IMAGE/
done
cp -r image/sprites image/cards image/ui build/classes/image/
cp -r sound build/classes/
# perkecil PNG di JAR (butuh Python + Pillow; dilewati bila tidak ada). Matikan dengan NO_COMPRESS=1
if [ -z "$NO_COMPRESS" ] && python3 -c "import PIL" 2>/dev/null; then
    python3 tools/compress_images.py build/classes/image
fi
# daftar gambar (Assets.preloadList): latar dulu, lalu UI, kartu, sprite
(cd build/classes && for d in image/IMAGE image/ui image/cards image/sprites; do find "$d" -type f \( -name '*.png' -o -name '*.jpg' \) | sort; done) > build/classes/image/preload.txt
jar --create --file dist/plants-vs-zombies.jar --main-class game.Game -C build/classes .
rm -rf build
echo "Selesai: dist/plants-vs-zombies.jar"
