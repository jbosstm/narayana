call:comment_on_pull "Starting tests %BUILD_URL%"

call build.bat clean install "-DskipTests" || (call:comment_on_pull "Tests Failed" && exit -1)

echo "Cloning AS"
rmdir /S /Q jboss-as
git clone https://github.com/jbosstm/jboss-as.git
if %ERRORLEVEL% NEQ 0 exit -1
cd jboss-as
git checkout -t origin/5_BRANCH
if %ERRORLEVEL% NEQ 0 exit -1
git remote add upstream https://github.com/wildfly/wildfly.git
git pull --rebase --ff-only -s recursive -Xtheirs upstream master
if %ERRORLEVEL% NEQ 0 exit -1

echo "Building AS"
set MAVEN_OPTS="-Xmx768M"
call build.bat clean install -DskipTests || (call:comment_on_pull "Tests Failed" && exit -1)

call:comment_on_pull "Tests Passed"

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
