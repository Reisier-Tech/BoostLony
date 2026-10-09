@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo             BoostColony Build System
echo ===================================================

if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    )
)

if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME is not set and JDK 17 was not found in Program Files!
    echo Please install Java 17 or set JAVA_HOME.
    pause
    exit /b 1
)

echo [INFO] Using JAVA_HOME: %JAVA_HOME%
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [INFO] Building BoostColony JAR with Gradle...
call .\gradlew.bat build -x test

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed! Check the Gradle errors above.
    pause
    exit /b %ERRORLEVEL%
)

if not exist "dist" mkdir "dist"
copy /y "build\libs\BoostColony-1.20.1-*.jar" "dist\" >nul 2>&1

echo.
echo ===================================================
echo [SUCCESS] BoostColony has been successfully built!
echo.
echo The mod JAR file is located at:
dir /b "dist\BoostColony-1.20.1-*.jar"
echo Full path: %~dp0dist\
echo.
echo Copy this JAR file into your Minecraft .minecraft\mods folder!
echo ===================================================
pause
