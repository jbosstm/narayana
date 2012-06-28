function fatal {
        echo "$1"
        exit -1
}

#BUILD NARAYANA WITH FINDBUGS
function build_narayana {
  echo "Building Narayana"
  [ $NARAYANA_TESTS = 1 ] && NARAYANA_ARGS= || NARAYANA_ARGS="-DskipTests"
  ./build.sh -Dfindbugs.skip=false -Dfindbugs.failOnError=false "$@" $NARAYANA_ARGS $IPV6_OPTS clean install
  [ $? = 0 ] || fatal "narayana build failed"
  cp_narayana_to_as
}

function cp_narayana_to_as {
  cd $WORKSPACE
  JBOSS_VERSION=`ls -1 ${WORKSPACE}/jboss-as/build/target | grep jboss-as`
  [ $? = 0 ] || return 1
  export JBOSS_HOME=${WORKSPACE}/jboss-as/build/target/${JBOSS_VERSION}

  echo "WARNING - using narayana version ${NARAYANA_VERSION}"
  JAR1=narayana-jts-integration-${NARAYANA_VERSION}.jar
  JAR2=narayana-jts-${NARAYANA_VERSION}.jar
# TODO make sure ${JBOSS_HOME} doesn't already contain a different version of narayana
  echo "cp ./ArjunaJTS/integration/target/$JAR1 ${JBOSS_HOME}/modules/org/jboss/jts/integration/main/"
  echo "cp ./ArjunaJTS/narayana-jts/target/$JAR2 ${JBOSS_HOME}/modules/org/jboss/jts/main/"
  cp ./ArjunaJTS/integration/target/$JAR1 ${JBOSS_HOME}/modules/org/jboss/jts/integration/main/
  cp ./ArjunaJTS/narayana-jts/target/$JAR2 ${JBOSS_HOME}/modules/org/jboss/jts/main/
  return 0
}


function build_as {
  echo "Building AS"
  GIT_URL="https://github.com/jbosstm/jboss-as.git"
  UPSTREAM_GIT_URL="https://github.com/jbossas/jboss-as.git"

  cd ${WORKSPACE}
  rm -rf jboss-as
  git clone $GIT_URL
  [ $? = 0 ] || fatal "git clode $GIT_URL failed"

  cd jboss-as
  git checkout -t origin/5_BRANCH
  [ $? = 0 ] || fatal "git checkout 5_BRANCH failed"

  git remote add upstream $UPSTREAM_GIT_URL
  git pull --rebase --ff-only upstream master
  [ $? = 0 ] || fatal "git rebase failed"

  MAVEN_OPTS="-XX:MaxPermSize=256m"
  ./build.sh clean install -DskipTests -Dts.smoke=false $IPV6_OPTS
  [ $? = 0 ] || fatal "AS build failed"
}

function xts_wstx11_tests {
  echo "#1 XTS: WSTX11 INTEROP and UNIT TESTS"

  if [ $WSTX_MODULES ]; then
    echo "BUILDING SPECIFIC WSTX11 modules"
    ./build.sh -f XTS/localjunit/pom.xml --projects "$WSTX_MODULES" -P$ARQ_PROF "$@" $IPV6_OPTS clean test
  else
    ./build.sh -f XTS/localjunit/pom.xml -P$ARQ_PROF "$@" $IPV6_OPTS clean test
  fi

  [ $? = 0 ] || fatal "XTS: WSTX11 INTEROP and UNIT TESTS failed"
}
function xts_crash_rec_tests {
  echo "#2 XTS CRASH RECOVERY TESTS"
  ./build.sh -f XTS/sar/crash-recovery-tests/pom.xml -P$ARQ_PROF "$@" $IPV6_OPTS test
  [ $? = 0 ] || fatal "XTS: XTS CRASH RECOVERY TESTS failed"

  (cd XTS/sar/crash-recovery-tests && java -cp target/classes/ com.arjuna.qa.simplifylogs.SimplifyLogs ./target/log/ ./target/log-simplified)
  [ $? = 0 ] || fatal "Simplify CRASH RECOVERY logs failed"
}
function tx_bridge_tests {
  echo "XTS: TXBRIDGE TESTS"
  CONF="${JBOSS_HOME}/docs/examples/configs/standalone-xts.xml"
  grep recovery-listener "$CONF"
  sed -e s/recovery-listener=\"true\"//g -i $CONF
  sed -e "s#\(recovery-environment\) \(socket-binding\)#\\1 recovery-listener=\"true\" \\2#" -i $CONF

#  sed -e "s#\(recovery-environment\) \(socket-binding\)#\\1 recovery-listener=\"true\" \\2#" -i $CONF
  [ $? = 0 ] || fatal "#3.TXBRIDGE TESTS: sed failed"

  echo "XTS: TXBRIDGE TESTS"
  ./build.sh -f txbridge/pom.xml -P$ARQ_PROF "$@" $IPV6_OPTS test
  [ $? = 0 ] || fatal "#3.TXBRIDGE TESTS failed"
}
function xts_tests {
  cd $WORKSPACE
  JBOSS_VERSION=`ls -1 ${WORKSPACE}/jboss-as/build/target | grep jboss-as`
  export JBOSS_HOME=${WORKSPACE}/jboss-as/build/target/${JBOSS_VERSION}
  echo "JBOSS_HOME=$JBOSS_HOME"

  [ $wstx11 = 1 ] && xts_wstx11_tests $@
  [ $xts_crash_rec = 1 ] && xts_crash_rec_tests $@
  [ $txbridge = 1 ] && tx_bridge_tests $@
}

