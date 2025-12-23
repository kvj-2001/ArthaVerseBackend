#!/bin/bash

echo "===================================="
echo "   Billing App - Backend Startup"
echo "===================================="
echo

echo "Checking Java version..."
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    exit 1
fi
java -version

echo
echo "Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven 3.6+ or use the Maven wrapper"
    exit 1
fi
mvn --version

echo
echo "Starting Backend Application..."
echo "Backend will be available at: http://localhost:8080"
echo "Swagger UI will be available at: http://localhost:8080/swagger-ui/index.html"
echo

mvn spring-boot:run
