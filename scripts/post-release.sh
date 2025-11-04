#!/bin/bash

. pre-release-vars.sh

function fatal {
  echo "Failed: $1"
  exit 1
}

echo "This script will:"
echo "================="
echo "Push commits and tags upstream"
echo ""
echo "Are you sure you want to continue? (y/n)"

read PROCEED

if [ "$PROCEED" != "y" ]; then
    echo "Aborting"
    exit 1
fi

echo "Proceeding..."

set -e
TEMP_WORKING_DIR="$HOME/tmp/narayana/*/sources/"
mkdir -p $TEMP_WORKING_DIR
cd $TEMP_WORKING_DIR || fatal

for REPO in quickstart performance narayana
do
  echo ""
  echo "=== GIT PUSHING $REPO ==="
  echo ""

  cd $REPO
  git push origin $BRANCH --tags || fatal
  cd ..
done
