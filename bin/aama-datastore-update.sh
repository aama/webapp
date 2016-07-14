#!/bin/sh

# rev 02/13/15

# Script to delete current arg graphs from datastore, regenerate ttl/rdf
# from corrected edn, upload to fuseki, generate pnames files necessary for
# pdgm "pname" display; assumes fuseki has been launched by bin/fuseki.sh.
# Meant to be used when one or more lang edn files have been updated.
# usage (from ~/webapp): bin/aama-datastore-update.sh ../aama-data/data/[LANGDOMAIN]

#. bin/constants.sh
ldomain=${1//,/ }
ldomain=${ldomain//\"/}

# DOESN'T WORK ldomain="bayso beja-atmaan beja-hadendowa bilin boni-kilii boni-jara boni-kijee-bala burunge burji dahalo elmolo iraqw kambaata kemant saho shinassha wolaytta yemsa"

echo "ldomain is ${ldomain}"

for f in `find $ldomain -name "*.edn"`
do
    f2=${f%/*-pdgms.edn}
    echo "delete f = ${f2}"
    bin/fudelete.sh $f2
    #bin/fuqueries.sh
    echo " "
    #echo "[Enter] to continue or Ctl-C to exit"
    #read
    echo "edn2ttl2rdf f = ${f}"
    bin/aama-edn2rdf.sh $f
    echo "2fuseki f = ${f2}"
    bin/aama-rdf2fuseki.sh $f2
    #bin/fuqueries.sh
    echo "======================="
    #bin/aama-cp2lngrepo.sh $f
done
