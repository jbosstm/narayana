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

# test whether the IPV6 evironment variable is set
if [ -v IPV6 ]; then
  mvn_arqprof="-ParqIPv6"
else
  mvn_arqprof="-Parq"
fi


# FOR DEBUGGING SUBSEQUENT ISSUES
free -m

#Make sure no JBoss processes running
for i in `ps -eaf | grep java | grep "standalone" | grep -v grep | cut -c10-15`; do kill $i; done

#BUILD NARAYANA WITH FINDBUGS
./build.sh -Dfindbugs.skip=false -Dfindbugs.failOnError=false "$@" clean install -DskipTests=true
if [ "$?" != "0" ]; then
	exit -1
fi

GIT_URL="https://github.com/jbosstm/jboss-as.git"
UPSTREAM_GIT_URL="https://github.com/jbossas/jboss-as.git"

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

git remote add upstream $UPSTREAM_GIT_URL
git pull --rebase --ff-only upstream master
if [ "$?" != "0" ]; then
	exit -1
fi

MAVEN_OPTS=-XX:MaxPermSize=256m ./build.sh "$@" clean install -DskipTests
if [ "$?" != "0" ]; then
	exit -1
fi

export JBOSS_HOME=${WORKSPACE}/jboss-as/build/target/`ls -1 ${WORKSPACE}/jboss-as/build/target | grep jboss-as`

cd ${WORKSPACE}

#0. TXFramework Tests
#cp ./rest-tx/webservice/target/rest-tx-web-*.war $JBOSS_HOME/standalone/deployments
#./build.sh -f ./txframework/pom.xml $mvn_arqprof "$@" test
#if [ "$?" != "0" ]; then
#        exit -1
#fi

ps -eaf | grep java

#1.WSTX11 INTEROP and UNIT TESTS and CRASH RECOVERY TESTS
./build.sh -f XTS/localjunit/pom.xml --projects xtstest,crash-recovery-tests $mvn_arqprof "$@" clean install -Dtest=TestATCrashDuringCommit#MultiParticipantPrepareAndCommitTest
if [ "$?" != "0" ]; then
	exit -1
fi
