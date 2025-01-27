git rebase --abort
git clean -f -d -x
git branch -D 7.1
git branch 7.1 origin/7.1
git branch -D main
git branch main origin/main
git pull --rebase --ff-only origin main
