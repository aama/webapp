# webapp (branch dev)

A skeletal clojure webapp (https:/github.com/gbgg/webapp) demonstrating access to aama paradigmatic and other data via a local Fuseki server. The branch dev webapp is intended as a test bed for SPARQL queries in a clojure context. It uses "live" aama data, and assumes that language data has been loaded into the aama datastore (cf. aama-data/bin/aama-datastore-setup.sh and aama-data/bin/README.md)

## Fuseki setup

Run `$ bin/fuseki.sh` in order to launch the server using the same datastore as is being used in aama-data.

### Verify setup

Navigate to localhost:3030, click on the Control Panel link,
select the `/aama` dataset, then run the following query:

```
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX aama: <http://oi.uchicago.edu/aama/schema/2013#>
SELECT DISTINCT ?g
WHERE
{
  GRAPH ?g  {?s ?p ?o }
 }
```

The result (text format) should look like:

```
------------------------------------------------------------
| g                                                        |
============================================================
| <http://oi.uchicago.edu/aama/2013/graph/afar>            |
| <http://oi.uchicago.edu/aama/2013/graph/akkadian-ob>     |
| <http://oi.uchicago.edu/aama/2013/graph/alaaba>          |
| <http://oi.uchicago.edu/aama/2013/graph/arabic>          |
| <http://oi.uchicago.edu/aama/2013/graph/arbore>          |
| <http://oi.uchicago.edu/aama/2013/graph/awngi>           |
| <http://oi.uchicago.edu/aama/2013/graph/bayso>           |
| <http://oi.uchicago.edu/aama/2013/graph/beja-arteiga>    |
| <http://oi.uchicago.edu/aama/2013/graph/beja-atmaan>     |
| <http://oi.uchicago.edu/aama/2013/graph/beja-beniamer>   |
| <http://oi.uchicago.edu/aama/2013/graph/beja-bishari>    |
| <http://oi.uchicago.edu/aama/2013/graph/beja-hadendowa>  |
| <http://oi.uchicago.edu/aama/2013/graph/bilin>           |
| <http://oi.uchicago.edu/aama/2013/graph/boni-jara>       |
| <http://oi.uchicago.edu/aama/2013/graph/boni-kijee-bala> |
| <http://oi.uchicago.edu/aama/2013/graph/boni-kilii>      |
| <http://oi.uchicago.edu/aama/2013/graph/burji>           |
| <http://oi.uchicago.edu/aama/2013/graph/burunge>         |
| <http://oi.uchicago.edu/aama/2013/graph/coptic-sahidic>  |
| <http://oi.uchicago.edu/aama/2013/graph/dahalo>          |
| <http://oi.uchicago.edu/aama/2013/graph/dhaasanac>       |
| <http://oi.uchicago.edu/aama/2013/graph/dizi>            |
| <http://oi.uchicago.edu/aama/2013/graph/egyptian-middle> |
| <http://oi.uchicago.edu/aama/2013/graph/elmolo>          |
| <http://oi.uchicago.edu/aama/2013/graph/gawwada>         |
| <http://oi.uchicago.edu/aama/2013/graph/gedeo>           |
| <http://oi.uchicago.edu/aama/2013/graph/geez>            |
| <http://oi.uchicago.edu/aama/2013/graph/hadiyya>         |
| <http://oi.uchicago.edu/aama/2013/graph/hebrew>          |
| <http://oi.uchicago.edu/aama/2013/graph/iraqw>           |
| <http://oi.uchicago.edu/aama/2013/graph/kambaata>        |
| <http://oi.uchicago.edu/aama/2013/graph/kemant>          |
| <http://oi.uchicago.edu/aama/2013/graph/khamtanga>       |
| <http://oi.uchicago.edu/aama/2013/graph/koorete>         |
| <http://oi.uchicago.edu/aama/2013/graph/maale>           |
| <http://oi.uchicago.edu/aama/2013/graph/oromo>           |
| <http://oi.uchicago.edu/aama/2013/graph/rendille>        |
| <http://oi.uchicago.edu/aama/2013/graph/saho>            |
| <http://oi.uchicago.edu/aama/2013/graph/shinassha>       |
| <http://oi.uchicago.edu/aama/2013/graph/sidaama>         |
| <http://oi.uchicago.edu/aama/2013/graph/somali-standard> |
| <http://oi.uchicago.edu/aama/2013/graph/syriac>          |
| <http://oi.uchicago.edu/aama/2013/graph/tsamakko>        |
| <http://oi.uchicago.edu/aama/2013/graph/wolaytta>        |
| <http://oi.uchicago.edu/aama/2013/graph/yaaku>           |
| <http://oi.uchicago.edu/aama/2013/graph/yemsa>           |
------------------------------------------------------------
```

The webapp also queries this data at http://localhost:3000/aama.
(Notice the webapp port is 3000, Fuseki's is 3030.)

## Webapp

In one shell (terminal session) run ``$ lein cljsbuild auto ``

This compiles the clojurescript and reloads code on edit.  See
[lein cljsbuild](https://github.com/emezeske/lein-cljsbuild) for
details.

In another shell (terminal session), launch the app in a local
webserver: ``$ lein ring server-headless``

Now you can open localhost:3000 in your browser. In localhost:3000/sparql, 
you will see the result of one query run against the datastore. You can 
make changes in the clojure and clojurescript source, reload the page, 
and see the results. 

For example, try (by commenting and uncommenting) alternating between

```
"format" "application/sparql-results+json"
```
 and
 
```
"format" "text"
```
 in the

```
{:query-params . . . }
```
 of ``core.clj``.

The text file webapp/queries.txt contains a number of queries, in native 
SPARQL format and in the matsu format developed for clojure. You can 
substitute any of these queries for the 
```
(defquery aama-qry [] . . .)
```  
currently contained in ``src/clj/webapp/core.clj``, or try out SPARQL queries 
of your own (in matsu format!).

Other trial pages, showing clojure renderings of html drop-down query pick lists and a help page can be seen at:

* /beja-arteiga2
* /oromo
* /guide

The clojure rendering is effected by [hiccup-bridge](https://github.com/hozumi/hiccup-bridge), whereby 
```
$ lein hicv 2 clj

```
converts all html files in the hicv directory to hiccup form (presupposes that \[hiccup-bridge "1.0.0-SNAPSHOT"\] has been added to project plugins).

## REPL

For the Clojure REPL see `lein repl`,
[tools.nrepl](https://github.com/clojure/tools.nrepl), and
[cider](NNNNhttps://github.com/clojure-emacs/cider).

You do not have to use emacs.  Many Clojure hackers do, but recently
[Lighttable](http://www.chris-granger.com/lighttable/) has also become
quite popular.

For the Clojurescript REPL, See
* [The REPL and Evaluation Environments](https://github.com/clojure/clojurescript/wiki/The-REPL-and-Evaluation-Environments)
* [REPL Support](https://github.com/emezeske/lein-cljsbuild/blob/1.0.3/doc/REPL.md) for lein cljsbuild
