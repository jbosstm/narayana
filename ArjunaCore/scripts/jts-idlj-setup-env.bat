rem
rem SPDX short identifier: Apache-2.0
rem
@echo off

if "%JAVA_HOME%"=="" goto java_home_error

echo Environment variable JAVA_HOME set to "%JAVA_HOME%"

if "%NARAYANA_HOME%"=="" goto home_error

echo Environment variable NARAYANA_HOME set to "%NARAYANA_HOME%"

rem Setup EXT classpath

echo Setting up environment

set PRODUCT_CLASSPATH=%NARAYANA_HOME%\lib\jts\narayana-jts.jar
set PRODUCT_CLASSPATH=%PRODUCT_CLASSPATH%;%NARAYANA_HOME%\etc\

setlocal ENABLEDELAYEDEXPANSION
FOR /R %NARAYANA_HOME%\lib\ext %%G IN (*.jar) DO set EXT_CLASSPATH=%%G;!EXT_CLASSPATH!
endlocal & set EXT_CLASSPATH=%EXT_CLASSPATH%

set CLASSPATH=.;%PRODUCT_CLASSPATH%;%EXT_CLASSPATH%;%NARAYANA_HOME%\lib\jts\narayana-jts-idlj.jar

goto end

:java_home_error
echo Environment variable JAVA_HOME not set
goto end

:home_error
echo Environment variable NARAYANA_HOME not set
goto end

:end
