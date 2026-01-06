#!/bin/bash -e
set +x

function fatal {
  echo "$1"
  exit 1
}

function which_java {
  type -p java 2>&1 > /dev/null
  if [ $? = 0 ]; then
    _java=java
  elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    _java="$JAVA_HOME/bin/java"
  else
    unset _java
  fi

  if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | grep -oP 'version "?(1\.)?\K\d+' || true)
    echo $version
  fi
}

function min_java {
  MIN_JAVA=17
  _jdk=`which_java`
  if [ "$_jdk" -lt "$MIN_JAVA" ]; then
    fatal $1
  fi
}

# return 0 if using the IBM java compiler
function is_ibm {
  jvendor=$(java -XshowSettings:properties -version 2>&1 | awk -F '"' '/java.vendor = / {print $1}' || :)
  [[ $jvendor == *"IBM Corporation"* ]]
}

function init_test_variables {
  is_ibm || :
  ISIBM=$?

  min_java "Narayana does not support JDKs less than 17"

  [ $NARAYANA_CURRENT_VERSION ] || NARAYANA_CURRENT_VERSION="7.3.4.Final-SNAPSHOT"
  [ $CODE_COVERAGE ] || CODE_COVERAGE=0
  [ x"$CODE_COVERAGE_ARGS" != "x" ] || CODE_COVERAGE_ARGS=""
  [ $ARQ_PROF ] || ARQ_PROF=arq	# IPv4 arquillian profile

  if [[ $PROFILE == "CORE" ]]; then
    export AS_BUILD=1 AS_TESTS=0 NARAYANA_BUILD=1 NARAYANA_TESTS=1 XTS_AS_TESTS=0 XTS_TESTS=0 COMPENSATIONS_TESTS=0 txbridge=0
    export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=1 QA_TESTS=0 JAC_ORB=0 JTA_AS_TESTS=1
  elif [[ $PROFILE == "AS_TESTS" ]]; then
    export AS_BUILD=1 AS_TESTS=1 NARAYANA_BUILD=1 NARAYANA_TESTS=0 XTS_AS_TESTS=0 XTS_TESTS=0 COMPENSATIONS_TESTS=0 txbridge=0
    export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 JAC_ORB=0 JTA_AS_TESTS=0
  elif [[ $PROFILE == "RTS" ]]; then
    export AS_BUILD=1 AS_TESTS=0 NARAYANA_BUILD=1 NARAYANA_TESTS=0 XTS_AS_TESTS=0 XTS_TESTS=0 COMPENSATIONS_TESTS=0 txbridge=0
    export RTS_AS_TESTS=1 RTS_TESTS=1 JTA_CDI_TESTS=0 QA_TESTS=0 JAC_ORB=0 JTA_AS_TESTS=0
  elif [[ $PROFILE == "JACOCO" ]]; then
    export AS_BUILD=1 AS_TESTS=0 NARAYANA_BUILD=1 NARAYANA_TESTS=1 XTS_AS_TESTS=0 XTS_TESTS=1 COMPENSATIONS_TESTS=1 txbridge=1
    export RTS_AS_TESTS=0 RTS_TESTS=1 JTA_CDI_TESTS=1 QA_TESTS=1 JAC_ORB=0 JTA_AS_TESTS=1
    export CODE_COVERAGE=1 CODE_COVERAGE_ARGS="-PcodeCoverage -Pfindbugs"
    [ -z ${MAVEN_OPTS+x} ] && export MAVEN_OPTS="-Xms2048m -Xmx2048m"
  elif [[ $PROFILE == "XTS" ]]; then
    export AS_BUILD=1 AS_TESTS=0 NARAYANA_BUILD=1 NARAYANA_TESTS=0 XTS_AS_TESTS=1 XTS_TESTS=1 COMPENSATIONS_TESTS=1 txbridge=1
    export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 JAC_ORB=0 JTA_AS_TESTS=0
  elif [[ $PROFILE == "QA_JTA" ]]; then
    export AS_BUILD=0 AS_TESTS=0 NARAYANA_BUILD=1 NARAYANA_TESTS=0 XTS_AS_TESTS=0 XTS_TESTS=0 COMPENSATIONS_TESTS=0 txbridge=0
    export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 OPENJDK_ORB=1 JAC_ORB=0 QA_TARGET=ci-tests-nojts JTA_AS_TESTS=0
  elif [[ $PROFILE == "QA_JTS_OPENJDKORB" ]]; then
    export AS_BUILD=0 AS_TESTS=0 NARAYANA_BUILD=1  NARAYANA_TESTS=0 XTS_AS_TESTS=0 XTS_TESTS=0 COMPENSATIONS_TESTS=0 txbridge=0
    export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 OPENJDK_ORB=1 JAC_ORB=0 QA_TARGET=ci-jts-tests
    export JTA_AS_TESTS=0
  elif [[ $PROFILE == "PERFORMANCE" ]]; then
    export AS_BUILD=0 AS_TESTS=0 NARAYANA_BUILD=1 NARAYANA_TESTS=0 XTS_AS_TESTS=0 XTS_TESTS=0 COMPENSATIONS_TESTS=0 txbridge=0
    export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 JAC_ORB=0 JTA_AS_TESTS=0 PERF_TESTS=1
  elif [[ $PROFILE == "DB_TESTS" ]]; then
    export AS_BUILD=0 AS_TESTS=0 NARAYANA_BUILD=1 NARAYANA_TESTS=1 XTS_AS_TESTS=0 XTS_TESTS=0 COMPENSATIONS_TESTS=0 txbridge=0
    export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 OPENJDK_ORB=1 JAC_ORB=0 JTA_AS_TESTS=0
  fi

  [ $NARAYANA_TESTS ] || NARAYANA_TESTS=0	# run the narayana surefire tests
  [ $NARAYANA_BUILD ] || NARAYANA_BUILD=0 # build narayana
  [ $AS_BUILD ] || AS_BUILD=0 # build the AS
  [ $AS_TESTS ] || AS_TESTS=0 # Run WildFly/JBoss EAP testsuite
  [ $COMPENSATIONS_TESTS ] || COMPENSATIONS_TESTS=0 # compensations tests
  [ $XTS_TESTS ] || XTS_TESTS=0 # XTS tests
  [ $XTS_AS_TESTS ] || XTS_AS_TESTS=0 # XTS tests
  [ $RTS_AS_TESTS ] || RTS_AS_TESTS=0 # RTS tests
  [ $RTS_TESTS ] || RTS_TESTS=0 # REST-AT Test
  [ $JTA_CDI_TESTS ] || JTA_CDI_TESTS=0 # JTA CDI Tests
  [ $JTA_AS_TESTS ] || JTA_AS_TESTS=0 # JTA AS tests
  [ $QA_TESTS ] || QA_TESTS=0 # QA test suite
  [ $OPENJDK_ORB ] || OPENJDK_ORB=0 # Run QA test suite against the openjdk orb
  [ $txbridge ] || txbridge=0 # bridge tests
  [ $PERF_TESTS ] || PERF_TESTS=0 # benchmarks

  JAVA_VERSION=$(java -version 2>&1 | grep "\(java\|openjdk\) version" | cut -d\  -f3 | tr -d '"' | tr -d '[:space:]' | awk -F . '{if ($1==1) print $2; else print $1}')

  # Using bash for building/testing WildFly
  export BASH_INTERPRETER=/bin/bash
}

