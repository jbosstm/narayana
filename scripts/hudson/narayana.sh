#!/bin/bash

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

# return 0 if using the IBM java compiler
function is_ibm {
  jvendor=$(java -XshowSettings:properties -version 2>&1 | awk -F '"' '/java.vendor = / {print $1}')
  [[ $jvendor == *"IBM Corporation"* ]]
}

function get_pull_xargs {
  rval=0
  res=$(echo $1 | sed 's/\\r\\n/ /g')
  res=$(echo $res | sed 's/"/ /g')
  OLDIFS=$IFS
  IFS=', ' read -r -a array <<< "$res"
  echo "get_pull_xargs: parsing $1" 

  for element in "${array[@]}"
  do
    if [[ $element == *"="* ]]; then
      if [[ $element == "PROFILE="* ]]; then
        echo "comparing PROFILE=$2 with $element"
        if [[ ! "PROFILE=$2" == $element ]]; then
          echo "SKIPING PROFILE $2"
          rval=1
        fi
      else
        echo "exporting $element"
        export $element
      fi
    fi
  done

  IFS=$OLDIFS

  return $rval
}

function init_test_options {
    is_ibm
    ISIBM=$?
    [ $NARAYANA_CURRENT_VERSION ] || NARAYANA_CURRENT_VERSION="5.10.6.Final-SNAPSHOT"
    [ $CODE_COVERAGE ] || CODE_COVERAGE=0
    [ x"$CODE_COVERAGE_ARGS" != "x" ] || CODE_COVERAGE_ARGS=""
    [ $ARQ_PROF ] || ARQ_PROF=arq	# IPv4 arquillian profile
    [ $IBM_ORB ] || IBM_ORB=0
    [ $ENABLE_LRA_TRACE_LOGS ] || ENABLE_LRA_TRACE_LOGS=" -Dtest.logs.to.file=true -Ptrace.thorntail.test -Ptrace.lra.coordinator"

    AS_TESTS=0

    if ! get_pull_xargs "$PULL_DESCRIPTION_BODY" $PROFILE; then # see if the PR description overrides the profile
        echo "SKIPPING PROFILE=$PROFILE"
        export COMMENT_ON_PULL=""
        export AS_BUILD=0 NARAYANA_BUILD=0 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
        export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 OPENJDK_ORB=0 JAC_ORB=0 JTA_AS_TESTS=0
        export PERF_TESTS=0 OSGI_TESTS=0 TOMCAT_TESTS=0 LRA_TESTS=0
    elif [[ $PROFILE == "CORE" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!MAIN* ]]; then
          comment_on_pull "Started testing this pull request with MAIN profile: $BUILD_URL"
          export AS_BUILD=0 NARAYANA_BUILD=1 NARAYANA_TESTS=1 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0 JTA_AS_TESTS=0 OSGI_TESTS=1
          export TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "TOMCAT" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!TOMCAT* ]]; then
          comment_on_pull "Started testing this pull request with $PROFILE profile: $BUILD_URL"
          [ -z $NARAYANA_BUILD ] && NARAYANA_BUILD=1
          export AS_BUILD=0 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0 JTA_AS_TESTS=0 OSGI_TESTS=0
          export TOMCAT_TESTS=1 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "AS_TESTS" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!AS_TESTS* ]]; then
          comment_on_pull "Started testing this pull request with $PROFILE profile: $BUILD_URL"
          export AS_BUILD=1 AS_TESTS=1 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=1 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0 JTA_AS_TESTS=1 OSGI_TESTS=0
          export TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "RTS" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!RTS* ]]; then
          comment_on_pull "Started testing this pull request with RTS profile: $BUILD_URL"
          export AS_BUILD=1 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=1 RTS_TESTS=1 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0 JTA_AS_TESTS=0 OSGI_TESTS=0
          export TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "JACOCO" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!JACOCO* ]]; then
          comment_on_pull "Started testing this pull request with JACOCO profile: $BUILD_URL"
          export AS_BUILD=1 NARAYANA_BUILD=1 NARAYANA_TESTS=1 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=1 TXF_TESTS=1 txbridge=1
          export RTS_AS_TESTS=0 RTS_TESTS=1 JTA_CDI_TESTS=1 QA_TESTS=1 SUN_ORB=1 JAC_ORB=0 JTA_AS_TESTS=1 OSGI_TESTS=0
          export TOMCAT_TESTS=1 LRA_TESTS=0 CODE_COVERAGE=1 CODE_COVERAGE_ARGS="-PcodeCoverage -Pfindbugs"
          [ -z ${MAVEN_OPTS+x} ] && export MAVEN_OPTS="-Xms2048m -Xmx2048m"
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "XTS" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!XTS* ]]; then
          comment_on_pull "Started testing this pull request with XTS profile: $BUILD_URL"
          export AS_BUILD=1 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=1 XTS_TESTS=1 TXF_TESTS=1 txbridge=1
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0 JTA_AS_TESTS=0
          export TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "QA_JTA" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!QA_JTA* ]]; then
          comment_on_pull "Started testing this pull request with QA_JTA profile: $BUILD_URL"
          export AS_BUILD=0 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 SUN_ORB=0 JAC_ORB=1 QA_TARGET=ci-tests-nojts JTA_AS_TESTS=0
          export TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "QA_JTS_JACORB" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!QA_JTS_JACORB* ]]; then
          comment_on_pull "Started testing this pull request with QA_JTS_JACORB profile: $BUILD_URL"
          export AS_BUILD=0 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 OPENJDK_ORB=0 SUN_ORB=0 JAC_ORB=1 QA_TARGET=ci-jts-tests JTA_AS_TESTS=0
          export TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "QA_JTS_JDKORB" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!QA_JTS_JDKORB* ]]; then
          comment_on_pull "Started testing this pull request with QA_JTS_JDKORB profile: $BUILD_URL"
          export AS_BUILD=0 NARAYANA_BUILD=1  NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 OPENJDK_ORB=0 SUN_ORB=1 JAC_ORB=0 QA_TARGET=ci-jts-tests JTA_AS_TESTS=0
          export TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "QA_JTS_OPENJDKORB" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!QA_JTS_OPENJDKORB* ]]; then
          comment_on_pull "Started testing this pull request with QA_JTS_OPENJDKORB profile: $BUILD_URL"
          export AS_BUILD=0 NARAYANA_BUILD=1  NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 OPENJDK_ORB=1 SUN_ORB=0 JAC_ORB=0 QA_TARGET=ci-jts-tests
          export JTA_AS_TESTS=0 TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "BLACKTIE" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!BLACKTIE* ]]; then
          comment_on_pull "Started testing this pull request with BLACKTIE profile on Linux: $BUILD_URL"
          export AS_BUILD=0 NARAYANA_BUILD=0 NARAYANA_TESTS=0 BLACKTIE=1 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0 JTA_AS_TESTS=0
          export TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "PERFORMANCE" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!PERF* ]]; then
          comment_on_pull "Started testing this pull request with PERF profile: $BUILD_URL"
          export AS_BUILD=0 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0 JTA_AS_TESTS=0 OSGI_TESTS=0 PERF_TESTS=1
          export TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "LRA" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!LRA* ]]; then
          comment_on_pull "Started testing this pull request with LRA profile: $BUILD_URL"
          export AS_BUILD=1 NARAYANA_BUILD=1 NARAYANA_TESTS=0 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=0 SUN_ORB=0 JAC_ORB=0 JTA_AS_TESTS=0
          export TOMCAT_TESTS=0 LRA_TESTS=1
        else
          export COMMENT_ON_PULL=""
        fi
    elif [[ $PROFILE == "DB_TESTS" ]]; then
        if [[ ! $PULL_DESCRIPTION_BODY == *!DB_TESTS* ]]; then
          comment_on_pull "Started testing this pull request with DB_TESTS profile: $BUILD_URL"
          export AS_BUILD=0 NARAYANA_BUILD=1 NARAYANA_TESTS=1 BLACKTIE=0 XTS_AS_TESTS=0 XTS_TESTS=0 TXF_TESTS=0 txbridge=0
          export RTS_AS_TESTS=0 RTS_TESTS=0 JTA_CDI_TESTS=0 QA_TESTS=1 SUN_ORB=0 JAC_ORB=0 JTA_AS_TESTS=0
          export TOMCAT_TESTS=0 LRA_TESTS=0
        else
          export COMMENT_ON_PULL=""
        fi
    else
        export COMMENT_ON_PULL=""
        comment_on_pull "Started testing this pull request with $PROFILE profile: $BUILD_URL"
    fi
    [ $NARAYANA_TESTS ] || NARAYANA_TESTS=0	# run the narayana surefire tests
    [ $NARAYANA_BUILD ] || NARAYANA_BUILD=0 # build narayana
    [ $AS_BUILD ] || AS_BUILD=0 # git clone and build a fresh copy of the AS
    [ $BLACKTIE ] || BLACKTIE=0 # Build BlackTie
    [ $OSGI_TESTS ] || OSGI_TESTS=0 # OSGI tests
    [ $TXF_TESTS ] || TXF_TESTS=0 # compensations tests
    [ $XTS_TESTS ] || XTS_TESTS=0 # XTS tests
    [ $XTS_AS_TESTS ] || XTS_AS_TESTS=0 # XTS tests
    [ $RTS_AS_TESTS ] || RTS_AS_TESTS=0 # RTS tests
    [ $RTS_TESTS ] || RTS_TESTS=0 # REST-AT Test
    [ $LRA_TESTS ] || LRA_TESTS=0 # LRA Test
    [ $TOMCAT_TESTS ] || TOMCAT_TESTS=0 # Narayana Tomcat tests
    [ $JTA_CDI_TESTS ] || JTA_CDI_TESTS=0 # JTA CDI Tests
    [ $JTA_AS_TESTS ] || JTA_AS_TESTS=0 # JTA AS tests
    [ $QA_TESTS ] || QA_TESTS=0 # QA test suite
    [ $SUN_ORB ] || SUN_ORB=0 # Run QA test suite against the Sun orb
    [ $OPENJDK_ORB ] || OPENJDK_ORB=0 # Run QA test suite against the openjdk orb
    [ $JAC_ORB ] || JAC_ORB=0 # Run QA test suite against JacORB
    [ $txbridge ] || txbridge=0 # bridge tests
    [ $PERF_TESTS ] || PERF_TESTS=0 # benchmarks
    [ $REDUCE_SPACE ] || REDUCE_SPACE=1 # Whether to reduce the space used

    get_pull_xargs "$PULL_DESCRIPTION_BODY" $PROFILE # see if the PR description overrides any of the defaults 

    JAVA_VERSION=$(java -version 2>&1 | grep "\(java\|openjdk\) version" | cut -d\  -f3 | tr -d '"' | tr -d '[:space:]' | awk -F . '{if ($1==1) print $2; else print $1}')
}

