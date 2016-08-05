(defproject edn2ttl2 "0.1.0-SNAPSHOT"
  :description "Develop routines to transform aama project edn files to well-formed ttl for loading into RDF  database"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [stencil "0.3.4"]]
  :aot [edn2ttl.core]
  :main edn2ttl.core
  ;; :target-path "target/%s"
  ;; :profiles {:uberjar {:aot :all}}
  )
