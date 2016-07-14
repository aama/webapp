#!/bin/bash
# usage:  fuput "dir"
# examples:
#    aama/$ bin/fuput "data/*" --  puts everything
#    aama/$ bin/fuput "data/alaaba" -- puts only alaabe
#    aama/$ bin/fuput "data/alaaba data/burji data/coptic" -- puts all 3 datasets
#    aama/$ bin/fuput "schema" -- puts all 3 datasets
# cumulative logfile written to logs/fuput.log

# 07/13/13: cf. fupost-default.sh for loading data into single default graph


#. bin/constants.sh

echo "fuload.log" > logs/fuload.log;
for f in `find $1 -name *.rdf`
do
    l=${f%.rdf}
    lang=${l#../aama-data/data/}
    graph="http://oi.uchicago.edu/aama/2013/graph/`dirname ${lang/\/\///}`"
    echo posting $f to $graph;
    ../aama/fuseki/jena-fuseki-1.1.1/s-post -v http://localhost:3030/aama/data $graph  $f 2>&1 >>logs/fuload.log
	#${FUSEKIDIR}/s-post -v http://localhost:3030/aamaData/data 'default'   $f 2>&1 >>logs/fuload.log
done


# i.e.
#    ${FUSEKIDIR}/s-post -v http://localhost:3030/aama/data \
#                          http://oi.uchicago.edu/aama/2013/graph/beja-arteiga  \
#			  data/beja-arteiga//beja-arteiga-pdgms.rdf \
#			  2>&1 \ #"redirect stderr to stdout"
#			  >>logs/fuload.log
