@echo off
chcp 65001 >nul
echo ===================================================================
echo     CARDROID - CÔNG CỤ KÍCH HOẠT NHANH 1-CLICK QUA ADB (KHÔNG ROOT)
echo ===================================================================
echo.
echo Đang kiểm tra kết nối thiết bị Android...
adb devices
echo.

echo 1. Cấp quyền hệ thống cho CarDroid...
adb shell pm grant com.cardroid.app android.permission.WRITE_SECURE_SETTINGS 2>nul
adb shell pm grant com.cardroid.app android.permission.PACKAGE_USAGE_STATS 2>nul
adb shell pm grant com.cardroid.app android.permission.SYSTEM_ALERT_WINDOW 2>nul
adb shell appops set com.cardroid.app SYSTEM_ALERT_WINDOW allow 2>nul
adb shell appops set com.cardroid.app GET_USAGE_STATS allow 2>nul

echo 2. Bật dịch vụ hỗ trợ cảm ứng (Accessibility Service) tự động...
adb shell settings put secure enabled_accessibility_services com.cardroid.app/com.cardroid.app.RemoteControlService
adb shell settings put secure accessibility_enabled 1

echo 3. Tìm đường dẫn APK trên thiết bị...
for /f "tokens=2 delims=:" %%a in ('adb shell pm path com.cardroid.app') do (
    set APK_PATH=%%a
    goto :found_apk
)

:found_apk
set APK_PATH=%APK_PATH: =%
set APK_PATH=%APK_PATH:~0,-1%
echo Đường dẫn APK: %APK_PATH%

echo 4. Lấy UID của ứng dụng...
for /f "tokens=2 delims=:" %%b in ('adb shell id -u') do (
    set SHELL_UID=%%b
)
for /f "tokens=2 delims=:" %%c in ('adb shell "dumpsys package com.cardroid.app | grep userId="') do (
    set APP_UID=%%c
    goto :found_uid
)

:found_uid
set APP_UID=%APP_UID: =%
echo App UID: %APP_UID%

echo 5. Khởi động CarDroid Display Helper chạy nền vĩnh viễn...
adb shell "CLASSPATH=%APK_PATH% setsid nohup app_process /system/bin --nice-name=cardroid-helper com.cardroid.app.helper.HelperMain --port=8253 --app-uid=%APP_UID% > /data/local/tmp/cardroid-helper.log 2>&1 &"

echo.
echo ===================================================================
echo [THÀNH CÔNG] CarDroid đã được cấp đủ quyền và khởi chạy Helper!
echo Bây giờ bạn có thể mở ứng dụng CarDroid trên điện thoại và kết nối xe.
echo KHÔNG CẦN CÀI ĐẶT SHIZUKU HAY ROOT MÁY!
echo ===================================================================
pause
