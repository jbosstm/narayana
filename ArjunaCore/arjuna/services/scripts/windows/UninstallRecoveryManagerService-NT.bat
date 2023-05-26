rem
rem SPDX short identifier: Apache-2.0
rem
@echo off
rem
rem Find the application home.
rem
if "%OS%"=="Windows_NT" goto nt

echo This is not NT, so please edit this script and set _APP_HOME manually
set _APP_HOME=..

goto conf

:nt
rem %~dp0 is name of current script under NT
set _APP_HOME=%~dp0
rem : operator works similar to make : operator
set _APP_HOME=%_APP_HOME:\bin\windows\=%


rem
rem Find the wrapper.conf
rem
:conf
set _WRAPPER_CONF="%~f1"
if not %_WRAPPER_CONF%=="" goto startup
set _WRAPPER_CONF="%_APP_HOME%\config\recoveryservice.conf"


rem
rem Run the application.
rem At runtime, the current directory will be that of Wrapper.exe
rem
:startup
"%_APP_HOME%\bin\windows\Wrapper.exe" -r %_WRAPPER_CONF%
if not errorlevel 1 goto end
pause

:end
set _APP_HOME=
set _WRAPPER_CONF=