function kill_qa_suite_processes {
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

function build_narayana {
  echo "Checking if need SPI PR"
  if [ -n "$SPI_BRANCH" ]; then
    echo "Building SPI PR"
    if [ -d jboss-transaction-spi ]; then
      rm -rf jboss-transaction-spi
    fi
    git clone https://github.com/jbosstm/jboss-transaction-spi.git -o jbosstm
    [ $? -eq 0 ] || fatal "git clone https://github.com/jbosstm/jboss-transaction-spi.git failed"
    cd jboss-transaction-spi
    git fetch jbosstm +refs/pull/*/head:refs/remotes/jbosstm/pull/*/head
    [ $? -eq 0 ] || fatal "git fetch of pulls failed"
    git checkout $SPI_BRANCH
    [ $? -eq 0 ] || fatal "git fetch of pull branch failed"
    cd ../
    ./build.sh -f jboss-transaction-spi/pom.xml -B clean install
    [ $? -eq 0 ] || fatal "Build of SPI failed"
  fi

  echo "Building Narayana"
  cd $WORKSPACE

  [ $NARAYANA_TESTS = 1 ] && NARAYANA_ARGS= || NARAYANA_ARGS="-DskipTests"

  echo "Using MAVEN_OPTS: $MAVEN_OPTS"

  ./build.sh -B -Pcommunity$OBJECT_STORE_PROFILE $ORBARG "$@" $NARAYANA_ARGS $IPV6_OPTS $CODE_COVERAGE_ARGS clean install

  [ $? -eq 0 ] || fatal "narayana build failed"

  return 0
}

function clone_as {
  cd ${WORKSPACE}
  if [ -d jboss-as ]; then
    echo "Using existing checkout of WildFly. If a fresh build should be used, delete the folder ${WORKSPACE}/jboss-as"
    cd jboss-as
  else
    echo "Cloning AS sources from https://github.com/jbosstm/jboss-as.git"
    echo "First time checkout of WildFly"
    git clone https://github.com/jbosstm/jboss-as.git -o jbosstm
    [ $? -eq 0 ] || fatal "git clone https://github.com/jbosstm/jboss-as.git failed"

    cd jboss-as

    git remote add upstream https://github.com/wildfly/wildfly.git

    [ -z "$AS_BRANCH" ] || git fetch jbosstm +refs/pull/*/head:refs/remotes/jbosstm/pull/*/head
    [ $? -eq 0 ] || fatal "git fetch of pulls failed"
    [ -z "$AS_BRANCH" ] || git checkout $AS_BRANCH
    [ $? -eq 0 ] || fatal "git fetch of pull branch failed"
    [ -z "$AS_BRANCH" ] || echo "Using non-default AS_BRANCH: $AS_BRANCH"
    if [ -n "$AS_BRANCH" ] && [ -n "$NARAYANA_FORK_BRANCH_TO_REBASE_ON" ]; then
        git pull --rebase jbosstm "$NARAYANA_FORK_BRANCH_TO_REBASE_ON" \
            && echo "Rebased AS_BRANCH on jbosstm/jboss-as $NARAYANA_FORK_BRANCH_TO_REBASE_ON" \
            || fatal "git pull --rebase failed"
    fi

    git fetch upstream
    echo "This is the JBoss-AS commit"
    echo $(git rev-parse upstream/main)
    echo "This is the AS_BRANCH $AS_BRANCH commit"
    echo $(git rev-parse HEAD)

    echo "Rebasing the wildfly upstream/main on top of the AS_BRANCH $AS_BRANCH"
    git pull --rebase upstream main
    [ $? -eq 0 ] || fatal "git rebase failed"
  fi

  WILDFLY_CLONED_REPO=$(pwd)
  cd $WORKSPACE
}

function build_as {
  min_java "Requested JDK version $_jdk cannot build JBoss EAP/WildFly: please use jdk 17 instead"

  echo "Building JBoss EAP/WildFly"

  cd ${WORKSPACE}
  if [ -d jboss-as ]; then
    echo "Using existing checkout of WildFly. If a fresh build should be used, delete the folder ${WORKSPACE}/jboss-as"
    cd jboss-as
    WILDFLY_CLONED_REPO=$(pwd)
  else
    clone_as
    cd $WILDFLY_CLONED_REPO
  fi

  WILDFLY_VERSION_FROM_JBOSS_AS=`awk '/wildfly-parent/ { while(!/<version>/) {getline;} print; }' ${WILDFLY_CLONED_REPO}/pom.xml | cut -d \< -f 2|cut -d \> -f 2`

  # execute all tests only if AS_TESTS is set to 1
  if [ "$AS_TESTS" = 1 ]; then
      WILDFLY_ARGS=${WILDFLY_ARGS:-"-DallTests"}
  else
      WILDFLY_ARGS=${WILDFLY_ARGS:-"-DskipTests"}
  fi
  ./build.sh clean install -Prelease -B -fae $WILDFLY_ARGS $IPV6_OPTS -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@"
  [ $? -eq 0 ] || fatal "AS build failed"

  echo "AS version is ${WILDFLY_VERSION_FROM_JBOSS_AS}"
  JBOSS_HOME=${WILDFLY_CLONED_REPO}/dist/target/wildfly-${WILDFLY_VERSION_FROM_JBOSS_AS}
  export JBOSS_HOME=`echo  $JBOSS_HOME`

  # init files under JBOSS_HOME before AS TESTS is started
  init_jboss_home

  cd $WORKSPACE
}

function init_jboss_home {
  [ -d $JBOSS_HOME ] || fatal "missing AS - $JBOSS_HOME is not a directory"
  echo "JBOSS_HOME=$JBOSS_HOME"
  cp ${JBOSS_HOME}/docs/examples/configs/standalone-xts.xml ${JBOSS_HOME}/standalone/configuration
  cp ${JBOSS_HOME}/docs/examples/configs/standalone-rts.xml ${JBOSS_HOME}/standalone/configuration
  # configuring bigger connection timeout for jboss cli (WFLY-13385)
  CONF="${JBOSS_HOME}/bin/jboss-cli.xml"
  sed -e 's#^\(.*</jboss-cli>\)#<connection-timeout>30000</connection-timeout>\n\1#' "$CONF" > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
  grep 'connection-timeout' "${CONF}"
  #Enable remote debugger
  echo JAVA_OPTS='"$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"' >> "$JBOSS_HOME"/bin/standalone.conf

  if [ $AS_TESTS_TRACE ]; then
    enable_as_trace "$JBOSS_HOME/standalone/configuration/standalone.xml"
    enable_as_trace "$JBOSS_HOME/standalone/configuration/standalone-full.xml"
  fi
}

function xts_as_tests {
  echo "#-1. XTS AS Tests"
  [ -z "$WILDFLY_CLONED_REPO" ] && clone_as
  cd $WILDFLY_CLONED_REPO
  ./build.sh -f xts/pom.xml -B $IPV6_OPTS -Dtimeout.factor=300 -Dsurefire.forked.process.timeout=12000 -Djboss.dist="$JBOSS_HOME" -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@" test
  [ $? -eq 0 ] || fatal "XTS AS Test failed"
  ./build.sh -f testsuite/integration/xts/pom.xml -fae -B -Pxts.integration.tests.profile -Djboss.dist="$JBOSS_HOME" -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@" test
  [ $? -eq 0 ] || fatal "XTS AS Integration Test failed"
  cd ${WORKSPACE}
}

function rts_as_tests {
  echo "#-1. RTS AS Tests"
  [ -z "$WILDFLY_CLONED_REPO" ] && clone_as
  cd $WILDFLY_CLONED_REPO
  ./build.sh -f rts/pom.xml -B $IPV6_OPTS -Dtimeout.factor=300 -Dsurefire.forked.process.timeout=12000 -Djboss.dist="$JBOSS_HOME" -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@" test
  [ $? -eq 0 ] || fatal "RTS AS Test failed"
  ./build.sh -f testsuite/integration/rts/pom.xml -fae -B -Prts.integration.tests.profile -Djboss.dist="$JBOSS_HOME" -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@" test
  [ $? -eq 0 ] || fatal "RTS AS Integration Test failed"
  cd ${WORKSPACE}
}

function jta_as_tests {
  echo "#-1. JTA AS Tests"
  # If ARQ_PROF is not set an arquillian profile will not run but I guess the jdk17 profile would/could be activated still
  ./build.sh -f ArjunaJTA/jta/pom.xml -fae -B -DarqProfileActivated=$ARQ_PROF $CODE_COVERAGE_ARGS "$@" test
  [ $? -eq 0 ] || fatal "JTA AS Integration Test failed"
  [ -z "$WILDFLY_CLONED_REPO" ] && clone_as
  cd $WILDFLY_CLONED_REPO
  # Execute some directly relevant tests from modules of the application server
  ./build.sh -f transactions/pom.xml -B $IPV6_OPTS -Dtimeout.factor=300 -Dsurefire.forked.process.timeout=12000 -Djboss.dist="$JBOSS_HOME" -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@" test
  [ $? -eq 0 ] || fatal "JTA AS transactions Test failed"
  ./build.sh -f iiop-openjdk/pom.xml -B $IPV6_OPTS -Dtimeout.factor=300 -Dsurefire.forked.process.timeout=12000 -Djboss.dist="$JBOSS_HOME" -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@" test
  [ $? -eq 0 ] || fatal "JTA AS iiop-openjdk Test failed"
  cd ${WORKSPACE}
}

function rts_tests {
  echo "#0. REST-AT Integration Test"
  ./build.sh -f rts/at/integration/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" test
  [ $? -eq 0 ] || fatal "REST-AT Integration Test failed"

  echo "#0. REST-AT To JTA Bridge Test"
  ./build.sh -f rts/at/bridge/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" test
  [ $? -eq 0 ] || fatal "REST-AT To JTA Bridge Test failed"
}

function jta_cdi_tests {
  echo "#0. JTA CDI Tests"
  ./build.sh -f ArjunaJTA/cdi/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" test
  [ $? -eq 0 ] || fatal "JTA CDI Test failed"
}

function compensations_tests {
  echo "#0. compensations Test"
  [ -e ./rts/at/webservice/target/restat-web-*.war ] || ./build.sh -f rts/pom.xml -fae -B install -DskipTests
  cp ./rts/at/webservice/target/restat-web-*.war $JBOSS_HOME/standalone/deployments
  ./build.sh -f compensations/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" test
  [ $? -eq 0 ] || fatal "compensations build failed"
  ./build.sh -f compensations/pom.xml -fae -B -P$ARQ_PROF-distributed $CODE_COVERAGE_ARGS "$@" test
  [ $? -eq 0 ] || fatal "compensations build failed"
  ./build.sh -f compensations/pom.xml -fae -B -P$ARQ_PROF-weld $CODE_COVERAGE_ARGS "$@" test
  [ $? -eq 0 ] || fatal "compensations build failed"
  rm $JBOSS_HOME/standalone/deployments/restat-web-*.war
  [ $? -eq 0 ] || fatal "Could not remove .war file"
}

function xts_tests {
  echo "#1 XTS: WSTX11 INTEROP, UNIT TESTS and CRASH RECOVERY TESTS"

  CONF="${JBOSS_HOME}/standalone/configuration/standalone-xts.xml"
  [ $XTS_TRACE ] && enable_as_trace "$CONF"

  cd $WORKSPACE
  ran_crt=1

  grep async-registration "$CONF" && sed -e 's#<[^<]*async-registration[^>]*>##g' $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
  sed -e 's#\(<subsystem.*xts.*\)#\1\n            <async-registration enabled="true"/>#' $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"

  if [ $WSTX_MODULES ]; then
    [[ $WSTX_MODULES = *crash-recovery-tests* ]] || ran_crt=0
    echo "BUILDING SPECIFIC WSTX11 modules"
    ./build.sh -f XTS/localjunit/pom.xml -B --projects "$WSTX_MODULES" -P$ARQ_PROF "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? -eq 0 ] || fatal "XTS/localjunit/pom.xml failed"
  else
    ./build.sh -f XTS/localjunit/unit/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? -eq 0 ] || fatal "XTS localjunit unit build failed"
    ./build.sh -f XTS/localjunit/disabled-context-propagation/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? -eq 0 ] || fatal "XTS localjunit disabled-context-propagation build failed"
    ./build.sh -f XTS/localjunit/WSTX11-interop/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? -eq 0 ] || fatal "XTS localjunit WSTX11 build failed"
    ./build.sh -f XTS/localjunit/WSTFSC07-interop/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? -eq 0 ] || fatal "XTS localjunit WSTFSC07 build failed"
    ./build.sh -f XTS/localjunit/xtstest/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? -eq 0 ] || fatal "XTS localjunit xtstest build failed (no test run)"
    ./build.sh -f XTS/localjunit/crash-recovery-tests/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? -eq 0 ] || fatal "XTS localjunit crash-recovery-tests build failed"
  fi

  [ $? -eq 0 ] || fatal "XTS: SOME TESTS failed"
  if [ $ran_crt = 1 ] && [[ ! "$@" =~ "-DskipTests" ]] && [ $XTS_AS_TESTS = 1 ]; then
    (cd XTS/localjunit/crash-recovery-tests && java -cp target/classes/ com.arjuna.qa.simplifylogs.SimplifyLogs ./target/log/ ./target/log-simplified)
    if [[ $? != 0 && $ISIBM != 0 && -z $CODE_COVERAGE_ARGS ]]; then
      fatal "Simplify CRASH RECOVERY logs failed"
    fi
  fi
}

