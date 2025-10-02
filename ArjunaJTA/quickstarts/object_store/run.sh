# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running object_store quickstart"

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.VolatileStoreExample
if [ "$?" != "0" ]; then
	exit -1
fi

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.HornetqStoreExample
if [ "$?" != "0" ]; then
	exit -1
fi

mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.FileStoreExample
if [ "$?" != "0" ]; then
	exit -1
fi
