(ns webapp.routes.pdgmindiv
  (:refer-clojure :exclude [filter concat group-by max min count])
  (:require 
   ;;[clojure.core/count :as count]
   [compojure.core :refer :all]
   [webapp.views.layout :as layout]
   [webapp.models.sparql :as sparql]
   [compojure.handler :as handler]
   [compojure.route :as route]
   ;;[clojure.string :as str]
   [clojure.string :refer [capitalize lower-case split upper-case]]
   [stencil.core :as tmpl]
   [clj-http.client :as http]
   ;;[boutros.matsu.sparql :refer :all]
   ;;[boutros.matsu.core :refer [register-namespaces]]
   [clojure.tools.logging :as log]
   [hiccup.element :refer [link-to]]
   [hiccup.form :refer :all]))


(def aama "http://localhost:3030/aama/query")

(defn pdgmindiv []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
    (layout/common 
     [:h3 "Individal Paradigm: Detail Display"]
     ;;[:p "Use this option to pick one or more  paradigms from a given language or set of languages to be displayed in fixed format vertical succession."]
     [:p "Choose Language"]
     ;; [:p error]
     ;;[:hr]
     (form-to [:post "/pdgmindivqry"]
              [:table
               [:tr [:td "PDGM Language: " ]
                [:td [:select#language.required
                      {:title "Choose a language.", :name "language"}
                      (for [language languages]
                        [:option {:value (lower-case language)} language])]]]
               [:tr [:td ]
                [:td [:input#submit
                      {:value "Get PDGM Value Clusters", :name "submit", :type "submit"}]]]]
              )
     [:hr])))