function tx_bridge_tests {
  echo "XTS: TXBRIDGE TESTS update conf"
  CONF="${JBOSS_HOME}/standalone/configuration/standalone-xts.xml"
  [ $XTS_TRACE ] && enable_as_trace "$CONF"
  cd $WORKSPACE
  grep recovery-listener "$CONF" && sed -e s/recovery-listener=\"true\"//g   $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
  sed -e "s#\(recovery-environment\) \(socket-binding\)#\\1 recovery-listener=\"true\" \\2#"   $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
  [ $? -eq 0 ] || fatal "#3.TXBRIDGE TESTS: sed failed"

  echo "XTS: TXBRIDGE TESTS"
  ./build.sh -f txbridge/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS install "$@"
  [ $? -eq 0 ] || fatal "#3.TXBRIDGE TESTS failed"
}

function set_qa_log_level {
echo "creating file $WORKSPACE/qa/ext/etc/log4j.xml"
mkdir -p $WORKSPACE/qa/ext/etc/
touch $WORKSPACE/qa/ext/etc/log4j.xml
cat << EOF > $WORKSPACE/qa/ext/etc/log4j.xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">

<log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">

    <appender name="console" class="org.apache.log4j.ConsoleAppender">
        <param name="Target" value="System.err"/>
        <param name="Threshold" value="$1"/>

        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%c\t[%t]\t%m%n"/>
        </layout>
    </appender>

    <appender name="file" class="org.apache.log4j.FileAppender">
        <param name="File" value="logs/test.log"/>
        <param name="Append" value="false"/>
        <param name="Threshold" value="$1"/>

        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%c\t[%t]\t%m%n"/>
        </layout>
    </appender>

    <category name="com.arjuna">
        <level value="$1"/>
        <appender-ref ref="console"/>
        <appender-ref ref="file"/>
    </category>
</log4j:configuration>
EOF
}

