@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0gradlew.ps1" %*
exit /b %ERRORLEVEL%
