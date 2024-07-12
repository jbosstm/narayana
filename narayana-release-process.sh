#!/bin/bash

# You need to run this script with 2 arguments.
# Arguments are then tranformed to env varibales CURRENT and NEXT
# Release process releases version ${CURRENT} and prepares github with commits to ${NEXT}-SNAPSHOT
#
# 2 arguments: `./narayana-release-process.sh CURRENT NEXT`

if [ $# -ne 2 ]; then
  echo 1>&2 "$0: usage: CURRENT NEXT"
  exit 2
else
  CURRENT=$1
  NEXT=$2
fi

echo "You will need: VPN, credentials for jbosstm host, jira admin, github permissions on all jbosstm/ repo and nexus permissions." 
echo "By default you will need the ability to upload to a server for hosting the website and downloads." 
echo "Please check the configuration in ~/.m2/settings.xml with repository 'jboss-releases-repository' and correct username/password." 
echo "Until ./scripts/release/update_jira.py -k JBTM -t 5.next -n $CURRENT is fixed you will need to go to https://issues.jboss.org/projects/JBTM?selectedItem=com.atlassian.jira.jira-projects-plugin%3Arelease-page&status=released-unreleased, rename (Actions -> Edit) 5.next to $CURRENT, create a new 5.next version, Actions -> Release on the new $CURRENT."
read -p "Have you done these steps? y/n " STEPSOK
if [[ $STEPSOK == n* ]]
then
  exit
fi
# we want to check if this repo has already been tagged with the same version before
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
  
  read -p "Until JBTM-3891 is resolved, please review with project team to ensure that it is safe to release. Please check for failed CI jobs before continuing. Continue? y/n " NOBLOCKERS
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
if [[ $? != 0 ]]
then
  echo 1>&2 Could not clean install documentation
  exit
fi
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
# uploaded artifacts go straight live without the ability to close the repo at the end, so the install is done to verify that the build will work
./build.sh clean install -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -Dorson.jar.location=$ORSON_PATH -Pcommunity
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

# Post-release steps
echo "Please visit Narayana CI and check the quickstarts are working with the release and obtain performance numbers for the blog post"
echo "narayana.io needs updating, please update the narayana.io repository (see https://github.com/jbosstm/narayana.io/blob/develop/README.md)"
echo "Please open a PR to lra-coordinator-quarkus when the artifact is available on nexus."
echo "Please raise a Jira and pull request to update WildFly to the released version of Narayana"