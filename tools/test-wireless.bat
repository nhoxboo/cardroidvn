@echo off
setlocal
title CarDroidVN - Test man hinh xe KHONG DAY (Wi-Fi) tren Laptop

echo ===================================================================
echo     CARDROIDVN - TEST MAN HINH CARPLAY KHONG DAY (WI-FI)
echo                 Ban quyen (c) Hoai Nam
echo ===================================================================
echo.

where adb >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Android\sdk\platform-tools\adb.exe" (
        set "PATH=C:\Android\sdk\platform-tools;%PATH%"
    ) else if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools;%PATH%"
    )
)

:: Run test in wireless mode
python "%~dp0test-carplay-laptop.py" --host auto %*

echo.
pause
