@echo off
echo ====================================
echo    Billing App - Backend Startup
echo ====================================
echo.

echo Checking Java version...
java -version
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

echo.
echo Checking Maven...
mvn --version
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven 3.6+ or use the Maven wrapper
    pause
    exit /b 1
)

echo.
echo Starting Backend Application...
echo Backend will be available at: http://localhost:8080
echo Swagger UI will be available at: http://localhost:8080/swagger-ui/index.html
echo.

mvn spring-boot:run

pause
