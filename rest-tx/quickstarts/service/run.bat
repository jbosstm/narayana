@echo off

echo "Running service quickstart"

mvn clean compile exec:exec
IF %ERRORLEVEL% NEQ 0 exit -1
