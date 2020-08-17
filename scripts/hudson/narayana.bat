if not defined WORKSPACE (call:fail_build & exit -1)

set NOPAUSE=true
@echo off

call:comment_on_pull "Started testing this pull request on Windows: %BUILD_URL%"

java -version 2>tmp.txt
findstr version tmp.txt > version.txt
FOR /F delims^=^"^ tokens^=2 %%i in (version.txt) DO @set JAVA_VERSION=%%i
if %JAVA_VERSION% == 9 set MAVEN_OPTS="--add-modules=java.corba,java.xml.bind,java.xml.ws"

IF NOT [%NOTMAIN%] == [] (call:build_spi_pr
call build.bat clean install %* || (call:comment_on_pull "Tests failed on Windows - Narayana Failed %BUILD_URL%" & exit -1)
)
IF NOT [%NOTBT%] == [] (for /f "usebackq delims=<,> tokens=3" %%i in (`findstr "version.org.wildfly.wildfly-parent" blacktie\pom.xml`) do @set WILDFLY_MASTER_VERSION=%%i
if not defined JBOSSAS_IP_ADDR echo "JBOSSAS_IP_ADDR not set" & for /f "delims=" %%a in ('hostname') do @set JBOSSAS_IP_ADDR=%%a
)
IF NOT [%NOTBT%] == [] (set JBOSS_HOME=%WORKSPACE%\blacktie\wildfly-%WILDFLY_MASTER_VERSION%
)
IF NOT [%NOTBT%] == [] (echo "set WILDFLY_MASTER_VERSION=%WILDFLY_MASTER_VERSION% set JBOSSAS_IP_ADDR=%JBOSSAS_IP_ADDR%"
rem SHUTDOWN ANY PREVIOUS BUILD REMNANTS
FOR /F "usebackq tokens=5" %%i in (`"netstat -ano|findstr 9990.*LISTENING"`) DO taskkill /F /PID %%i
tasklist
taskkill /F /IM mspdbsrv.exe
taskkill /F /IM testsuite.exe
taskkill /F /IM server.exe
taskkill /F /IM client.exe
taskkill /F /IM cs.exe
wmic Path win32_process Where "Caption Like '%java.exe%' AND CommandLine Like '%standalone%'" Call Terminate
tasklist

echo "Building Blacktie Subsystem"
call build.bat -f blacktie\wildfly-blacktie\pom.xml clean install %* || (call:comment_on_pull "Tests failed on Windows - Build Blacktie Subsystem Failed %BUILD_URL%" & exit -1)

echo "Installing app server"
cd blacktie
rmdir wildfly-%WILDFLY_MASTER_VERSION% /s /q
IF NOT EXIST wildfly-%WILDFLY_MASTER_VERSION%.zip copy c:\Users\hudson\Downloads\wildfly-%WILDFLY_MASTER_VERSION%.zip .
if %ERRORLEVEL% NEQ 0 (call:comment_on_pull "Pull failed on Windows - could not download http://download.jboss.org/wildfly/%WILDFLY_MASTER_VERSION%/wildfly-%WILDFLY_MASTER_VERSION%.zip" & exit -1)
unzip wildfly-%WILDFLY_MASTER_VERSION%
rem REPLACE THE OPENJDK-ORB WITH THE 8.0.8.Final
wget --no-check-certificate https://repository.jboss.org/nexus/content/repositories/releases/org/jboss/openjdk-orb/openjdk-orb/8.0.8.Final/openjdk-orb-8.0.8.Final.jar -O %JBOSS_HOME%\modules\system\layers\base\javax\orb\api\main\openjdk-orb-8.0.8.Final.jar
set OPENJDK_ORB_MODULE_XML=%JBOSS_HOME%\modules\system\layers\base\javax\orb\api\main\module.xml
powershell -Command "((Get-Content ($env:OPENJDK_ORB_MODULE_XML)).replace('8.0.6.Final', '8.0.8.Final') | Set-Content ($env:OPENJDK_ORB_MODULE_XML))"
unzip wildfly-blacktie\build\target\wildfly-blacktie-build-5.10.6.Final-SNAPSHOT-bin.zip -d %JBOSS_HOME%
cd ..\

rem INITIALIZE JBOSS

call ant -f blacktie\scripts\hudson\initializeJBoss.xml -DJBOSS_HOME=%JBOSS_HOME% initializeJBoss -debug || (call:fail_build & exit -1)

rem START JBOSS
rem set JAVA_OPTS="%JAVA_OPTS% -Xmx1024m -XX:MaxPermSize=512m"
start /B %JBOSS_HOME%\bin\standalone.bat -c standalone-blacktie.xml -Djboss.bind.address=%JBOSSAS_IP_ADDR% -Djboss.bind.address.unsecure=%JBOSSAS_IP_ADDR% -Djboss.bind.address.management=%JBOSSAS_IP_ADDR%
echo "Started server"
@ping 127.0.0.1 -n 20 -w 1000 > nul

rem BUILD BLACKTIE
set JAVA_OPTS="%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError"
call build.bat -f blacktie\pom.xml clean install "-Djbossas.ip.addr=%JBOSSAS_IP_ADDR%" %* || (call:fail_build & exit -1)

rem SHUTDOWN ANY PREVIOUS BUILD REMNANTS
tasklist & FOR /F "usebackq tokens=5" %%i in (`"netstat -ano|findstr 9990.*LISTENING"`) DO taskkill /F /PID %%i
)

