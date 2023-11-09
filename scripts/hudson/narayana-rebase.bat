git rebase --abort
git clean -f -d -x
git branch -D 6.0
git branch 6.0 origin/6.0
git branch -D main
git branch main origin/main
git pull --rebase --ff-only origin main
