(defproject webapp "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [matsu "0.1.2"] ;; SPARQL query constructor
                 [clj-http "1.0.0"] ;; http client lib
                 [org.clojure/tools.logging "0.3.0"]
                 [org.clojure/clojurescript "0.0-2311"]]
                 ;; [org.clojure/clojurescript "0.0-2197"]]

  :source-paths ["src/clj"]

  :cljsbuild {
    :builds [{:id "webapp"
              :source-paths ["src/cljs"]
              :compiler {
                :output-to "resources/public/js/webapp.js"
                :output-dir "resources/public/js/"
                :optimizations :none
                :source-map true}}]}

  :plugins [[lein-ring "0.8.11"]
            ;; [lein-cljsbuild "1.0.3"]]
            [lein-cljsbuild "1.0.4-SNAPSHOT"]]

  :ring {:handler webapp.core/app}

  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
