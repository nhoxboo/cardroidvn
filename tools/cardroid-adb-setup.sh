#!/usr/bin/env bash
set -e

echo "==================================================================="
echo "    CARDROID - CÔNG CỤ KÍCH HOẠT NHANH 1-CLICK QUA ADB (KHÔNG ROOT)"
echo "==================================================================="
echo ""

echo "Đang kiểm tra kết nối thiết bị Android..."
adb devices
echo ""

echo "1. Cấp quyền hệ thống cho CarDroid..."
adb shell pm grant com.cardroid.app android.permission.WRITE_SECURE_SETTINGS 2>/dev/null || true
adb shell pm grant com.cardroid.app android.permission.PACKAGE_USAGE_STATS 2>/dev/null || true
adb shell pm grant com.cardroid.app android.permission.SYSTEM_ALERT_WINDOW 2>/dev/null || true
adb shell appops set com.cardroid.app SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
adb shell appops set com.cardroid.app GET_USAGE_STATS allow 2>/dev/null || true

echo "2. Bật dịch vụ hỗ trợ cảm ứng (Accessibility Service) tự động..."
adb shell settings put secure enabled_accessibility_services com.cardroid.app/com.cardroid.app.RemoteControlService
adb shell settings put secure accessibility_enabled 1

echo "3. Lấy đường dẫn APK trên thiết bị..."
APK_PATH=$(adb shell pm path com.cardroid.app | head -n 1 | cut -d: -f2 | tr -d '\r\n')
echo "Đường dẫn APK: $APK_PATH"

echo "4. Lấy UID của ứng dụng..."
APP_UID=$(adb shell dumpsys package com.cardroid.app | grep "userId=" | head -n 1 | cut -d= -f2 | tr -d '\r\n')
echo "App UID: $APP_UID"

echo "5. Khởi động CarDroid Display Helper chạy nền vĩnh viễn..."
adb shell "CLASSPATH=$APK_PATH setsid nohup app_process /system/bin --nice-name=cardroid-helper com.cardroid.app.helper.HelperMain --port=8253 --app-uid=$APP_UID > /data/local/tmp/cardroid-helper.log 2>&1 &"

echo ""
echo "==================================================================="
echo "[THÀNH CÔNG] CarDroid đã được cấp đủ quyền và khởi chạy Helper!"
echo "Bây giờ bạn có thể mở ứng dụng CarDroid trên điện thoại và kết nối xe."
echo "KHÔNG CẦN CÀI ĐẶT SHIZUKU HAY ROOT MÁY!"
echo "==================================================================="
