#!/bin/bash
# usage:  ~/webapp/bin/aama-edn2ttl.sh "dir"

# 03/21/14; rev. 10/30/20 -- insert .ttl into fuseki 

#. bin/constants.sh

 
#for d in burji dizi  hebrew kemant saho yaaku

#for d in `ls data`
#do
#    echo "$d ********************************************"
#	fs=`find data/$d -name *edn`
    fs=`find $1 -name *edn`
    for f in $fs
	do
		echo "generating ${f%\.json}.ttl  from  $f "
                echo "(future project)"
		
                python3 json2ttl.py $f > ${f%\.json).ttl

	done
#done
