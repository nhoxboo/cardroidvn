# CarDroid - ADB Setup Tool for Windows PowerShell (Non-Root / No Shizuku)
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "===================================================================" -ForegroundColor Cyan
Write-Host "    CARDROID - KÍCH HOẠT NHANH 1-CLICK QUA ADB (KHÔNG CẦN ROOT)    " -ForegroundColor Cyan
Write-Host "===================================================================" -ForegroundColor Cyan
Write-Host ""

# Tìm adb
$adb = "adb"
if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    $candidates = @(
        "C:\Android\sdk\platform-tools\adb.exe",
        "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
        "$env:USERPROFILE\.openclaw\workspace\platform-tools\adb.exe"
    )
    foreach ($cand in $candidates) {
        if (Test-Path $cand) {
            $adb = $cand
            break
        }
    }
}

Write-Host "[1/5] Kiểm tra thiết bị Android kết nối..." -ForegroundColor Yellow
& $adb devices
Write-Host ""

Write-Host "[2/5] Cấp quyền hệ thống cho CarDroid..." -ForegroundColor Yellow
& $adb shell pm grant com.cardroid.app android.permission.WRITE_SECURE_SETTINGS 2>$null
& $adb shell pm grant com.cardroid.app android.permission.PACKAGE_USAGE_STATS 2>$null
& $adb shell pm grant com.cardroid.app android.permission.SYSTEM_ALERT_WINDOW 2>$null
& $adb shell appops set com.cardroid.app SYSTEM_ALERT_WINDOW allow 2>$null
& $adb shell appops set com.cardroid.app GET_USAGE_STATS allow 2>$null
Write-Host "[OK] Đã cấp quyền hệ thống." -ForegroundColor Green
Write-Host ""

Write-Host "[3/5] Kích hoạt Dịch vụ Trợ năng hỗ trợ Cảm ứng (Accessibility)..." -ForegroundColor Yellow
& $adb shell settings put secure enabled_accessibility_services com.cardroid.app/com.cardroid.app.RemoteControlService
& $adb shell settings put secure accessibility_enabled 1
Write-Host "[OK] Đã bật trợ năng cảm ứng." -ForegroundColor Green
Write-Host ""

Write-Host "[4/5] Tìm đường dẫn APK và UID ứng dụng..." -ForegroundColor Yellow
$pmPath = (& $adb shell pm path com.cardroid.app 2>$null) | Out-String
if ($pmPath -match 'package:(.+base\.apk|.+apk)') {
    $apkPath = $matches[1].Trim()
    Write-Host "Đường dẫn APK: $apkPath" -ForegroundColor Green
} else {
    Write-Host "[LỖI] Chưa cài đặt CarDroid trên điện thoại!" -ForegroundColor Red
    Write-Host "Vui lòng cài đặt file CarDroid-v0.2.0-VN-release.apk vào máy trước." -ForegroundColor Red
    Read-Host "Nhấn Enter để thoát..."
    exit 1
}

$dumpsys = (& $adb shell "dumpsys package com.cardroid.app | grep userId=" 2>$null) | Out-String
if ($dumpsys -match 'userId=(\d+)') {
    $appUid = $matches[1]
} else {
    $appUid = "10000"
}
Write-Host "App UID: $appUid" -ForegroundColor Green
Write-Host ""

Write-Host "[5/5] Khởi động Display Helper daemon chạy nền vĩnh viễn..." -ForegroundColor Yellow
& $adb shell "CLASSPATH=$apkPath setsid nohup app_process /system/bin --nice-name=cardroid-helper com.cardroid.app.helper.HelperMain --port=8253 --app-uid=$appUid > /data/local/tmp/cardroid-helper.log 2>&1 &"

Write-Host ""
Write-Host "===================================================================" -ForegroundColor Green
Write-Host "[THÀNH CÔNG] CarDroid đã được kích hoạt hoàn tất!" -ForegroundColor Green
Write-Host "Bây giờ bạn có thể mở CarDroid trên điện thoại và kết nối với xe." -ForegroundColor Green
Write-Host "HOÀN TOÀN KHÔNG CẦN ROOT HAY CÀI SHIZUKU!" -ForegroundColor Green
Write-Host "===================================================================" -ForegroundColor Green
Write-Host ""
Read-Host "Nhấn Enter để kết thúc..."
