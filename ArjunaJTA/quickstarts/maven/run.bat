@echo off

echo "Running quickstart"

mvn compile exec:exec
IF %ERRORLEVEL% NEQ 0 exit -1