echo "Finished build"

call:comment_on_pull "Tests passed on Windows - Job complete %BUILD_URL%"

rem ------------------------------------------------------
rem -                 Functions below                    -
rem ------------------------------------------------------

goto:eof

:build_spi_pr
  if "%SPI_BRANCH%"=="" goto:eof
  
  echo "Cloning SPI"
  rmdir /S /Q jboss-transaction-spi
  git clone https://github.com/jbosstm/jboss-transaction-spi.git || (call:comment_on_pull "SPI clone Failed %BUILD_URL%" & exit -1)
  cd jboss-transaction-spi
  if defined SPI_BRANCH git fetch origin +refs/pull/*/head:refs/remotes/jbosstm/pull/*/head & git checkout %SPI_BRANCH%
  if %ERRORLEVEL% NEQ 0 exit -1
  cd ..
  call build.bat clean install "-f" "jboss-transaction-spi\pom.xml" || (call:comment_on_pull "SPI build Failed %BUILD_URL%" & exit -1)
goto:eof

:fail_build
  call:comment_on_pull "Build failed %BUILD_URL%"
  tasklist & FOR /F "usebackq tokens=5" %%i in (`"netstat -ano|findstr 9990.*LISTENING"`) DO taskkill /F /PID %%i
  tasklist
  taskkill /F /IM mspdbsrv.exe
  taskkill /F /IM testsuite.exe
  taskkill /F /IM server.exe
  taskkill /F /IM client.exe
  taskkill /F /IM cs.exe
  wmic Path win32_process Where "Caption Like '%java.exe%' AND CommandLine Like '%standalone%'" Call Terminate
  tasklist
  exit -1
goto:eof

:comment_on_pull
   if not "%COMMENT_ON_PULL%"=="1" goto:eof

   for /f "tokens=1,2,3,4 delims=/" %%a in ("%GIT_BRANCH%") do set IS_PULL=%%b&set PULL_NUM=%%c
   if not "%IS_PULL%"=="pull" goto:eof
   
   curl -k -d "{ \"body\": \"%~1\" }" -H "Authorization: token %GITHUB_TOKEN%" https://api.github.com/repos/%GIT_ACCOUNT%/%GIT_REPO%/issues/%PULL_NUM%/comments

goto:eof
