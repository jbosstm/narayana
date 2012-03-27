if [ -z "${WORKSPACE}" ]; then
  echo "UNSET WORKSPACE"
  exit -1;
fi

#BUILD NARAYANA WITH FINDBUGS
./build.sh -Dfindbugs.skip=false -Dfindbugs.failOnError=false clean install
if [ "$?" != "0" ]; then
	exit -1
fi

#RUN QA TESTS
cd qa
if [ "$?" != "0" ]; then
	exit -1
fi

sed -i TaskImpl.properties -e "s#^COMMAND_LINE_0=.*#COMMAND_LINE_0=${JAVA_HOME}/bin/java#"
if [ "$?" != "0" ]; then
	exit -1
fi

ant -Ddriver.url=file:///home/hudson/dbdrivers get.drivers dist
if [ "$?" != "0" ]; then
	exit -1
fi

ant -f run-tests.xml ci-tests
if [ "$?" != "0" ]; then
	exit -1
fi


#GET JBOSS
cd ${WORKSPACE}
ant -f scripts/hudson/initializeJBoss.xml -Dbasedir=. initializeJBoss -debug
chmod u+x $WORKSPACE/jboss-as-7.1.1.Final/bin/jboss-cli.sh
if [ "$?" != "0" ]; then
	exit -1
fi

export JBOSS_HOME=${WORKSPACE}/jboss-as-7.1.1.Final

#1.WSTX11 INTEROP
#build interop11.war for testing
./build.sh -f XTS/localjunit/WSTX11-interop/pom.xml clean install -DskipTests 
if [ "$?" != "0" ]; then
	exit -1
fi

#running the tests
./build.sh -f XTS/localjunit/WSTX11-interop/pom.xml -Parq test 
if [ "$?" != "0" ]; then
	exit -1
fi


#2.XTS UNIT TESTS
./build.sh -f XTS/localjunit/pom.xml -Parq test
if [ "$?" != "0" ]; then
	exit -1
fi

#3.XTS CRASH RECOVERY TESTS
./build.sh -f XTS/sar/crash-recovery-tests/pom.xml test
if [ "$?" != "0" ]; then
	exit -1
fi

cd XTS/sar/crash-recovery-tests
if [ "$?" != "0" ]; then
	exit -1
fi

java -cp target/classes/ com.arjuna.qa.simplifylogs.SimplifyLogs ./target/log/ ./target/log-simplified
if [ "$?" != "0" ]; then
	exit -1
fi

