function fatal {
  echo "Failed: $1"
  exit 1
}

. branch-names.sh

echo "Rebaseing origin/$ORIGIN_AS_BRANCH with upstream/$UPSTREAM_AS_BRANCH"

GIT_URL="git@github.com:jbosstm/jboss-as.git"
UPSTREAM_GIT_URL="https://github.com/jbossas/jboss-as.git"
TEMPORARY_REBASE_LOCATION=/tmp/rebase-jbossas-branch

rm -rf $TEMPORARY_REBASE_LOCATION || fatal
mkdir $TEMPORARY_REBASE_LOCATION || fatal
cd $TEMPORARY_REBASE_LOCATION || fatal

git clone $GIT_URL || fatal
cd jboss-as
git checkout -t origin/$ORIGIN_AS_BRANCH || fatal

git remote add upstream $UPSTREAM_GIT_URL
git pull --rebase --ff-only upstream $UPSTREAM_AS_BRANCH

while [ $? != 0 ]
do
 for i in `git status -s | sed "s/UU \(.*\)/\1/g"`
 do
    awk '/^<+ HEAD$/,/^=+$/{next} /^>+ /{next} 1' $i > $i.bak; mv $i.bak $i; git add $i
 done
 git rebase --continue
done
[ $? = 0 ] || fatal "git rebase failed"

git push origin $ORIGIN_AS_BRANCH -f
rm -rf $TEMPORARY_REBASE_LOCATION
