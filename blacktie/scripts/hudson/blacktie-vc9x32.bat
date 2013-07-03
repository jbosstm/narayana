set NOPAUSE=true

rem SHUTDOWN ANY PREVIOUS BUILD REMNANTS
FOR /F "usebackq tokens=5" %%i in (`"netstat -ano|findstr 9999.*LISTENING"`) DO taskkill /F /PID %%i
tasklist
taskkill /F /IM mspdbsrv.exe
taskkill /F /IM testsuite.exe
taskkill /F /IM server.exe
taskkill /F /IM client.exe
taskkill /F /IM cs.exe
tasklist

if not defined WORKSPACE (call:fail_build && exit -1)

if not defined JBOSSAS_IP_ADDR echo "JBOSSAS_IP_ADDR not set" & for /f "delims=" %%a in ('hostname') do @set JBOSSAS_IP_ADDR=%%a

rem INITIALIZE JBOSS
cd %WORKSPACE%
call ant -f scripts/hudson/initializeJBoss.xml -DJBOSS_HOME=%JBOSS_HOME% -Dbasedir=. initializeJBoss -debug || (call:fail_build && exit -1)

rem wget -P jboss-as\standalone\deployments\ -N http://172.17.131.2/job/narayana-populateM2-taconic/lastSuccessfulBuild/artifact/rest-tx/webservice/target/restat-web-5.0.0.M2-SNAPSHOT.war
rem IF %ERRORLEVEL% NEQ 0 call:comment_on_pull "Can not wget restat-web war" & exit -1

set JBOSS_HOME=

rem START JBOSS
cd jboss-as\bin
rem set JAVA_OPTS="%JAVA_OPTS% -Xmx1024m -XX:MaxPermSize=512m"
start /B standalone.bat -c standalone-full.xml -Djboss.bind.address=%JBOSSAS_IP_ADDR% -Djboss.bind.address.unsecure=%JBOSSAS_IP_ADDR%
echo "Started server"
@ping 127.0.0.1 -n 20 -w 1000 > nul

rem BUILD BLACKTIE
cd %WORKSPACE%
call build.bat clean install "-Djbossas.ip.addr=%JBOSSAS_IP_ADDR%" || (call:fail_build && exit -1)

rem SHUTDOWN ANY PREVIOUS BUILD REMNANTS
tasklist & FOR /F "usebackq tokens=5" %%i in (`"netstat -ano|findstr 9999.*LISTENING"`) DO taskkill /F /PID %%i
echo "Finished build"

rem -------------------------------------------------------
rem -                 Functions bellow                    -
rem -------------------------------------------------------

goto:eof

:fail_build
  call:comment_on_pull "Build failed %BUILD_URL%"
  tasklist & FOR /F "usebackq tokens=5" %%i in (`"netstat -ano|findstr 9999.*LISTENING"`) DO taskkill /F /PID %%i
  exit -1
goto:eof

:comment_on_pull
   if not "%COMMENT_ON_PULL%"=="1" goto:eof

   for /f "tokens=1,2,3,4 delims=/" %%a in ("%GIT_BRANCH%") do set IS_PULL=%%b&set PULL_NUM=%%c
   if not "%IS_PULL%"=="pull" goto:eof
   
   curl -d "{ \"body\": \"%~1\" }" -ujbosstm-bot:%BOT_PASSWORD% https://api.github.com/repos/%GIT_ACCOUNT%/%GIT_REPO%/issues/%PULL_NUM%/comments

goto:eof
