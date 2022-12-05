git rebase --abort
git clean -f -d -x
git branch -D 4.17
git branch 4.17 origin/4.17
git branch -D main
git branch main origin/main
git pull --rebase --ff-only origin main
