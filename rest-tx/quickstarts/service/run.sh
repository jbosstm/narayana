# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running service quickstart"

mvn clean compile exec:exec
if [ "$?" != "0" ]; then
	exit -1
fi
