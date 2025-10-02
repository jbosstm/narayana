@echo off

echo "Running txoj quickstart"

mvn compile exec:exec
IF %ERRORLEVEL% NEQ 0 exit -1
