# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running recovery quickstart"

# To run an example use the maven java exec pluging. For example to run the second recovery example
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.examples.recovery.DummyRecovery -Dexec.args="-f"
# We expect this to fail
#if [ "$?" != "0" ]; then
#	exit -1
#fi
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.examples.recovery.DummyRecovery -Dexec.args="-r"
if [ "$?" != "0" ]; then
	exit -1
fi

# And to run the JMS recovery example:
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.examples.recovery.JmsRecovery -Dexec.args="-f"
# We expect this to fail
#if [ "$?" != "0" ]; then
#	exit -1
#fi
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.examples.recovery.JmsRecovery -Dexec.args="-r"
if [ "$?" != "0" ]; then
	exit -1
fi
