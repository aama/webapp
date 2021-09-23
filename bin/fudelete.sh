#!/bin/bash

# Does not work for any final graph; get message:
# Error 500: Quad: object cannot be null. Same message in Fuseki interface.

usage() {
    printf "Usage:  fudelete [options] ARG\n"
    printf "Delete language-specific graphs.\n"
    printf "\tARG	data/<lang> or just data for all langs\n"
    printf "\tOptions:\n"
    printf "\t\t-h, --help	Display this message\n\n"
}
#. bin/constants.sh

#global:
newpath=""

case $1 in
    -n) DRY_RUN=1	 ;;
    -h|--help) usage; exit ;;
    # --) shift; break ;;
    -*) usage "$1: unknown option" ;;
    *) ;;
esac

# echo "fudelete.log" > logs/fudelete.log;
for f in `find $1 -type d`
do
    echo $f
    l=${f%/}
    lang=${l#../aama-data/data/}
    #uncapitalize_path $lang
    graph="http://oi.uchicago.edu/aama/2013/graph/${lang}"
    echo deleting $graph;
    ../jena/apache-jena-fuseki-3.16.0/bin/s-delete -v http://localhost:3030/aama/data $graph 2>&1
    # >>logs/fudelete.log
done
