(defproject webapp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [hiccup "1.0.5"]
                 [org.clojure/tools.logging "0.3.0"]
                 [stencil "0.3.4"]
                 [csv-map "0.1.2"]
                 ;;[matsu "0.1.2"] ;; SPARQL query constructor
                 [clj-http "1.0.0"] ;; http client lib
                 [org.clojure/clojurescript "0.0-2311"]
                 [lib-noir "0.7.6"]
                 [ring-server "0.3.1"]
                 [jayq "2.5.4"]
                 ;;[clojure-csv/clojure-csv "2.0.1"]
                 ]
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
            [lein-cljsbuild "1.0.4-SNAPSHOT"]
            [hiccup-bridge "1.0.0-SNAPSHOT"]]
  :ring {:handler webapp.handler/app
         :init webapp.handler/init
         :destroy webapp.handler/destroy}
  :aot :all
  :profiles
  {:production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] 
                   [javax.servlet/servlet-api "2.5"]
                   [ring/ring-devel "1.2.1"]]}
   ;;{:plugins [[cider/cider-nrepl "0.7.0"]]}
   })