function initGithubVariables
{
     [ "$PULL_NUMBER" = "" ] &&\
         PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')

     if [ "$PULL_NUMBER" != "" ]
     then
         [ "x${PULL_DESCRIPTION}" = "x" ] &&\
             PULL_DESCRIPTION=$(curl -ujbosstm-bot:$BOT_PASSWORD -s https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/pulls/$PULL_NUMBER)
         [ "x${PULL_DESCRIPTION_BODY}" = "x" ] &&\
             PULL_DESCRIPTION_BODY=$(printf '%s' "$PULL_DESCRIPTION" | grep \"body\":)
     else
             PULL_DESCRIPTION=""
             PULL_DESCRIPTION_BODY=""
     fi
}

function comment_on_pull
{
    if [ "$COMMENT_ON_PULL" = "" ]; then echo $1; return; fi

    if [ "$PULL_NUMBER" != "" ]
    then
        JSON="{ \"body\": \"$1\" }"
        curl -d "$JSON" -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/issues/$PULL_NUMBER/comments
    else
        echo "Not a pull request, so not commenting"
    fi
}

function check_if_pull_closed
{
    if [ "$PULL_NUMBER" != "" ]
    then
      if [[ $PULL_DESCRIPTION =~ "\"state\": \"closed\"" ]]
      then
          echo "pull closed"
          exit 0
      else
          echo "pull open"
      fi
    fi
}

function check_if_pull_noci_label
{
    if [ "$PULL_NUMBER" != "" ]
    then
        if [[ $PULL_DESCRIPTION =~ "\"name\": \"NoCI\"" ]]
        then
            echo "pull request $PULL_NUMBER is defined with NoCI label, exiting this CI execution"
            exit 0
        else
            echo "NoCI label is not present at the pull request $PULL_NUMBER"
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

function build_narayana {
  echo "Checking if need SPI PR"
  if [ -n "$SPI_BRANCH" ]; then
    echo "Building SPI PR"
    if [ -d jboss-transaction-spi ]; then
      rm -rf jboss-transaction-spi
    fi
    git clone https://github.com/jbosstm/jboss-transaction-spi.git -o jbosstm
    [ $? = 0 ] || fatal "git clone https://github.com/jbosstm/jboss-transaction-spi.git failed"
    cd jboss-transaction-spi
    git fetch jbosstm +refs/pull/*/head:refs/remotes/jbosstm/pull/*/head
    [ $? = 0 ] || fatal "git fetch of pulls failed"
    git checkout $SPI_BRANCH
    [ $? = 0 ] || fatal "git fetch of pull branch failed"    
    cd ../
    ./build.sh -f jboss-transaction-spi/pom.xml -B clean install
    [ $? = 0 ] || fatal "Build of SPI failed"
  fi
  
  echo "Building Narayana"
  cd $WORKSPACE

  [ $NARAYANA_TESTS = 1 ] && NARAYANA_ARGS= || NARAYANA_ARGS="-DskipTests"

  if [ $IBM_ORB = 1 ]; then
    ORBARG="-Dibmorb-enabled -Djacorb-disabled -Didlj-disabled -Dopenjdk-disabled"
    ${JAVA_HOME}/bin/java -version 2>&1 | grep IBM
    [ $? = 0 ] || fatal "You must use the IBM jdk to build with ibmorb"
  fi
  echo "Using MAVEN_OPTS: $MAVEN_OPTS"
  
  ./build.sh -B -Prelease,community$OBJECT_STORE_PROFILE $ORBARG "$@" $NARAYANA_ARGS $IPV6_OPTS $CODE_COVERAGE_ARGS clean install

  [ $? = 0 ] || fatal "narayana build failed"

  return 0
}

function build_as {
  echo "Building AS"

  cd ${WORKSPACE}
  if [ -d jboss-as ]; then
    echo "Updating existing checkout of AS7"
    cd jboss-as

    git remote | grep upstream
    if [ $? -ne 0 ]; then
      git remote add upstream git://github.com/wildfly/wildfly.git
    fi
    #Abort any partially complete rebase
    git rebase --abort
    git checkout 5_BRANCH
    [ $? = 0 ] || fatal "git checkout 5_BRANCH failed"
    git fetch
    [ $? = 0 ] || fatal "git fetch git://github.com/jbosstm/jboss-as.git failed"
    git reset --hard jbosstm/5_BRANCH
    [ $? = 0 ] || fatal "git reset 5_BRANCH failed"
    git clean -f -d -x
    [ $? > 1 ] || fatal "git clean failed"
    git rebase --abort
    rm -rf .git/rebase-apply
  else
    echo "First time checkout of AS7"
    git clone git://github.com/jbosstm/jboss-as.git -o jbosstm
    [ $? = 0 ] || fatal "git clone git://github.com/jbosstm/jboss-as.git failed"

    cd jboss-as

    git remote add upstream git://github.com/wildfly/wildfly.git
  fi

  [ -z "$AS_BRANCH" ] || git fetch jbosstm +refs/pull/*/head:refs/remotes/jbosstm/pull/*/head
  [ $? = 0 ] || fatal "git fetch of pulls failed"
  [ -z "$AS_BRANCH" ] || git checkout $AS_BRANCH
  [ $? = 0 ] || fatal "git fetch of pull branch failed"
  [ -z "$AS_BRANCH" ] || echo "Using non-default AS_BRANCH: $AS_BRANCH"

  git fetch upstream
  echo "This is the JBoss-AS commit"
  echo $(git rev-parse upstream/master)
  echo "This is the AS_BRANCH $AS_BRANCH commit"
  echo $(git rev-parse HEAD)

  echo "Rebasing the wildfly upstream/master on top of the AS_BRANCH $AS_BRANCH"
  git pull --rebase --ff-only upstream master
  [ $? = 0 ] || fatal "git rebase failed"
  
  if [ $REDUCE_SPACE = 1 ]; then
    echo "Deleting git dir to reduce disk usage"
    rm -rf .git
  fi

  # building WildFly
  export MAVEN_OPTS="-XX:MaxPermSize=512m -XX:+UseConcMarkSweepGC $MAVEN_OPTS"
  JAVA_OPTS="-Xms1303m -Xmx1303m -XX:MaxPermSize=512m $JAVA_OPTS" ./build.sh clean install -B -DskipTests -Dts.smoke=false $IPV6_OPTS -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION}
  [ $? = 0 ] || fatal "AS build failed"

  # init files under JBOSS_HOME before AS TESTS is started
  init_jboss_home

  # running WildFly testsuite if configured to be run by axis AS_TESTS
  if [ $AS_TESTS = 1 ]; then
    JAVA_OPTS="-Xms1303m -Xmx1303m -XX:MaxPermSize=512m $JAVA_OPTS" ./integration-tests.sh clean verify -B $IPV6_OPTS -Dtimeout.factor=300 -Dsurefire.forked.process.timeout=12000 -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} -Djboss.dist="$JBOSS_HOME" -DallTests=true -fae
    [ $? = 0 ] || fatal "AS tests failed"
  fi

  #Enable remote debugger
  echo JAVA_OPTS='"$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"' >> ./build/target/wildfly-*/bin/standalone.conf

  cd $WORKSPACE
}

