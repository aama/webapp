(ns webapp.routes.helpaddnewlanguage
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

(defn helpaddnewlanguage []
  (layout/common 
   [:div {:class "info-page"}
    [:h3 "Help: Add New Language to Datastore"]
    [:p "This involves essentially two steps:"]
    [:ul 
     [:li [:p "First: Create a new edn file with the language data. The following short-cuts may help."
    [:ol
     [:li "Create new directory " [:em "[LANG]"] " in  " [:em "aama-data/data-new."]]
     [:li "Using  " [:em "pdgm-table-templates.docx"] "  and  " [:em "data-template.edn"] "  from  " [:em "aama-data/data-new"] " , create files  " [:em "[LANG]-pdgm-table.docx"] "  and  " [:em "[LANG]-data-template.edn"] "  in  " [:em "aama-data/data-new[LANG]."]]
     [:li "Using Word (or possibly other app with good table-creation routines) fill in table templates in  " [:em "docx"] "  file with data."]
      [:ul
       [:li "Before each table give pdgm descriptive label [NO NON-ALPHANUMERIC CHARACTERS BESIDES '-' AND '_']. source information, and vector of column property lables"]
     [:li "When satisfied with tables, transform each to csv in new file  " [:em "[LANG]-pdgm-csv.docx."]]
     [:li "Using Emacs, fill in general lang info and for each paradigm create termcluster with :label, :note,  :common property-value pairs, and :term head column-label vector in " [:em "[LANG]-data-template.edn."]]
     [:li "Copy and paste each csv under its column vector"]]
     [:li "When all data has been transfered, to edn file, run the following global replacements (NB: will be much easier if docx file already contains plain ASCII quotation marks instead of MS quotes.)"
      [:ol
       [:li "\",[sp]\"] => \"]"]
       [:li ",[sp]\",[sp] => [sp]\""]
       [:li ",[sp] => [sp]"]
       [:li "[sp]\"[sp]\"[sp] => \"[sp]\""]]]
     [:li "Run " [:em "edn2ttl.clj"] " on edn file to test for format problems [There will be some!]"]
     [:li "When satisfied, copy edn file to new file:  " [:br] [:em "aama-data/data/[LANG]/[LANG]-pdgms.edn"] "  file."]]]]
[:li [:p "Second: Incorporate the new data into the datastore"
 [:ol
  [:li "Create [LANG].ttl and [LANG].rdf and add to local datastore with command-line update shell-script: " [:br] [:em "bin/aama-datastore-update.sh ../aama-data/data/[LANG]"]]
  [:li "In " [:em "github.com/aama"] " create a new empty remote repository " [:em "aama/[LANG]"] " ."]
  [:li "Clone the new github repository into local repository " [:em "~/aama"] ", with: " [:br] [:em "git clone https://github.com/aama/[LANG].git"] [:br] "This will create a local directory " [:em "~/aama/[LANG]"] "."]
  [:li "Copy the [LANG] edn, ttl, and rdf files to local " [:em "~/aama/[LANG]"]" and remote " [:em "github.com/aama/[LANG]"] " with shell-script: " [:br] [:em "bin/aama-cp2lngrepo.sh ../aama-data/data/[LANG]"] " ." [:hr]]]]]]]))

(defroutes helpaddnewlanguage-routes
  (GET "/helpaddnewlanguage" [] (helpaddnewlanguage)))


