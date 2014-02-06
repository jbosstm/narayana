git rebase --abort
git clean -f -d -x
git branch -D 4.17
git branch 4.17 origin/4.17
git branch -D master
git branch master origin/master
git pull --rebase --ff-only origin master
