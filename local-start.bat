@echo off
cd /d "%~dp0"
java -Dspring.profiles.active=dev -jar parver-backend\target\parver-backend-0.1.0.jar
