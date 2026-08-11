# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running maven quickstart"

mvn compile exec:exec
if [ "$?" != "0" ]; then
	exit -1
fi
