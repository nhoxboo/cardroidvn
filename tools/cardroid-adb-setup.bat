@echo off
setlocal EnableDelayedExpansion
title CarDroid - ADB Setup Tool (Non-Root)

echo ===================================================================
echo     CARDROID - 1-CLICK ADB SETUP TOOL (NO ROOT / NO SHIZUKU)
echo ===================================================================
echo.

:: 1. Check ADB in PATH or common directories
where adb >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Android\sdk\platform-tools\adb.exe" (
        set "PATH=C:\Android\sdk\platform-tools;%PATH%"
    ) else if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools;%PATH%"
    ) else if exist "%USERPROFILE%\.openclaw\workspace\platform-tools\adb.exe" (
        set "PATH=%USERPROFILE%\.openclaw\workspace\platform-tools;%PATH%"
    ) else (
        echo [ERROR] ADB not found! Please connect phone with USB Debugging enabled.
        pause
        exit /b 1
    )
)

echo [1/6] Checking connected Android devices...
adb devices
echo.

:: 2. Check if APK is installed, if not auto-install
set "LOCAL_APK="
if exist "%~dp0..\CarDroid-v0.2.0-VN-release.apk" set "LOCAL_APK=%~dp0..\CarDroid-v0.2.0-VN-release.apk"
if not defined LOCAL_APK if exist "%~dp0..\app\build\outputs\apk\release\app-release.apk" set "LOCAL_APK=%~dp0..\app\build\outputs\apk\release\app-release.apk"
if not defined LOCAL_APK if exist "%~dp0CarDroid-v0.2.0-VN-release.apk" set "LOCAL_APK=%~dp0CarDroid-v0.2.0-VN-release.apk"

adb shell pm path com.cardroid.app >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if defined LOCAL_APK (
        echo [2/6] CarDroid is not installed. Installing !LOCAL_APK!...
        adb install -r -g "!LOCAL_APK!"
        echo [OK] CarDroid installed successfully.
    ) else (
        echo [NOTICE] CarDroid not found on device.
    )
) else (
    echo [2/6] CarDroid is already installed.
)
echo.

echo [3/6] Granting permissions to CarDroid...
adb shell pm grant com.cardroid.app android.permission.WRITE_SECURE_SETTINGS 2>nul
adb shell pm grant com.cardroid.app android.permission.PACKAGE_USAGE_STATS 2>nul
adb shell pm grant com.cardroid.app android.permission.SYSTEM_ALERT_WINDOW 2>nul
adb shell appops set com.cardroid.app SYSTEM_ALERT_WINDOW allow 2>nul
adb shell appops set com.cardroid.app GET_USAGE_STATS allow 2>nul
echo [OK] Permissions granted.
echo.

echo [4/6] Enabling Touch Accessibility Service...
adb shell settings put secure enabled_accessibility_services com.cardroid.app/com.cardroid.app.RemoteControlService
adb shell settings put secure accessibility_enabled 1
echo [OK] Accessibility service enabled.
echo.

echo [5/6] Resolving CarDroid APK path and UID...
set "APK_PATH="
for /f "tokens=2 delims=:" %%A in ('adb shell pm path com.cardroid.app 2^>nul') do (
    if not defined APK_PATH (
        set "RAW_PATH=%%A"
        set "RAW_PATH=!RAW_PATH:~0,-1!"
        set "APK_PATH=!RAW_PATH!"
    )
)

if not defined APK_PATH (
    echo [ERROR] CarDroid app is not installed on this device!
    echo Please install CarDroid-v0.2.0-VN-release.apk first.
    pause
    exit /b 1
)
echo APK Path: !APK_PATH!

set "APP_UID="
for /f "tokens=2 delims==" %%B in ('adb shell "dumpsys package com.cardroid.app 2>/dev/null | grep userId="' ) do (
    if not defined APP_UID (
        for /f "tokens=1" %%C in ("%%B") do set "APP_UID=%%C"
    )
)

if not defined APP_UID set "APP_UID=10000"
echo App UID: !APP_UID!
echo.

echo [6/6] Launching background Display Helper daemon...
adb shell "CLASSPATH=!APK_PATH! setsid nohup app_process /system/bin --nice-name=cardroid-helper com.cardroid.app.helper.HelperMain --port=8253 --app-uid=!APP_UID! > /data/local/tmp/cardroid-helper.log 2>&1 &"

echo.
echo ===================================================================
echo [SUCCESS] CarDroid is now fully activated!
echo You can open CarDroid on your phone and connect to your car.
echo NO ROOT OR SHIZUKU REQUIRED!
echo ===================================================================
echo.
pause
