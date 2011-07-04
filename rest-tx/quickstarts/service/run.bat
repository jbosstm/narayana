@echo off

echo "Running service quickstart"

mvn compile exec:exec
IF %ERRORLEVEL% NEQ 0 exit -1
