#!/bin/sh

# rev 12/22/12
# Start the server before queries
FUSEKI_HOME=/Users/genegragg/jena

 cd ${FUSEKI_HOME}/apache-jena-fuseki-3.16.0/
./fuseki-server  --config=aamaconfig.ttl 
#./fuseki-server -v  --update --loc=aama /aamaData
