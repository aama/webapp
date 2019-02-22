(ns webapp.routes.helplistgen
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

(defn helplistgen []
  (layout/common 
   [:div {:class "info-page"}
     [:h3 "Help: List Generation"]
    [:ul
     [:li (link-to "/listmenulang" "Generate Language Lists") "." 
     [:p "This option will (re-)generate the following indices:"
      [:ol 
       [:li [:em "pvlists/menu-langs.txt"]": a sorted list of all languages in the archive for use in language-selection menus."] 
       [:li [:em "pvlists/lprefs.clj"]": a map linking each language name in the archive with the unique language prefix used in the RDF files and SPARQL queries."]
       [:li [:em "pvlists/ldomainlist.txt"]": a sorted list of all the language groupings recognized by the archive user on whatever basis (genetic, geographic, typological, project-related, etc.). It is up to the user to maintain and modify this list by hand; for comparison a genetic (language-tree) example of an ldomainlist.txt file can be consulted in the resources/public directory. By default the language indexing routine will simply update the final 'All' domain -- very useful when producing displays and indices ranging over the whole archive."]]
       [:p "To be invoked whenever a language or language-variety has been added to the archive or a language-name has been modified."]]]
     [:li (link-to "/bibIndexGen" "Generate Bibliography Keyword Indices") "."
      [:p "This option will (re-)generate the following indices:"
      [:ol 
       [:li [:em "pvlists/bibkwindex.edn"]": a map linking each keyword used in  bibrefs.edn, with a list of the associated bibref IDs."]
       [:li [:em "pvlists/bibref-master-list.txt"]": a sorted list of all the bibref IDs [used in the general bibliography menu checkbox list]."]
       [:li [:em "pvlists/bibref-keyword-list.txt"]": a sorted list of all the keywords [used in the keyword menu selection list]."]
       [:li [:em "resources/public/bibrefs-back.edn"]": a new sorted file of bibrefs -- to be substituted for old bibrefs.edn after proof-reading and emacs substitute:"
             [:ol
              [:li "'{ }' for '([ ])'"]
              [:li "']^J:' for ']] [:'"]]]]]]
     [:li (link-to "/listmenulpv" "Generate Property/Value Lists") "."
      [:p "Lists all morphosyntactic properties and values in the data store; for search for specific properties and values."]]
     [:li (link-to "/pdgmIndex" "Paradigm Value-Cluster Lists")
      [:p "This set of queries makes a list of the set of values ('Value Clusters', 'Paradigm Names') which define each paradigm in the AAMA application, along with the paradigm label(s) associated with that paradigm in the edn data-file. It then creates the following files:"
      [:ol
       [:li "A text file, pvlists/pdgm-list-LANG.txt, consisting of comma-separated value combinations existing in the various paradigms. This file is read-in to various language-specific paradigm-selection menus. List
 entry format is explained in the " (link-to "/helppdgmdata" "Paradigm Data") " help section."]
       [:li "A text file, pvlists/pdgm-table-LANG.txt, which gives a set of  all the morphological properties used in the paradigms of the language, to serve as a header for a table whose rows specify the combination of values used in each paradigm. To be set-united with headers of any other languages used in a combined property-value table of the paradigms of a set of languages. This is used in the \"property-value table\" option of multiple paradigm display."]
       [:li "A edn file, pvlists/pdgm-label-LANG.edn, which maps the paradigms' labels to the corresponding value-clusters -- used in 'Search > Form Search' to find the paradigm(s) associated with a queried form."]]]]
     [:li (link-to "/makeschemata" "Make a revised schemata section for a 'LANG-pdgms.edn' file:'")
      [:p "In the course of revising property and/or value terms in the ':termclusters' section of a 'LANG-pdgms.edn' file, the data can get out of sync with the ':schemata' section. This routine gathers all the property-value pairs in the data section of the file, and presents them in the format of the sechmata section, which can then be copied and pasted into the pdgms.edn file in place of the old schemata section."]]]
]))

(defroutes helplistgen-routes
  (GET "/helplistgen" [] (helplistgen)))


