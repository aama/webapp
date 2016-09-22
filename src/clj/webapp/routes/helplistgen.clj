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
       [:li [:em "pvlists/bibref-keyword-list.txt"]": a sorted list of all the keywords [used in the keyword menu selection list]."]]]]
     [:li (link-to "/listmenulpv" "Generate Property/Value Lists") "."
      [:p "Lists all morphosyntactic properties and values in the data store; for search for specific properties and values."]]
     [:li (link-to "/listvlcl" "Paradigm Value-Cluster Lists")
      [:p "This set of queries makes a list of the set of values ('Value Clusters', 'Paradigm Names') which define each paradigm in the AAMA application, along with the paradigm label(s) associated with that paradigm in the edn data-file. It then creates the following files:"
      [:ol
       [:li "A text file, pvlists/vlcl-list-LANG-POS.txt, which it is read-in to the various paradigm-selection menus."]
       [:li "A edn file, pvlists/dataID-vlcl-LANG-POS.edn, which maps the dataID to the value-cluster -- used in 'Search > Form Search' to give paradigms associated with a queried form."]
       [:li "A edn file, pvlists/vlcl-dataID-LANG-POS.edn, which maps each value-cluster to the corresponding paradigm label in the data file -- used in 'Search > Paradigm Search > Single Paradigm Detail' for comments and notes on individual paradigms"]]]
      [:p "(In the case of finite verbs these values are those shared by the default person-number-gender paradigms for pronouns and person-number-gender subject agreement paradigms for finite verbs. The relevant dimensions for noun and non-finite verb are less clear, and a suitable set of comparable dimensions remains to be worked out. Note that at present, noun paradigms are recorded only exceptionally in this archive.)"]]
]]))

(defroutes helplistgen-routes
  (GET "/helplistgen" [] (helplistgen)))


