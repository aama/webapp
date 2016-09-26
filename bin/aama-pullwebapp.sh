#!/bin/bash
# usage:  ~/aama/tools/bin/aama-pullwebapp.sh 
# examples:
# for use on remote repository, to update webapp files
# (probably needs more revision; revisit as revise data

# 09/22/16
# PROBLEM: I am currenly getting cl emacs for commit/merge message on each
# directory. Do I need to do a commit before the pull?

#. bin/constants.sh

echo "********************************************"
echo pulling tools to aama/tools/bin
cd tools/bin
git pull
git commit -am "most recent tools pulled"
cd ../../
echo "********************************************"
echo pulling webapp to aama/webapp
cd webapp
git pull
git commit -am "most recent webapp version pulled"
cd ../../
