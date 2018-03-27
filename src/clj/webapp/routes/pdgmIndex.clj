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
     [:h3 "Create value-cluster lists"]
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
               ;; Can't find out why following doesn't work:
               ;; [:td [:select#language.required
               ;;       {:title "Choose a language.", :name "language"}
               ;;     (for [language languages]
               ;;     [:option {:value (lower-case language)} language])]]]
               ;;(submit-button "Make pdgm list")
               [:tr [:td ]
                [:td [:input#submit
                      {:value "PDGM Value-Cluster List", :name "submit", :type "submit"}]]]]))))

(defn makeplist
  [termclusters]
    (for [termcluster termclusters]
      (let [terms (termcluster :terms)
            schema (pop (first terms))
            common (into (sorted-map) (termcluster :common))
            posval (common :pos)
            lexval (common :lexeme)
            commonVSet (vals common)
            morphClassval (cond 
                           (= posval :Pronoun) 
                            (common :proClass) 
                            (= posval :Noun)
                            (common :nmorphClass)
                            :else
                            (common :vmorphClass))
            pvstring (str (dissoc common :pos :lexeme :vmorphClass :nmorphClass :proClass))
            pvstring1 (clojure.string/replace pvstring #"(\w) :" "$1=")
            pvstring2 (clojure.string/replace pvstring1 #"[/{/}\s]" "")
            schemastr (clojure.string/replace (apply str schema) #":" ",")
            pindex1 (str posval "," morphClassval "," pvstring2 "%" schemastr )
            ;;pindex2 (clojure.string/replace pindex1 #":" "")
            ;; if want to take :lexeme out of common
            pindex2 (str (clojure.string/replace pindex1 #":" "") lexval)
            ;; for next two, adjust N on pindexN acc. to choice
            ;; if want to have Prop:Val in schema
            ;;pindex3 (clojure.string/replace pindex2 #",(\w*?)=" ",$1:")
            ;; if want only Val in schema
            ;;pindex3 (clojure.string/replace pindex2 #",\w*?=" ",")
            ]
            (clojure.string/replace pindex2 #",*%,*" "%"))))

(defn handle-pdgmIndex-gen
  [ldomain]
  ;;[:h3#clickable "Value-clusters used in " pos " pdgms for: " ldomain]
  (let [inputfile ( str "../aama-data/data/" ldomain "/" ldomain "-pdgms.edn")
        pdgmstring (slurp inputfile)
        pdgm-map (read-string pdgmstring)
        ;; The following are possible lists and tables of pdgm values
        vlcllist (str "pvlists/pdgm-index-" ldomain ".txt")
        termclusters (:termclusters pdgm-map)
        vlclvec1 (makeplist termclusters)
        vlclvec2 (join "\n" (into (sorted-set) vlclvec1))
        ]
    (spit vlcllist vlclvec2)
    (layout/common
     [:body
      [:div 
       [:p [:b "Ldomain: "] ldomain]
       [:p [:b "File vlcllist:    "] [:pre vlcllist]]
       [:p [:b "File vlclvec1:    "] [:p vlclvec1]]
       [:p [:b "File vlclvec2:    "] [:p vlclvec2]]
       [:p "==========================="]]
     [:script {:src "js/goog/base.js" :type "text/javascript"}]
     [:script {:src "js/webapp.js" :type "text/javascript"}]
     [:script {:type "text/javascript"}
      "goog.require('webapp.core');"]])
    ))

(defroutes pdgmIndex-routes
  (GET "/pdgmIndex" [] (pdgmIndex))
  (POST "/pdgmIndex-gen" [ldomain] (handle-pdgmIndex-gen ldomain)))


