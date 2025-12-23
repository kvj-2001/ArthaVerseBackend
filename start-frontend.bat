@echo off
echo ====================================
echo   Billing App - Frontend Startup
echo ====================================
echo.

echo Checking Node.js version...
node --version
if %ERRORLEVEL% neq 0 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js 16 or higher
    pause
    exit /b 1
)

echo.
echo Checking npm version...
npm --version
if %ERRORLEVEL% neq 0 (
    echo ERROR: npm is not available
    pause
    exit /b 1
)

echo.
echo Navigating to frontend directory...
cd frontend

echo.
echo Installing dependencies...
npm install
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to install dependencies
    pause
    exit /b 1
)

echo.
echo Starting Frontend Application...
echo Frontend will be available at: http://localhost:3000
echo Make sure the backend is running on port 8080
echo.

npm start

pause
