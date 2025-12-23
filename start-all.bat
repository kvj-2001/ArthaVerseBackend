@echo off
echo ====================================
echo     Billing App - Full Startup
echo ====================================
echo.

echo This script will start both backend and frontend
echo Make sure you have Java 17+, Maven, and Node.js installed
echo.
pause

echo Starting Backend in a new window...
start "Backend - Billing App" cmd /k "start-backend.bat"

echo Waiting 30 seconds for backend to start...
timeout /t 30 /nobreak

echo Starting Frontend in a new window...
start "Frontend - Billing App" cmd /k "start-frontend.bat"

echo.
echo ====================================
echo Both applications are starting...
echo Backend: http://localhost:8080
echo Frontend: http://localhost:3000
echo ====================================
echo.
echo Press any key to exit this window...
pause
