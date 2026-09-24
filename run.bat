@echo off
REM Compile dan jalankan game. Harus dijalankan dari folder ini (folder image/ dibaca relatif).
cd /d "%~dp0"
if exist bin rmdir /s /q bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
if errorlevel 1 (
    del sources.txt
    echo Compile gagal.
    pause
    exit /b 1
)
del sources.txt
java -cp bin game.Game
