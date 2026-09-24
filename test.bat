@echo off
REM Compile lalu jalankan semua unit test (JUnit 5). Jalankan dari folder ini.
cd /d "%~dp0"
if exist bin-test rmdir /s /q bin-test
dir /s /b src\*.java test\*.java > sources.txt
javac -encoding UTF-8 -cp lib\junit-platform-console-standalone-1.10.2.jar -d bin-test @sources.txt
if errorlevel 1 (
    del sources.txt
    echo Compile gagal.
    pause
    exit /b 1
)
del sources.txt
java -jar lib\junit-platform-console-standalone-1.10.2.jar execute --class-path bin-test --scan-class-path --disable-banner
pause
