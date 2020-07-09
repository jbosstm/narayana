#!/bin/bash

# You can run this script with 0, 1 or 3 arguments.
# Arguments are then tranformed to env varibales CURRENT, NEXT
# Release process releases version ${CURRENT} and prepares github with commits to ${NEXT}-SNAPSHOT
#
# 0 argument: information about CURRENT, NEXT is taken from pom.xml
#             CURRENT is taken as what is version from pom.xml without '-SNAPSHOT' suffix,
#             NEXT is CURRENT with increased micro version
#             e.g. pom.xml talks about version 5.7.3.Final-SNAPSHOT, CURRENT is 5.7.3.Final and NEXT is 5.7.4.Final
# 2 arguments: `./narayana-release-process.sh CURRENT NEXT`

command -v ant >/dev/null 2>&1 || { echo >&2 "I require ant but it's not installed.  Aborting."; exit 1; }
if [ $# -ne 2 ]; then
  echo 1>&2 "$0: usage: CURRENT NEXT"
  exit 2
else
  CURRENT=$1
  NEXT=$2
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
read -p "Until ./scripts/release/update_jira.py -k JBTM -t 5.next -n $CURRENT is fixed you will need to go to https://issues.jboss.org/projects/JBTM?selectedItem=com.atlassian.jira.jira-projects-plugin%3Arelease-page&status=released-unreleased, rename (Actions -> Edit) 5.next to $CURRENT, create a new 5.next version, Actions -> Release on the new $CURRENT. Have you done this? y/n " JIRAOK
if [[ $JIRAOK == n* ]]
then
  exit
fi

git fetch upstream --tags
set +e
git tag | grep $CURRENT
if [[ $? != 0 ]]
then
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
  echo "Checking if there were any failed jobs, this may be interactive so please stand by"
  JIRA_HOST=issues.redhat.com JENKINS_JOBS=narayana59 ./scripts/release/pre_release.py
  echo "Executing pre-release script, this may be interactive so please stand by"
  (cd ./scripts/ ; ./pre-release.sh $CURRENT $NEXT)
  echo "This script is only interactive at the very end now, press enter to continue"
  read
  # Start the blacktie builds now
  json='{"parameter": {"name": "TAG_NAME", "value": "'${CURRENT}'"}, "parameter": {"name": "WFLY_PR_BRANCH", "value": "'master'"}}'
  # jenkins XSS needs a token
  COOKIE_PATH=/tmp/cookie_jenkins_crumb.txt
  crumb=$(curl -s -c "$COOKIE_PATH" 'http://narayanaci1.eng.hst.ams2.redhat.com/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,":",//crumb)')
  curl -v -b "$COOKIE_PATH" -X POST -H "$crumb" http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana/build?delay=0sec --data-urlencode json="$json"
  curl -v -b "$COOKIE_PATH" -X POST -H "$crumb" http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana-catelyn/build?delay=0sec --data-urlencode json="$json"
  set +e
  git fetch upstream --tags
  #./scripts/release/update_jira.py -k JBTM -t 5.next -n $CURRENT
else
  echo "This script is only interactive at the very end now, press enter to continue"
  read
fi

rm -rf $PWD/localm2repo
./build.sh clean install -Dmaven.repo.local=${PWD}/localm2repo -Prelease
rm -rf $PWD/localrepo
cd -
cd ~/tmp/narayana/$CURRENT/sources/narayana/
git checkout $CURRENT
if [[ $? != 0 ]]
then
  echo 1>&2 narayana: Tag '$CURRENT' did not exist
  exit
fi
MAVEN_OPTS="-XX:MaxPermSize=512m" 

if [[ $(uname) == CYGWIN* ]]
then
  ORSON_PATH=`cygpath -w $PWD/ext/`
else
  ORSON_PATH=$PWD/ext/
fi

rm -rf $PWD/localm2repo
./build.sh clean install -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -gs tools/maven/conf/settings.xml -Dorson.jar.location=$ORSON_PATH -Pcommunity
if [[ $? != 0 ]]
then
  echo 1>&2 Could not install narayana
  exit
fi
./build.sh clean deploy -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -gs tools/maven/conf/settings.xml -Dorson.jar.location=$ORSON_PATH -Prelease,community
if [[ $? != 0 ]]
then
  echo 1>&2 Could not deploy narayana to nexus
  exit
fi
./build.sh clean deploy -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -gs tools/maven/conf/settings.xml -Prelease -f blacktie/utils/cpp-plugin/pom.xml
if [[ $? != 0 ]]
then
  echo 1>&2 Could not deploy blacktie cpp-plugin to nexus
  exit
fi
./build.sh clean deploy -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -gs tools/maven/conf/settings.xml -Prelease  -f blacktie/pom.xml -pl :blacktie-jatmibroker-nbf -am
if [[ $? != 0 ]]
then
  echo 1>&2 Could not deploy jatmibroker to nexus
  exit
fi
git archive -o ../../narayana-full-$CURRENT-src.zip $CURRENT
cd -

xdg-open http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/ &
echo "Press enter when the centos54x64 and vc9x32 artifacts are available from http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/ are available"
read
wget http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-centos54x64-bin.tar.gz
wget http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana-catelyn/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-vc9x32-bin.zip
scp blacktie-${CURRENT}-centos54x64-bin.tar.gz jbosstm@filemgmt.jboss.org:/downloads_htdocs/jbosstm/${CURRENT}/binary/
scp blacktie-${CURRENT}-vc9x32-bin.zip jbosstm@filemgmt.jboss.org:/downloads_htdocs/jbosstm/${CURRENT}/binary/
