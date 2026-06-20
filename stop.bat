@echo off
title Library System - Stop

echo ========================================
echo   Library Management System
echo   Stop All Services
echo ========================================
echo.

REM --- Stop processes on port 9090 (Backend) ---
echo [1/2] Stopping Backend (port 9090) ...
set found=0
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":9090" ^| findstr "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1
    echo       Stopped process PID: %%a
    set found=1
)
if %found%==0 echo       No process on port 9090

REM --- Stop processes on port 3000 (Frontend) ---
echo.
echo [2/2] Stopping Frontend (port 3000) ...
set found=0
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000" ^| findstr "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1
    echo       Stopped process PID: %%a
    set found=1
)
if %found%==0 echo       No process on port 3000

REM --- Clean up orphaned maven/node processes ---
taskkill /F /FI "WINDOWTITLE eq Backend" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Frontend" >nul 2>&1

echo.
echo ========================================
echo   All services stopped.
echo ========================================
echo.
pause