function enable_as_trace {
    CONF=${1:-"${JBOSS_HOME}/standalone/configuration/standalone-xts.xml"}
    echo "Enable trace logs for file '$CONF'"

    sed -e '/<logger category="com.arjuna">$/N;s/<logger category="com.arjuna">\n *<level name="WARN"\/>/<logger category="com.arjuna"><level name="TRACE"\/><\/logger><logger category="org.jboss.narayana"><level name="TRACE"\/><\/logger><logger category="org.jboss.jbossts"><level name="TRACE"\/><\/logger><logger category="org.jboss.jbossts.txbridge"><level name="TRACE"\/>/' $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
    sed -e '/<console-handler name="CONSOLE">$/N;s/<console-handler name="CONSOLE">\n *<level name="INFO"\/>/<console-handler name="CONSOLE"><level name="TRACE"\/>/' $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
}

function add_qa_xargs {
  NXT=$(grep "NEXT_COMMAND_LINE_ARG=" TaskImpl.properties)
  [ $? -eq 0 ] || return 1

  let i=$(echo $NXT | sed 's/^.*[^0-9]\([0-9]*\).*$/\1/')

  XARGS=
  IFS=' ' read -ra ADDR <<< "$1"
  for j in "${ADDR[@]}"; do
    XARGS="${XARGS}COMMAND_LINE_$i=$j\n"
    let i=i+1
  done

  sed -e "s#NEXT_COMMAND_LINE_ARG=.*\$#${XARGS}#" TaskImpl.properties > "TaskImpl.properties.tmp" && mv "TaskImpl.properties.tmp" "TaskImpl.properties"
}

