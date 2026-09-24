@echo off
REM Membuat dist\plants-vs-zombies.jar: satu file berisi kode + semua aset.
REM Jalankan: java -jar dist\plants-vs-zombies.jar
cd /d "%~dp0"
if exist build rmdir /s /q build
if exist dist rmdir /s /q dist
mkdir build\classes\image\IMAGE
mkdir dist
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d build\classes @sources.txt
if errorlevel 1 (
    del sources.txt
    echo Compile gagal.
    pause
    exit /b 1
)
del sources.txt
for %%f in ("MENU.png" "HELP.png" "INVENTORY (7).png" "BACKGROUND DAY.png" "BACKGROUND NIGHT.png" "WIN.png") do copy "image\IMAGE\%%~f" build\classes\image\IMAGE\ > nul
xcopy /e /i /q image\sprites build\classes\image\sprites > nul
xcopy /e /i /q image\cards build\classes\image\cards > nul
xcopy /e /i /q image\ui build\classes\image\ui > nul
xcopy /e /i /q sound build\classes\sound > nul
jar --create --file dist\plants-vs-zombies.jar --main-class game.Game -C build\classes .
rmdir /s /q build
echo Selesai: dist\plants-vs-zombies.jar
pause
