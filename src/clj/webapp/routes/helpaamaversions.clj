(ns webapp.routes.helpaamaversions
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))

(defn helpaamaversions []
  (layout/common 
   [:div {:class "info-page"}
     [:h3 "AAMA Versions"]
    [:p "The AAMA application exists in four versions:"
    [:ol
     [:li [:h4 "The Development Version"]
      [:p "Actual working version with many provisional and note files. Application is in ~/webapp, tools are in ~/webapp/bin/tools, and data is in ~/aama-data/data/[LANG]"]]
     [:li [:h4 "The Local Repository"]
      [:p "Files from development version which have been copied to ~/aama and are ready to be pushed to origin. Subdirectories are:"
       [:ul
        [:li "~/aama/[LANG]:" [:em "one directory for each archive language"]]
        [:li "~/aama/fuseki:" [:em "current fuseki version"]]
        [:li "~/aama/jar:" [:em "jar files for aama-edn2ttl (to transform edn language files to ttl) and rdf2rdf (to transform ttl to rdf) "]]
        [:li "~/aama/tools:" [:em " aamaconfig.ttl (for fuseki queries), plus subdirs /bin (application shell scripts) and /clj (source code for edn2ttl)"]]
        [:li "~/aama/webapp:" [:em "clojure source code for application"]]]]]
     [:li [:h4 "The github Repository (Origin)"]
      [:p "The files pushed from the local repository."]]
     [:li [:h4 "One or More Remote Repositories"]
      [:p "The AAMA application, data and webapp, as downloaded (pulled) from the github repository. For compatablility with development version, there is some rearrangement of the dirs. Currently we only provide for a version which runs as a Leiningen Ring app using the downloaded webapp clojure code."]]]]
   [:HR]
   [:h5 "[For more detail on the above, cf. " (link-to "http://aama.github.io" "aama.github.io") "; some older information is also available in the github aama-data/bin " (link-to "https://github.com/gbgg/aama-data/blob/master/bin/README.md" "README")"]"]]))

(defroutes helpaamaversions-routes
  (GET "/helpaamaversions" [] (helpaamaversions)))


