@echo off
setlocal
set "APP_HOME=%~dp0"
set "JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
set "URL=https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar"
set "SHA=2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046"

if not exist "%JAR%" (
  echo Gradle wrapper JAR is missing; downloading Gradle 8.11.1 bootstrap...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing '%URL%' -OutFile '%JAR%'"
  if errorlevel 1 exit /b 1
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$h=(Get-FileHash -Algorithm SHA256 '%JAR%').Hash.ToLower(); if ($h -ne '%SHA%') { exit 2 }"
  if errorlevel 1 (
    del /q "%JAR%" 2>nul
    echo Gradle wrapper checksum verification failed.
    exit /b 1
  )
)

java -Xmx64m -Xms64m -classpath "%JAR%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
