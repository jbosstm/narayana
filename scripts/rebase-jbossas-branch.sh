function fatal {
  echo "Failed: $1"
  exit 1
}

. branch-names.sh

echo "Rebaseing origin/$ORIGIN_AS_BRANCH with upstream/$UPSTREAM_AS_BRANCH"

GIT_URL="git@github.com:jbosstm/jboss-as.git"
UPSTREAM_GIT_URL="https://github.com/wildfly/wildfly.git"
TEMPORARY_REBASE_LOCATION=/tmp/rebase-jbossas-branch

rm -rf $TEMPORARY_REBASE_LOCATION || fatal
mkdir $TEMPORARY_REBASE_LOCATION || fatal
cd $TEMPORARY_REBASE_LOCATION || fatal

git clone $GIT_URL || fatal
cd jboss-as
git fetch
git checkout $ORIGIN_AS_BRANCH || fatal

git remote add upstream $UPSTREAM_GIT_URL
git pull --rebase --ff-only upstream $UPSTREAM_AS_BRANCH

if [ $? != 0 ]; then
  echo "Merge conflict needs manual intervention. Please go to '$TEMPORARY_REBASE_LOCATION/jboss-as' and resolve, then run:"
  echo ""
  echo "    git push origin $ORIGIN_AS_BRANCH"
  echo "    rm -rf $TEMPORARY_REBASE_LOCATION"
  exit -1
fi

git push origin $ORIGIN_AS_BRANCH -f
rm -rf $TEMPORARY_REBASE_LOCATION
