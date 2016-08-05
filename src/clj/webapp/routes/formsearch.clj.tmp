(ns webapp.routes.formsearch
 (:refer-clojure :exclude [filter concat group-by max min])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize lower-case upper-case split join]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn formsearch []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Language-Property-Value Cooccurrences"]
   [:h4 "Choose Language Domain and Enter qstring: prop=Val,...prop=?prop,..."]
   (form-to [:post "/formdisplay"]
            [:table
             [:tr [:td "Language(s) to be Queried: " ]
              [:td 
               {:title "Choose one or more languages.", :name "language"}
               (for [language languages]
                 ;;[:option {:value (lower-case language)} language])]]]
                 [:div {:class "form-group"}
                  [:label 
                   (check-box {:name "languages[]" :value (lower-case language)} language) language]])]]
             [:tr [:td "Query List: " ]
              [:td 
               (text-field 
                {:placeholder "person=Person2,gender=Fem,pos=?pos,number=?number"} 
                "qstring") ]]
             [:tr [:td "RE Form Filter: " ]
              [:td
               (text-field {:placeholder ""} "filter")]]
              ;;(submit-button "Get values")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get Queried Forms ", :name "submit", :type "submit"}]]]]))))

(defn csv2formtable
"Takes sorted n-col csv list with vectors of headers, and outputs n-col html table."
 [heads formstr]
(let  [formrows (split formstr #"\r\n")]
  [:div
   (form-to [:post "/formpvlist"]
   [:table {:id "handlerTable" :class "tablesorter sar-table"}
    [:thead
     [:tr
      (for [head heads]
        [:th [:div {:class "some-handle"}[:br] (upper-case head)]])]]
    [:tbody 
     (for [formrow formrows]
       (let [formrow1 (clojure.string/replace formrow #"(.*),(.*?,.*?)$" "$1%%$2")
             formrow2 (split formrow1 #"%%")
             qprops (split (first formrow2) #",")
             language (first qprops)
             tokenID (last formrow2)
             dataID (first (split tokenID #","))
             token (last (split tokenID #","))]
         [:tr
           (for [qprop qprops]
            [:td qprop])
          [:td dataID]
          [:td 
           [:label (str token " : ") (check-box {:name "tokenIDs[]" :value (str language "%%" tokenID)} token) ]]]))
     [:tr
      (for [x (range 1 (count heads) )]
            [:td])
          [:td
           [:input#submit {:value "Show", :name "submit", :type "submit"}]]]]])]))

(defn handle-formdisplay
  "This version has form for parallel display of tokens."
  [languages qstring filter]
  ;; send SPARQL over HTTP request
  (let [query-sparql (sparql/prvllg-sparql languages qstring filter)
        query-sparql-pr (clojure.string/replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        ;;"format" "text"}})]
                        "format" "csv"}})
        csvstring (:body req)
        csvstr (split csvstring #"\r\n" 2)
        ;; csvstr is a string of comma-separated cells, with rows separated by \r\n 
        ;; Take off the top header
        headers (first csvstr)
        heads (split headers #",")
        formstring (last csvstr)
        formstring2 (clojure.string/replace formstring #"\r\n$" "")
        ;; the following is to strip the lang name from the pdgmLabel
        ;;formstring3  (clojure.string/replace formstring2 #"(\r\n.*,.*),.*?-(.*,)" "$1,$2")
        formtable (csv2formtable heads formstring2)]
    (log/info "sparql result status: " (:status req))
    (layout/common
     [:body
      [:h3#clickable "Language-Property-Values: " ]
      [:p [:h4 "Language Domain: "]
       [:em (str languages)]]
      [:p [:h4 "Query String: "]
       [:em qstring]]
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
      [:p "Click in check-box next to token for complete MS analysis of form."]
      [:hr]
      formtable
      [:hr]
      [:div [:h4 "======= Debug Info: ======="]
       [:p "heads: "]
       [:p [:pre heads]]
       [:p "formstring2: "]
       [:p [:pre formstring2]]
       [:p "Query: "]
       [:p [:pre query-sparql-pr]]
       [:p "Response: "]
       [:p [:pre csvstring]]
       [:h4 "==========================="]]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))

(defn handle-formpvlist
  [tokenIDs]
  (layout/common
   [:body
    [:p [:h3 "Form Property-Value Table: "] ]
    [:hr]
    (form-to [:post "/pidseqdisplay"]
             [:table 
              [:thead
               [:tr
                [:th  [:br] "LANGUAGE"]
                [:th  [:br] "PDGM-TYPE" ]
                [:th  [:br] "TOKEN" ]
                [:th  [:br]  "DATA ID" ]
                [:th  [:br]  "MS VALUES" ]
                [:th  [:br]  "PARADIGMS" ]
                ;;[:th "qtype1"] [:th "pos1" ] [:th "ptype"]
                ;;[:th  [:br]  "query"]
                ]]
              [:tbody 
               (for [tokenID tokenIDs]
                 [:tr
                  (let [ldata (split tokenID #"%%" 2)
                        language (first ldata)
                        tknID (last ldata)
                        dataID (first (split tknID #","))
                        token (last (split tknID #","))
                        query-sparql-pos (sparql/formpos-sparql tknID)
                        req-pos (http/get aama
                                      {:query-params
                                       {"query" query-sparql-pos
                                        "format" "csv"}})
                        pos (clojure.string/replace (:body req-pos) #"^.*?\r\n" "")
                        pos1 (clojure.string/replace pos #"\r\n" "")
                        query-sparql-ptype (sparql/formptype-sparql tknID)
                        req-ptype (http/get aama
                                            {:query-params
                                             {"query" query-sparql-ptype
                                              "format" "csv"}})
                        qptype (clojure.string/replace (:body req-ptype) #"^.*?\r\n" "")
                        qptype1 (clojure.string/replace (:body req-ptype) #"\r\n" "")
                        ptype (cond
                               (= pos1 "Pronoun")
                               "pro"
                               (= pos1 "Noun")
                               "noun"
                               (= pos1 "Verb")
                               (if (= qptype1 "_askResulttrue")
                                 "fv"
                               "nfv")
                               )
                        query-sparql-pv (sparql/formpv-sparql tknID)
                        req-pv (http/get aama
                                      {:query-params
                                       {"query" query-sparql-pv ;;generated sparql
                                        "format" "text"}})
                        pdgmmap (read-string (slurp (str "pvlists/dataID-pdgm-" (lower-case language) "-" ptype ".edn")))
                        dataIDkey (read-string (str ":" dataID))
                        pdgmstr (dataIDkey pdgmmap)
                        pdgms (split pdgmstr #" ")
                        ]
                    [:div
                     [:td language]
                     [:td ptype]
                     [:td token]
                     [:td dataID] 
                     [:td [:pre (:body req-pv)]]
                     [:td 
                      (for [pdgm pdgms]
                       [:label (str pdgm ": ") (check-box {:class "checkbox1" :name "pdgms[]" :value (str language "+" ptype "%%" pdgm)} pdgm)])]
                    ;;[:td [:pre qptype1]]
                    ;;[:td [:pre pos1]]
                    ;;[:td [:pre ptype ]] 
                     ])])
               [:tr [:td ][:td][:td][:td][:td]
                [:td [:input#submit
                      {:value "Display pdgms", :name "submit", :type "submit"}]]]]])
    [:hr]
    [:p " "]
    [:div [:h4 "======= Debug Info: ======="]
     [:p "tokenIDs: " [:pre tokenIDs]]
     [:p "tokenIDs: " (str tokenIDs)]
     [:p "==========================="]]
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]]))

(defn handle-pidseqdisplay
 [pdgms]
 (layout/common
  (let 
      [lprefmap (read-string (slurp "pvlists/lprefs.clj"))]
    (for [pdgm pdgms]
      (let [vals (split pdgm #"%%")
            lgptype (split (first vals) #"\+")
            language (lower-case (first lgptype))
            ptype (last lgptype)
            vcluster (last vals)
            lang (read-string (str ":" language))
            lpref (lang lprefmap)
            valstrng (clojure.string/replace vcluster #",*person|,*gender|,*number" "")
            valstr (clojure.string/replace valstrng #":," ":")
            query-sparql (cond 
                          (= ptype "pro")
                          (sparql/pdgmqry-sparql-pro language lpref valstr)
                          (= ptype "nfv")
                          (sparql/pdgmqry-sparql-nfv language lpref vcluster)
                          (= ptype "noun")
                          (sparql/pdgmqry-sparql-noun language lpref vcluster)
                          :else (sparql/pdgmqry-sparql-fv language lpref vcluster))
            query-sparql-pr (clojure.string/replace query-sparql #"<" "&lt;")
            req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})
            req2 (clojure.string/replace (:body req) #"%%" " + ")
            ]
        [:div
         [:hr]
         [:h4 "Pdgm: "]
         [:p (str language ": " vcluster )]
         ;;[:pre (:body req)]
         [:pre req2]
         [:hr]
         [:h3#clickable "Query:"]
         [:pre query-sparql-pr]
        ])))
        [:script {:src "js/goog/base.js" :type "text/javascript"}]
        [:script {:src "js/webapp.js" :type "text/javascript"}]
        [:script {:type "text/javascript"}
         "goog.require('webapp.core');"]))

(defn handle-multiseqdisplay-org
 [valclusters pos]
 (layout/common
  (let 
      [lprefmap (read-string (slurp "pvlists/lprefs.clj"))]
    (for [valcluster valclusters]
      (let [vals (split valcluster #"," 2)
            language (first vals)
            vcluster (last vals)
            lang (read-string (str ":" language))
            lpref (lang lprefmap)
            valstrng (clojure.string/replace vcluster #",*person|,*gender|,*number" "")
            valstr (clojure.string/replace valstrng #":," ":")
            query-sparql (cond 
                          (= pos "pro")
                          (sparql/pdgmqry-sparql-pro language lpref valstr)
                          (= pos "nfv")
                          (sparql/pdgmqry-sparql-nfv language lpref vcluster)
                          (= pos "noun")
                          (sparql/pdgmqry-sparql-noun language lpref vcluster)
                          :else (sparql/pdgmqry-sparql-fv language lpref vcluster))
            query-sparql-pr (clojure.string/replace query-sparql #"<" "&lt;")
            req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})
            req2 (clojure.string/replace (:body req) #"%%" " + ")
            ]
        [:div
         [:hr]
         [:h4 "Valcluster: " valcluster]
         ;;[:pre (:body req)]
         [:pre req2]
         ;;[:hr]
         ;;[:h3#clickable "Query:"]
         ;;[:pre query-sparql-pr]
        ])))
        [:script {:src "js/goog/base.js" :type "text/javascript"}]
        [:script {:src "js/webapp.js" :type "text/javascript"}]
        [:script {:type "text/javascript"}
         "goog.require('webapp.core');"]))

(defroutes formsearch-routes
  (GET "/formsearch" [] (formsearch))
  (POST "/formdisplay" [languages qstring filter] (handle-formdisplay languages qstring filter))
  (POST "/formpvlist" [tokenIDs] (handle-formpvlist tokenIDs)) 
  (POST "/pidseqdisplay" [pdgms] (handle-pidseqdisplay pdgms))  )


