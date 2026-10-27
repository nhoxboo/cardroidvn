@echo off
setlocal
title CarDroidVN - Test man hinh xe tren Laptop

echo ===================================================================
echo     CARDROIDVN - MO PHONG MAN HINH CARPLAY TREN LAPTOP
echo                 Ban quyen (c) Hoai Nam
echo ===================================================================
echo.

:: Add ADB to PATH if needed
where adb >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Android\sdk\platform-tools\adb.exe" (
        set "PATH=C:\Android\sdk\platform-tools;%PATH%"
    ) else if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools;%PATH%"
    )
)

:: Run test-carplay-laptop.py with Python
python "%~dp0test-carplay-laptop.py" %*

echo.
pause
