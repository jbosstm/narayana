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
read -p "Have you created a WFLY issue at https://issues.jboss.org/secure/CreateIssue.jspa and have the number?" WISSUEOK
if [[ $WISSUEOK == n* ]]
then
  exit
fi
if [ $# -ne 4 ]; then
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
read -p "You will need: VPN, credentials for jbosstm host, jira admin, github permissions on all jbosstm/ repo and nexus permissions. Do you have these?" ENVOK
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
read -p "To upload an lra-coordinator image to quay.io you will need to open a PR against https://github.com/jbosstm/lra-coordinator-quarkus. Proceed?: " PROCEED
if [[ $PROCEED == n* ]]
then
  exit
fi
read -p "Until ./scripts/release/update_jira.py -k JBTM -t 5.next -n $CURRENT is fixed you will need to go to https://issues.jboss.org/projects/JBTM?selectedItem=com.atlassian.jira.jira-projects-plugin%3Arelease-page&status=released-unreleased, rename (Actions -> Edit) 5.next to $CURRENT, create a new 5.next version, Actions -> Release on the new $CURRENT. Have you done this? y/n " JIRAOK
if [[ $JIRAOK == n* ]]
then
  exit
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
  
  read -p "Please check for Jira Blocker issues with Fix.Version set to {version}.next which are not yet resolved(https://issues.redhat.com/issues/?jql=project%20%3D%20JBTM%20AND%20priority%20%3D%20Blocker%20AND%20resolution%20%3D%20Unresolved%20ORDER%20BY%20priority%20DESC%2C%20updated%20DESC) and failed CI jobs before continuing. Continue? y/n " NOBLOCKERS
  if [[ $NOBLOCKERS == n* ]]
  then
    exit
  fi
  
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
# It is important in the deploy step that if you are deploying to nexus you provide a reference to your settings file as the ./build.sh overrides the default settings file discovery of Maven. Please see https://github.com/jbosstm/narayana/wiki/Narayana-Release-Process for details of the settings.xml requirements
./build.sh clean deploy -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -gs ~/.m2/settings.xml -Dorson.jar.location=$ORSON_PATH -Prelease,community -DskipNexusStagingDeployMojo=false
if [[ $? != 0 ]]
then
  echo 1>&2 Could not deploy narayana to nexus
  exit
fi
git archive -o ../../narayana-full-$CURRENT-src.zip $CURRENT
# build-release-pkgs.xml needs to be updated, update the website manually
echo " narayana.io needs updating, please update the narayana.io repository (see https://github.com/jbosstm/narayana.io/blob/develop/README.md)"

read -p "Please update narayana.io before continuing. Continue? y/n " NARAYANAIO
if [[ $NARAYANAIO == n* ]]
then
  exit
fi

cd -

# After the release open a PR for the new Narayana release against lra-coordinator quarkus to upload the docker image to quay.io 
read -p "Please open a PR to lra-coordinator-quarkus when the artifact is available on nexus. OK? y/n " QUAYIO
  if [[ $QUAYIO == n* ]]
  then
    exit
  fi

echo "Please raise a Jira and pull request to update WildFly to the released version of Narayana"
echo "Please visit Narayana CI and check the quickstarts are working with the release and obtain performance numbers for the blog post"