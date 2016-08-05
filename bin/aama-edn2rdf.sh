#!/bin/bash
# usage:  ~/aama-data/bin/edn2ttl.sh "dir"

# 03/21/14: 

#. bin/constants.sh

 
#for d in burji dizi  hebrew kemant saho yaaku

#for d in `ls data`
#do
#    echo "$d ********************************************"
#	fs=`find data/$d -name *edn`
    fs=`find $1 -name *edn`
    for f in $fs
	do
		echo "generating ${f%\.edn}.ttl  from  $f "
		#comment out following line to test ttl file generated 
		#direct from edn2ttl.core
		java -jar ../.jar/aama-edn2ttl.jar $f > ${f%\.edn}.ttl
		tofile=${f%\.edn}.rdf
		fromfile=${f%\.edn}.ttl
		#lname=`basename ${f%-pdgms.edn`
		#echo "generating ${f%\.edn}.rdf  from  ${f%\.edn}.ttl "
		echo "generating $tofile  from  $fromfile "
		# Can't get rdf2rdf to directly generate rdf file outside
		# calling directory (i.e. webapp)
		java -jar ../.jar/rdf2rdf-1.0.1-2.3.1.jar \
		               ${f%\.edn}.ttl \
		               pdgms.rdf
		mv pdgms.rdf ${tofile}
	done
#done