function init_jboss_home {
  WILDFLY_VERSION_FROM_JBOSS_AS=`awk '/wildfly-parent/ { while(!/<version>/) {getline;} print; }' ${WORKSPACE}/jboss-as/pom.xml | cut -d \< -f 2|cut -d \> -f 2`
  echo "AS version is ${WILDFLY_VERSION_FROM_JBOSS_AS}"
  JBOSS_HOME=${WORKSPACE}/jboss-as/build/target/wildfly-${WILDFLY_VERSION_FROM_JBOSS_AS}
  export JBOSS_HOME=`echo  $JBOSS_HOME`
  [ -d $JBOSS_HOME ] || fatal "missing AS - $JBOSS_HOME is not a directory"
  echo "JBOSS_HOME=$JBOSS_HOME"
  cp ${JBOSS_HOME}/docs/examples/configs/standalone-xts.xml ${JBOSS_HOME}/standalone/configuration
  cp ${JBOSS_HOME}/docs/examples/configs/standalone-rts.xml ${JBOSS_HOME}/standalone/configuration
  # configuring bigger connection timeout for jboss cli (WFLY-13385)
  CONF="${JBOSS_HOME}/bin/jboss-cli.xml"
  sed -e 's#^\(.*</jboss-cli>\)#<connection-timeout>30000</connection-timeout>\n\1#' "$CONF" > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
  grep 'connection-timeout' "${CONF}"
}

