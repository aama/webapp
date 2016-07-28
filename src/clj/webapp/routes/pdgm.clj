(ns webapp.routes.pdgm
 (:refer-clojure :exclude [filter concat group-by max min replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize lower-case replace split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pdgm []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Individual Paradigm Detail"]
   (form-to [:post "/pdgmqry"]
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

(defn handle-pdgmqry
  [language pos]
   (let [langlist (slurp "pvlists/menu-langs.txt")
         languages (split langlist #"\n")
         valclusterfile (str "pvlists/plist-" pos "-" language ".txt")
         valclusterlist (slurp valclusterfile)
         valclusters (clojure.string/split valclusterlist #"\n")]
     (layout/common 
     ;;[:h3 "Paradigms"]
     ;;[:p "Choose Value Clusters"]
     ;;[:p error]
      [:h3 "Individual Paradigms"]
      
      ;; repeat of language/pdgm-type choice form
      (form-to [:post "/pdgmqry"]
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
                [:tr 
                 [:td {:colspan "2"} [:input#submit
                      {:value "Value Clusters: ", :name "submit", :type "submit"}]]]])

      ;;main form
      (form-to [:post "/pdgmdisplay"]
               [:table
                [:tr 
                 [:td "Value Clusters for: " 
                  [:select#language.required {:title "Choose a language.", :name "language"} 
                   [:option {:value language :label (clojure.string/capitalize language)}]] " -- "
                  [:select#pos.required {:title "Choose a pdgm type.", :name "pos"} 
                   [:option {:value pos :label (clojure.string/upper-case pos)}]]]]
                [:tr 
                 [:td [:select#valstring.required
                       {:title "Choose a value.", :name "valstring"}
                       (for [valcluster valclusters]
                         [:option  valcluster])]]]
                ;;(submit-button "Get pdgm")
                [:tr
                 [:td [:input#submit
                       {:value "Paradigm: ", :name "submit", :type "submit"}]]]]))))

(defn pdgmnotes
  [pdgmrows]
  (let [notes (atom [])]
    (for [pdgmrow pdgmrows]
       (let [rowcells (split pdgmrow #",")
             note (clojure.string/replace (first rowcells) #"%%" ",")
             lex (last rowcells)]
            (if-not (.contains (str @notes) note)
            (let [newnote (str lex " : \"" note "\"// ")]
              (reset! notes newnote)))
         ))))

(defn pdgmnotes2
  [pdgmrows]
  (let [notes (ref [])]
    (for [pdgmrow pdgmrows]
       (let [rowcells (split pdgmrow #",")
             note (clojure.string/replace (first rowcells) #"%%" ",")
             lex (last rowcells)]
          (if-not (.contains (str @notes) note)
            (dosync
            (alter notes conj (str lex " : \"" note "\"// "))))))))

(defn handle-pdgmdisplay
  [language valstring pos]
  ;; send SPARQL over HTTP request
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")
        valclusterfile (str "pvlists/plist-" pos "-" language ".txt")
        valclusterlist (slurp valclusterfile)
        valclusters (clojure.string/split valclusterlist #"\n")
        Language (capitalize language)
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        lang (read-string (str ":" language))
        lpref (lang lprefmap)
        valstrng (clojure.string/replace valstring #",*person|,*gender|,*number" "")
        valstr (clojure.string/replace valstrng #":," ":")
        ;; In single pdgm query only, asking for note (9/29/15) and lex (10/9/15)
        query-sparql (cond 
                      (= pos "pro")
                      (sparql/pdgmqry-sparql-pro-note language lpref valstr)
                      (= pos "nfv")
                      (sparql/pdgmqry-sparql-nfv-note language lpref valstring)
                      (= pos "noun")
                      (sparql/pdgmqry-sparql-noun-note language lpref valstring)
                      :else (sparql/pdgmqry-sparql-fv-note language lpref valstring))
        query-sparql-pr (replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "csv"}})
        ;; I have no idea why the following works; why it is necessary
        ;; to replace \r\n by something else (here &&) in order to
        ;; split (:body req).
        pdgmstr (clojure.string/replace (:body req) #"\r\n" "&&")
        psplit (split pdgmstr #"&&")
        header (first psplit)
        pdgmrows (rest psplit)
        prow (first pdgmrows)
        pvals (split prow #",")
        note (clojure.string/replace (first pvals) #"%%" ",")
        pnotes (pdgmnotes pdgmrows)
        notelist (split (apply str pnotes) #"// ")
        pheader (split header #",")
        pnote (first pheader)
        pheads (rest pheader)
        ;;notes (atom #{})
        ]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body

           ;; insert repeat of language/pdgm-type choice form
           (form-to [:post "/pdgmqry"]
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
                     [:tr 
                      [:td {:colspan "2"} [:input#submit
                        {:value "Value Clusters: ", :name "submit", :type "submit"}]]]])

           ;;insert repeat of pdgm choice form
           (form-to [:post "/pdgmdisplay"]
                    [:table
                     [:tr 
                      [:td "Value Clusters for: " 
                       [:select#language.required {:title "Choose a language.", :name "language"} 
                        [:option {:value language :label (clojure.string/capitalize language)}]] " -- "
                       [:select#pos.required {:title "Choose a pdgm type.", :name "pos"} 
                        [:option {:value pos :label (clojure.string/upper-case pos)}]]]]
                     [:tr 
                      [:td [:select#valstring.required
                            {:title "Choose a value.", :name "valstring"}
                            (for [valcluster valclusters]
                              [:option  valcluster])]]]
                     [:tr
                      [:td [:input#submit
                            {:value "Paradigm: ", :name "submit", :type "submit"}]]]])

           ;;back to main pdgm display
           [:h3#clickable "Paradigm: " Language " / " valstring]
           [:table {:id "handlerTable" :class "tablesorter sar-table"}
           ;;[:table
            [:thead
             [:tr
              (for [head pheads]
                [:th [:div {:class "some-handle"}] head])]]
            ;;[:th head])]]
            [:tbody 
             (for [pdgmrow pdgmrows]
               [:tr
                (let [rowcells (split pdgmrow #",")
                      note (first rowcells)
                      pcells (rest rowcells)
                      ]
                  [:div
                  (for [pcell pcells]
                    [:td (clojure.string/replace pcell #"%%" ",")])])])]]
 
           ;; Note that following works only if  
           ;; webpp.sparql.pdgmqry-sparql-fv-note has ?lex as first variable
           ;; in "ORDER BY". Otherwise repeats notes for repeating lex. Has
           ;; something to do with how ".contains" works in pdgmnotes. Note
           ;; also that in pdgmnotes only reset! works (not swap! or 
           ;; ref + alter, cf pdgmnotes2)
          (if (re-find #"\w" (str note))
              [:ol "NOTE: "
              (for [note notelist]
                [:li note])]
           )
           ;;[:hr]
           [:h3 "Query Response:"]
           [:pre (:body req)]
           ;;[:pre pdgmstr]
           [:hr]
           [:h3#clickable "Query:"]
           [:pre query-sparql-pr]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defroutes pdgm-routes
  (GET "/pdgm" [] (pdgm))
  (POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  (POST "/pdgmdisplay" [language valstring pos] (handle-pdgmdisplay language valstring pos))
  )
