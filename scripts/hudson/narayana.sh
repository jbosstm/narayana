if [ -z "${WORKSPACE}" ]; then
  echo "UNSET WORKSPACE"
  exit -1;
fi

isIdlj=0
for arg in "$@"; do
  if [ `echo "$arg" |grep "idlj"` ]; then
    isIdlj=1
  fi
done

# FOR DEBUGGING SUBSEQUENT ISSUES
free -m

#Make sure no JBoss processes running
for i in `ps -eaf | grep java | grep "standalone*.xml" | grep -v grep | cut -c10-15`; do kill $i; done

#BUILD NARAYANA WITH FINDBUGS
./build.sh -Dfindbugs.skip=false -Dfindbugs.failOnError=false "$@" clean install
if [ "$?" != "0" ]; then
	exit -1
fi

GIT_URL="https://github.com/jbosstm/jboss-as.git"
#GIT_URL="git://github.com/jbosstm/jboss-as.git"

#BUILD JBOSS-AS
cd ${WORKSPACE}
rm -rf jboss-as
git clone $GIT_URL
if [ "$?" != "0" ]; then
	exit -1
fi

cd jboss-as
git checkout -t origin/5_BRANCH
if [ "$?" != "0" ]; then
	exit -1
fi

git remote add upstream $GIT_URL
git pull --rebase --ff-only upstream master
if [ "$?" != "0" ]; then
	exit -1
fi

MAVEN_OPTS=-XX:MaxPermSize=256m ./build.sh "$@" clean install -DskipTests
if [ "$?" != "0" ]; then
	exit -1
fi

export JBOSS_HOME=${WORKSPACE}/jboss-as/build/target/jboss-as-7.1.2.Final-SNAPSHOT
cd ${WORKSPACE}

#1.WSTX11 INTEROP and UNIT TESTS
./build.sh -f XTS/localjunit/pom.xml -Parq "$@" test
if [ "$?" != "0" ]; then
	exit -1
fi

#2.XTS CRASH RECOVERY TESTS
./build.sh -f XTS/sar/crash-recovery-tests/pom.xml -Parq "$@" clean test
if [ "$?" != "0" ]; then
	exit -1
fi

(cd XTS/sar/crash-recovery-tests && java -cp target/classes/ com.arjuna.qa.simplifylogs.SimplifyLogs ./target/log/ ./target/log-simplified)
if [ "$?" != "0" ]; then
	exit -1
fi

#3.TXBRIDGE TESTS
cd ${WORKSPACE}
sed -e "s#\(recovery-environment\) \(socket-binding\)#\\1 recovery-listener=\"true\" \\2#" -i ${JBOSS_HOME}/docs/examples/configs/standalone-xts.xml
if [ "$?" != "0" ]; then
	exit -1
fi

./build.sh -f txbridge/pom.xml -Parq "$@" clean test
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

if [ $isIdlj == 1 ]; then
# delete lines containing jacorb
	sed -i TaskImpl.properties -e  '/^.*separator}jacorb/ d'
fi

ant -DisIdlj=$isIdlj -Ddriver.url=file:///home/hudson/dbdrivers get.drivers dist
if [ "$?" != "0" ]; then
	exit -1
fi

ant -f run-tests.xml ci-tests
if [ "$?" != "0" ]; then
	exit -1
fi
