#!/bin/sh

# rev 12/22/12
# Start the server before queries

 cd ../aama/fuseki/jena-fuseki-1.1.1/
./fuseki-server  --config=aamaconfig.ttl 
#./fuseki-server -v  --update --loc=aama /aamaData
