@echo off

echo "Running simple quickstart"

mvn compile exec:exec
IF %ERRORLEVEL% NEQ 0 exit -1
