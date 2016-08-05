# Afroasiatic Morphological Archive

## Web Application

This is a skeletal clojure webapp  demonstrating access to an RDF dataset
of  paradigmatic and other rmorphological data via a local Fuseki server. 
The webapp is intended as a test bed for applying a wide variety of SPARQL 
queries in a clojure context. 

The application  uses "live" aama data. It assumes that language data has been 
loaded into the aama RDF datastore, as described in 
[aama.github.io](http://aama.github.io), 
and that the Fuseki server has been launched from the webapp directory by the 
command: 
```clojure
$ bin/fuseki.sh
``` 
## Organization of the Code

### Clojure Libraries

As can be seen from the dependencies in ``project.clj``, ``aama-webapp``is,
like most contemporary clojure web applications,
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
the values referenced in the templates. This permits someone familiar with 
the querying of RDF datasets, but not necessariy with Clojure, to inspect the
queries, and suggest and even effect extensions and modifications to them.
 
(Analogous considerations may eventually motivate the replacement of Hiccup by
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
response and displays it as html output.

### Launching the Webapp

In one shell (terminal session) run ``$ lein cljsbuild auto ``

This compiles the clojurescript and reloads code on edit.  See
[lein cljsbuild](https://github.com/emezeske/lein-cljsbuild) for
details.

In another shell (terminal session), launch the app in a local
webserver: ``$ lein ring server-headless``

Now you can open localhost:3000 in your browser, and you will see the application's
main menu.

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