function qa_tests_once {
  echo "QA Test Suite $@"

  # Download dependencies
  cd $WORKSPACE
  ./build.sh -f qa/pom.xml -B dependency:copy-dependencies
  [ $? -eq 0 ] || fatal "Copy dependency failed"

  cd $WORKSPACE/qa
  unset orb
  codeCoverage=false;

  # look for an argument of the form orb=<something>
  for i in $@; do
    [ ${i%%=*} = "orb" ] && orb=${i##*=}
    [ -n "$CODE_COVERAGE_ARGS" ] && codeCoverage=true
  done

  cp TaskImpl.properties.template TaskImpl.properties

  # check to see which orb we are running against:
  if [ x$orb = x"openjdk" ]; then
    orbtype=openjdk
  else
    fatal "Narayana does not support the specified ORB. The only supported ORB is openjdk"
  fi

  testoutputzip="testoutput-${orbtype}.zip"

  sed -e "s#^COMMAND_LINE_0=.*#COMMAND_LINE_0=${JAVA_HOME}/bin/java#" TaskImpl.properties > "TaskImpl.properties.tmp" && mv "TaskImpl.properties.tmp" "TaskImpl.properties"
  [ $? -eq 0 ] || fatal "sed TaskImpl.properties failed"

  if [[ x"$EXTRA_QA_SYSTEM_PROPERTIES" != "x" ]]; then
    add_qa_xargs "$EXTRA_QA_SYSTEM_PROPERTIES"
  fi

  # if the env variable MFACTOR is set then set the bean property CoreEnvironmentBean.timeoutFactor
  if [[ -n "$MFACTOR" ]] ; then
    sed -e "s/COMMAND_LINE_12=-DCoreEnvironmentBean.timeoutFactor=[0-9]*/COMMAND_LINE_12=-DCoreEnvironmentBean.timeoutFactor=${MFACTOR}/" TaskImpl.properties > "TaskImpl.properties.tmp" && mv "TaskImpl.properties.tmp" "TaskImpl.properties"
    # Note that setting the timeout too high (eg 2*240) will cause the defaulttimeout test cases to take
    # longer than the Task kill timeout period
    let txtimeout=$MFACTOR*120
    sed -e "s/COMMAND_LINE_13=-DCoordinatorEnvironmentBean.defaultTimeout=[0-9]*/COMMAND_LINE_13=-DCoordinatorEnvironmentBean.defaultTimeout=${txtimeout}/" TaskImpl.properties > "TaskImpl.properties.tmp" && mv "TaskImpl.properties.tmp" "TaskImpl.properties"
  fi

  [ -z "${IPV6_OPTS+x}" ] && ant -Dorbtype=$orbtype "$QA_BUILD_ARGS" dist ||
    ant -Dorbtype=$orbtype "$QA_BUILD_ARGS" dist

  [ $? -eq 0 ] || fatal "qa build failed"

  if [[ $# == 0 || $# > 0 && "$1" != "-DskipTests" ]]; then
    # determine which QA test target to call
    target="ci-tests" # the default is to run everything (ci-tests)

    # if IPV6_OPTS is set then do not do the jdbc tests (ie run target junit-testsuite)
    [ -z "${IPV6_OPTS+x}" ] || target="junit"

    # QA_TARGET overrides the previous settings
    [ x$QA_TARGET = x ] || target=$QA_TARGET # the caller can force the build to run a specific target

    # run the ant target (QA_TESTMETHODS is a list of method names in QA_TESTGROUP to be executed)
    if [ "x$QA_TRACE" = "x1" ]; then
        set_qa_log_level TRACE
    else
        set_qa_log_level INFO
    fi
    [ $QA_TESTMETHODS ] || QA_TESTMETHODS=""

    if [ "x$QA_TESTGROUP" != "x" ]; then
      ok=0
      if [[ -n "$QA_STRESS" ]] ; then
        for i in `seq 1 $QA_STRESS`; do
          echo run $i;
          ant -f run-tests.xml $QA_PROFILE -Dtest.name=$QA_TESTGROUP -Dtest.methods="$QA_TESTMETHODS" onetest -Dcode.coverage=$codeCoverage -Dorbtype=$orbtype;
          if [ $? -ne 0 ]; then
            ok=1; break;
          fi
        done
      else
        for testgroup in $QA_TESTGROUP; do
          ant -f run-tests.xml $QA_PROFILE -Dtest.name=$testgroup -Dtest.methods="$QA_TESTMETHODS" onetest -Dcode.coverage=$codeCoverage -Dorbtype=$orbtype;
          if [ $? -ne 0 ]; then
            echo "test group $testgroup failed"
            ok=1;
          fi
        done
      fi
    else
      ant -f run-tests.xml $target $QA_PROFILE -Dcode.coverage=$codeCoverage -Dorbtype=$orbtype || :
      ok=$?
    fi

    if [ -f TEST-failures.txt ]; then
      echo "Test Failures:"
      cat TEST-failures.txt
    fi

    if [ $codeCoverage = true ]; then
      echo "generating test coverage report"
      ant -f run-tests.xml jacoco-report
      [ $? -eq 0 ] || fatal "Jacoco report generation failed"
    fi

    # archive the jtsremote test output (use a name related to the orb that was used for the tests)
    # if the tests fail very early a testoutput folder may not exist
    [[ -d testoutput ]] || mkdir testoutput
    mv TEST-*.txt testoutput 2>/dev/null
    ant -f run-tests.xml testoutput.zip -Dtestoutput.zipname=$testoutputzip
    return $ok
  fi
  return 0
}

function qa_tests {
  openjdk_orb_tests_ok=0;

  # OPENJDK_ORB #
  # run qa against the openjdk orb
  qa_tests_once "orb=openjdk" "$@" || :
  openjdk_orb_tests_ok=$?

  [ $openjdk_orb_tests_ok = 0 ] || fatal "some openjdk ORB QA tests failed"
}

function hw_spec {
  # macOS
  if command -v system_profiler >/dev/null 2>&1; then
    echo "sw_vers:"; sw_vers
    echo "system_profiler:"; system_profiler SPHardwareDataType SPSoftwareDataType
    return
  fi

  # Linux / CI
  echo "uname -a:"; uname -a

  echo "os-release:"
  [ -f /etc/redhat-release ] && cat /etc/redhat-release
  [ -f /etc/os-release ] && cat /etc/os-release

  echo "java version:"; java -version
  echo "free:"; command -v free >/dev/null && free -m
  echo "cpuinfo:"; [ -r /proc/cpuinfo ] && cat /proc/cpuinfo
  echo "meminfo:"; [ -r /proc/meminfo ] && cat /proc/meminfo
  echo "devices:"; [ -r /proc/devices ] && cat /proc/devices
  echo "scsi:"; [ -r /proc/scsi/scsi ] && cat /proc/scsi/scsi
  echo "partitions:"; [ -r /proc/partitions ] && cat /proc/partitions
  echo "lspci:"; command -v lspci >/dev/null && timeout 5s lspci || echo "skipped"
  echo "lsusb:"; command -v lsusb >/dev/null && timeout 5s lsusb || echo "skipped"
  echo "lsblk:"; command -v lsblk >/dev/null && timeout 5s lsblk || echo "skipped"
  echo "df:"; df
  echo "mount:"; mount | grep -E 'ext[234]|xfs|btrfs' || true
}

function perf_tests {
  cd $WORKSPACE
  [[ -d tmp ]] || mkdir tmp
  cd tmp
  rm -rf performance
  git clone https://github.com/jbosstm/performance
  cd performance/
  if [ -n "$PERF_PR_NUMBER" ];
  then
    git fetch origin +refs/pull/*/head:refs/remotes/origin/pull/*/head
    [ $? -eq 0 ] || fatal "git fetch of pulls failed"
    git checkout remotes/origin/pull/$PERF_PR_NUMBER/head
    [ $? -eq 0 ] || fatal "git fetch of pull branch failed"
    git pull --rebase --ff-only origin main
    [ $? -eq 0 ] || fatal "git rebase failed"
  fi

  BUILD_NARAYANA=n WORKSPACE=$(pwd) ./scripts/run_bm.sh || :
  res=$?
  cd $WORKSPACE

  hw_spec | tee hwinfo.txt

  [ $res = 0 ] || fatal "there were regressions in one or more of the benchmarks (see previous PR comment for details"
}

function generate_code_coverage_report {
  echo "Generating code coverage report"
  cd ${WORKSPACE}
  ./build.sh -B -f code-coverage/pom.xml $CODE_COVERAGE_ARGS "$@" clean install
  [ $? -eq 0 ] || fatal "Code coverage report generation failed"
}

ulimit -a
ulimit -c unlimited
ulimit -a

init_test_variables

# if QA_BUILD_ARGS is unset then get the db drivers form the file system otherwise get them from the
# default location (see build.xml). Note ${var+x} substitutes null for the parameter if var is undefined
[ -z "${QA_BUILD_ARGS+x}" ] && QA_BUILD_ARGS="-Ddriver.url=file:///home/jenkins/dbdrivers"

# Note: set QA_TARGET if you want to override the QA test ant target

# for IPv6 testing use export ARQ_PROF=arqIPv6
# if you don't want to run all the XTS tests set WSTX_MODULES to the ones you want, eg:
# export WSTX_MODULES="WSAS,WSCF,WSTX,WS-C,WS-T,xtstest,crash-recovery-tests"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE=$(cd "$SCRIPT_DIR/../.." && pwd)
echo "WORKSPACE is set to: ${WORKSPACE}"

# FOR DEBUGGING SUBSEQUENT ISSUES
if [ -x /usr/bin/free ]; then
    /usr/bin/free
elif [ -x /usr/bin/vm_stat ]; then
    /usr/bin/vm_stat
else
    echo "Skipping memory report: no free or vm_stat"
fi

#Make sure no JBoss processes running
for i in `ps -eaf | grep java | grep "standalone.*.xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
#Make sure no processes from a previous test suite run is still running
MainClassPatterns="org.jboss.jbossts.qa com.arjuna.ats.arjuna.recovery.RecoveryManager"
kill_qa_suite_processes $MainClassPatterns

export MEM_SIZE=1024m
[ -z ${MAVEN_OPTS+x} ] && export MAVEN_OPTS="-Xms$MEM_SIZE -Xmx$MEM_SIZE"
export ANT_OPTS="-Xms$MEM_SIZE -Xmx$MEM_SIZE"
export EXTRA_QA_SYSTEM_PROPERTIES="-Xms$MEM_SIZE -Xmx$MEM_SIZE -XX:ParallelGCThreads=2"

# if we are building with IPv6 tell ant about it
export ANT_OPTS="$ANT_OPTS $IPV6_OPTS"

# run the job

[ $NARAYANA_BUILD = 1 ] && build_narayana "$@"
[[ $JBOSS_HOME && $AS_BUILD == 0 ]] && init_jboss_home "$@"
[ $AS_BUILD = 1 ] && build_as "$@"
[ $JTA_CDI_TESTS = 1 ] && jta_cdi_tests "$@"
[ $XTS_AS_TESTS = 1 ] && xts_as_tests "$@"
[ $RTS_AS_TESTS = 1 ] && rts_as_tests "$@"
[ $JTA_AS_TESTS = 1 ] && jta_as_tests "$@"
[ $COMPENSATIONS_TESTS = 1 ] && [ "$_jdk" -ge 17 ] && compensations_tests "$@"
[ $XTS_TESTS = 1 ] && xts_tests "$@"
[ $txbridge = 1 ] && tx_bridge_tests "$@"
[ $RTS_TESTS = 1 ] && rts_tests "$@"
[ $QA_TESTS = 1 ] && qa_tests "$@"
[ $PERF_TESTS = 1 ] && perf_tests "$@"
[ $CODE_COVERAGE = 1 ] && generate_code_coverage_report "$@"

exit 0 # any failure would have resulted in fatal being called which exits with a value of 1
