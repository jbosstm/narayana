@echo off

echo "Running recovery quickstart"

mvn clean compile exec:java -Dexec.mainClass=quickstart.ParticipantRecovery -Dexec.args="-f"
IF %ERRORLEVEL% NEQ 0 exit -1
echo "Recovering failed service - this could take up to 2 minutes"
mvn compile exec:java -Dexec.mainClass=quickstart.ParticipantRecovery -Dexec.args="-r"
IF %ERRORLEVEL% NEQ 0 exit -1
echo "Service recovery example succeeded"
