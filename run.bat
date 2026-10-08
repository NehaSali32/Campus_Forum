@echo off
title Campus Connect Platform
cd /d "%~dp0"

if exist "%LOCALAPPDATA%\jdks\jdk-25.0.2" (
    set "JAVA_HOME=%LOCALAPPDATA%\jdks\jdk-25.0.2"
)

set "PATH=%JAVA_HOME%\bin;%USERPROFILE%\.maven\apache-maven-3.9.6\bin;%PATH%"

echo =========================================================================
echo   CAMPUS CONNECT - College Communication and Management Platform
echo =========================================================================
echo.
echo Starting Spring Boot Web Server on http://localhost:8080 ...
echo Database: MySQL (campus_connect_db)
echo.

call "%USERPROFILE%\.maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
pause
