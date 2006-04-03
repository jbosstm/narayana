rem
rem JBoss, Home of Professional Open Source
rem Copyright 2006, JBoss Inc., and others contributors as indicated 
rem by the @authors tag. All rights reserved. 
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
"%_APP_HOME%\bin\windows\Wrapper.exe" -c %_WRAPPER_CONF%
if not errorlevel 1 goto end
pause

:end
set _APP_HOME=
set _WRAPPER_CONF=

