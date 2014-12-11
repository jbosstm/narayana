@echo off
rem
rem JBoss, Home of Professional Open Source
rem Copyright 2008, Red Hat, Inc., and others contributors as indicated
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

set BLACKTIE_HOME=REPLACE_WITH_INSTALL_LOCATION
set JBOSSAS_IP_ADDR=REPLACE_WITH_JBOSSAS_IP_ADDR
for /f "delims=" %%a in ('hostname') do @set HOSTNAME=%%a

set BLACKTIE_SCHEMA_DIR=%BLACKTIE_HOME%\xsd

set PATH=%BLACKTIE_HOME%\bin;%PATH%
set PATH=%BLACKTIE_HOME%\lib\cxx;%PATH%
set PATH=.;%PATH%

setlocal ENABLEDELAYEDEXPANSION
FOR /R %BLACKTIE_HOME%\lib\java %%G IN (*.jar) DO set CLASSPATH=%%G;!CLASSPATH!
endlocal & set CLASSPATH=%CLASSPATH%
set CLASSPATH=.;%CLASSPATH%
