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
if [ $# -ne 4 ]; then
  echo 1>&2 "$0: usage: CURRENT NEXT WFLYISSUE RSYNC_HOST"
  exit 2
else
  CURRENT=$1
  NEXT=$2
  WFLYISSUE=$3
  RSYNC_HOST=$4
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
read -p "You will need: VPN, credentials for jbosstm@${RSYNC_HOST}, jira admin, github permissions on all jbosstm/ repo and nexus permissions. Do you have these?" ENVOK
if [[ $ENVOK == n* ]]
then
  exit
fi
read -p "By default you will need the ability to upload to a server for hosting the website and downloads. Do you have this (if you have access but don't want to upload, answer n* and then the next question)?" RSYNCOK
if [[ $RSYNCOK != n* ]]
then
  RSYNC_ENABLED="true"
else
  read -p "Do you want to continue without uploading the website?" NORSYNC
  if [[ $NORSYNC == n* ]]
  then
    exit
  fi
  RSYNC_ENABLED="false"
fi
read -p "Have you configured ~/.m2/settings.xml with repository 'jboss-releases-repository' and correct username/password?" M2OK
if [[ $M2OK == n* ]]
then
  exit
fi
read -p "To upload an lra-coordinator image to quay.io you will need a quay.io account with permission to push to jbosstm. Do you have this (if you have access but don't want to upload, answer n* and then the next question)?: " QUAYIOOK
if [[ $QUAYIOOK == n* ]]
then
  read -p "Do you want to continue without uploading lra-coordinator image?" NOQUAYIO
  if [[ $NOQUAYIO == n* ]]
  then
    exit
  fi
  QUAYIOOK=n
fi
read -p "Until ./scripts/release/update_jira.py -k JBTM -t 5.next -n $CURRENT is fixed you will need to go to https://issues.jboss.org/projects/JBTM?selectedItem=com.atlassian.jira.jira-projects-plugin%3Arelease-page&status=released-unreleased, rename (Actions -> Edit) 5.next to $CURRENT, create a new 5.next version, Actions -> Release on the new $CURRENT. Have you done this? y/n " JIRAOK
if [[ $JIRAOK == n* ]]
then
  exit
fi

if [[ $QUAYIOOK != n* ]]
then
  # Do this early to prevent later interactive need
  echo "logging in to quay.io ..."
  # log in to quay.io
  podman login quay.io
  [ $? -ne 0 ] && echo "Login to quay.io was not succesful" && exit
fi

#if [ -z "$WFLYISSUE" ]
#then
  #./scripts/release/update_upstream.py -s WFLY -n $CURRENT
#  exit
#fi

git fetch upstream --tags
if [[ $? != 0 ]]; then
  echo "fetch upstream failed, exiting"
  exit
fi
set +e
git tag | grep -x $CURRENT
if [[ $? != 0 ]]
then
  read -p "Should the release be aborted if there are local commits (y/n): " ok
  if [[ $ok == y* ]]; then
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
  git reset --hard upstream/main
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
git archive -o ../../narayana-full-$CURRENT-src.zip $CURRENT
ant -f build-release-pkgs.xml -Drsync.host=${RSYNC_HOST} -Drsync.enabled=${RSYNC_ENABLED} -Dawestruct.executable="awestruct" all
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

if [[ $QUAYIOOK != n* ]]
then
  echo "building, tagging and pushing podman images to quay.io ..."
#read -p "Have you logged in (podman login quay.io), if not you will be prompted (y/n): " ok
#if [[ $ok == y* ]]; then
  # build the lra-coordinator image
  podman build --tag lra-coordinator --build-arg NARAYANA_VERSION=${CURRENT} .
  # tag it and push it
  podman tag lra-coordinator quay.io/jbosstm/lra-coordinator:${CURRENT}
  podman tag lra-coordinator quay.io/jbosstm/lra-coordinator:latest

  # push the image
  podman push quay.io/jbosstm/lra-coordinator:${CURRENT}
  podman push quay.io/jbosstm/lra-coordinator:latest
  # the image will appear in https://quay.io/repository/jbosstm/lra-coordinator?tab=tags
#else
#  echo "alternatively run the above commands manually"
#fi
fi

# not sure why we need to look at CI at this point so commenting it out
# xdg-open http://narayanaci1.eng.hst.ams2.redhat.com/ &
