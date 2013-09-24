call:comment_on_pull "Started testing this pull request with BLACKTIE profile on Windows: %BUILD_URL%"

call build.bat clean install "-DskipTests" || (call:comment_on_pull "BLACKTIE profile tests failed on Windows - Narayana Failed %BUILD_URL%" && exit -1)

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
call build.bat clean install "-DskipTests" "-Drelease=true" || (call:comment_on_pull "BLACKTIE profile tests failed on Windows - AS Failed %BUILD_URL%" && exit -1)

echo "Building Blacktie Subsystem"
cd ..\
call build.bat -f blacktie\wildfly-blacktie\pom.xml clean install || (call:comment_on_pull "BLACKTIE profile tests failed on Windows - Build Blacktie Subsystem Failed %BUILD_URL%" && exit -1)

echo "Building BlackTie
cd blacktie
rmdir wildfly-%WILDFLY_MASTER_VERSION% /s /q
unzip ..\jboss-as\dist\target\wildfly-%WILDFLY_MASTER_VERSION%.zip
set JBOSS_HOME=%CD%\wildfly-%WILDFLY_MASTER_VERSION%\
unzip wildfly-blacktie\build\target\wildfly-blacktie-build-%WILDFLY_MASTER_VERSION%-bin.zip -d %JBOSS_HOME%
set WORKSPACE=%WORKSPACE%\blacktie 
call scripts\hudson\blacktie-vc9x32.bat || (call:comment_on_pull "BLACKTIE profile tests failed on Windows - BlackTie Failed %BUILD_URL%" && exit -1)

call:comment_on_pull "BLACKTIE profile tests passed on Windows - Job complete %BUILD_URL%"

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
