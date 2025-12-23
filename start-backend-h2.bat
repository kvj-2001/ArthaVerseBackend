@echo off
echo Starting Billing Backend Application...
echo.

REM Check if Java is available
java -version 2>nul
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    pause
    exit /b 1
)

REM Check if Maven is available
mvn -version 2>nul
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    pause
    exit /b 1
)

echo Compiling application...
mvn clean compile
if errorlevel 1 (
    echo ERROR: Compilation failed
    pause
    exit /b 1
)

echo Starting application with H2 database...
mvn spring-boot:run -Dspring-boot.run.profiles=h2

pause