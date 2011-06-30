# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running javax_transaction quickstart"

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.examples.TransactionExample
if [ "$?" != "0" ]; then
	exit -1
fi
