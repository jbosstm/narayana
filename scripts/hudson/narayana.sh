if [ -z "${WORKSPACE}" ]; then
  echo "UNSET WORKSPACE"
  exit -1;
fi

#BUILD NARAYANA WITH FINDBUGS
./build.sh -Dfindbugs.skip=false -Dfindbugs.failOnError=false clean install

#RUN QA TESTS
cd qa
sed -i TaskImpl.properties -e "s#^COMMAND_LINE_0=.*#COMMAND_LINE_0=/usr/local/jdk1.6.0_26/bin/java#"
ant -Ddriver.url=file:///home/hudson/dbdrivers get.drivers dist
ant -f run-tests.xml ci-tests

#GET JBOSS
cd ${WORKSPACE}
ant -f scripts/hudson/initializeJBoss.xml -Dbasedir=. initializeJBoss -debug
export JBOSS_HOME=${WORKSPACE}/jboss-as-7.1.1.Final

#1.WSTX11 INTEROP
#build interop11.war for testing
./build.sh -f XTS/localjunit/WSTX11-interop/pom.xml clean install -DskipTests 
#running the tests
./build.sh -f XTS/localjunit/WSTX11-interop/pom.xml -Parq test 

#2.XTS UNIT TESTS
./build.sh -f XTS/localjunit/pom.xml -Parq test

#3.XTS CRASH RECOVERY TESTS
./build.sh -f XTS/sar/crash-recovery-tests/pom.xml test
cd XTS/sar/crash-recovery-tests
java -cp target/classes/ com.arjuna.qa.simplifylogs.SimplifyLogs ./target/log/ ./target/log-simplified
