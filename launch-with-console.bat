@echo off
REM ====================================================
REM  He Thong Ban Ve Tau - Debug Launcher
REM  This launcher shows console output for debugging
REM ====================================================

title He Thong Ban Ve Tau - Debug Console

echo.
echo ====================================================
echo   HE THONG BAN VE TAU - DEBUG LAUNCHER
echo ====================================================
echo.
echo This window will show startup messages and errors.
echo.
echo Current Time: %DATE% %TIME%
echo Current Directory: %CD%
echo.
echo ====================================================
echo.

REM Change to installation directory if running from elsewhere
cd /d "%~dp0"

REM Check if the EXE exists
if exist "target\jpackage\HeThongBanVeTau-1.0.0.exe" (
    echo [INFO] Found EXE in target\jpackage\
    echo [INFO] Launching application...
    echo.
    start "" /WAIT "target\jpackage\HeThongBanVeTau-1.0.0.exe"
) else if exist "HeThongBanVeTau.exe" (
    echo [INFO] Found EXE in current directory
    echo [INFO] Launching application...
    echo.
    start "" /WAIT "HeThongBanVeTau.exe"
) else (
    echo [ERROR] Cannot find HeThongBanVeTau executable!
    echo [ERROR] Please make sure you are in the correct directory.
    echo.
    pause
    exit /b 1
)

echo.
echo ====================================================
echo   Application has exited
echo ====================================================
echo.

REM Check for log files on Desktop
set DESKTOP=%USERPROFILE%\Desktop

echo Checking for log files...
echo.

if exist "%DESKTOP%\HeThongBanVeTau-launched.txt" (
    echo === LAUNCH MARKER FOUND ===
    type "%DESKTOP%\HeThongBanVeTau-launched.txt"
    echo.
) else (
    echo [WARNING] No launch marker found on Desktop
)

if exist "%DESKTOP%\HeThongBanVeTau-startup.log" (
    echo === STARTUP LOG FOUND ===
    type "%DESKTOP%\HeThongBanVeTau-startup.log"
    echo.
) else (
    echo [INFO] No startup log found
)

if exist "%DESKTOP%\startup-error.log" (
    echo === ERROR LOG FOUND ===
    type "%DESKTOP%\startup-error.log"
    echo.
) else (
    echo [INFO] No error log found
)

echo.
echo Press any key to close this window...
pause >nul
