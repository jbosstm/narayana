# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running recovery quickstart"

mvn clean compile exec:java -Dexec.mainClass=quickstart.ParticipantRecovery -Dexec.args="-f"
# We expect this to fail
#if [ "$?" != "0" ]; then
#   exit -1
#fi
echo "Recovering failed service - this could take up to 2 minutes"
mvn compile exec:java -Dexec.mainClass=quickstart.ParticipantRecovery -Dexec.args="-r"
if [ "$?" != "0" ]; then
    echo "Service recovery example FAILED"
    exit -1
else
    echo "Service recovery example SUCCEEDED"
fi

