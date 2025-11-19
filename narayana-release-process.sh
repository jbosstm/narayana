#!/bin/bash

# You need to run this script with at least 2 arguments.
# The third argument ORG is optional.
# Arguments are then transformed to env variables CURRENT, NEXT, and ORG.
# Release process releases version ${CURRENT} and prepares github with commits to ${NEXT}-SNAPSHOT
#
# 3 arguments: `./narayana-release-process.sh CURRENT NEXT ORG`

if [ $# -lt 2 ]; then
  echo 1>&2 "$0: usage: CURRENT NEXT ORG"
  exit 2
else
  CURRENT=$1
  NEXT=$2
  if [ -z "$3" ]; then
    ORG=jbosstm
  else
    ORG=$3
  fi
fi

echo "You will need: VPN, credentials for jbosstm host, jira admin, github permissions on all $ORG repo and nexus permissions." 
echo "Please check the configuration in ~/.m2/settings.xml with repository 'jboss-releases-repository' and correct username/password." 
read -p "Have you done these steps? y/n " STEPSOK
if [[ $STEPSOK == n* ]]; then
  exit
fi

# add the upstream remote
git remote -v | grep upstream
if [[ $? != 0 ]]; then
  git remote add upstream git@github.com:jbosstm/narayana.git
fi

if [ ! $SNAPSHOT ]; then
  # we want to check if this repo has already been tagged with the same version before
  git fetch upstream --tags
  if [[ $? != 0 ]]; then
    echo "fetch upstream failed, exiting"
    exit
  fi
  set +e
  git tag | grep -x $CURRENT
  if [[ $? != 0 ]]; then
    read -p "Should the release be aborted if there are local commits (y/n): " ok
    if [[ $ok == y* ]]; then
      git status | grep "nothing to commit"
      if [[ $? != 0 ]]; then
        git status
        exit
      fi
      git status | grep "ahead"
      if [[ $? != 1 ]]; then
        git status
        exit
      fi
    fi
    git log -n 5
    read -p "Did the log before look OK?" ok
    if [[ $ok == n* ]]; then
      exit
    fi
    set -e

    read -p "Until JBTM-3891 is resolved, please review with project team to ensure that it is safe to release. Please check for failed CI jobs before continuing. Continue? y/n " NOBLOCKERS
    if [[ $NOBLOCKERS == n* ]]; then
      exit
    fi

    echo "Executing pre-release script, this may be interactive so please stand by"
    (cd ./scripts/ ; ./pre-release.sh $CURRENT $NEXT $ORG)
    set +e
    git fetch upstream --tags
  fi

  echo "This script is only interactive at the very end now, press enter to continue"
  read

  if [ -z "${DONOTDELETELOCALM2REPO}" ]; then
    rm -rf $PWD/localm2repo
  else
    echo "Not deleting existing $PWD/localm2repo as requested"
  fi

  cd -
  cd ~/tmp/narayana/$CURRENT/sources/narayana/
  git checkout $CURRENT
  if [[ $? != 0 ]]
  then
    echo 1>&2 narayana: Tag '$CURRENT' did not exist
    exit
  fi

  ORSON_PATH=$PWD/ext/

  # uploaded artifacts go straight live without the ability to close the repo at the end, so the install is done to verify that the build will work
  ./build.sh clean install -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -Pcommunity -Drelease -DreleaseStaging
  if [[ $? != 0 ]]; then
    echo 1>&2 Could not install narayana
    exit
  fi
fi

if [ -z "${ORSON_PATH}" ]; then
  ORSON_PATH=$PWD/ext/
fi

# It is important in the deploy step that, if you are deploying, you provide a reference to your settings file as the ./build.sh overrides the default settings file discovery of Maven.
# Please see https://github.com/jbosstm/narayana/wiki/Narayana-Release-Process for details of the settings.xml requirements
./build.sh clean deploy -Dmaven.repo.local=${PWD}/localm2repo -DskipTests -gs ~/.m2/settings.xml -Pcommunity -Dsign -Ddocs -Drelease -DreleaseStaging
if [[ $? != 0 ]]; then
  echo 1>&2 Could not deploy narayana
  exit
fi

echo "After Nexus has verified that all artifacts meet Maven Central's requirements, please run post-release.sh"