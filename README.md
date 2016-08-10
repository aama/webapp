# Afroasiatic Morphological Archive

## Web Application

This is a skeletal Clojure webapp  demonstrating access to an 
[Resource Description Framework (RDF)](https://www.w3.org/RDF/) dataset
of  paradigmatic and other morphological data via a local RDF server, 
in our case [Apache Jena Fuseki](http://jena.apache.org/index.html).
The webapp is intended as a test bed for applying a wide variety of 
[SPARQL](http://www.w3.org/TR/rdf-sparql-query/) 
queries about morphological data in a Clojure context. 

We have chosen to write the application in the LISP dialect 
[Clojure](http://clojure.org/index) in part because we find 
its functional structure congenial and conducive
to insights into the problem at hand, and in part 
because of the formidable and constantly growing set of libraries 
created by its very involved community of users. 
However, as we note in [aama.github.io](http://aama.github.io),  
essentially the same basic functionality could be achieved 
by any software framework which can provide a web interface for handling
SPARQL queries submitted to an  RDF datastore.

The application  uses "live" aama data. It assumes that: 

1.   language data in
[Extensible Data Notation (edn)](https://github.com/edn-format/edn) format
has been downloaded from one or more of the aama language repositories, 
2.   transformed into [ttl/rdf-xml](http://www.w3.org/TR/turtle/) format
using the appropriate shell script in ``aama/tools/bin``, and
3.   loaded into the Fuseki datastore, 

following the process described in [aama.github.io](http://aama.github.io); 
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
SPARQL query templates, and suggest and even effect extensions and modifications to them.
 
(Analogous considerations, i.e., input from someone familiar with webpage design 
but not with Clojure, may eventually motivate the replacement of Hiccup by
a templating approach to HTML such as [Enlive](https://github.com/cgrand/enlive) or 
[Selmer](https://github.com/yogthos/Selmer).)

### Application Code [src/clj/webapp/]

The organization of the application code itself follows a rather consistent pattern.
As usual in a Compojure-based application,  the various parts are held together
by a small set of functions in a ``webapp.handler`` namespace file. 
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

## Launching the Webapp

Cloning the aama/webapp repository, 
as per [aama.github.io](http://aama.github.io), 
will have downloaded both the source-code and a stand-alone jar file.
 The application can be run in the webapp directory either:

1. From the downloaded sorce-code,  using [Leiningen](http://leiningen.org):

    Enter the following Leiningen command in a shell (terminal session) 
    to launch the app in a local webserver:
    ```clojure
    aama/webapp $ lein ring server-headless
    ```

    [Note that at present use of 
    [Clojurescsript(cljs)](https://github.com/clojure/clojurescript) 
    is at best marginal in
    this application. However we are currently experimenting with the
    transfer of at least some js functionality to cljs, and to the extent
    that this happens, the user, in another shell, will want to run:
    ```clojure
    aama/webapp $ lein cljsbuild auto 
    ```
    which will compile the Clojurescript and reload code on edit.  See
    [lein cljsbuild](https://github.com/emezeske/lein-cljsbuild) for
    details.]


2. As a Java application from the jar file to be  found in the webapp directory, 
with the command: 
    ```java
    aama/webapp $ java -jar aama-webapp.jar
    ```

In either case,  you can then open localhost:3000 in your browser, 
and you will see the application's main menu. Go immediately to the
```html
Help > Initialize Application
``` 
menu item to generate the application-specific
lists and indices used in menus, queries, and displays (otherwise on 
most pages you will not see anything).

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
