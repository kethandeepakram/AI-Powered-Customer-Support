@echo off
title SupportAI - Launch Platform
echo ========================================================
echo Launching SupportAI Fullstack Platform
echo ========================================================
echo Starting Backend in a separate window...
start "SupportAI Backend" "%~dp0start-backend.bat"

echo Waiting for backend initialization...
timeout /t 6 /nobreak >nul

echo Starting Frontend in a separate window...
start "SupportAI Frontend" "%~dp0start-frontend.bat"

echo.
echo Both servers started!
echo Frontend: http://localhost:3000
echo Backend:  http://localhost:8080
echo.
echo Press any key to exit this launcher window (services will stay running).
pause >nul
