call:comment_on_pull "Started testing this pull request: %BUILD_URL%"

call build.bat clean install "-DskipTests" || (call:comment_on_pull "Narayana Failed %BUILD_URL%" && exit -1)

echo "Cloning AS"
rmdir /S /Q jboss-as
git clone https://github.com/jbosstm/jboss-as.git
if %ERRORLEVEL% NEQ 0 exit -1
cd jboss-as
git remote add upstream https://github.com/wildfly/wildfly.git
git pull --rebase --ff-only -s recursive -Xtheirs upstream master
if %ERRORLEVEL% NEQ 0 exit -1

echo "Building AS"
set MAVEN_OPTS="-Xmx768M"
call build.bat clean install "-DskipTests" "-Drelease=true" || (call:comment_on_pull "AS Failed %BUILD_URL%" && exit -1)

echo "Building BlackTie
cd ..\blacktie
rmdir wildfly-8.0.0.Alpha3-SNAPSHOT /s /q
unzip ..\jboss-as\dist\target\wildfly-8.0.0.Alpha3-SNAPSHOT.zip
set JBOSS_HOME=%CD%\wildfly-8.0.0.Alpha3-SNAPSHOT\
copy ..\rest-tx\webservice\target\restat-web-%NARAYANA_CURRENT_VERSION%.war %JBOSS_HOME%\standalone\deployments\
set WORKSPACE=%WORKSPACE%\blacktie 
call scripts\hudson\blacktie-vc9x32.bat || (call:comment_on_pull "BlackTie Failed %BUILD_URL%" && exit -1)

call:comment_on_pull "All tests passed - Job complete %BUILD_URL%"

rem -------------------------------------------------------
rem -                 Functions bellow                    -
rem -------------------------------------------------------

goto:eof

:comment_on_pull
   if not "%COMMENT_ON_PULL%"=="1" goto:eof

   for /f "tokens=1,2,3,4 delims=/" %%a in ("%GIT_BRANCH%") do set IS_PULL=%%b&set PULL_NUM=%%c
   if not "%IS_PULL%"=="pull" goto:eof
   
   curl -d "{ \"body\": \"%~1\" }" -ujbosstm-bot:%BOT_PASSWORD% https://api.github.com/repos/%GIT_ACCOUNT%/%GIT_REPO%/issues/%PULL_NUM%/comments

goto:eof
