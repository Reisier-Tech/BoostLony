@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo       BoostColony Direct Client Testing
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

echo [INFO] Launching Minecraft Forge Client with BoostColony and MineColonies...
call .\gradlew.bat runClient

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Minecraft client closed with error code %ERRORLEVEL%.
    pause
    exit /b %ERRORLEVEL%
)

echo [INFO] Minecraft client closed.
pause