function osgi_tests {
  echo "#-1. OSGI Test"
  cd ${WORKSPACE}
  ./build.sh -f osgi/jta/pom.xml -B -Parq-karaf-managed clean integration-test "$@"
  [ $? = 0 ] || fatal "OSGI Test failed"
}

function xts_as_tests {
  echo "#-1. XTS AS Integration Test"
  cd ${WORKSPACE}/jboss-as
  ./build.sh -f testsuite/integration/xts/pom.xml -fae -B -Pxts.integration.tests.profile -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@" test
  [ $? = 0 ] || fatal "XTS AS Integration Test failed"
  cd ${WORKSPACE}
}

function rts_as_tests {
  echo "#-1. RTS AS Integration Test"
  cd ${WORKSPACE}/jboss-as
  ./build.sh -f testsuite/integration/rts/pom.xml -fae -B -Prts.integration.tests.profile -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@" test
  [ $? = 0 ] || fatal "RTS AS Integration Test failed"
  cd ${WORKSPACE}
}

function jta_as_tests {
  echo "#-1. JTA AS Integration Test"
  cp ArjunaJTA/jta/src/test/resources/standalone-cmr.xml ${JBOSS_HOME}/standalone/configuration/
  ./build.sh -f ArjunaJTA/jta/pom.xml -fae -B -Parq $CODE_COVERAGE_ARGS "$@" test
  [ $? = 0 ] || fatal "JTA AS Integration Test failed"
  cd ${WORKSPACE}
}


function rts_tests {
  echo "#0. REST-AT Integration Test"
  ./build.sh -f rts/at/integration/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" test
  [ $? = 0 ] || fatal "REST-AT Integration Test failed"

  echo "#0. REST-AT To JTA Bridge Test"
  ./build.sh -f rts/at/bridge/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" test
  [ $? = 0 ] || fatal "REST-AT To JTA Bridge Test failed"
}

function lra_tests {
  echo "#0. LRA Test"

  # we can't use 'mvn -f' option beacuse of Thorntail plugin issue THORN-2049
  cd ./rts/lra/

  PRESERVE_WORKING_DIR=true ../../build.sh -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS $ENABLE_LRA_TRACE_LOGS -Dlra.tck.timeout.factor=1.5 "$@"
  [ $? = 0 ] || fatal "LRA Test failed"
  cd - # back to original directory
}

