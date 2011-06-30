@echo off

echo "Running javax_transaction quickstart"

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.examples.TransactionExample
IF %ERRORLEVEL% NEQ 0 exit -1
