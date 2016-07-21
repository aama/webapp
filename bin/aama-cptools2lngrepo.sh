#!/bin/bash
# usage:  ~/webapp/bin/aama-cptools2langrepo.sh

# 03/21/14: 
# 09/23/15: to be added to upload when aama-edn2ttl.jar has been revised

echo "tools ********************************************"
echo "copying edn2ttl jar and source files to aama/tools"
cp bin/tools/edn2ttl/project.clj ../aama/tools/clj/edn2ttl/
cp bin/tools/edn2ttl/src/edn2ttl/core.clj ../aama/tools/clj/edn2ttl/src/edn2ttl/
cp bin/tools/ednsort/project.clj ../aama/tools/clj/ednsort/
cp bin/tools/ednsort/src/ednsort/core.clj ../aama/tools/clj/ednsort/src/ednsort/
cp bin/tools/edn2ttl/aama-edn2ttl.jar ../aama/tools/clj/
cp bin/*.sh ../aama/tools/bin
cd ../aama/tools
git add clj/aama-edn2ttl.jar
git add clj/edn2ttl/project.clj
git add clj/edn2ttl/src/*
git add clj/ednsort/project.clj
git add clj/ednsort/src/*
git add bin/*.sh
git commit -am "revised shell scripts added to aama/tools/bin"
git push origin master
cd ../../webapp
