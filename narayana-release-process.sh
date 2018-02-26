#!/bin/bash

# You can run this script with 0, 1 or 3 arguments.
# Arguments are then tranformed to env varibales CURRENT, NEXT and WFLYISSUE
# Release process releases version ${CURRENT} and prepares github with commits to ${NEXT}-SNAPSHOT
# If WFLYISSUE is not defined then script tries to create new jira with upgrade component.
#
# 0 argument: information about CURRENT, NEXT is taken from pom.xml, WFLYISSUE is ignored
#             CURRENT is taken as what is version from pom.xml without '-SNAPSHOT' suffix,
#             NEXT is CURRENT with increased micro version
#             e.g. pom.xml talks about version 5.7.3.Final-SNAPSHOT, CURRENT is 5.7.3.Final and NEXT is 5.7.4.Final
# 1 argument: same as 0 argument but taken in care WFLYISSUE thus script don't try to create new issue
# 3 arguments: `./narayana-release-process.sh CURRENT NEXT WFLYISSUE`

if [[ $(uname) == CYGWIN* ]]
then
  read -p "ARE YOU RUNNING AN ELEVATED CMD PROMPT mvn.cmd needs this" ELEV
  if [[ $ELEV == n* ]]
  then
    exit
  fi
fi
read -p "You will need: VPN, credentials for jbosstm@filemgmt, jira admin, github permissions on all jbosstm/ repo and nexus permissions. Do you have these?" ENVOK
if [[ $ENVOK == n* ]]
then
  exit
fi
read -p "Have you configured ~/.m2/settings.xml with repository 'jboss-releases-repository' and correct username/password?" M2OK
if [[ $M2OK == n* ]]
then
  exit
fi

command -v mvn >/dev/null 2>&1 || { echo >&2 "I require mvn but it's not installed.  Aborting."; exit 1; }
command -v ant >/dev/null 2>&1 || { echo >&2 "I require ant but it's not installed.  Aborting."; exit 1; }
command -v awestruct >/dev/null 2>&1 || { echo >&2 "I require awestruct (http://awestruct.org/getting_started) but it's not installed.  Aborting."; exit 1; }

