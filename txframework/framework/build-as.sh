#!/bin/bash

function fail
{
	echo "Failure"
	exit -1
}


mvn install || fail

for i in `ps -eaf | grep java | grep "standalone*.xml" | grep -v grep | cut -c10-15`; do kill $i; done || fail

cd ~/dev/jboss-as/ || fail

./build-xts.sh || fail

cp ~/dev/narayana_trunk/narayana/rest-tx/webservice/target/rest-tx-web-5.0.0.M2-SNAPSHOT.war ~/dev/jboss-as/build/target/jboss-as-7.1.2.Final-SNAPSHOT/standalone/deployments/
