@echo off

set PWD=%~dp0

SETLOCAL

set CLASSPATH=
set M2_HOME=
set MAVEN_HOME=

set MAVEN_OPTS=%MAVEN_OPTS% -Xms1024m -XX:MaxMetaspaceSize=256m
set MVN_OPTIONS=-gs .mvn\wrapper\settings.xml -Dorson.jar.location=%PWD%\ext\ -Dbpa=vc9x32

set MVN=%PWD%mvnw.cmd
set GOAL=%1
if "%GOAL%"=="" set GOAL=install
echo Calling %MVN% %MVN_OPTIONS% %GOAL% %2 %3 %4 %5 %6 %7 %8
call %MVN% %MVN_OPTIONS% %GOAL% %2 %3 %4 %5 %6 %7 %8

if "%NOPAUSE%" == "" pause
