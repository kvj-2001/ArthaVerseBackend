#!/bin/bash

echo "===================================="
echo "   Billing App - Frontend Startup"
echo "===================================="
echo

echo "Checking Node.js version..."
if ! command -v node &> /dev/null; then
    echo "ERROR: Node.js is not installed or not in PATH"
    echo "Please install Node.js 16 or higher"
    exit 1
fi
node --version

echo
echo "Checking npm version..."
if ! command -v npm &> /dev/null; then
    echo "ERROR: npm is not available"
    exit 1
fi
npm --version

echo
echo "Navigating to frontend directory..."
cd frontend

echo
echo "Installing dependencies..."
npm install
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to install dependencies"
    exit 1
fi

echo
echo "Starting Frontend Application..."
echo "Frontend will be available at: http://localhost:3000"
echo "Make sure the backend is running on port 8080"
echo

npm start
