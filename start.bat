@echo off
title Library System - Start
setlocal enabledelayedexpansion

echo ========================================
echo   Library Management System
echo   Start All Services
echo ========================================
echo.

REM --- Start Backend ---
echo [1/2] Starting Backend on port 9090 ...
start "Backend" mvn -f "%~dp0library-server\pom.xml" spring-boot:run
echo       Backend launching ...

REM --- Wait for backend ready (poll port 9090) ---
echo       Waiting for backend ready ...
:wait_backend
ping 127.0.0.1 -n 3 >nul 2>&1
netstat -ano | findstr ":9090 " | findstr "LISTENING" >nul 2>&1
if errorlevel 1 goto wait_backend
echo       Backend ready.

REM --- Start Frontend ---
echo.
echo [2/2] Starting Frontend on port 3000 ...
cd /d "%~dp0library-web"

if not exist node_modules (
    echo       Installing npm packages ...
    call npm install
)

start "Frontend" npm run dev
cd /d "%~dp0"

echo.
echo ========================================
echo   All services started!
echo   Backend : http://localhost:9090
echo   Frontend: http://localhost:3000
echo   API Doc : http://localhost:9090/doc.html
echo ========================================
echo.
echo   Close this window to keep services running.
pause
