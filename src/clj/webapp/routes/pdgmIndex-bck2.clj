(ns webapp.routes.pdgmIndex
  (:refer-clojure :exclude [filter group-by max min  replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split lower-case replace join]]
            [clojure.set :refer [difference]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            [clojure.java.io :as io]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(defn pdgmIndex []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")]
    (layout/common 
     [:h3 "Create value-cluster lists and maps to and from value-clusters to input paradigm IDs"]
     (form-to [:post "/pdgmIndex-gen"]
              [:table
               [:tr [:td "PDGM Language Domain: " ]
                [:td [:select#ldomain.required
                      {:title "Choose a language domain.", :name "ldomain"}
                      [:optgroup {:label "Languages"} 
                       (for [language languages]
                         [:option {:value (lower-case language)} language])]
                      [:optgroup {:label "Language Families"} 
                       (for [ldom ldoms]
                         (let [opts (split ldom #" ")]
                           [:option {:value (last opts)} (first opts) ]))
                       [:option {:disabled "disabled"} "Other"]]]]]

              ;; [:td [:select#language.required
               ;;       {:title "Choose a language.", :name "language"}
                 ;;     (for [language languages]
                   ;;     [:option {:value (lower-case language)} language])]]]
               ;;(submit-button "Make pdgm list")
               [:tr [:td ]
                [:td [:input#submit
                      {:value "Make Data Label / PDGM Value-Cluster Map", :name "submit", :type "submit"}]]]]))))


(defn handle-pdgmIndex-gen
  [ldomain]
    ;;[:h3#clickable "Value-clusters used in " pos " pdgms for: " ldomain]
    (let [vlclvec (atom [])
          inputfile ( str "../aama-data/data/" ldomain "/" ldomain "-pdgms.edn")
          pdgmstring (slurp inputfile)
          pdgm-map (read-string pdgmstring)
          ;; The following are possible lists and tables of pdgm values
          vlcllist (str "pvlists/pdgm-index-" ldomain ".txt")
          ;; 'dataID' is the datastore's pdgm name. For the moment only the
          ;; valueCluster - dataID map is generated, to retrieve comments
          ;; about a datastore pdgm. Code is present to generate dataID - valueCluster
          ;; maps should that ever become relevant.
          dataIDvlcl (str "pvlists/dataID-vlcl-" ldomain ".edn")
          vlcldataID (str "pvlists/vlcl-dataID-" ldomain ".edn")
          ;; a sortable table of pdgm values is at present generated for
          ;; the finite verb only, the only case where there are a large number
          ;; of value combinations for a given set of pdgm categories.
          vlcltable (str "pvlists/pdgm-table-" ldomain ".txt")
          termclusters (pdgm-map :termclusters)
          ]
      (for [termcluster termclusters]
        (let [terms (termcluster :terms)
              schema (pop (first terms))
              common (termcluster :common)
              posval (common :pos)
              lexval (if (common :lexeme) (str (common :lexeme)))
              commonVSet (vals common)
              morphClassval (if (= posval :Pronoun) 
                              (common :proClass) 
                              (common :vmorphClass))
              pvstring (str (dissoc common :pos :lexeme :vmorphClass :proClass))
              pvstring1 (clojure.string/replace pvstring #"(\w) :" "$1%")
              pvstring2 (clojure.string/replace pvstring1 #"[/{/}\s]" "")
              schemastr (clojure.string/replace (apply str schema) #":" ",")
              pindex1 (str posval "," morphClassval "," pvstring2  schemastr )
              pindex2 (str (clojure.string/replace pindex1 #":" "") lexval)
              pindex3 (clojure.string/replace pindex2 #",\w*?%" ",")
              pindex4 (clojure.string/replace pindex3 #",," ",")]
          (swap! vlclvec conj pindex4)))
      ;;(spit dataIDvlcl req-dataIDvlcl)
      ;;(spit vlcldataID req-vlcldataID)
      ;;(spit vlcltable req2-table))
      (spit vlcllist (join "\n" @vlclvec))))

(defroutes pdgmIndex-routes
  (GET "/pdgmIndex" [] (pdgmIndex))
  (POST "/pdgmIndex-gen" [ldomain] (handle-pdgmIndex-gen ldomain)))


