#!/bin/bash

. pre-release-vars.sh

function fatal {
  echo "Failed: $1"
  exit 1
}

#Get the versions, stripping off any '-SNAPSHOT' suffix
CURRENT=$(echo ${1} | awk -F '-SNAPSHOT' '{ print $1 }')
NEXT=$(echo ${2} | awk -F '-SNAPSHOT' '{ print $1 }')

if [ "$NEXT" == "" ]; then
    echo "usage: $0 <current version> <next version>"
    exit 1
fi

#Now add '-SNAPSHOT' to next version
NEXT="${NEXT}-SNAPSHOT"

echo "This script will:"
echo "================="
echo "Tag $CURRENT"
echo "Update Narayana $BRANCH to $NEXT"
echo ""
echo "Are you sure you want to continue? (y/n)"

read PROCEED

if [ "$PROCEED" != "y" ]; then
    echo "Aborting"
    exit 1
fi

echo "Proceeding..."

set -e
TEMP_WORKING_DIR=~/tmp/narayana/$CURRENT/sources/
mkdir -p $TEMP_WORKING_DIR
cd $TEMP_WORKING_DIR || fatal

for REPO in documentation quickstart performance narayana
do
    echo ""
    echo "=== TAGGING AND UPDATING $REPO ==="
    echo ""

    if ! [[ "$REPO" =~ 'github.com' ]]; then
      REPO_TO_CLONE="git@github.com:jbosstm/$REPO.git"
    else
      # for repositories in different namespace than Narayana expecting the repo is followed by name of repository to be cloned to
      REPO_TO_CLONE="$REPO"
      REPO=${REPO##* }
    fi

    git clone $REPO_TO_CLONE || fatal
    cd $REPO
    git checkout $BRANCH || fatal

    if [[ "$(uname)" == "Darwin" ]] # MacOS
    then
      find . -name \*.java -o -name \*.xml -o -name \*.template -o -name \*.properties -o -name \*.ent -o -name \INSTALL -o -name \README -o -name pre-release-vars.sh -o -name \*.sh -o -name \*.bat -o -name \*.cxx -o -name \*.c -o -name \*.cpp -o -iname \makefile | grep -v ".svn" | grep -v ".git" | grep -v target | grep -v .idea | xargs sed -i "" "s/$CURRENT_SNAPSHOT_VERSION/$CURRENT/g" || fatal
    else
      find . -name \*.java -o -name \*.xml -o -name \*.template -o -name \*.properties -o -name \*.ent -o -name \INSTALL -o -name \README -o -name pre-release-vars.sh -o -name \*.sh -o -name \*.bat -o -name \*.cxx -o -name \*.c -o -name \*.cpp -o -iname \makefile | grep -v ".svn" | grep -v ".git" | grep -v target | grep -v .idea | xargs sed -i "s/$CURRENT_SNAPSHOT_VERSION/$CURRENT/g" || fatal
    fi
    set +e
    git status | grep "new\|deleted"
    if [ $? -eq 0 ]
    then
      "Found new files changing to tag - very strange!"
      exit
    fi
    set -e
    # when there is nothing to commit then do not commit
    toCommit=`git diff | wc -l`
    if [ $toCommit -ge 1 ]; then
      git commit -am "Updated to $CURRENT" || fatal
    fi
    git tag $CURRENT || fatal

    if [[ "$(uname)" == "Darwin" ]] # MacOS
    then
      find . -name \*.java -o -name \*.xml -o -name \*.template -o -name \*.properties -o -name \*.ent -o -name \INSTALL -o -name \README -o -name pre-release-vars.sh -o -name \*.sh -o -name \*.bat -o -name \*.cxx -o -name \*.c -o -name \*.cpp -o -iname \makefile | grep -v ".svn" | grep -v ".git" | grep -v target | grep -v .idea | xargs sed -i "" "s/$CURRENT/$NEXT/g" || fatal
    else
      find . -name \*.java -o -name \*.xml -o -name \*.template -o -name \*.properties -o -name \*.ent -o -name \INSTALL -o -name \README -o -name pre-release-vars.sh -o -name \*.sh -o -name \*.bat -o -name \*.cxx -o -name \*.c -o -name \*.cpp -o -iname \makefile | grep -v ".svn" | grep -v ".git" | grep -v target | grep -v .idea | xargs sed -i "s/$CURRENT/$NEXT/g" || fatal
    fi
    set +e
    git status | grep "new\|deleted"
    if [ $? -eq 0 ]
    then
      "Found new files changing to new snapshot - very strange!"
      exit
    fi
    set -e
    toCommit=`git diff | wc -l`
    if [ $toCommit -ge 1 ]; then
      git commit -am "Updated to $NEXT" || fatal
    fi
    git push origin $BRANCH --tags || fatal
    cd ..
done
