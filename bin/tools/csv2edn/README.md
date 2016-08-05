# csv2edn

A Clojure library designed to take a bipartite csv output (generally from a SPARQL query -- bipartite means usually that the first csv value is to be the token), compact it if necessary (i.e. combine value sets into a vector/space-separated string/ etc., if there is more than one value set associated with a "token"), and make a hash-map out of the resulting string.

Current src directory contains file listvlclplabel.clj from web app. Current version tries to combine all pos value-cluster pdgm designations into a single hash-map, 'datamap', and then write that to a file  -- doesn't work as formulated.

## Usage

FIXME

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
