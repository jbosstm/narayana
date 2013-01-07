#!/bin/bash

function fail
{
    echo "Failed: " $1
    exit 1
}
START=$(date +%s)

JBOSS_BUILD_HOME=$1

if [ "" == "$JBOSS_BUILD_HOME" ]; then
    echo "usage: $0 <JBOSS SRC HOME> [--clean]"
    exit 1
fi

if [ "$2" == "--clean" ] || [ "$3" == "--clean" ]; then
    CLEAN=clean
fi

if [ "$2" == "--xts" ] || [ "$3" == "--xts" ]; then
    cd ..
    ./build.sh -f ./XTS/pom.xml $CLEAN install -DskipTests || fail
    cd -
fi

mvn $CLEAN install || fail
kill-jboss
$JBOSS_BUILD_HOME/build.sh $CLEAN install -f $JBOSS_BUILD_HOME/xts/pom.xml -DskipTests || fail
$JBOSS_BUILD_HOME/build.sh $CLEAN install -f $JBOSS_BUILD_HOME/build/pom.xml -DskipTests || fail

JBOSS_BUILD_NAME=$(ls $JBOSS_BUILD_HOME/build/target/ | grep jboss-as)
cp $JBOSS_BUILD_HOME/build/target/$JBOSS_BUILD_NAME/docs/examples/configs/standalone-xts.xml $JBOSS_BUILD_HOME/build/target/$JBOSS_BUILD_NAME/standalone/configuration/ || fail
echo JAVA_OPTS=\"\$JAVA_OPTS -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n\" >> $JBOSS_BUILD_HOME/build/target/$JBOSS_BUILD_NAME/bin/standalone.conf
echo JAVA_OPTS=\"\$JAVA_OPTS -Dorg.jboss.byteman.verbose -Djboss.modules.system.pkgs=org.jboss.byteman -Dorg.jboss.byteman.transform.all -javaagent:/opt/byteman-download-2.1.2/lib/byteman.jar=listener:true\" >> $JBOSS_BUILD_HOME/build/target/$JBOSS_BUILD_NAME/bin/standalone.conf
cp ../rest-tx/webservice/target/restat-web-*.war $JBOSS_BUILD_HOME/build/target/$JBOSS_BUILD_NAME/standalone/deployments/

END=$(date +%s)
DIFF=$(( $END - $START ))
echo -e  "\n\nRE-BUILD SUCCESSFUL: took $DIFF seconds"
