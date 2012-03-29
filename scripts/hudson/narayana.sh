if [ -z "${WORKSPACE}" ]; then
  echo "UNSET WORKSPACE"
  exit -1;
fi

#BUILD NARAYANA WITH FINDBUGS
./build.sh -Dfindbugs.skip=false -Dfindbugs.failOnError=false clean install
if [ "$?" != "0" ]; then
	exit -1
fi

#BUILD JBOSS-AS
cd ${WORKSPACE}
rm -rf jboss-as
git clone git://github.com/jbosstm/jboss-as.git
if [ "$?" != "0" ]; then
	exit -1
fi

cd jboss-as
git checkout -t origin/5_BRANCH
if [ "$?" != "0" ]; then
	exit -1
fi

./build.sh clean install
if [ "$?" != "0" ]; then
	exit -1
fi

export JBOSS_HOME=${WORKSPACE}/jboss-as/build/target/jboss-as-7.1.1.Final
cd ${WORKSPACE}

#1.WSTX11 INTEROP and UNIT TESTS
./build.sh -f XTS/localjunit/pom.xml -Parq test
if [ "$?" != "0" ]; then
	exit -1
fi

#2.XTS CRASH RECOVERY TESTS
./build.sh -f XTS/sar/crash-recovery-tests/pom.xml -Parq test
if [ "$?" != "0" ]; then
	exit -1
fi

(cd XTS/sar/crash-recovery-tests && java -cp target/classes/ com.arjuna.qa.simplifylogs.SimplifyLogs ./target/log/ ./target/log-simplified)
if [ "$?" != "0" ]; then
	exit -1
fi

#RUN QA TESTS
cd $WORKSPACE/qa
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
