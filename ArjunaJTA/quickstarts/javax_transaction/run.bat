@echo off

echo "Running maven quickstart"

mvn compile exec:exec
IF %ERRORLEVEL% NEQ 0 exit -1
