(ns webapp.routes.valclmod
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require 
            ;;[clojure.core/count :as count]
            [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize lower-case split upper-case join]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))


(def aama "http://localhost:3030/aama/query")

(defn valclmod []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Create Prop-x-Val Table"]
   (form-to [:post "/listvalclmod-gen"]
            [:table
             [:tr [:td "PDGM Type: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "fv" :label "Finite Verb"}]
                    [:option {:value "nfv" :label "Non-finite Verb"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:value "noun" :label "Noun"}]
                    ]]]
             [:tr [:td "PDGM Language: " ]
              [:td [:select#language.required
                    {:title "Choose a language.", :name "language"}
                    (for [language languages]
                        [:option {:value (lower-case language)} language])]]]
             ;;(submit-button "Get pdgm")
             [:tr 
              [:td {:colspan "2"} [:input#submit
                    {:value "Value Clusters: ", :name "submit", :type "submit"}]]]]
            )
   [:p])))

(defn normorder
  "Takes property list output by listlgpr-sparql-POS and returns string with properties in (partial) order specified by porder."
  [pstring porder]
  (let [
        pordervec (split porder #",")
        pstringvec (split pstring #",")
        diffset (clojure.set/difference (set pstringvec) (set pordervec))
        diffvec (into [] diffset)]
    (str porder "," (join "," diffvec))))

(defn handle-listvalclmod-gen
  [language pos]
  (layout/common
   [:body
    ;;[:h3#clickable "Value-clusters used in " pos " pdgms for: " language]
      (let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))
            lang (read-string (str ":" language))
            lpref (lang lprefmap)
            ;; send SPARQL over HTTP request
            outfile (str "pvlists/vlcl-list-" language "-" pos ".txt")
            query-sparql1 (cond 
                           (= pos "pro")
                           (sparql/listlgpr-sparql-pro language lpref)
                           (= pos "nfv")
                           (sparql/listlgpr-sparql-nfv language lpref)
                           (= pos "noun")
                           (sparql/listlgpr-sparql-noun language lpref)
                           (= pos "fv")
                           (sparql/listlgpr-sparql-fv language lpref))
            query-sparql1-pr (clojure.string/replace query-sparql1 #"<" "&lt;")
            req1 (http/get aama
                          {:query-params
                           {"query" query-sparql1 ;;generated sparql
                            "format" "csv"}})
                            ;;"format" "application/sparql-results+json"}})
                            ;;"format" "text"}})
            propstring (if (= (:body req1) "property")
                         (str "no_" pos)
                         (clojure.string/replace (:body req1) #"\r\n" ","))
            ;;propstring2 (clojure.string/replace propstring1 #"^property," "")
            pstring (clojure.string/replace propstring #"property,|,$" "")
            porder (str "conjClass,tam,polarity,rootClass")
            normstring (normorder pstring porder)
            ;;plist (clojure.string/replace pstring #"," ", ")
            query-sparql2 (cond 
                           (= pos "pro")
                           (sparql/listvlcl-sparql-pro language lpref propstring)
                           (= pos "nfv")
                           (sparql/listvlcl-sparql-nfv language lpref propstring)
                           (= pos "noun")
                           (sparql/listvlcl-sparql-noun language lpref propstring)
                           :else (sparql/listvlcl-sparql-fv language lpref pstring))
            query-sparql2-pr (clojure.string/replace query-sparql2 #"<" "&lt;")
            req2 (http/get aama
                           {:query-params
                            {"query" query-sparql2 ;;generated sparql
                             ;;"format" "application/sparql-results+json"}})
                             "format" "csv"}})
            body (:body req2)
            brows (split body #"\r\n")
            header (first brows)
            headers (split header #",")
            valrows (rest brows)
            ]
        (log/info "sparql result status: " (:status req2))
        ;;(spit outfile req4-out)
        [:div
         [:p [:b "Language: "] language]
         [:p [:b "File:     "] outfile]
         [:p [:b "Propstring: " ] propstring]
         [:p [:b "Pstring: " ] pstring]
         [:p [:b "Porder:  " ] porder]
         [:p [:b "Normstring: "] normstring]
         [:h4  "Value Clusters: " ]
         [:table {:id "handlerTable" :class "tablesorter sar-table"}
          [:thead
           [:tr
            [:th [:div {:class "some-handle"} [:br] "PDGM"]]
            (for [head headers]
              [:th [:div {:class "some-handle"} [:br] head]])]]
          [:tbody 
           (for [valrow valrows]
             [:tr
              [:td 
               [:div {:class "form-group"}
                [:label
                 (check-box {:name "pcell" :value "PCELL"} "pcell") "pcell"]]]

              ;;[:td "CHECKBOX"]
              (let [valcells (split valrow #",")]
                (for [valcell valcells]
                  [:td valcell]))])]]
           [:h4 "======= Debug Info: ======="]
           [:h3#clickable "Query1:"] 
           [:pre query-sparql1-pr] 
         [:p "Query Input (:body req1): "
          [:pre (:body req1)]]
           [:h3#clickable "Query2:"] 
           [:pre query-sparql2-pr] 
           [:hr] 
           [:p "Query Output (:body req2): " 
            [:pre (:body req2)]]
           [:p "==========================="]])]
  [:script {:src "js/goog/base.js" :type "text/javascript"}]
  [:script {:src "js/webapp.js" :type "text/javascript"}]
  [:script {:type "text/javascript"}
   "goog.require('webapp.core');"]))


(defroutes valclmod-routes
  (GET "/valclmod" [] (valclmod))
  (POST "/listvalclmod-gen" [language pos] (handle-listvalclmod-gen language pos)))
