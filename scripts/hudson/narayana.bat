call:comment_on_pull "Started testing this pull request: %BUILD_URL%"

call build.bat clean install "-DskipTests" || (call:comment_on_pull "Narayana Failed %BUILD_URL%" && exit -1)

rem echo "Cloning AS"
rem rmdir /S /Q jboss-as
rem git clone https://github.com/jbosstm/jboss-as.git
rem if %ERRORLEVEL% NEQ 0 exit -1
rem cd jboss-as
rem git checkout -t origin/4_BRANCH
rem if %ERRORLEVEL% NEQ 0 exit -1
rem git remote add upstream https://github.com/jbossas/jboss-as.git
rem git pull --rebase --ff-only -s recursive -Xtheirs upstream master
rem if %ERRORLEVEL% NEQ 0 exit -1

rem echo "Building AS"
rem set MAVEN_OPTS="-Xmx768M"
rem build.bat clean install -DskipTests

call:comment_on_pull "All tests passed - Job complete %BUILD_URL%"

rem -------------------------------------------------------
rem -                 Functions bellow                    -
rem -------------------------------------------------------

goto:eof

:comment_on_pull
   if not "%COMMENT_ON_PULL%"=="1" goto:eof

   for /f "tokens=1,2,3,4 delims=/" %%a in ("%GIT_BRANCH%") do set IS_PULL=%%b&set PULL_NUM=%%c
   if not "%IS_PULL%"=="pull" goto:eof
   
   curl -k -d "{ \"body\": \"%~1\" }" -H "Authorization: token %GITHUB_TOKEN%" https://api.github.com/repos/%GIT_ACCOUNT%/%GIT_REPO%/issues/%PULL_NUM%/comments

goto:eof
