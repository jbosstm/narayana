#!/bin/bash
read -p "You will need: jira admin, github permissions on all jbosstm/ repo and nexus permissions. Do you have these?" ENVOK
if [[ $ENVOK == n* ]]
then
  exit
fi

if [ $# -eq 0 ]; then
  . scripts/pre-release-vars.sh
  CURRENT=`echo $CURRENT_SNAPSHOT_VERSION | sed "s/-SNAPSHOT//"`
  NEXT=`echo $CURRENT_SNAPSHOT_VERSION | sed "s/.Final//"`
  NEXT="${NEXT%.*}.$((${NEXT##*.}+1))".Final
elif [ $# -lt 2 ]; then
  echo 1>&2 "$0: not enough arguments: CURRENT NEXT (versions should end in .Final or similar)"
  exit 2
elif [ $# -gt 2 ]; then
  echo 1>&2 "$0: too many arguments: CURRENT NEXT (versions should end in .Final or similar)"
  exit 2
else
  CURRENT=$1
  NEXT=$2
fi

set +e
git fetch upstream --tags
git tag | grep $CURRENT
if [[ $? != 0 ]]
then
  set -e
  git checkout 4.17
  git log -n 10
  echo Mark version as released in Jira and create next version: https://issues.jboss.org/plugins/servlet/project-config/JBTM/versions
  echo Make sure you have the credentials in your .m2/settings.xml and ignore an error in the final module about missing javadocs
  echo Watch out for sed -i "" in the pre-release.sh as it is does not work on Cygwin
  read -p "Did the log before look OK?" ok
  if [[ $ok == n* ]]
  then
    exit
  else
    ok=y
  fi
  (cd ./scripts/ ; ./pre-release.sh $CURRENT $NEXT)
else
  set -e
  ok=y
fi

if [[ $ok == y* ]]
then
  git fetch upstream
  git checkout $CURRENT
  git clean -f -d
  # Add -x (this will delete all files (e.g. IDE, new features) not under source control)
  rm -rf $PWD/localm2repo
  ./build.sh clean deploy -Dmaven.repo.local=${PWD}/localm2repo -Prelease,all -Dmaven.javadoc.skip=true -DskipTests -pl :jbossjts-jacorb,:jbossxts,:jbossjts-integration,:byteman_support,:jbosstxbridge -am
  rm -rf $PWD/localrepo

  echo "Go to https://repository.jboss.org/nexus/index.html#welcome
  Go to Staging Repositories
  Find your repo
  Close (no description)
  Release (no description)"
fi
