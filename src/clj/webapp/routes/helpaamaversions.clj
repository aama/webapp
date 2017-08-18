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
    [:p "The AAMA application exists in up to five versions:"
    [:ol
     [:li [:h4 "The Development Version"]
      [:p "Actual working version with many provisional and note files. Application is in ~/webapp, tools are in ~/webapp/bin/tools, and data is in ~/aama-data/data/[LANG]"]]
     [:li [:h4 "The Local Repository"]
      [:p "Vetted files from the development version are  copied to local ~/aama, a set of git repositories cloned from the " (link-to "https://github.com/aama" "AAMA") " github organization, and from there pushed to origin. Subdirectories are:"
       [:ul
        [:li "~/aama/[LANG]:" [:em " one directory/repository for each archive language"]]
        [:li "~/aama/fuseki:" [:em " current fuseki version"]]
        [:li "~/aama/jar:" [:em " jar files for aama-edn2ttl (to transform edn language files to ttl) and rdf2rdf (to transform ttl to rdf) "]]
        [:li "~/aama/tools:" [:em " aamaconfig.ttl (for fuseki queries), plus subdirs /bin (application shell scripts) and /clj (source code for edn2ttl)"]]
        [:li "~/aama/webapp:" [:em " source code for application"]]]]]
     [:li [:h4 (link-to "https://github.com/aama" "AAMA")" - the github \"organization\" of aama language and code repositories  (Origin)"]
      [:p "Code and language repositories are created here. Their content is created in the local development version, and pushed from the local repository."]]
     [:li [:h4 "One or More Remote Repositories"]
      [:p "The AAMA application, data, and webapp code can be downloaded (pulled) into other local repositories from the github AAMA organization of repositories, usually into the remote repositories' \"aama/data-repo/LANG\" directories. The instructions for installing and configuring the required software, and for downloading data, tools, and app code is outlined in " [:em "https://aama.github.io"] " For compatablility with development version, there is some rearrangement of the dirs. Currently we only provide for versions which run as a Leiningen Ring app using the downloaded webapp clojure code. The application can also be compiled into a jar file and run from there; instructions for setting up that environment are being developed."]]
     [:li [:h4 "Remote Working Versions"]
      [:p "In order not to interfere with the data pulled from the github repository, we have found it more feasible for remote working versions to index, inspect, manipulate (and eventually edit) data from a separate working version, usually in local \"aama/data/LANG\" directories, automatically copied from the \"aama/data-repo/LANG\" directories by the \"aama-pulldata.sh\" script."]]]]
   [:HR]
   [:h5 "[For more detail on the above, cf. " (link-to "http://aama.github.io" "aama.github.io") "; some older information is also available in the github aama-data/bin " (link-to "https://github.com/gbgg/aama-data/blob/master/bin/README.md" "README")"]"]]))

(defroutes helpaamaversions-routes
  (GET "/helpaamaversions" [] (helpaamaversions)))


