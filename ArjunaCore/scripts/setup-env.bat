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
rem Arjuna Transaction Service
rem
rem Arjuna Technologies Ltd.
rem Copyright 2004
rem

if "%JAVA_HOME%"=="" goto java_home_error

echo Environment variable JAVA_HOME set to "%JAVA_HOME%"

if "%ARJUNATS_HOME%"=="" goto jts_home_error

echo Environment variable ARJUNATS_HOME set to "%ARJUNATS_HOME%"

rem Setup EXT classpath

echo Setting up environment

set PRODUCT_CLASSPATH=%ARJUNATS_HOME%\lib\@PRODUCT_NAME@.jar
set PRODUCT_CLASSPATH=%PRODUCT_CLASSPATH%;%ARJUNATS_HOME%\lib\@PRODUCT_NAME@-jacorb.jar
set PRODUCT_CLASSPATH=%PRODUCT_CLASSPATH%;%ARJUNATS_HOME%\bin\tsmx-tools.jar
set PRODUCT_CLASSPATH=%PRODUCT_CLASSPATH%;%ARJUNATS_HOME%\etc\

set EXT_CLASSPATH=%ARJUNATS_HOME%\lib\ext\jbossts-common.jar
set EXT_CLASSPATH=%EXT_CLASSPATH%;%ARJUNATS_HOME%\lib\ext\commons-logging.jar
set EXT_CLASSPATH=%EXT_CLASSPATH%;%ARJUNATS_HOME%\lib\ext\connector-api.jar
set EXT_CLASSPATH=%EXT_CLASSPATH%;%ARJUNATS_HOME%\lib\ext\jdbc2_0-stdext.jar
set EXT_CLASSPATH=%EXT_CLASSPATH%;%ARJUNATS_HOME%\lib\ext\jmxri.jar
set EXT_CLASSPATH=%EXT_CLASSPATH%;%ARJUNATS_HOME%\lib\ext\jndi.jar
set EXT_CLASSPATH=%EXT_CLASSPATH%;%ARJUNATS_HOME%\lib\ext\jta-spec1_0_1.jar
set EXT_CLASSPATH=%EXT_CLASSPATH%;%ARJUNATS_HOME%\lib\ext\log4j-1.2.8.jar
set EXT_CLASSPATH=%EXT_CLASSPATH%;%ARJUNATS_HOME%\lib\ext\xercesImpl.jar
set EXT_CLASSPATH=%EXT_CLASSPATH%;%ARJUNATS_HOME%\lib\ext\xmlParserAPIs.jar

set JACORB_HOME=PUT_JACORB_HOME_HERE
set JACORB_CLASSPATH=%JACORB_HOME%\lib\jacorb.jar
set JACORB_CLASSPATH=%JACORB_CLASSPATH%;%JACORB_HOME%\lib\idl.jar
set JACORB_CLASSPATH=%JACORB_CLASSPATH%;%JACORB_HOME%\lib\logkit-1.2.jar
set JACORB_CLASSPATH=%JACORB_CLASSPATH%;%JACORB_HOME%\lib\avalon-framework-4.1.5.jar
set JACORB_CLASSPATH=%JACORB_CLASSPATH%;%JACORB_HOME%\etc

set CLASSPATH=.;%PRODUCT_CLASSPATH%;%EXT_CLASSPATH%;%JACORB_CLASSPATH%

goto end

:java_home_error
echo Environment variable JAVA_HOME not set
goto end

:jts_home_error
echo Environment variable ARJUNATS_HOME not set
goto end

:end
