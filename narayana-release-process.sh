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

command -v ant >/dev/null 2>&1 || { echo >&2 "I require ant but it's not installed.  Aborting."; exit 1; }
command -v awestruct >/dev/null 2>&1 || { echo >&2 "I require awestruct (http://awestruct.org/getting_started) but it's not installed.  Aborting."; exit 1; }
read -p "Have you created a WFLY issue at https://issues.jboss.org/secure/CreateIssue.jspa and have the number?" WISSUEOK
if [[ $WISSUEOK == n* ]]
then
  exit
fi
if [ $# -ne 3 ]; then
  echo 1>&2 "$0: usage: CURRENT NEXT WFLYISSUE"
  exit 2
else
  CURRENT=$1
  NEXT=$2
  WFLYISSUE=$3
fi

if [[ $(uname) == CYGWIN* ]]
then
  docker-machine env --shell bash
  if [[ $? != 0 ]]; then
    exit
  fi
  eval "$(docker-machine env --shell bash)"
  read -p "ARE YOU RUNNING AN ELEVATED CMD PROMPT docker needs this" ELEV
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
read -p "You will need: docker.io account with permission to push under https://hub.docker.com/u/jbosstm/. Do you have these? y/n: " ENVOK
if [[ $ENVOK == n* ]]
then
  exit
fi
read -p "Until ./scripts/release/update_jira.py -k JBTM -t 5.next -n $CURRENT is fixed you will need to go to https://issues.jboss.org/projects/JBTM?selectedItem=com.atlassian.jira.jira-projects-plugin%3Arelease-page&status=released-unreleased, rename (Actions -> Edit) 5.next to $CURRENT, create a new 5.next version, Actions -> Release on the new $CURRENT. Have you done this? y/n " JIRAOK
if [[ $JIRAOK == n* ]]
then
  exit
fi

# Do this early to prevent later interactive need
docker login docker.io
[ $? -ne 0 ] && echo "Login to docker.io was not succesful" && exit

#if [ -z "$WFLYISSUE" ]
#then
  #./scripts/release/update_upstream.py -s WFLY -n $CURRENT
#  exit
#fi

git fetch upstream --tags
set +e
git tag | grep -x $CURRENT
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
  JIRA_HOST=issues.redhat.com JENKINS_JOBS=narayana,narayana-catelyn,narayana-documentation,narayana-hqstore,narayana-jdbcobjectstore,narayana-quickstarts,narayana-quickstarts-catelyn\
      ./scripts/release/pre_release.py
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

if [ ! -d "jboss-as" ]
then
  (git clone git@github.com:jbosstm/jboss-as.git -o jbosstm; cd jboss-as; git remote add upstream git@github.com:wildfly/wildfly.git)
fi
cd jboss-as
git fetch jbosstm
git branch | grep $WFLYISSUE
if [[ $? != 0 ]]
then
  git fetch upstream; 
  git checkout -b ${WFLYISSUE}
  git reset --hard upstream/master
  CURRENT_VERSION_IN_WFLY=`grep 'narayana>' pom.xml | cut -d \< -f 2|cut -d \> -f 2`
  if [[ $(uname) == CYGWIN* ]]
  then
    sed -i "s/narayana>$CURRENT_VERSION_IN_WFLY/narayana>$CURRENT/g" pom.xml
  else
    sed -i "s/narayana>$CURRENT_VERSION_IN_WFLY/narayana>$CURRENT/g" pom.xml
  fi
  git add pom.xml
  git commit -m "${WFLYISSUE} Upgrade Narayana to $CURRENT"
  git push --set-upstream jbosstm ${WFLYISSUE}
  git checkout 5_BRANCH
  git reset --hard jbosstm/5_BRANCH
  xdg-open https://github.com/jbosstm/jboss-as/pull/new/$WFLYISSUE &
fi
cd ..

cd ~/tmp/narayana/$CURRENT/sources/documentation/
git checkout $CURRENT
if [[ $? != 0 ]]
then
  echo 1>&2 documentation: Tag '$CURRENT' did not exist
  exit
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
./build.sh clean install -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -gs ~/.m2/settings.xml -Dorson.jar.location=$ORSON_PATH -Pcommunity
if [[ $? != 0 ]]
then
  echo 1>&2 Could not install narayana
  exit
fi
./build.sh clean deploy -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -gs ~/.m2/settings.xml -Dorson.jar.location=$ORSON_PATH -Prelease,community
if [[ $? != 0 ]]
then
  echo 1>&2 Could not deploy narayana to nexus
  exit
fi
./build.sh clean deploy -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -gs ~/.m2/settings.xml -Prelease -f blacktie/utils/cpp-plugin/pom.xml
if [[ $? != 0 ]]
then
  echo 1>&2 Could not deploy blacktie cpp-plugin to nexus
  exit
fi
./build.sh clean deploy -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -gs ~/.m2/settings.xml -Prelease  -f blacktie/pom.xml -pl :blacktie-jatmibroker-nbf -am
if [[ $? != 0 ]]
then
  echo 1>&2 Could not deploy jatmibroker to nexus
  exit
fi
git archive -o ../../narayana-full-$CURRENT-src.zip $CURRENT
ant -f build-release-pkgs.xml -Dawestruct.executable="awestruct" all
if [[ $? != 0 ]]
then
  echo 1>&2 COULD NOT BUILD Narayana RELEASE PKGS
  exit
fi
cd -

# Building and pushing the lra coordinator docker image
cd ~/tmp/narayana/$CURRENT/sources/jboss-dockerfiles/lra/lra-coordinator
git checkout $CURRENT
if [[ $? != 0 ]]
then
  echo 1>&2 jboss-dockerfiles: Tag $CURRENT did not exist
  exit
fi
docker build -t lra-coordinator --build-arg NARAYANA_VERSION=${CURRENT} .
docker tag lra-coordinator:latest docker.io/jbosstm/lra-coordinator:${CURRENT}
docker tag lra-coordinator:latest docker.io/jbosstm/lra-coordinator:latest
docker push docker.io/jbosstm/lra-coordinator:${CURRENT}
docker push  docker.io/jbosstm/lra-coordinator:latest

xdg-open http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/ &

curl -Is http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-centos54x64-bin.tar.gz | head -1 | grep 20
while [ $? != 0 ];
do
  echo date "Wait 60 seconds or press enter when http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-centos54x64-bin.tar.gz is available"
  read -t 60
  curl -Is http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-centos54x64-bin.tar.gz | head -1 | grep 20
done
wget http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-centos54x64-bin.tar.gz
scp blacktie-${CURRENT}-centos54x64-bin.tar.gz jbosstm@filemgmt.jboss.org:/downloads_htdocs/jbosstm/${CURRENT}/binary/&

curl -Is http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana-catelyn/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-vc9x32-bin.zip | head -1 | grep 20
while [ $? != 0 ];
do
  echo date "Wait 60 seconds or press enter when http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana-catelyn/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-vc9x32-bin.zip is available"
  read -t 60
  curl -Is http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana-catelyn/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-vc9x32-bin.zip | head -1 | grep 20
done
wget http://narayanaci1.eng.hst.ams2.redhat.com/view/Release/job/release-narayana-catelyn/lastSuccessfulBuild/artifact/blacktie/blacktie/target/blacktie-${CURRENT}-vc9x32-bin.zip
scp blacktie-${CURRENT}-vc9x32-bin.zip jbosstm@filemgmt.jboss.org:/downloads_htdocs/jbosstm/${CURRENT}/binary/
