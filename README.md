# webapp

A skeletal Clojure webapp demonstrating access to aama data via a local Fuseki server. 

## Fuseki setup

Assuming you have cloned the aama/webapp repo into aama/webapp, edit
webapp/etc/fuseki to set FUSEKI_HOME to the correct directory.  Then
source it: `$ source webapp/etc/fuseki`.

Then run the server:  `$ `echo $FUSEKI_HOME`/fuseki-server --config=etc/aamaconfig.ttl`

### Load test data:

We'll use the test data that comes with the Fuseki distrib to make
sure the service is up and running.

`$ `echo $FUSEKI_HOME\`/s-put http://localhost:3030/aama/data books \`echo $FUSEKI_HOME\`/Data/books.ttl`

The syntax of the command:  `s-put datasetURI graphName [file]`; see http://jena.apache.org/documentation/serving_data/soh.html

### Verify setup

Navigate to localhost:3030, click on the Control Panel link,
select the `/aama` dataset, then run the following query:

```
PREFIX  ex: <http://example.org/book>
PREFIX  dc: <http://purl.org/dc/elements/1.1/>
SELECT ?title ?author
WHERE
  { ?book dc:title ?title .
      ?book dc:creator ?author . }
```

The result (text format) should look like:

```
----------------------------------------------------------------
| title                                       | author         |
================================================================
| "Harry Potter and the Philosopher's Stone"  | "J.K. Rowling" |
| "Harry Potter and the Chamber of Secrets"   | _:b0           |
| "Harry Potter and the Prisoner Of Azkaban"  | _:b0           |
| "Harry Potter and the Order of the Phoenix" | "J.K. Rowling" |
| "Harry Potter and the Half-Blood Prince"    | "J.K. Rowling" |
| "Harry Potter and the Deathly Hallows"      | "J.K. Rowling" |
----------------------------------------------------------------
```

(Results may vary depending on version of fuseki; check the results
against the input file.)

The webapp also queries this data at http://localhost:3000/books.
(Notice the webapp port is 3000, Fuseki's is 3030.)

## Webapp

In one shell (terminal session) run ``lein cljsbuild auto ``

This compiles the clojurescript and reloads code on edit.  See
[lein cljsbuild](https://github.com/emezeske/lein-cljsbuild) for
details.

In another shell (terminal session), launch the app in a local
webserver: ``$ lein ring server-headless``

Now you can open localhost:3000 in your browser.  You can make changes
in the clojure and clojurescript source, reload the page, and see the
results.  But you don't have a repl yet.

## REPL

For the Clojure REPL see `lein repl`,
[tools.nrepl](https://github.com/clojure/tools.nrepl), and
[cider](https://github.com/clojure-emacs/cider).

You do not have to use emacs.  Many Clojure hackers do, but recently
[Lighttable](http://www.chris-granger.com/lighttable/) has also become
quite popular.

For the Clojurescript REPL, See
* [The REPL and Evaluation Environments](https://github.com/clojure/clojurescript/wiki/The-REPL-and-Evaluation-Environments)
* [REPL Support](https://github.com/emezeske/lein-cljsbuild/blob/1.0.3/doc/REPL.md) for lein cljsbuild
