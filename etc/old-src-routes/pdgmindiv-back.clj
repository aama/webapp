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
  "Note that this pdgm display routine, which gives almost all the information in a :termcluster, essentially repeats the lvalcluster parsing which is done in models.sparql/pdgmqry-sparql-gen-vrbs."
  [lvalcluster]
  (layout/common
   (let [;; parse lvalcluster string (as in sparql/pdgmqry-sparql-gen-vrbs)
         values (split lvalcluster #"," 2)
         language (first values)
         Language (capitalize language)
         sourcevcs (split (last values) #"," 2)
         srce (first sourcevcs)
         vcs (last sourcevcs)
         pos (first (split vcs #"," 2))
         mpropsvals1 (last (split vcs #"," 2))
         mpropsvals2 (split mpropsvals1 #"%" 2)
         mprops (first mpropsvals2)
         morphclass (first (split mprops #"," 2))
         propstr (last (split mprops #"," 2))
         props (if (re-find #"=" propstr) 
                 (split propstr #"," ) 
                 "")
         lexeme (if (re-find #"lexeme=" propstr) 
                  (first (split (last (split propstr #"lexeme=" 2)) #",")) 
                  (str ""))
         valstr (last mpropsvals2)

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
         ;; get dataID, lexdata, and  pdgmcmmt for display
         ;; TODO 07/18/18 (reformulate :lexemes with lpref:LEX, then new sparql
         query-sparql-lexeme (sparql/lexqry-sparql lexeme Language)
         query-sparql-lexeme-pr (clojure.string/replace query-sparql-lexeme #"<" "&lt;")
         req-lexeme (http/get aama
                       {:query-params
                        {"query" query-sparql-lexeme ;;generated sparql
                         ;;"format" "application/sparql-results+json"}})]
                         "format" "csv"}})
         ;;lexdata (split (rest (split (:body req-lexeme) #"\n")) #",")
         lexdata1 (split (:body req-lexeme) #"\r\n")
         head (first lexdata1)
         lexinfo1 (next lexdata1)
         lexinfo2 (str (next lexdata1))
         ;;lexinfo2 (clojure.string/replace lexinfo1 #"," ", ")
         ;;lexinfo3 (clojure.string/replace lexinfo1 #"\"" "'")
         lexdata2 (clojure.string/replace lexinfo2 #"[()\"\\]" "")
         lexdata3 (split  lexdata2 #"," 2)
         ;; if make the following distinctions, have problem with 
         ;; clojure.lang.PersistentVector$ChunkedSeq
         lemma (first lexdata3)
         gloss (rest lexdata3)
         query-sparql-pdgmcmmt (sparql/pdgmqry-sparql-comment lvalcluster)
         query-sparql-pdgmcmmt-pr (clojure.string/replace query-sparql-pdgmcmmt #"<" "&lt;")
         req-pdgmcmmt (http/get aama
                                {:query-params
                                 {"query" query-sparql-pdgmcmmt ;;generated sparql
                                  ;;"format" "application/sparql-results+json"}})]
                                  "format" "csv"}})
         pdgmcmmt1 (last (split (:body req-pdgmcmmt) #"\n" 2))
         pdgmcmmt2 (clojure.string/replace pdgmcmmt1 #"%%" ",")
         pdgmcmmt3 (if (re-find #"::" pdgmcmmt2)
                     (nth (split pdgmcmmt2 #"::", 2) 1)
                     "")
         ]

     [:div
      [:hr]
      [:h4 "Paradigm Properties: "]
      [:p [:em "Language: "] Language]
      [:p [:em "Source: "] srce ]
      [:p [:em "POS: "] pos ]
      [:p [:em "MorphClass: "] morphclass ]
      (if (re-find #"\w" lexeme)
        [:p [:em "Paradigm Lexeme: "] lexeme 
         [:ul 
          ;;[:li [:em "lemma, gloss: " lexinfo1 ]]
          [:li [:em "lemma: "  lemma ]]
          [:li [:em "gloss: \"" gloss "\""]]
         ]]
        [:p [:em "(No Paradigm Lexeme)"]])
      (if (re-find #"\w" propstr)
        [:p [:em "Fixed Property/Value Pairs: "] 
         [:ul
          (for [prop props]
              [:li (clojure.string/replace prop #"=" " = ")])]])
      [:p [:em  "Variable Properties: "] (clojure.string/replace valstr #"," ", ") ]
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
      [:em "Paradigm Comment: "] [:ul [:li pdgmcmmt3]]
      [:p "  "]
      [:h4 "======= Debug Info: ======="]
      [:h4#clickable "The paradigm and comment are generated by the SPARQL queries:"]
      [:pre query-sparql-pr]
      [:h4 "Query CSV Output:"]
      [:p  [:pre req2]]
      [:h4 "Comment query:"]
      [:pre query-sparql-pdgmcmmt-pr]
      [:h4 "======= Debug Info: ======="]
      ])
   [:script {:src "js/goog/base.js" :type "text/javascript"}]
   [:script {:src "js/webapp.js" :type "text/javascript"}]
   [:script {:type "text/javascript"}
    "goog.require('webapp.core');"]))

(defroutes pdgmindiv-routes
  (GET "/pdgmindiv" [] (pdgmindiv))
  (POST "/pdgmindivqry" [language] (handle-pdgmindivqry language))
  (POST "/pdgmindivdisplay" [lvalcluster] (handle-pdgmindivdisplay lvalcluster)))
