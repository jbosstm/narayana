rem JBoss, Home of Professional Open Source
rem Copyright 2006, Red Hat Middleware LLC, and individual contributors
rem as indicated by the @author tags.
rem See the copyright.txt in the distribution for a
rem full listing of individual contributors.
rem This copyrighted material is made available to anyone wishing to use,
rem modify, copy, or redistribute it subject to the terms and conditions
rem of the GNU Lesser General Public License, v. 2.1.
rem This program is distributed in the hope that it will be useful, but WITHOUT A
rem WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
rem PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
rem You should have received a copy of the GNU Lesser General Public License,
rem v.2.1 along with this distribution; if not, write to the Free Software
rem Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
rem MA  02110-1301, USA.
rem
rem (C) 2005-2006,
rem @author JBoss Inc.
rem
@echo off

rem Arjuna Technologies Ltd.
rem Copyright 2004
rem

if "%JAVA_HOME%"=="" goto java_home_error

echo Environment variable JAVA_HOME set to "%JAVA_HOME%"

if "%NARAYANA_HOME%"=="" goto home_error

echo Environment variable NARAYANA_HOME set to "%NARAYANA_HOME%"

rem Setup EXT classpath

echo Setting up environment

set PRODUCT_CLASSPATH=%NARAYANA_HOME%\lib\jta\narayana-jta.jar
set PRODUCT_CLASSPATH=%PRODUCT_CLASSPATH%;%NARAYANA_HOME%\etc\

setlocal ENABLEDELAYEDEXPANSION
FOR /R %NARAYANA_HOME%\lib\ext %%G IN (*.jar) DO set EXT_CLASSPATH=%%G;!EXT_CLASSPATH!
endlocal & set EXT_CLASSPATH=%EXT_CLASSPATH%


set CLASSPATH=.;%PRODUCT_CLASSPATH%;%EXT_CLASSPATH%

goto end

:java_home_error
echo Environment variable JAVA_HOME not set
goto end

:home_error
echo Environment variable NARAYANA_HOME not set
goto end

:end
