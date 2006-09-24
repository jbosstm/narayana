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

if "%@HOME_DIRECTORY@%"=="" goto atserror

rem Setup the environment for the JBoss Transaction Service
call "%@HOME_DIRECTORY@%\bin\setup-env.bat"

rem Add ext libraries required for tools
set CLASSPATH=%CLASSPATH%;%@HOME_DIRECTORY@%\bin\tools\ext\jfreechart-0.9.15.jar
set CLASSPATH=%CLASSPATH%;%@HOME_DIRECTORY@%\bin\tools\ext\jcommon-0.9.0.jar

"%JAVA_HOME%\bin\java" "-Dcom.arjuna.mw.ArjunaToolsFramework.lib=%@HOME_DIRECTORY@%\bin\tools" com.arjuna.ats.tools.toolsframework.ArjunaToolsFramework
goto finished

:atserror
echo The environment variable @HOME_DIRECTORY@ is not set
goto finished

:finished