if [ $# -eq 0 ]; then
  . scripts/pre-release-vars.sh
  CURRENT=`echo $CURRENT_SNAPSHOT_VERSION | sed "s/-SNAPSHOT//"`
  NEXT=`echo $CURRENT_SNAPSHOT_VERSION | sed "s/.Final//"`
  NEXT="${NEXT%.*}.$((${NEXT##*.}+1))".Final
elif [ $# -eq 1 ]; then
  . scripts/pre-release-vars.sh
  CURRENT=`echo $CURRENT_SNAPSHOT_VERSION | sed "s/-SNAPSHOT//"`
  NEXT=`echo $CURRENT_SNAPSHOT_VERSION | sed "s/.Final//"`
  NEXT="${NEXT%.*}.$((${NEXT##*.}+1))".Final
  WFLYISSUE=$1
elif [ $# -lt 2 ]; then
  echo 1>&2 "$0: not enough arguments: CURRENT NEXT <WFLYISSUE>(versions should end in .Final or similar)"
  exit 2
elif [ $# -gt 3 ]; then
  echo 1>&2 "$0: too many arguments: CURRENT NEXT <WFLYISSUE>(versions should end in .Final or similar)"
  exit 2
else
  CURRENT=$1
  NEXT=$2
  WFLYISSUE=$3
fi

set +e
git fetch upstream --tags
git tag | grep $CURRENT
if [[ $? != 0 ]]
then
  set -e
  JENKINS_JOBS=narayana,narayana-catelyn,narayana-codeCoverage,narayana-documentation,narayana-hqstore,narayana-jdbcobjectstore,narayana-quickstarts,narayana-quickstarts-catelyn ./scripts/release/pre_release.py  
  set +e
  git status | grep "nothing to commit"
  if [[ $? != 0 ]]
  then
    git status
    exit
  fi
  git status | grep "ahead"
  if [[ $? != 1 ]]
  then
    git status
    exit
  fi
  git log -n 5
  read -p "Did the log before look OK?" ok
  if [[ $ok == n* ]]
  then
    exit
  fi
  set -e 
  (cd ./scripts/ ; ./pre-release.sh $CURRENT $NEXT)
  git fetch upstream --tags
  ./scripts/release/update_jira.py -k JBTM -t 5.next -n $CURRENT
else
  set -e
fi

if [ -z "$WFLYISSUE" ]
then
  ./scripts/release/update_upstream.py -s WFLY -n $CURRENT
  read -p "Enter WFLY issue: " WFLYISSUE
  if [ ! -d "jboss-as" ]
  then
    (git clone git@github.com:jbosstm/jboss-as.git -o jbosstm; cd jboss-as; git remote add upstream git@github.com:wildfly/wildfly.git)
  fi
  (cd jboss-as; git fetch upstream; git checkout -b ${WFLYISSUE}; git reset --hard upstream/master)
  cd jboss-as
  CURRENT_VERSION_IN_WFLY=`grep 'narayana>' pom.xml | cut -d \< -f 2|cut -d \> -f 2`
  if [[ $(uname) == CYGWIN* ]]
  then
    sed -i "s/narayana>$CURRENT_VERSION_IN_WFLY/narayana>$CURRENT/g" pom.xml
  else
    sed -i "" "s/narayana>$CURRENT_VERSION_IN_WFLY/narayana>$CURRENT/g" pom.xml
  fi
  git add pom.xml
  git commit -m "${WFLYISSUE} Upgrade Narayana to $CURRENT"
  git push --set-upstream jbosstm ${WFLYISSUE}
  git checkout 5_BRANCH
  git reset --hard jbosstm/5_BRANCH
  cd -
fi

cd ~/tmp/narayana/$CURRENT/sources/documentation/
git checkout $CURRENT
mvn clean install -Prelease
cd -
cd ~/tmp/narayana/$CURRENT/sources/narayana/
git checkout $CURRENT
MAVEN_OPTS="-XX:MaxPermSize=512m" 
mvn clean install -DskipTests -gs tools/maven/conf/settings.xml -Dorson.jar.location=./ext/
mvn clean deploy -DskipTests -gs tools/maven/conf/settings.xml -Dorson.jar.location=./ext/ -Prelease
mvn clean deploy -DskipTests -gs tools/maven/conf/settings.xml -Prelease -f blacktie/utils/cpp-plugin/pom.xml
mvn clean deploy -DskipTests -gs tools/maven/conf/settings.xml -Prelease  -f blacktie/pom.xml -pl :blacktie-jatmibroker-nbf -am
git archive -o ../../narayana-full-$CURRENT-src.zip $CURRENT
ant -f build-release-pkgs.xml -Dawestruct.executable="awestruct" all
cd -

echo "build and retrieve the centos54x64 and vc9x32 binaries from http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/"
wget --post-data='json={"parameter": {"name": "TAG_NAME", "value": "'${CURRENT}'"}, "parameter": {"name": "WFLY_PR_BRANCH", "value": "'master'"}}' http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana/build?delay=0sec
wget --post-data='json={"parameter": {"name": "TAG_NAME", "value": "'${CURRENT}'"}, "parameter": {"name": "WFLY_PR_BRANCH", "value": "'master'"}}' http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana-catelyn/build?delay=0sec
echo "Press enter when the artifacts are available"
read

wget http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-centos54x64-bin.tar.gz
wget http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana-catelyn/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-vc9x32-bin.zip
scp blacktie-${CURRENT}-centos54x64-bin.tar.gz jbosstm@filemgmt.jboss.org:/downloads_htdocs/jbosstm/${CURRENT}/binary/
scp blacktie-${CURRENT}-vc9x32-bin.zip jbosstm@filemgmt.jboss.org:/downloads_htdocs/jbosstm/${CURRENT}/binary/
