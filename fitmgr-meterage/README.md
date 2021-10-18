Command line instructions

Git global setup
git config --global user.name "冉幕飞"
git config --global user.email "ranmufei@linksame.cn"

Create a new repository
git clone ssh://git@gitlab.ls.com:2289/operation-cmp/fitmgr-measurement.git
cd fitmgr-measurement
touch README.md
git add README.md
git commit -m "add README"
git push -u origin master

Existing folder
cd existing_folder
git init
git remote add origin ssh://git@gitlab.ls.com:2289/operation-cmp/fitmgr-measurement.git
git add .
git commit -m "Initial commit"
git push -u origin master

Existing Git repository
cd existing_repo
git remote rename origin old-origin
git remote add origin ssh://git@gitlab.ls.com:2289/operation-cmp/fitmgr-measurement.git
git push -u origin --all
git push -u origin --tags