function blacktie {
  echo "#0. BlackTie"
  if [ -z "${JBOSSAS_IP_ADDR+x}" ]; then
    echo JBOSSAS_IP_ADDR not set
    JBOSSAS_IP_ADDR=localhost
  fi
  # KILL ANY PREVIOUS BUILD REMNANTS
  ps -f
  for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
  pkill -9 memcheck # pkill arg is a pattern (cf killall -r)
  pkill -9 testsuite
  pkill -9 server
  pkill -9 client
  pkill -9 cs
  ps -f
  # FOR DEBUGGING SUBSEQUENT ISSUES
  free -m

  echo "Building Blacktie Subsystem"
  cd ${WORKSPACE}
  WILDFLY_MASTER_VERSION=`grep 'version.org.wildfly.wildfly-parent' blacktie/pom.xml | cut -d \< -f 2|cut -d \> -f 2`
  echo "SET WILDFLY_MASTER_VERSION=${WILDFLY_MASTER_VERSION}"
  [ ${WILDFLY_MASTER_VERSION} == ${WILDFLY_VERSION_FROM_JBOSS_AS} ] || echo "WARN: May need to upgrade version.org.wildfly.wildfly-parent in the narayana/blacktie pom.xml to ${WILDFLY_VERSION_FROM_JBOSS_AS}"

  ./build.sh -f blacktie/wildfly-blacktie/pom.xml -B clean install "$@"
  [ $? = 0 ] || fatal "Blacktie Subsystem build failed"
  rm -rf ${WORKSPACE}/blacktie/wildfly-${WILDFLY_MASTER_VERSION}
  if [ ! -e wildfly-${WILDFLY_MASTER_VERSION}.zip ];
  then
    wget http://download.jboss.org/wildfly/${WILDFLY_MASTER_VERSION}/wildfly-${WILDFLY_MASTER_VERSION}.zip
    [ $? = 0 ] || fatal "Could not download wildfly"
  fi
  unzip wildfly-${WILDFLY_MASTER_VERSION}.zip -d blacktie/
  [ $? = 0 ] || fatal "Could not unzip wildfly"
  unzip ${WORKSPACE}/blacktie/wildfly-blacktie/build/target/wildfly-blacktie-build-${NARAYANA_CURRENT_VERSION}-bin.zip -d $PWD/blacktie/wildfly-${WILDFLY_MASTER_VERSION}
  [ $? = 0 ] || fatal "Could not unzip blacktie into widfly"
  # INITIALIZE JBOSS
  ant -f blacktie/scripts/hudson/initializeJBoss.xml -DJBOSS_HOME=$WORKSPACE/blacktie/wildfly-${WILDFLY_MASTER_VERSION} initializeJBoss
  if [ "$?" != "0" ]; then
	  fatal "Failed to init JBoss: $BUILD_URL"
  fi
  chmod u+x $WORKSPACE/blacktie/wildfly-${WILDFLY_MASTER_VERSION}/bin/standalone.sh

  if [[ $# == 0 || $# > 0 && "$1" != "-DskipTests" ]]; then
    # START JBOSS
    if [ $JAVA_VERSION -ge "9" ]; then
      # replace the openjdk-orb with the 8.0.8.Final
      wget https://repository.jboss.org/nexus/content/repositories/releases/org/jboss/openjdk-orb/openjdk-orb/8.0.8.Final/openjdk-orb-8.0.8.Final.jar -O blacktie/wildfly-${WILDFLY_MASTER_VERSION}/modules/system/layers/base/javax/orb/api/main/openjdk-orb-8.0.8.Final.jar
      sed -i s/8.0.6.Final/8.0.8.Final/g blacktie/wildfly-${WILDFLY_MASTER_VERSION}/modules/system/layers/base/javax/orb/api/main/module.xml
      JBOSS_HOME=`pwd`/blacktie/wildfly-${WILDFLY_MASTER_VERSION} JAVA_OPTS="--add-opens=java.base/java.security=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED -Xms256m -Xmx256m $JAVA_OPTS" blacktie/wildfly-${WILDFLY_MASTER_VERSION}/bin/standalone.sh -c standalone-blacktie.xml -Djboss.bind.address=$JBOSSAS_IP_ADDR -Djboss.bind.address.unsecure=$JBOSSAS_IP_ADDR -Djboss.bind.address.management=$JBOSSAS_IP_ADDR&
    else
      JBOSS_HOME=`pwd`/blacktie/wildfly-${WILDFLY_MASTER_VERSION} JAVA_OPTS="-Xms256m -Xmx256m -XX:MaxPermSize=256m $JAVA_OPTS" blacktie/wildfly-${WILDFLY_MASTER_VERSION}/bin/standalone.sh -c standalone-blacktie.xml -Djboss.bind.address=$JBOSSAS_IP_ADDR -Djboss.bind.address.unsecure=$JBOSSAS_IP_ADDR -Djboss.bind.address.management=$JBOSSAS_IP_ADDR&
    fi
    sleep 5
  fi

  # BUILD BLACKTIE
  ./build.sh -f blacktie/pom.xml -B clean install -Djbossas.ip.addr=$JBOSSAS_IP_ADDR "$@"
  if [ "$?" != "0" ]; then
  	ps -f
	  for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
  	pkill -9 testsuite
	  pkill -9 server
	  pkill -9 client
  	pkill -9 cs
    ps -f
  	fatal "Some tests failed: $BUILD_URL"
  fi

  # KILL ANY BUILD REMNANTS
  ps -f
  for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
  pkill -9 testsuite
  pkill -9 server
  pkill -9 client
  pkill -9 cs
  ps -f
  [ $? = 0 ] || fatal "BlackTie build failed: $BUILD_URL"
}

function jta_cdi_tests {
  echo "#0. JTA CDI Tests"
  ./build.sh -f ArjunaJTA/cdi/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" test
  [ $? = 0 ] || fatal "JTA CDI Test failed"
}

function compensations_tests {
  echo "#0. compensations Test"
  cp ./rts/at/webservice/target/restat-web-*.war $JBOSS_HOME/standalone/deployments
  ./build.sh -f txframework/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" test
  [ $? = 0 ] || fatal "txframework build failed"
  ./build.sh -f compensations/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" test
  [ $? = 0 ] || fatal "compensations build failed"
  ./build.sh -f compensations/pom.xml -fae -B -P$ARQ_PROF-distributed $CODE_COVERAGE_ARGS "$@" test
  [ $? = 0 ] || fatal "compensations build failed"
  ./build.sh -f compensations/pom.xml -fae -B -P$ARQ_PROF-weld $CODE_COVERAGE_ARGS "$@" test
  [ $? = 0 ] || fatal "compensations build failed"
}

function xts_tests {
  echo "#1 XTS: WSTX11 INTEROP, UNIT TESTS and CRASH RECOVERY TESTS"

  [ $XTS_TRACE ] && enable_xts_trace

  cd $WORKSPACE
  ran_crt=1

  CONF="${JBOSS_HOME}/standalone/configuration/standalone-xts.xml"
  grep async-registration "$CONF"
  sed -e 's#<[^<]*async-registration[^>]*>##g' $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
  sed -e 's#\(<subsystem.*xts.*\)#\1\n            <async-registration enabled="true"/>#' $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"

  if [ $WSTX_MODULES ]; then
    [[ $WSTX_MODULES = *crash-recovery-tests* ]] || ran_crt=0
    echo "BUILDING SPECIFIC WSTX11 modules"
    ./build.sh -f XTS/localjunit/pom.xml -B --projects "$WSTX_MODULES" -P$ARQ_PROF "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? = 0 ] || fatal "XTS/localjunit/pom.xml failed"
  else
    ./build.sh -f XTS/localjunit/unit/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? = 0 ] || fatal "XTS localjunit unit build failed"
    ./build.sh -f XTS/localjunit/disabled-context-propagation/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? = 0 ] || fatal "XTS localjunit disabled-context-propagation build failed"
    ./build.sh -f XTS/localjunit/WSTX11-interop/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? = 0 ] || fatal "XTS localjunit WSTX11 build failed"
    ./build.sh -f XTS/localjunit/WSTFSC07-interop/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? = 0 ] || fatal "XTS localjunit WSTFSC07 build failed"
    ./build.sh -f XTS/localjunit/xtstest/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? = 0 ] || fatal "XTS localjunit xtstest build failed (no test run)"
    ./build.sh -f XTS/localjunit/crash-recovery-tests/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
    [ $? = 0 ] || fatal "XTS localjunit crash-recovery-tests build failed"
  fi

  [ $? = 0 ] || fatal "XTS: SOME TESTS failed"
  if [ $ran_crt = 1 ]; then
    if [[ $# == 0 || $# > 0 && "$1" != "-DskipTests" ]]; then
      (cd XTS/localjunit/crash-recovery-tests && java -cp target/classes/ com.arjuna.qa.simplifylogs.SimplifyLogs ./target/log/ ./target/log-simplified)
      if [[ $? != 0 && $ISIBM != 0 && -z $CODE_COVERAGE_ARGS ]]; then
        fatal "Simplify CRASH RECOVERY logs failed"
      fi
    fi
  fi
}

function tx_bridge_tests {
  echo "XTS: TXBRIDGE TESTS update conf"
  [ $XTS_TRACE ] && enable_xts_trace
  cd $WORKSPACE
  CONF="${JBOSS_HOME}/standalone/configuration/standalone-xts.xml"
  grep recovery-listener "$CONF"
  sed -e s/recovery-listener=\"true\"//g   $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
  sed -e "s#\(recovery-environment\) \(socket-binding\)#\\1 recovery-listener=\"true\" \\2#"   $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
  # sed -e "s#\(recovery-environment\) \(socket-binding\)#\\1 recovery-listener=\"true\" \\2#" -i $CONF
  [ $? = 0 ] || fatal "#3.TXBRIDGE TESTS: sed failed"

  echo "XTS: TXBRIDGE TESTS"
  ./build.sh -f txbridge/pom.xml -fae -B -P$ARQ_PROF $CODE_COVERAGE_ARGS "$@" $IPV6_OPTS install "$@"
  [ $? = 0 ] || fatal "#3.TXBRIDGE TESTS failed"
}

function tomcat_tests {
    echo "Initializing Narayana Tomcat tests"
    cd ${WORKSPACE}
    TOMCAT_VERSION=9.0.11
    wget -nc https://archive.apache.org/dist/tomcat/tomcat-9/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.zip
    rm -rf apache-tomcat-$TOMCAT_VERSION
    unzip apache-tomcat-$TOMCAT_VERSION.zip
    case "$(uname -s)" in
    CYGWIN*)    export CATALINA_HOME=`cygpath -w $(pwd)/apache-tomcat-$TOMCAT_VERSION/`;;
    *)          export CATALINA_HOME=$(pwd)/apache-tomcat-$TOMCAT_VERSION/
    esac
    chmod +x ${CATALINA_HOME}/bin/catalina.sh
    sed -i 's/<\/tomcat-users>/<user username="arquillian" password="arquillian" roles="manager-script"\/>\n<\/tomcat-users>/' ${CATALINA_HOME}/conf/tomcat-users.xml
    cat <<EOT >> ${CATALINA_HOME}/conf/logging.properties
