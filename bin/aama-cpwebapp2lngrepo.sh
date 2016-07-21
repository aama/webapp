#!/bin/bash
# usage:  ~/webapp/bin/aama-cpwebapp2langrepo.sh

# 07/21/16: (for the moment includes upload of aama-webapp.jar 

echo "webapp ********************************************"
echo "copying ~/webapp  to aama/webapp"

cd ~/webapp
cp LICENSE ../aama/webapp/
cp README.md ../aama/webapp/
cp project.clj ../aama/webapp/
cp aama-webapp.jar ../aama/webapp/
cp -R etc/* ../aama/webapp/etc/
cp -R bin/* ../aama/webapp/bin/
cp -R src/* ../aama/webapp/src/
cp -R resources/* ../aama/webapp/resources/
cd ../aama/webapp
git add LICENSE 
git add README.md 
git add project.clj 
git add aama-webapp.jar 
git add etc/* 
git add bin/* 
git add src/* 
git add resources/* 
git commit -am "revised webapp added to aama/webapp"
git push origin master
cd ../../webapp
