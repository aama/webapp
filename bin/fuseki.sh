#!/bin/sh

# rev 12/22/12
# Start the server before queries
FUSEKI_HOME=../fuseki

 cd ${FUSEKI_HOME}/apache-jena-fuseki-2.4.0/
./fuseki-server  --config=aamaconfig.ttl 
#./fuseki-server -v  --update --loc=aama /aamaData
