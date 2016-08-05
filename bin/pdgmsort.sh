#!/bin/sh

# rev 03/30/16

# Script to run ednsort/core.clj to order to  sort and order 
# edn files of pdgms in aama-data/data

#   cd ~/webapp/bin/tools/ednsort

echo "Start sort:"

languages="akkadian-ob arabic arbore burunge dhasaanac geez hebrew iraqw kambaata koorete maale oromo shinassha sidaamo wolaytta yemsa beja-atmaan bilin boni-kilii burji coptic-sahidic dahalo elmolo hadiyya kemant saho"

for lang in $language
do
    echo "language: ${lang}"
    lein run ~/aama-data/data/${lang}/${lang}-pdgms.edn > notes/${lang}-pdgms-sort.edn
    echo "======================="
done
