function fatal {
  if [[ -z $PROFILE ]]; then
      comment_on_pull "Tests failed ($BUILD_URL): $1"
  elif [[ $PROFILE == "BLACKTIE" ]]; then
      comment_on_pull "$PROFILE profile tests failed on Linux ($BUILD_URL): $1"
  else
      comment_on_pull "$PROFILE profile tests failed ($BUILD_URL): $1"
  fi

  echo "$1"
  exit 1
}

function comment_on_pull
{
    if [ "$COMMENT_ON_PULL" = "" ]; then return; fi

    PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
    if [ "$PULL_NUMBER" != "" ]
    then
        JSON="{ \"body\": \"$1\" }"
        curl -d "$JSON" -ujbosstm-bot:$BOT_PASSWORD https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/issues/$PULL_NUMBER/comments
    else
        echo "Not a pull request, so not commenting"
    fi
}

function rebase_narayana {
  echo "Rebasing Narayana"
  cd $WORKSPACE

  # Clean up the local repo
  git rebase --abort
  rm -rf .git/rebase-apply
  git clean -f -d -x

  # Work out the branch point
  git branch -D 4.17
  git branch 4.17 origin/4.17
  git branch -D master
  git branch master origin/master
  myRev=`git rev-parse HEAD`
  ancestor417=`git merge-base $myRev 4.17`
  ancestorMaster=`git merge-base $myRev master`
  distanceFromMaster=`git log $ancestorMaster..$myRev | grep commit | wc | cut -c 1-7 | tr -d ' '`
  distanceFrom417=`git log $ancestor417..$myRev | grep commit | wc | cut -c 1-7 | tr -d ' '`
  if [ "$distanceFromMaster" -lt "$distanceFrom417" ]
  then
    export BRANCHPOINT=master
  else
    export BRANCHPOINT=4.17
  fi

  # Update the pull to head  
  git pull --rebase --ff-only origin $BRANCHPOINT

  if [ $? -ne 0 ]; then
    #comment_on_pull "Narayana rebase failed. Please rebase it manually."
    fatal "Narayana rebase on $BRANCHPOINT failed. Please rebase it manually"
  fi
}

rebase_narayana "$@"
exit 0