(defn handle-pdgmindivqry
  [language]
  (layout/common 
   (let [valclusterfile (str "pvlists/pdgm-index-" language ".txt")
         valclusterlist (slurp valclusterfile)
         ;;valclusterlst (clojure.string/replace valclusterlist #":.*?\n" "\n")
         valclusterset (into (sorted-set) (clojure.string/split valclusterlist #"\n"))]
     [:h3 "Individual Paradigm Detail Display"]
     [:p "Choose Value Clusters For: " language]
     ;;[:p error]
     [:hr]
     (form-to [:post "/pdgmindivdisplay"]
              [:table  {:class "linfo-table"}
               [:tr [:td "Language: "] [:td (str "   " (capitalize language))]]
               [:tr [:td "PDGM Value Clusters:  "]
                [:td [:select#lvalcluster.required
                      {:title "Choose a value.", :name "lvalcluster"}
                      (for [valcluster valclusterset]
                        [:option {:value (str language "," valcluster)} valcluster])]]]
               ;;(submit-button "Get pdgm")
               [:tr [:td]
                [:td [:input#submit
                      {:value "Display pdgm", :name "submit", :type "submit"}]]]]
              )
     )))

(defn handle-pdgmindivdisplay
  [lvalcluster]
  (layout/common
   (let [;; parse lvalcluster string (as in sparql/pdgmqry-sparql-gen-vrbs)
         vals (split lvalcluster #"," 2)
         language (first vals)
         vcs (split (last vals) #"," 2)
         pos (first vcs)
         mvalsprops (split (last vcs) #"%" 2)
         mv (first mvalsprops)
         proplex (last mvalsprops)
         morphclass (first (split mv #"," 2))
         props (if (re-find #"," mv)
                 (last (split mv #"," 2))
                 "")
         valsLex (split proplex #":" 2)
         valstr (first valsLex)
         lex (if (re-find #":" proplex)
               (last (split proplex #":" 2))
               "")
         valvec (split valstr #",")
         ;; get data
         query-sparql (sparql/pdgmqry-sparql-gen-vrbs lvalcluster)
         query-sparql-pr (clojure.string/replace query-sparql #"<" "&lt;")
         req (http/get aama
                       {:query-params
                        {"query" query-sparql ;;generated sparql
                         ;;"format" "application/sparql-results+json"}})]
                         "format" "csv"}})
         req2 (clojure.string/replace (:body req) #"%%" " + ")
         psplit (split (:body req) #"\n")
         header (first psplit)
         pdgmrows (rest psplit)
         pheads (split header #",")
         ;; get dataID and pdgmcmmt
         pdgmmap (read-string (slurp (str "pvlists/pdgm-label-" language ".edn")))
         pname (keyword (clojure.string/replace (last vals) #"," "_"))
         ;;dataID (read-string (pname pdgmmap))
         ;;query-sparql-pdgmcmmt (sparql/pdgmqry-sparql-comment dataID)
         ;;query-sparql-pdgmcmmt-pr (clojure.string/replace query-sparql-pdgmcmmt #"<" "&lt;")
         ;;req-pdgmcmmt (http/get aama
         ;;                       {:query-params
         ;;                        {"query" query-sparql-pdgmcmmt ;;generated sparql
         ;;                         ;;"format" "application/sparql-results+json"}})]
         ;;                         "format" "csv"}})
         ;;pdgmcmmt1 (last (split (:body req-pdgmcmmt) #"\n" 2))
         ;;pdgmcmmt2 (clojure.string/replace pdgmcmmt1 #"%%" ",")
         ]

     [:div
      [:hr]
      [:h4 "Paradigm Properties: "]
      [:p [:em "Language: "] (capitalize language)]
      [:p [:em "POS: "] pos ]
      [:p [:em "MorphClass: "] morphclass ]
      (if (re-find #"\w" props)
        [:p [:em "Fixed Property/Value Pairs: "] 
         [:ul
          (for [prop (split props #",")]
            [:li (clojure.string/replace prop #"=" " = ")])]])
      [:p [:em  "Variable Properties: "] (clojure.string/replace valstr #"," ", ") ]
      (if (re-find #"\w" lex)
        [:p [:em "Lexeme: "] lex ])
      [:hr]
      
      [:h4 "Paradigm: "]
      ;;[:pre (:body req)]
      [:p [:em "Click on column to sort (multiple sort by holding down shift key)."] [:br]
       [:em  "Columns can be dragged by clicking and holding on 'drag-bar' 
                at top of column."]]
      [:table {:id "handlerTable" :class "tablesorter sar-table"}
       ;;[:table
       [:thead
        (for [head pheads]
          [:th [:div {:class "some-handle"}  (capitalize head)]])
        ]
       ;;[:th head])]]
       [:tbody 
        (for [pdgmrow pdgmrows]
          [:tr
           (let [rowcells (split pdgmrow #",")]
             [:div
              (for [rowcell rowcells]
                [:td (clojure.string/replace rowcell #"%%" ",")])])])]]


      [:p]
      ;;[:hr]
      [:p]
      ;;[:p "vals: " vals] 
      ;;[:p "pname: " pname]
      ;;[:em "Paradigm Label: "] [:ul [:li dataID]]
      ;;[:em "Paradigm Comment: "] [:ul [:li pdgmcmmt2]]
      [:p "  "]
      [:hr]
      [:h4#clickable "This paradigm is generated by the SPARQL query:"]
      [:pre query-sparql-pr]
      [:h4 "Query CSV Output:"]
      [:p  [:pre req2]]
      

      ;;[:h4 "======= Debug Info: ======="]
      ;;[:h3#clickable "Query:"]
      ;;[:pre query-sparql-pr]
      ;;[:p "req2: " [:pre req2]]
      ;;[:p "==========================="]
      ])
 
   [:script {:src "js/goog/base.js" :type "text/javascript"}]
   [:script {:src "js/webapp.js" :type "text/javascript"}]
   [:script {:type "text/javascript"}
    "goog.require('webapp.core');"]))

(defroutes pdgmindiv-routes
  (GET "/pdgmindiv" [] (pdgmindiv))
  (POST "/pdgmindivqry" [language] (handle-pdgmindivqry language))
  (POST "/pdgmindivdisplay" [lvalcluster] (handle-pdgmindivdisplay lvalcluster)))
