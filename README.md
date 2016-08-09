# Afroasiatic Morphological Archive

## Web Application

This is a skeletal Clojure webapp  demonstrating access to an 
[Resource Description Framework (RDF)](https://www.w3.org/RDF/) dataset
of  paradigmatic and other morphological data via a local RDF server, 
in our case [Apache Jena Fuseki](http://jena.apache.org/index.html).
The webapp is intended as a test bed for applying a wide variety of 
[SPARQL](http://www.w3.org/TR/rdf-sparql-query/) 
queries about morphological data in a Clojure context. 

The application  uses "live" aama data. It assumes that: 
1.  Language data in
[Extensible Data Notation (edn)](https://github.com/edn-format/edn) format
has been downloaded from one or more of the aama language repositories, 
2.  transformed into [ttl/rdf-xml](http://www.w3.org/TR/turtle/) format, and
3.  loaded into the Fuseki datastore, 

as described in [aama.github.io](http://aama.github.io), 
and that the Fuseki server has been launched by the 
command: 
```clojure
aama $ tools/bin/fuseki.sh
``` 
## Organization of the Code

### Clojure Libraries

As can be seen from the dependencies in ``project.clj``, ``aama-webapp``is,
like most [Leiningen](http://leiningen.org)-managed Clojure web applications,
based on the [Ring](https://github.com/ring-clojure/ring) 
web application library, complemented by the
[Compojure](https://github.com/weavejester/compojure) routing library; 
[clj-http](https://github.com/dakrone/clj-http) is its http client library. 

[Hiccup](https://github.com/weavejester/hiccup) is used here to represent
 HTML directly in Clojure. However, although
a similar direct representation of SPARQL queries in Clojure could be accomplished
by libraries such as [Matsu](https://github.com/boutros/matsu),
 we have preferred to handle these queries as
a collection of SPARQL templates, using the library
 [Stencil](https://github.com/davidsantiago/stencil) to instantiate
the values referenced in the templates. This makes it easier for
 someone familiar with 
the querying of RDF datasets, but not necessariy with Clojure, to inspect the
Extensible 
		  Data Notation queries, and suggest and even effect extensions and modifications to them.
 
(Analogous considerations, i.e., input from someone familiar with webpage design 
but not with Clojure, may eventually motivate the replacement of Hiccup by
a templating approach to HTML such as [Enlive](https://github.com/cgrand/enlive) or 
[Selmer](https://github.com/yogthos/Selmer).)

### Application Code

The organization of the application code itself follows a rather consistent pattern.
As usual in a Compojure-based application,  the various parts are held together
by by a small set of functions in a ``webapp.handler`` namespace file. 
The application background menu, as well as the basic page layout and invocation 
of the various javascript 
and css resources are taken care of in ``webapp.views.layout``.

The very large number of help, utility, and information-requesting pages are each
represented by a ``webapp.routes.[NS]`` file. These information-requesting
files are of a very uniform structure, familiar to almost any database application
in whatever context: almost every one presents an HTML form, with 
information (language, morphosyntactic categories, etc.) to be supplied by 
selection-lists, check-boxes, and text-input areas. The requested information
is passed to a handler function, which uses it to formulate a SPARQL query 
from one of the templates in ``webapp.models.sparql``,  submits the request to
the fuseki datastore via ``clj-http.client/get``, and finally parses the 
response, formats it, and displays both it, and, where feasible, the query,
 as html output.

### Launching the Webapp

Cloning the aama/webapp repository, 
as per [aama.github.io](http://aama.github.io), 
will have downloaded both the source-code and a stand-alone jar file.
 The application can be run in the webapp directory using either:

1. From the downloaded sorce-code,  using [Leiningen](http://leiningen.org):

    In one shell (terminal session) run 
    ```clojure
    aama/webapp $ lein cljsbuild auto 
    ```
    This compiles the Clojurescript and reloads code on edit.  See
    [lein cljsbuild](https://github.com/emezeske/lein-cljsbuild) for
    details. [Note that at present use of cljs is at best marginal in
    this application.]

    In another shell (terminal session), launch the app in a local
    webserver:
    ```clojure
    aama/webapp $ lein ring server-headless
    ```

2. As a Java application from the jar file to be  found in the webapp directory, 
with the command: 
    ```java
    aama/webapp $ java -jar aama-webapp.jar
    ```

In either case,  you can then open localhost:3000 in your browser, 
and you will see the application's main menu.

### The REPL

For the Clojure REPL see `lein repl`,
[tools.nrepl](https://github.com/clojure/tools.nrepl), and
[cider](NNNNhttps://github.com/clojure-emacs/cider).

You do not have to use emacs.  Many Clojure hackers do, but recently
[Lighttable](http://www.chris-granger.com/lighttable/) has also become
quite popular.

For the Clojurescript REPL, See
* [The REPL and Evaluation Environments](https://github.com/clojure/clojurescript/wiki/The-REPL-and-Evaluation-Environments)
* [REPL Support](https://github.com/emezeske/lein-cljsbuild/blob/1.0.3/doc/REPL.md) for lein cljsbuild
