#!/bin/bash

. pre-release-vars.sh

function fatal {
  echo "Failed: $1"
  exit 1
}

#Get the versions, stripping off any '-SNAPSHOT' suffix
CURRENT=$(echo ${CURRENT_SNAPSHOT_VERSION} | awk -F '-SNAPSHOT' '{ print $1 }')
NEXT=$(echo ${1} | awk -F '-SNAPSHOT' '{ print $1 }')

if [ "$NEXT" == "" ]; then
    echo "usage: $0 <next version>"
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

TEMP_WORKING_DIR=/tmp/$0
mkdir $TEMP_WORKING_DIR
cd $TEMP_WORKING_DIR || fatal

for REPO in documentation quickstart narayana
do
    echo ""
    echo "=== TAGGING AND UPDATING $REPO ==="
    echo ""

    git clone git@github.com:jbosstm/$REPO.git || fatal
    cd $REPO
    git checkout $BRANCH || fatal

    find . -type f | grep -v ".svn" | grep -v ".git" | grep -v target | grep -v .idea | xargs sed -i "s/$CURRENT-SNAPSHOT/$CURRENT/g" || fatal
    git commit -am "Updated to $CURRENT" || fatal
    git tag $CURRENT || fatal

    find . -type f | grep -v ".svn" | grep -v ".git" | grep -v target | grep -v .idea | xargs sed -i "s/$CURRENT/$NEXT/g" || fatal
    git commit -am "Updated to $NEXT" || fatal
    git push origin $BRANCH --tags || fatal
    cd ..
done

rm -rf $TEMP_WORKING_DIR

