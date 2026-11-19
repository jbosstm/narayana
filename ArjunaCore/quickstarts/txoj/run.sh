# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running txoj quickstart"

mvn compile exec:exec
if [ "$?" != "0" ]; then
	exit -1
fi
