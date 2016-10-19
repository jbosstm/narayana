if not defined WORKSPACE (call:fail_build & exit -1)

call:comment_on_pull "Started testing this pull request on Windows: %BUILD_URL%"

call:build_spi_pr

call build.bat clean install %* || (call:comment_on_pull "Windows - Narayana Failed %BUILD_URL%" & exit -1)

call:comment_on_pull "Windows - Job complete %BUILD_URL%"

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
   
   curl -k -d "{ \"body\": \"%~1\" }" -ujbosstm-bot:%BOT_PASSWORD% https://api.github.com/repos/%GIT_ACCOUNT%/%GIT_REPO%/issues/%PULL_NUM%/comments

goto:eof
