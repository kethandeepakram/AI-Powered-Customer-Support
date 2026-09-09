@echo off
title SupportAI - Backend Server (Port 8080)
echo ========================================================
echo Starting SupportAI Backend (Spring Boot 3.4 on Port 8080)
echo ========================================================
cd /d "%~dp0backend"
if exist target\supportai-backend-1.0.0.jar (
    java -jar target\supportai-backend-1.0.0.jar
) else (
    call .\mvnw.cmd spring-boot:run
)
pause
