@echo off
setlocal
set GRADLE_VERSION=9.2.1
set APP_HOME=%~dp0
set DIST_DIR=%APP_HOME%.gradle\bootstrap
set GRADLE_HOME=%DIST_DIR%\gradle-%GRADLE_VERSION%
set ZIP_FILE=%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip
set DIST_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip

where java >nul 2>nul
if errorlevel 1 (
  echo Java was not found on PATH. Minecraft 1.21.11/Fabric needs Java 21.
  exit /b 1
)

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ZIP_FILE%'"
  if errorlevel 1 exit /b 1
  echo Unpacking Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force -Path '%ZIP_FILE%' -DestinationPath '%DIST_DIR%'"
  if errorlevel 1 exit /b 1
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
