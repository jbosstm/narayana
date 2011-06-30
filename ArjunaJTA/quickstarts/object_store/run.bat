@echo off

echo "Running quickstart"

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.VolatileStoreExample
IF %ERRORLEVEL% NEQ 0 exit -1

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.HornetqStoreExample
IF %ERRORLEVEL% NEQ 0 exit -1

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.FileStoreTest
IF %ERRORLEVEL% NEQ 0 exit -1
