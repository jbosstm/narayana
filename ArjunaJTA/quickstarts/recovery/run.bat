@echo off

echo "Running recovery quickstart"

rem To run an example use the maven java exec pluging. For example to run the second recovery example
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.DummyRecovery -Dexec.args="-f"
IF %ERRORLEVEL% NEQ 0 exit -1
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.DummyRecovery -Dexec.args="-r"
IF %ERRORLEVEL% NEQ 0 exit -1
echo "Dummy recovery example succeeded"

rem And to run the JMS recovery example:
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.JmsRecovery -Dexec.args="-f"
IF %ERRORLEVEL% NEQ 0 exit -1
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.JmsRecovery -Dexec.args="-r"
IF %ERRORLEVEL% NEQ 0 exit -1
echo "JMS recovery example succeeded"

echo "All recovery examples succeeded"