org.apache.tomcat.tomcat-jdbc.level = ALL
org.h2.level = ALL
org.postgresql.level = ALL
javax.sql.level = ALL
org.apache.tomcat.tomcat-dbcp.level = ALL
com.arjuna.level = ALL
EOT
    rm -rf narayana-tomcat
    git clone https://github.com/jbosstm/narayana-tomcat.git
    echo "Executing Narayana Tomcat tests"
    ./build.sh -f narayana-tomcat/pom.xml -fae -B -P${ARQ_PROF}-tomcat ${CODE_COVERAGE_ARGS} -Dtest.db.type=h2 -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@" ${IPV6_OPTS} clean install "$@"
    RESULT=$?
    [ $RESULT = 0 ] || fatal "Narayana Tomcat tests failed H2"
    ./build.sh -f narayana-tomcat/pom.xml -fae -B -P${ARQ_PROF}-tomcat ${CODE_COVERAGE_ARGS} -Dtest.db.type=pgsql -Dversion.org.jboss.narayana=${NARAYANA_CURRENT_VERSION} "$@" ${IPV6_OPTS} clean install "$@"
    RESULT=$?
    [ $RESULT = 0 ] || fatal "Narayana Tomcat tests failed Postgres"
    rm -r ${CATALINA_HOME}
}