function qa_tests {
  cd $WORKSPACE/qa

  sed -i TaskImpl.properties -e "s#^COMMAND_LINE_0=.*#COMMAND_LINE_0=${JAVA_HOME}/bin/java#"
  [ $? = 0 ] || fatal "sed TaskImpl.properties failed"

  # delete lines containing jacorb
  [ $isIdlj == 1 ] && sed -i TaskImpl.properties -e  '/^.*separator}jacorb/ d'

  ant -DisIdlj=$isIdlj "$QA_BUILD_ARGS" get.drivers dist
  [ $? = 0 ] || fatal "qa build failed"

  [ $IPV6_OPTS ] && target="junit-testsuite" || target="ci-tests"
  ant -f run-tests.xml $target
  [ $? = 0 ] || fatal "some qa tests failed"
}

[ $NARAYANA_VERSION ] || NARAYANA_VERSION="5.0.0.M2-SNAPSHOT"

[ $NARAYANA_TESTS ] || NARAYANA_TESTS=1
[ $NARAYANA_BUILD ] || NARAYANA_BUILD=1
[ $AS_BUILD ] || AS_BUILD=1
[ $XTS_TESTS ] || XTS_TESTS=1
[ $QA_TESTS ] || QA_TESTS=1
[ $wstx11 ] || wstx11=1
[ $xts_crash_rec ] || xts_crash_rec=1
[ $txbridge ] || txbridge=1

[ $ARQ_PROF ] || ARQ_PROF=arq

#QA_BUILD_ARGS="-Ddriver.url=file:///home/hudson/dbdrivers"
QA_BUILD_ARGS=

## IPv6 job
#export ARQ_PROF=arqIPv6
#export NARAYANA_TESTS=0
#export NARAYANA_BUILD=0
#export AS_BUILD=0
#export XTS_TESTS=0
#export QA_TESTS=0
#export wstx11=0
#export xts_crash_rec=0
#export txbridge=0
#export QA_BUILD_ARGS=
#export WSTX_MODULES="WSAS,WSCF,WSTX,WS-C,WS-T"
##

[ -z "${WORKSPACE}" ] && fatal "UNSET WORKSPACE"

# FOR DEBUGGING SUBSEQUENT ISSUES
free -m

#Make sure no JBoss processes running
for i in `ps -eaf | grep java | grep "standalone*.xml" | grep -v grep | cut -c10-15`; do kill $i; done

isIdlj=0
for arg in "$@"; do
  [ `echo "$arg" |grep "idlj"` ] && isIdlj=1
done

export ANT_OPTS="$ANT_OPTS $IPV6_OPTS"

[ $NARAYANA_BUILD = 1 ] && build_narayana "$@"
[ $AS_BUILD = 1 ] && build_as "$@"
[ $XTS_TESTS = 1 ] && xts_tests "$@"
[ $QA_TESTS = 1 ] && qa_tests "$@"

exit 0
