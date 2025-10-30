@echo off
echo Starting Personal Finance Tracker...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

echo Java found!
echo.
echo Building and starting the application...
echo This may take a few minutes on first run to download Maven and dependencies...
echo.

REM Build and run the application using Maven wrapper
call mvnw.cmd spring-boot:run

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to start the application
    echo Check the error messages above
    pause
    exit /b 1
)

pause