function set_qa_log_level {
echo "creating file $WORKSPACE/qa/dist/narayana-full-${NARAYANA_CURRENT_VERSION}/etc/log4j.xml"
cat << EOF > $WORKSPACE/qa/dist/narayana-full-${NARAYANA_CURRENT_VERSION}/etc/log4j.xml
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

function enable_xts_trace {
    echo "Enable XTS trace logs"
    CONF="${JBOSS_HOME}/standalone/configuration/standalone-xts.xml"

    sed -e '/<logger category="com.arjuna">$/N;s/<logger category="com.arjuna">\n *<level name="WARN"\/>/<logger category="com.arjuna"><level name="TRACE"\/><\/logger><logger category="org.jboss.jbossts.txbridge"><level name="TRACE"\/>/' $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
    sed -e '/<console-handler name="CONSOLE">$/N;s/<console-handler name="CONSOLE">\n *<level name="INFO"\/>/<console-handler name="CONSOLE"><level name="TRACE"\/>/' $CONF > "$CONF.tmp" && mv "$CONF.tmp" "$CONF"
}

function add_qa_xargs {
  NXT=$(grep "NEXT_COMMAND_LINE_ARG=" TaskImpl.properties)
  [ $? = 0 ] || return 1

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
  [ $? = 0 ] || fatal "Copy dependency failed"
  
  cd $WORKSPACE/qa
  unset orb
  codeCoverage=false;

  # look for an argument of the form orb=<something>
  for i in $@; do
    [ ${i%%=*} = "orb" ] && orb=${i##*=}
    [ $CODE_COVERAGE = 1 ] && codeCoverage=true
  done

  cp TaskImpl.properties.template TaskImpl.properties

  # check to see which orb we are running against:
  if [ x$orb = x"openjdk" ]; then
    orbtype=openjdk
  elif [ x$orb = x"idlj" ]; then
    orbtype=idlj
  elif [ x$orb = x"ibmorb" ]; then
    orbtype=ibmorb
	sed -e "s#^  dist#  ${JAVA_HOME}\${file.separator}jre\${file.separator}lib\${file.separator}ibmorb.jar\\\\\\n  \${path.separator}${JAVA_HOME}\${file.separator}jre\${file.separator}lib\${file.separator}ibmorb.jar\\\\\\n  \${path.separator}dist#" TaskImpl.properties > "TaskImpl.properties.tmp" && mv "TaskImpl.properties.tmp" "TaskImpl.properties"
  else
    orbtype=jacorb
  fi

  testoutputzip="testoutput-${orbtype}.zip"

  sed -e "s#^COMMAND_LINE_0=.*#COMMAND_LINE_0=${JAVA_HOME}/bin/java#" TaskImpl.properties > "TaskImpl.properties.tmp" && mv "TaskImpl.properties.tmp" "TaskImpl.properties"
  [ $? = 0 ] || fatal "sed TaskImpl.properties failed"

  # store the origin orbtype
  origin_orbtype=orbtype

  if [ $orbtype = "openjdk" ]; then
    openjdkjar="dist/narayana-full-${NARAYANA_CURRENT_VERSION}/lib/ext/openjdk-orb.jar"
    if [ $JAVA_VERSION -lt "9" ]; then
        EXTRA_QA_SYSTEM_PROPERTIES="-Xbootclasspath/p:$openjdkjar $EXTRA_QA_SYSTEM_PROPERTIES"
    fi

    if [ $JAVA_VERSION -lt "11" ]; then
        orbtype="idlj"
    fi
  fi

  if [[ x"$EXTRA_QA_SYSTEM_PROPERTIES" != "x" ]]; then
    add_qa_xargs "$EXTRA_QA_SYSTEM_PROPERTIES"
  fi

  # delete lines containing jacorb
  [ $orbtype != "jacorb" ] && sed -e  '/^.*separator}jacorb/ d' TaskImpl.properties > "TaskImpl.properties.tmp" && mv "TaskImpl.properties.tmp" "TaskImpl.properties"

  # if the env variable MFACTOR is set then set the bean property CoreEnvironmentBean.timeoutFactor
  if [[ -n "$MFACTOR" ]] ; then
    sed -e "s/COMMAND_LINE_12=-DCoreEnvironmentBean.timeoutFactor=[0-9]*/COMMAND_LINE_12=-DCoreEnvironmentBean.timeoutFactor=${MFACTOR}/" TaskImpl.properties > "TaskImpl.properties.tmp" && mv "TaskImpl.properties.tmp" "TaskImpl.properties"
    # Note that setting the timeout too high (eg 2*240) will cause the defaulttimeout test cases to take
    # longer than the Task kill timeout period
    let txtimeout=$MFACTOR*120
    sed -e "s/COMMAND_LINE_13=-DCoordinatorEnvironmentBean.defaultTimeout=[0-9]*/COMMAND_LINE_13=-DCoordinatorEnvironmentBean.defaultTimeout=${txtimeout}/" TaskImpl.properties > "TaskImpl.properties.tmp" && mv "TaskImpl.properties.tmp" "TaskImpl.properties"
  fi
  # if IPV6_OPTS is not set get the jdbc drivers (we do not run the jdbc tests in IPv6 mode)
  ant get.drivers
  [ $? = 0 ] || fatal "get drivers failed"
  
  [ -z "${IPV6_OPTS+x}" ] && ant -Dorbtype=$orbtype "$QA_BUILD_ARGS" dist ||
    ant -Dorbtype=$orbtype "$QA_BUILD_ARGS" dist

  [ $? = 0 ] || fatal "qa build failed"

  # restore the orbtype if the jdk >= 9
  if [ $JAVA_VERSION -ge "9" ]; then
    orbtype="${origin_orbtype}"
  fi

  if [ $orbtype = "jacorb" ]; then
    sed -e "s#^jacorb.log.default.verbosity=.*#jacorb.log.default.verbosity=2#"   dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties > "dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties.tmp" && mv "dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties.tmp" "dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties"
    sed -e "s#^jacorb.poa.thread_pool_max=.*#jacorb.poa.thread_pool_max=100#"   dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties > "dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties.tmp" && mv "dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties.tmp" "dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties"
    sed -e "s#^jacorb.poa.thread_pool_min=.*#jacorb.poa.thread_pool_min=40#"   dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties > "dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties.tmp" && mv "dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties.tmp" "dist/narayana-full-${NARAYANA_CURRENT_VERSION}/jacorb/etc/jacorb.properties"
  fi

  if [[ $# == 0 || $# > 0 && "$1" != "-DskipTests" ]]; then
    # determine which QA test target to call
    target="ci-tests" # the default is to run everything (ci-tests)

    # if IPV6_OPTS is set then do not do the jdbc tests (ie run target junit-testsuite)
    [ -z "${IPV6_OPTS+x}" ] || target="junit"

    # if called with the sun or ibm orbs then only run the jtsremote tests
    [ $orbtype != "jacorb" ] && target="ci-jts-tests"

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
      ant -f run-tests.xml $target $QA_PROFILE -Dcode.coverage=$codeCoverage -Dorbtype=$orbtype
      ok=$?
    fi

    if [ -f TEST-failures.txt ]; then
      echo "Test Failures:"
      cat TEST-failures.txt 
    fi

    if [ $codeCoverage = true ]; then
      echo "generating test coverage report"
      ant -f run-tests.xml jacoco-report
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
  ok3=0;
  ok4=0;

  # if the jdk >= 11, we do not run with the sun orb since it has been removed
  if [ $JAVA_VERSION -ge "11" ]; then
	  SUN_ORB=0;
  fi

  if [ $IBM_ORB = 1 ]; then
    qa_tests_once "orb=ibmorb" "$@" # run qa against the IBM orb
    ok3=$?
  else
    if [ $SUN_ORB = 1 ]; then
      qa_tests_once "orb=idlj" "$@" # run qa against the Sun orb
      ok2=$?
    fi
    if [ $JAC_ORB = 1 ]; then
      qa_tests_once "orb=jacorb" "$@"    # run qa against the default orb
      ok1=$?
    fi
    if [ $OPENJDK_ORB = 1 ]; then
      qa_tests_once "orb=openjdk" "$@"    # run qa against the openjdk orb
      ok4=$?
    fi
  fi

  [ $ok1 = 0 ] || echo some jacorb QA tests failed
  [ $ok2 = 0 ] || echo some Sun ORB QA tests failed
  [ $ok3 = 0 ] || echo some IBM ORB QA tests failed
  [ $ok4 = 0 ] || echo some openjdk ORB QA tests failed

  [ $ok1 = 0 -a $ok2 = 0 -a $ok3 = 0 -a $ok4 = 0 ] || fatal "some qa tests failed"
}

function hw_spec {
  if [ -x /usr/sbin/system_profiler ]; then
    echo "sw_vers:"; sw_vers
    echo "system_profiler:"; /usr/sbin/system_profiler
  else
    set -o xtrace

    echo "uname -a"; uname -a
    echo "redhat release:"; cat /etc/redhat-release
    echo "java version:"; java -version
    echo "free:"; free -m
    echo "cpuinfo:"; cat /proc/cpuinfo
    echo "meminfo:"; cat /proc/meminfo
    echo "devices:"; cat /proc/devices
    echo "scsi:"; cat /proc/scsi/scsi
    echo "partitions:"; cat /proc/partitions

    echo "lspci:"; lspci
    echo "lsusb:"; lsusb
    echo "lsblk:"; lsblk
    echo "df:"; df
    echo "mount:"; mount | column -t | grep ext
  fi
}

function perf_tests {
  cd $WORKSPACE
  [[ -d tmp ]] || mkdir tmp
  cd tmp
  rm -rf performance
  git clone https://github.com/jbosstm/performance
  cd performance/
  if [ -n "$PULL_NUMBER" ]; 
  then
    echo $PULL_DESCRIPTION | grep https://github.com/jbosstm/performance/pull/
    if [[ "$?" -eq 0 ]]; then
      PERF_PR_NUMBER=$(echo $PULL_DESCRIPTION | sed "s#.*https://github.com/jbosstm/performance/pull/\([0-9]*\).*#\1#g")
      git fetch origin +refs/pull/*/head:refs/remotes/origin/pull/*/head
      [ $? = 0 ] || fatal "git fetch of pulls failed"
      git checkout remotes/origin/pull/$PERF_PR_NUMBER/head
      [ $? = 0 ] || fatal "git fetch of pull branch failed"
      git pull --rebase --ff-only origin master
      [ $? = 0 ] || fatal "git rebase failed"
    fi
  fi
  
  ./scripts/run_bm.sh
  res=$?
  cd $WORKSPACE

  hw_spec | tee hwinfo.txt

  PERF_OUTPUT=$(cat $WORKSPACE/benchmark-output.txt | sed ':a;N;$!ba;s/\n/\\n/g')

  PERF_OUTPUT="$PERF_OUTPUT\n\nFor information on the hardware config used for this PR please consult the CI job artefact hwinfo.txt or the job output"

  grep -q improvement $WORKSPACE/benchmark-output.txt
  if [ $? = 1 ]; then
    PERF_OUTPUT="$PERF_OUTPUT\n\n*If the purpose of this PR is to improve performance then there has been insufficient improvement to warrant a pass. See the previous text for the threshold (range) for passing optimization related PRs*"
  fi

  PERF_OUTPUT="Benchmark output (please refer to the article https://developer.jboss.org/wiki/PerformanceGatesForAcceptingPerformanceFixesInNarayana for information on our testing procedures.\n\nIf you just want to run a single benchmark then please refer to the README.md file in our benchmark repository at https://github.com/jbosstm/performance/tree/master/narayana\n\n$PERF_OUTPUT"

  comment_on_pull "$PERF_OUTPUT" "$BUILD_URL"

  [ $res = 0 ] || fatal "there were regressions in one or more of the benchmarks (see previous PR comment for details"
}

function generate_code_coverage_report {
  echo "Generating code coverage report"
  cd ${WORKSPACE}
  ./build.sh -B -f code-coverage/pom.xml $CODE_COVERAGE_ARGS "$@" clean install
  [ $? = 0 ] || fatal "Code coverage report generation failed"
}

ulimit -a
ulimit -u unlimited
ulimit -c unlimited
ulimit -a

initGithubVariables
check_if_pull_closed
check_if_pull_noci_label

init_test_options

# if QA_BUILD_ARGS is unset then get the db drivers form the file system otherwise get them from the
# default location (see build.xml). Note ${var+x} substitutes null for the parameter if var is undefined
[ -z "${QA_BUILD_ARGS+x}" ] && QA_BUILD_ARGS="-Ddriver.url=file:///home/jenkins/dbdrivers"

# Note: set QA_TARGET if you want to override the QA test ant target

# for IPv6 testing use export ARQ_PROF=arqIPv6
# if you don't want to run all the XTS tests set WSTX_MODULES to the ones you want, eg:
# export WSTX_MODULES="WSAS,WSCF,WSTX,WS-C,WS-T,xtstest,crash-recovery-tests"

[ -z "${WORKSPACE}" ] && fatal "UNSET WORKSPACE"

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
[ $AS_BUILD = 1 ] && build_as "$@"
[ $BLACKTIE = 1 ] && blacktie "$@"
[ $OSGI_TESTS = 1 ] && osgi_tests "$@"
[ $JTA_CDI_TESTS = 1 ] && jta_cdi_tests "$@"
[ $XTS_AS_TESTS = 1 ] && xts_as_tests
[ $RTS_AS_TESTS = 1 ] && rts_as_tests
[ $JTA_AS_TESTS = 1 ] && jta_as_tests
[ $TXF_TESTS = 1 ] && compensations_tests "$@"
[ $XTS_TESTS = 1 ] && xts_tests "$@"
[ $txbridge = 1 ] && tx_bridge_tests "$@"
[ $RTS_TESTS = 1 ] && rts_tests "$@"
[ $LRA_TESTS = 1 ] && lra_tests "$@"
[ $TOMCAT_TESTS = 1 ] && tomcat_tests "$@"
[ $QA_TESTS = 1 ] && qa_tests "$@"
[ $PERF_TESTS = 1 ] && perf_tests "$@"
[ $CODE_COVERAGE = 1 ] && generate_code_coverage_report "$@"

if [[ -z $PROFILE ]]; then
    comment_on_pull "All tests passed - Job complete $BUILD_URL"
elif [[ $PROFILE == "BLACKTIE" ]]; then
    comment_on_pull "$PROFILE profile tests passed on Linux - Job complete $BUILD_URL"
elif [[ $PROFILE == "PERF" ]]; then
    comment_on_pull "$PROFILE profile job finished $BUILD_URL"
else
    comment_on_pull "$PROFILE profile tests passed - Job complete $BUILD_URL"
fi

exit 0 # any failure would have resulted in fatal being called which exits with a value of 1
