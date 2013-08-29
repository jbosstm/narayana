function fatal {
  comment_on_pull "Tests failed: $1"
  echo "$1"
  exit -1
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

ulimit -c unlimited

# CHECK IF WORKSPACE IS SET
if [ -n "${WORKSPACE+x}" ]; then
  echo WORKSPACE is set
else
  echo WORKSPACE not set
  exit
fi

if [ -n "${JBOSS_HOME+x}" ]; then
  echo JBOSS_HOME is set
else
  echo JBOSS_HOME not set
  exit
fi


if [ -z "${JBOSSAS_IP_ADDR+x}" ]; then
  echo JBOSSAS_IP_ADDR not set
  JBOSSAS_IP_ADDR=localhost
fi

# KILL ANY PREVIOUS BUILD REMNANTS
ps -f
for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
killall -9 testsuite
killall -9 server
killall -9 client
killall -9 cs
ps -f

# FOR DEBUGGING SUBSEQUENT ISSUES
free -m

# GET THE TNS NAMES
TNS_ADMIN=$WORKSPACE/instantclient_11_2/network/admin
mkdir -p $TNS_ADMIN
if [ -e $TNS_ADMIN/tnsnames.ora ]; then
	echo "tnsnames.ora already downloaded"
else
	(cd $TNS_ADMIN; wget http://albany/userContent/blacktie/tnsnames.ora)
fi

# INITIALIZE JBOSS
ant -f scripts/hudson/initializeJBoss.xml -DJBOSS_HOME=$JBOSS_HOME -Dbasedir=. initializeJBoss
if [ "$?" != "0" ]; then
	fatal "Failed to init JBoss: $BUILD_URL"
fi

#wget -P $JBOSS_HOME/standalone/deployments/ -N http://172.17.131.2/job/narayana-populateM2-taconic/lastSuccessfulBuild/artifact/rts/at/webservice/target/restat-web-5.0.0.M2-SNAPSHOT.war
#if [ "$?" != "0" ]; then
#	fatal "Failed to get restat-web war"
#fi

export JBOSS_HOME=$WORKSPACE/jboss-as/

chmod u+x $JBOSS_HOME/bin/standalone.sh

# START JBOSS
export JAVA_OPTS="$JAVA_OPTS -Xmx256m -XX:MaxPermSize=256m"
$JBOSS_HOME/bin/standalone.sh -c standalone-full.xml -Djboss.bind.address=$JBOSSAS_IP_ADDR -Djboss.bind.address.unsecure=$JBOSSAS_IP_ADDR&
sleep 5

# BUILD BLACKTIE
cd $WORKSPACE
./build.sh clean install -Djbossas.ip.addr=$JBOSSAS_IP_ADDR "$@"
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
