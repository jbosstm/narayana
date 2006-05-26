rem
rem JBoss, Home of Professional Open Source
rem Copyright 2006, JBoss Inc., and individual contributors as indicated
rem by the @authors tag.  All rights reserved. 
rem See the copyright.txt in the distribution for a full listing 
rem of individual contributors.
rem This copyrighted material is made available to anyone wishing to use,
rem modify, copy, or redistribute it subject to the terms and conditions
rem of the GNU General Public License, v. 2.0.
rem This program is distributed in the hope that it will be useful, but WITHOUT A 
rem WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
rem PARTICULAR PURPOSE.  See the GNU General Public License for more details.
rem You should have received a copy of the GNU General Public License,
rem v. 2.0 along with this distribution; if not, write to the Free Software
rem Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
rem MA  02110-1301, USA.
rem 
rem (C) 2005-2006,
rem @author JBoss Inc.
rem
@echo off
rem ###########################################################################
rem # Wrapper script for building and deploying xts-demo                      #
rem # Usage: build.bat [weblogic|jboss] <target>                              #
rem ###########################################################################

if "%OS" == "Windows_NT" @setlocal

set PROGRAM=build.bat
if "%OS%" == "Windows_NT" set PROGNAME=%~nx0

if "%1" == "" goto USAGE

set PROPERTYFILE=%1.properties
if exist "%PROPERTYFILE%" goto RUN

:USAGE
echo Usage: %PROGRAM% [weblogic|jboss|webmethods] <target>
goto END

:RUN
shift
ant -Dxmlpropertyfile="%PROPERTYFILE%" %1 %2 %3 %4 %5 %6 %7 %8 %9
:END
if "%OS" == "Windows_NT" @endlocal
