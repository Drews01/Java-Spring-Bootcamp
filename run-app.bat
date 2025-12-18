@echo off
echo ========================================
echo Spring Boot Application Launcher
echo ========================================
echo.
echo Compiling application...
cd /d "%~dp0"

call mvnw.cmd clean compile
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    echo Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Starting Spring Boot Application
echo ========================================
echo.
echo Application will run at: http://localhost:8081
echo.
echo Database: LoanDatabase
echo Instance: DESKTOP-08BOFEE\MSSQLSERVER01
echo.
echo Press Ctrl+C to stop the application
echo ========================================
echo.

java -cp "target/classes;%USERPROFILE%\.m2\repository\*" com.example.demo.BootcampJavaSpringApplication

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo Application stopped with error
    echo ========================================
    echo.
    echo Please run this application from your IDE instead:
    echo 1. Open IntelliJ IDEA or VS Code
    echo 2. Open: BootcampJavaSpringApplication.java
    echo 3. Click Run
    echo.
)

pause
