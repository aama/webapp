#!/bin/bash
# usage:  ~/aama/tools/bin/aama-pulldata.sh "dir"
# examples:
# for use on remote repository, to update data files
# (probably needs more revision; revisit as revise data

# 09/15/16
# PROBLEM: I am currenly getting cl emacs for commit/merge message on each
# directory. Do I need to do a commit before the pull?

#. bin/constants.sh


fs=`find $1 -name "*.edn"`
for f in $fs
do
    l=${f%-pdgms.edn}
    lang=${l#data/*/}
    echo "$lang ********************************************"
    echo pulling data to aama/$lang
    cd data/$lang
    # git commit -am "committing current data"
    git pull
    git commit -am "most recent data pulled"
    cd ../../
done
