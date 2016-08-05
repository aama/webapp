#!/bin/sh

# rev 03/30/16

# Script to run ednsort/core.clj to order to  sort and order 
# edn files of pdgms in aama-data/data

#   cd ~/webapp/bin/tools/ednsort

echo "Start sort:"

#languages="akkadian-ob arabic arbore burunge dhaasanac geez hebrew iraqw kambaata koorete maale oromo shinassha sidaama wolaytta yemsa beja-atmaan bilin boni-kilii burji coptic-sahidic dahalo elmolo hadiyya kemant saho"

languages="akkadian-ob arabic arbore dhaasanac geez hebrew kambaata koorete sidaama"

for lang in $languages
do
    echo "language: ${lang}"
    lein run ../../../../aama-data/data/${lang}/${lang}-pdgms.edn > notes/${lang}-pdgms-sort.edn
    echo "======================="
done
