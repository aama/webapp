#!/bin/bash
# usage: ~/webapp/bin/aama-cp2lngrepo.sh ../aama-data/data/LDOMAIN

ldomain=${1//,/ }
ldomain=${ldomain//\"/}

echo "ldomain is ${ldomain}"

for f in `find $ldomain -name "*pdgms.json"`

#fs=`find $1 -name "*.json"`
#for f in $fs

do
  l=${f%-pdgms.json}
  lang=${l##../aama-data/data/*/}
  echo "$lang *****************************************"
  echo "copying $f to aama/data/$lang"
  #cp ../aama-data/data/$lang/$lang-pdgms\.edn ../aama/data/$lang/
  cp ~/aama-data/data/$lang/$lang-pdgms\.ttl ~/aama/data/$lang/  
  cp ~/aama-data/data/$lang/$lang-pdgms\.json ~/aama/data/$lang/
  cd ~/aama/data/$lang
  #git add *.edn
  git add *.ttl
  git add *.json
  git commit -am "json/ttl replacing edn/ttl added (after json2ttl in aama-data)"
  git push origin master
  cd ~/webapp
done
