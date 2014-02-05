function fatal {
  if [[ -z $PROFILE ]]; then
      comment_on_pull "Tests failed ($BUILD_URL): $1"
  elif [[ $PROFILE == "BLACKTIE" ]]; then
      comment_on_pull "$PROFILE profile tests failed on Linux ($BUILD_URL): $1"
  else
      comment_on_pull "$PROFILE profile tests failed ($BUILD_URL): $1"
  fi

  echo "$1"
  exit 1
}

function get_pull_description {
    PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')

    if [ "$PULL_NUMBER" != "" ]; then
        echo $(curl -ujbosstm-bot:$BOT_PASSWORD -s https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/pulls/$PULL_NUMBER | grep \"body\":)
    else
        echo ""
    fi
}

function init_test_options {
    [ $NARAYANA_VERSION ] || NARAYANA_VERSION="5.0.0.M3-SNAPSHOT"
    [ $ARQ_PROF ] || ARQ_PROF=arq	# IPv4 arquillian profile

    PULL_DESCRIPTION=$(get_pull_description)

    if [[ $PROFILE == "NO_TEST" ]] || [[ $PULL_DESCRIPTION =~ "NO_TEST" ]]; then
        export COMMENT_ON_PULL=""
        export AS_BUILD=0 NARAYANA_BUILD=0 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
        export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0
    elif [[ $PROFILE == "MAIN" ]] && [[ ! $PULL_DESCRIPTION =~ "!MAIN" ]]; then
        FINDBUGS=findbugs,
        comment_on_pull "Started testing this pull request with MAIN profile: $BUILD_URL"
        export AS_BUILD=1 NARAYANA_BUILD=1 NARAYANA_TESTS=1 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
        export RTS_AS_TESTS=1 RTS_TESTS=1 JTA_CDI_TESTS=1 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0
    elif [[ $PROFILE == "XTS" ]] && [[ ! $PULL_DESCRIPTION =~ "!XTS" ]]; then
        comment_on_pull "Started testing this pull request with XTS profile: $BUILD_URL"
        export AS_BUILD=1 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=1 XTS_TESTS=1 TXF_TESTS=1 txbridge=1
        export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0
    elif [[ $PROFILE == "QA_JTA" ]] && [[ ! $PULL_DESCRIPTION =~ "!QA_JTA" ]]; then
        comment_on_pull "Started testing this pull request with QA_JTA profile: $BUILD_URL"
        export AS_BUILD=0 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
        export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 SUN_ORB=0 JAC_ORB=1 QA_TARGET=ci-tests-nojts
    elif [[ $PROFILE == "QA_JTS_JACORB" ]] && [[ ! $PULL_DESCRIPTION =~ "!QA_JTS_JACORB" ]]; then
        comment_on_pull "Started testing this pull request with QA_JTS_JACORB profile: $BUILD_URL"
        export AS_BUILD=0 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
        export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 SUN_ORB=0 JAC_ORB=1 QA_TARGET=ci-jts-tests
    elif [[ $PROFILE == "QA_JTS_JDKORB" ]] && [[ ! $PULL_DESCRIPTION =~ "!QA_JTS_JDKORB" ]]; then
        comment_on_pull "Started testing this pull request with QA_JTS_JDKORB profile: $BUILD_URL"
        export AS_BUILD=0 NARAYANA_BUILD=1  NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
        export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 SUN_ORB=1 JAC_ORB=0 QA_TARGET=ci-jts-tests
    elif [[ $PROFILE == "BLACKTIE" ]] && [[ ! $PULL_DESCRIPTION =~ "!BLACKTIE" ]]; then
        comment_on_pull "Started testing this pull request with BLACKTIE profile on Linux: $BUILD_URL"
        export AS_BUILD=1 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=1 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
        export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0
    elif [[ -z $PROFILE ]]; then
        comment_on_pull "Started testing this pull request: $BUILD_URL"
        # if the following env variables have not been set initialize them to their defaults
        [ $NARAYANA_TESTS ] || NARAYANA_TESTS=1	# run the narayana surefire tests
        [ $NARAYANA_BUILD ] || NARAYANA_BUILD=1 # build narayana
        [ $AS_BUILD ] || AS_BUILD=1 # git clone and build a fresh copy of the AS
        [ $BLACKTIE ] || BLACKTIE=1 # Build BlackTie
        [ $TXF_TESTS ] || TXF_TESTS=1 # TxFramework tests
        [ $XTS_TESTS ] || XTS_TESTS=1 # XTS tests
        [ $XTS_AS_TESTS ] || XTS_AS_TESTS=1 # XTS tests
        [ $RTS_AS_TESTS ] || RTS_AS_TESTS=1 # RTS tests
        [ $RTS_TESTS ] || RTS_TESTS=1 # REST-AT Test
        [ $JTA_CDI_TESTS ] || JTA_CDI_TESTS=1 # JTA CDI Tests
        [ $QA_TESTS ] || QA_TESTS=1 # QA test suite
        [ $SUN_ORB ] || SUN_ORB=1 # Run QA test suite against the Sun orb
        [ $JAC_ORB ] || JAC_ORB=1 # Run QA test suite against JacORB
        [ $txbridge ] || txbridge=1 # bridge tests
    else
        export COMMENT_ON_PULL=""
        export AS_BUILD=0 NARAYANA_BUILD=0 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
        export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0
    fi
}

function set_ulimit {
    ulimit -u $1
    ulimit -a
}

function comment_on_pull
{
    if [ "$COMMENT_ON_PULL" = "" ]; then return; fi

    PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
    if [ "$PULL_NUMBER" != "" ]
    then
        JSON="{ \"body\": \"$1\" }"
        curl -d "$JSON" -ujbosstm-bot:$BOT_PASSWORD https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/issues/$PULL_NUMBER/comments
    else
        echo "Not a pull request, so not commenting"
    fi
}

function check_if_pull_closed
{
    PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
    if [ "$PULL_NUMBER" != "" ]
    then
	curl -ujbosstm-bot:$BOT_PASSWORD -s https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/pulls/$PULL_NUMBER | grep -q "\"state\": \"closed\""
	if [ $? -eq 1 ] 
	then
		echo "pull open"
	else
		echo "pull closed"
		exit 0
	fi
    fi
}

function kill_qa_suite_processes
{
  # list java processes including main class
  jps -l | while read ln; do
    pid=$(echo $ln | cut -f1 -d\ )
    main=$(echo $ln | cut -f2 -d\ )
    killit=0

    # see if any of the passed in java main patterns match the main class name of the java process
    for pat in $*; do
      [[ $main == ${pat}* ]] && killit=1
    done

    if [[ $killit == 1 ]]; then 
      echo "Test suite process $pid still running - terminating it with signal 9"
      kill -9 $pid
    fi

  done
}

#BUILD NARAYANA WITH FINDBUGS
function build_narayana {
  echo "Building Narayana"
  cd $WORKSPACE
  [ $NARAYANA_TESTS = 1 ] && NARAYANA_ARGS= || NARAYANA_ARGS="-DskipTests"

  ./build.sh -P${FINDBUGS}release,community$OBJECT_STORE_PROFILE -Didlj-enabled=true "$@" $NARAYANA_ARGS $IPV6_OPTS clean install
  [ $? = 0 ] || fatal "narayana build failed"

  return 0
}

function build_as {
  echo "Building AS"
  GIT_URL="https://github.com/jbosstm/jboss-as.git"
  UPSTREAM_GIT_URL="https://github.com/wildfly/wildfly.git"

  cd ${WORKSPACE}
  if [ -d jboss-as ]; then
    echo "Updating existing checkout of AS7"
    cd jboss-as

    git remote | grep upstream
    if [ $? -ne 0 ]; then
      git remote add upstream $UPSTREAM_GIT_URL
    fi
    #Abort any partially complete rebase
    git rebase --abort
    git checkout 5_BRANCH
    [ $? = 0 ] || fatal "git checkout 5_BRANCH failed"
    git fetch
    [ $? = 0 ] || fatal "git fetch $GIT_URL failed"
    git reset --hard origin/5_BRANCH
    [ $? = 0 ] || fatal "git reset 5_BRANCH failed"
    git clean -f -d -x
    [ $? = 0 ] || fatal "git clean failed"
    git rebase --abort
    rm -rf .git/rebase-apply
  else
    echo "First time checkout of AS7"
    git clone $GIT_URL
    [ $? = 0 ] || fatal "git clone $GIT_URL failed"

    cd jboss-as

    git remote add upstream $UPSTREAM_GIT_URL
  fi

  [ -z "$AS_BRANCH" ] || git fetch origin +refs/pull/*/head:refs/remotes/origin/pull/*/head
  [ $? = 0 ] || fatal "git fetch of pulls failed"
  [ -z "$AS_BRANCH" ] || git checkout $AS_BRANCH
  [ $? = 0 ] || fatal "git fetch of pull branch failed"

  git fetch upstream
  echo "This is the JBoss-AS commit"
  echo $(git rev-parse upstream/master)

  git pull --rebase --ff-only upstream master
  [ $? = 0 ] || fatal "git rebase failed"

  export MAVEN_OPTS="$MAVEN_OPTS -XX:MaxPermSize=512m"
  JAVA_OPTS="$JAVA_OPTS -Xms1303m -Xmx1303m -XX:MaxPermSize=512m" ./build.sh clean install -DskipTests -Dts.smoke=false $IPV6_OPTS -Drelease=true
  [ $? = 0 ] || fatal "AS build failed"
  
  #Enable remote debugger
  echo JAVA_OPTS='"$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"' >> ./build/target/wildfly-${WILDFLY_MASTER_VERSION}/bin/standalone.conf

  init_jboss_home
}

function init_jboss_home {
  cd $WORKSPACE
  JBOSS_HOME=${WORKSPACE}/jboss-as/build/target/wildfly-${WILDFLY_MASTER_VERSION}
  export JBOSS_HOME=`echo  $JBOSS_HOME`
  [ -d $JBOSS_HOME ] || fatal "missing AS - $JBOSS_HOME is not a directory"
  echo "JBOSS_HOME=$JBOSS_HOME"
  cp ${JBOSS_HOME}/docs/examples/configs/standalone-xts.xml ${JBOSS_HOME}/standalone/configuration
  cp ${JBOSS_HOME}/docs/examples/configs/standalone-rts.xml ${JBOSS_HOME}/standalone/configuration
}

function xts_as_tests {
  echo "#-1. XTS AS Integration Test"
  cd ${WORKSPACE}/jboss-as
  ./build.sh -f ./testsuite/integration/xts/pom.xml -Pxts.integration.tests.profile "$@" test
  [ $? = 0 ] || fatal "XTS AS Integration Test failed"
  cd ${WORKSPACE}
}

function rts_as_tests {
  echo "#-1. RTS AS Integration Test"
  cd ${WORKSPACE}/jboss-as
  ./build.sh -f ./testsuite/integration/rts/pom.xml -Prts.integration.tests.profile "$@" test
  [ $? = 0 ] || fatal "RTS AS Integration Test failed"
  cd ${WORKSPACE}
}


function rts_tests {
  echo "#0. REST-AT Integration Test"
  ./build.sh -f ./rts/at/integration/pom.xml -P$ARQ_PROF "$@" test
  [ $? = 0 ] || fatal "REST-AT Integration Test failed"

  echo "#0. REST-AT To JTA Bridge Test"
    ./build.sh -f ./rts/at/bridge/pom.xml -P$ARQ_PROF "$@" test
    [ $? = 0 ] || fatal "REST-AT To JTA Bridge Test failed"
}

function blacktie {
  echo "#0. BlackTie"
  ulimit -c unlimited
  if [ -z "${JBOSSAS_IP_ADDR+x}" ]; then
    echo JBOSSAS_IP_ADDR not set
    JBOSSAS_IP_ADDR=localhost
  fi
  # KILL ANY PREVIOUS BUILD REMNANTS
  ps -f
  for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
  killall -9 -r memcheck
  killall -9 testsuite
  killall -9 server
  killall -9 client
  killall -9 cs
  ps -f
  # FOR DEBUGGING SUBSEQUENT ISSUES
  free -m

  echo "Building Blacktie Subsystem"
  cd ${WORKSPACE}
  ./build.sh -f blacktie/wildfly-blacktie/pom.xml clean install "$@"
  [ $? = 0 ] || fatal "Blacktie Subsystem build failed"
  rm -rf ${WORKSPACE}/blacktie/wildfly-${WILDFLY_MASTER_VERSION}
  cp -rp ${WORKSPACE}/jboss-as/build/target/wildfly-${WILDFLY_MASTER_VERSION} -d $PWD/blacktie
  [ $? = 0 ] || fatal "Could not unzip wildfly"
  unzip ${WORKSPACE}/blacktie/wildfly-blacktie/build/target/wildfly-blacktie-build-5.1.0.Alpha1-SNAPSHOT-bin.zip -d $PWD/blacktie/wildfly-${WILDFLY_MASTER_VERSION}
  [ $? = 0 ] || fatal "Could not unzip blacktie into widfly"
  # INITIALIZE JBOSS
  ant -f blacktie/scripts/hudson/initializeJBoss.xml -DJBOSS_HOME=$WORKSPACE/blacktie/wildfly-${WILDFLY_MASTER_VERSION} initializeJBoss
  if [ "$?" != "0" ]; then
	  fatal "Failed to init JBoss: $BUILD_URL"
  fi
  chmod u+x $WORKSPACE/blacktie/wildfly-${WILDFLY_MASTER_VERSION}/bin/standalone.sh

  if [[ $# == 0 || $# > 0 && "$1" != "-DskipTests" ]]; then
    # START JBOSS
    JBOSS_HOME=`pwd`/blacktie/wildfly-${WILDFLY_MASTER_VERSION} JAVA_OPTS="$JAVA_OPTS -Xmx256m -XX:MaxPermSize=256m" blacktie/wildfly-${WILDFLY_MASTER_VERSION}/bin/standalone.sh -c standalone-blacktie.xml -Djboss.bind.address=$JBOSSAS_IP_ADDR -Djboss.bind.address.unsecure=$JBOSSAS_IP_ADDR -Djboss.bind.address.management=$JBOSSAS_IP_ADDR&
    sleep 5
  fi

  # BUILD BLACKTIE
  ./build.sh -f blacktie/pom.xml clean install -Djbossas.ip.addr=$JBOSSAS_IP_ADDR "$@"
  if [ "$?" != "0" ]; then
  	ps -f
	  for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
  	killall -9 testsuite
	  killall -9 server
	  killall -9 client
  	killall -9 cs
    ps -f
  	fatal "Some tests failed: $BUILD_URL"
  fi

  # KILL ANY BUILD REMNANTS
  ps -f
  for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
  killall -9 testsuite
  killall -9 server
  killall -9 client
  killall -9 cs
  ps -f
  [ $? = 0 ] || fatal "BlackTie build failed: $BUILD_URL"
}

function jta_cdi_tests {
  echo "#0. JTA CDI Tests"
  ./build.sh -f ./ArjunaJTA/cdi/pom.xml -P$ARQ_PROF "$@" test
  [ $? = 0 ] || fatal "JTA CDI Test failed"
}

function txframework_tests {
  echo "#0. TXFramework Test"
  cp ./rts/at/webservice/target/restat-web-*.war $JBOSS_HOME/standalone/deployments
  ./build.sh -f ./txframework/pom.xml -P$ARQ_PROF "$@" test
  [ $? = 0 ] || fatal "TxFramework build failed"
}

function xts_tests {
  echo "#1 XTS: WSTX11 INTEROP, UNIT TESTS, xtstest and CRASH RECOVERY TESTS"

  cd $WORKSPACE
  ran_crt=1
  set_ulimit 2048

  if [ $WSTX_MODULES ]; then
    [[ $WSTX_MODULES = *crash-recovery-tests* ]] || ran_crt=0
    echo "BUILDING SPECIFIC WSTX11 modules"
    ./build.sh -f XTS/localjunit/pom.xml --projects "$WSTX_MODULES" -P$ARQ_PROF "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
  else
    ./build.sh -f XTS/localjunit/pom.xml -P$ARQ_PROF "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
  fi

  [ $? = 0 ] || fatal "XTS: SOME TESTS failed"
  if [ $ran_crt = 1 ]; then
    if [[ $# == 0 || $# > 0 && "$1" != "-DskipTests" ]]; then
      (cd XTS/localjunit/crash-recovery-tests && java -cp target/classes/ com.arjuna.qa.simplifylogs.SimplifyLogs ./target/log/ ./target/log-simplified)
      [ $? = 0 ] || fatal "Simplify CRASH RECOVERY logs failed"
    fi
  fi
}

function tx_bridge_tests {
  echo "XTS: TXBRIDGE TESTS update conf"
  cd $WORKSPACE
  CONF="${JBOSS_HOME}/standalone/configuration/standalone-xts.xml"
  grep recovery-listener "$CONF"
  sed -e s/recovery-listener=\"true\"//g -i $CONF
  sed -e "s#\(recovery-environment\) \(socket-binding\)#\\1 recovery-listener=\"true\" \\2#" -i $CONF

#  sed -e "s#\(recovery-environment\) \(socket-binding\)#\\1 recovery-listener=\"true\" \\2#" -i $CONF
  [ $? = 0 ] || fatal "#3.TXBRIDGE TESTS: sed failed"

  echo "XTS: TXBRIDGE TESTS"
  ./build.sh -f txbridge/pom.xml -P$ARQ_PROF "$@" $IPV6_OPTS install "$@"
  [ $? = 0 ] || fatal "#3.TXBRIDGE TESTS failed"
}

function enable_qa_trace {
cat << 'EOF' > $WORKSPACE/qa/dist/${NARAYANA_VERSION}/etc/log4j.xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">

<log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">

    <appender name="console" class="org.apache.log4j.ConsoleAppender">
        <param name="Target" value="System.out"/>
        <param name="Threshold" value="TRACE"/>

        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%d{ABSOLUTE} {%8.8t} (%x) [%-5p,%-10c{1}] %m%n"/>
        </layout>
    </appender>

    <appender name="file" class="org.apache.log4j.FileAppender">
        <param name="File" value="logs/test.log"/>
        <param name="Append" value="false"/>
        <param name="Threshold" value="TRACE"/>

        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%d [%t] %p - %m%n"/>
        </layout>
    </appender>

    <category name="com.arjuna">
        <level value="TRACE"/>
        <appender-ref ref="console"/>
        <appender-ref ref="file"/>
    </category>
</log4j:configuration>
EOF
}

function qa_tests_once {
  echo "QA Test Suite $@"
  cd $WORKSPACE/qa
  unset orb

  # look for an argument of the form orb=<something>
  for i in $@; do
    [ ${i%%=*} = "orb" ] && orb=${i##*=}
  done

  # check to see if we were called with orb=idlj as one of the arguments
  if [ x$orb = x"idlj" ]; then
    IDLJ=1
    testoutputzip="testoutput-idlj.zip"
  else
    IDLJ=0
    testoutputzip="testoutput-jacorb.zip"
  fi

  git checkout TaskImpl.properties
  sed -i TaskImpl.properties -e "s#^COMMAND_LINE_0=.*#COMMAND_LINE_0=${JAVA_HOME}/bin/java#"
  [ $? = 0 ] || fatal "sed TaskImpl.properties failed"

  # delete lines containing jacorb
  [ $IDLJ = 1 ] && sed -i TaskImpl.properties -e  '/^.*separator}jacorb/ d'

  # if the env variable MFACTOR is set then set the bean property CoreEnvironmentBean.timeoutFactor
  if [[ "$MFACTOR" =~ ^[0-9]+$ ]] ; then
    sed -i TaskImpl.properties -e "s/COMMAND_LINE_12=-DCoreEnvironmentBean.timeoutFactor=[0-9]*/COMMAND_LINE_12=-DCoreEnvironmentBean.timeoutFactor=${MFACTOR}/"
    # Note that setting the timeout too high (eg 2*240) will cause the defaulttimeout test cases to take
    # longer than the Task kill timeout period
    let txtimeout=$MFACTOR*120
    sed -i TaskImpl.properties -e "s/COMMAND_LINE_13=-DCoordinatorEnvironmentBean.defaultTimeout=[0-9]*/COMMAND_LINE_13=-DCoordinatorEnvironmentBean.defaultTimeout=${txtimeout}/"
  fi
  # if IPV6_OPTS is not set get the jdbc drivers (we do not run the jdbc tests in IPv6 mode)
  [ -z "${IPV6_OPTS+x}" ] && ant -DisIdlj=$IDLJ "$QA_BUILD_ARGS" get.drivers dist ||
    ant -DisIdlj=$IDLJ "$QA_BUILD_ARGS" dist

  [ $? = 0 ] || fatal "qa build failed"

  if [[ $# == 0 || $# > 0 && "$1" != "-DskipTests" ]]; then
    # determine which QA test target to call
    target="ci-tests" # the default is to run everything (ci-tests)

    # if IPV6_OPTS is set then do not do the jdbc tests (ie run target junit-testsuite)
    [ -z "${IPV6_OPTS+x}" ] || target="junit"

    # IDLJ = 1 overrides the previous setting 
    [ $IDLJ = 1 ] && target="ci-jts-tests" # if called with orb=idlj then only run the jtsremote tests

    # QA_TARGET overrides the previous settings
    [ x$QA_TARGET = x ] || target=$QA_TARGET # the caller can force the build to run a specific target

    # run the ant target (QA_TESTMETHODS is a list of method names in QA_TESTGROUP to be executed)
    [ $QA_TRACE ] && enable_qa_trace
    [ $QA_TESTMETHODS ] || QA_TESTMETHODS=""

    if [ "x$QA_TESTGROUP" != "x" ]; then
      if [[ "$QA_STRESS" =~ ^[0-9]+$ ]] ; then
        ok=0
        for i in `seq 1 $QA_STRESS`; do
          echo run $i;
          ant -f run-tests.xml -Dtest.name=$QA_TESTGROUP -Dtest.methods="$QA_TESTMETHODS" onetest;
          if [ $? -ne 0 ]; then
            ok=1; break;
          fi
        done
      else
        ant -f run-tests.xml -Dtest.name=$QA_TESTGROUP -Dtest.methods="$QA_TESTMETHODS" onetest;
        ok=$?
      fi
    else
      ant -f run-tests.xml $target $QA_PROFILE
      ok=$?
    fi

    if [ -f TEST-failures.txt ]; then
      echo "Test Failures:"
      cat TEST-failures.txt 
    fi

    # archive the jtsremote test output (use a name related to the orb that was used for the tests)
    mv TEST-*.txt testoutput 2>/dev/null
    ant -f run-tests.xml testoutput.zip -Dtestoutput.zipname=$testoutputzip
    return $ok
  fi
  return 0
}

function qa_tests {
  ok1=0;
  ok2=0;
  if [ $SUN_ORB = 1 ]; then
    qa_tests_once "$@" "orb=idlj" "$@" # run qa against the Sun orb
    ok2=$?
  fi
  if [ $JAC_ORB = 1 ]; then
    qa_tests_once "$@"    # run qa against the default orb
    ok1=$?
  fi

  [ $ok1 = 0 ] || echo some jacorb QA tests failed
  [ $ok2 = 0 ] || echo some Sun ORB QA tests failed

  [ $ok1 = 0 -a $ok2 = 0 ] || fatal "some qa tests failed"
}

check_if_pull_closed

init_test_options

# if QA_BUILD_ARGS is unset then get the db drivers form the file system otherwise get them from the
# default location (see build.xml). Note ${var+x} substitutes null for the parameter if var is undefined
[ -z "${QA_BUILD_ARGS+x}" ] && QA_BUILD_ARGS="-Ddriver.url=http://172.17.131.2/userContent/dbdrivers"

# Note: set QA_TARGET if you want to override the QA test ant target

# for IPv6 testing use export ARQ_PROF=arqIPv6
# if you don't want to run all the XTS tests set WSTX_MODULES to the ones you want, eg:
# export WSTX_MODULES="WSAS,WSCF,WSTX,WS-C,WS-T,xtstest,crash-recovery-tests"

[ -z "${WORKSPACE}" ] && fatal "UNSET WORKSPACE"
[ -z "${WILDFLY_MASTER_VERSION}" ] && fatal "UNSET WILDFLY_MASTER_VERSION"

# FOR DEBUGGING SUBSEQUENT ISSUES
free -m

#Make sure no JBoss processes running
for i in `ps -eaf | grep java | grep "standalone.*.xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
#Make sure no processes from a previous test suite run is still running
MainClassPatterns="org.jboss.jbossts.qa com.arjuna.ats.arjuna.recovery.RecoveryManager"
kill_qa_suite_processes $MainClassPatterns

# if we are building with IPv6 tell ant about it
export ANT_OPTS="$ANT_OPTS $IPV6_OPTS"

# run the job
[ $NARAYANA_BUILD = 1 ] && build_narayana "$@"
[ $AS_BUILD = 1 ] && build_as "$@"
[ $BLACKTIE = 1 ] && blacktie "$@"
[ $JTA_CDI_TESTS = 1 ] && jta_cdi_tests "$@"
[ $XTS_AS_TESTS = 1 ] && xts_as_tests
[ $RTS_AS_TESTS = 1 ] && rts_as_tests
[ $TXF_TESTS = 1 ] && txframework_tests "$@"
[ $XTS_TESTS = 1 ] && xts_tests "$@"
[ $txbridge = 1 ] && tx_bridge_tests "$@"
[ $RTS_TESTS = 1 ] && rts_tests "$@"
[ $QA_TESTS = 1 ] && qa_tests "$@"

if [[ -z $PROFILE ]]; then
    comment_on_pull "All tests passed - Job complete $BUILD_URL"
elif [[ $PROFILE == "BLACKTIE" ]]; then
    comment_on_pull "$PROFILE profile tests passed on Linux - Job complete $BUILD_URL"
else
    comment_on_pull "$PROFILE profile tests passed - Job complete $BUILD_URL"
fi

exit 0 # any failure would have resulted in fatal being called which exits with a value of 1
