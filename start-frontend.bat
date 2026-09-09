@echo off
title SupportAI - Frontend Web Portal (Port 3000)
echo ========================================================
echo Starting SupportAI Frontend (React 19 on Port 3000)
echo ========================================================
cd /d "%~dp0frontend"
call npm run dev
pause
