(ns webapp.routes.pdgm
 (:refer-clojure :exclude [filter concat group-by max min replace])
  (:require [compojure.core :refer :all]
            [csv-map.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize lower-case replace split trim]]
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
                    [:option {:value "nfv" :label "Other Verb"}]
                    [:option {:value "sel" :label "Selector"}]
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
         valclusterfile (str "pvlists/vlcl-list-" language "-" pos ".txt")
         valclusterlist (slurp valclusterfile)
         ;;valclusterlst (clojure.string/replace valclusterlist #":.*?\n" "\n")
         valclusterset (into (sorted-set) (clojure.string/split valclusterlist #"\n"))]
     (layout/common 
     ;;[:h3 "Paradigms"]
     ;;[:p "Choose Value Clusters"]
     ;;[:p error]
      [:h3 "Individual Paradigms"]
      
;;      ;; repeat of language/pdgm-type choice form doesn't seem to work
;;      (form-to [:post "/pdgmqry"]
;;               [:table
;;                [:tr [:td "PDGM Type: "]
;;                 [:td [:select#pos.required
;;                       {:title "Choose a pdgm type.", :name "pos"}
;;                       [:option {:value "verb" :label "Verb"}]
;;                       [:option {:value "pro" :label "Pronoun"}]
;;                       [:option {:value "noun" :label "Noun"}]
;;                       ]]]
;;                [:tr [:td "PDGM Language: " ]
;;                 [:td [:select#language.required
;;                       {:title "Choose a language.", :name "language"}
;;                       (for [language languages]
;;                         [:option {:value (lower-case language)} language])]]]
;;                [:tr 
;;                 [:td {:colspan "2"} [:input#submit
;;                     {:value "Value Clusters: ", :name "submit", :type "submit"}]]]])

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
                       (for [valcluster valclusterset]
                         [:option  valcluster])]]]
                ;;(submit-button "Get pdgm")
                [:tr
                 (if (re-find #"EmptyList" valclusterlist)
                   [:td (str "There are no " pos " paradigms in the " language " archive.")]
                   [:td [:input#submit
                       {:value "Paradigm: ", :name "submit", :type "submit"}]])
                   ]]))))

(defn pdgmcolred
  "Finds single-value keys"
  [pmapvec1 pmapkeys]
  (let [sgvalcol (atom #{})]
    ;; get a set of single-value keys
    (for [pcolkey pmapkeys] 
        (if (= (count (set (for [mpv pmapvec1] (pcolkey mpv)))) 1)
          (swap! sgvalcol conj pcolkey)))))

;; following seems to suppose that first item in csv will be note
;; and that last will be lex
(defn pdgmnotes
  [pdgmrows]
  (let [notes (atom [])]
    (for [pdgmrow pdgmrows]
       (let [rowcells (split pdgmrow #",")
             note (clojure.string/replace (first rowcells) #"%%" ",")]
             ;;lex (last rowcells)]
            (if-not (.contains (str @notes) note)
            (let [newnote (str  " \"" note "\"// ")]
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
        lang (lower-case language)
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        valclusterfile (str "pvlists/vlcl-list-" language "-" pos ".txt")
        valclusterlist (slurp valclusterfile)
        ;;valclusterlst (clojure.string/replace valclusterlist #":.*?\n" "\n")
        valclusterset (into (sorted-set) (clojure.string/split valclusterlist #"\n"))
        langkey (read-string (str ":" language))
        lpref (langkey lprefmap)
        vcs (split valstring #"," 2)
        ;; Note, for fv this IS pdgmType, for rest is XmorphClass
        pdgmType (first vcs)
        pvalcluster (last vcs)
        ;;valstrng (clojure.string/replace valstring #",*person|,*gender|,*number" "")
        valstr (clojure.string/replace valstring #":," ":")
        ;; In single pdgm query only, asking for note (9/29/15) and lex (10/9/15)
        query-sparql-forms (cond 
                           (= pos "pro")
                           (sparql/pdgmqry-sparql-pro language lpref valstr)
                           (= pos "sel")
                           (sparql/pdgmqry-sparql-sel language lpref valstr)
                           (= pos "noun")
                           (sparql/pdgmqry-sparql-noun language lpref valstring)
                           (re-find #"Finite" pdgmType)
                           (sparql/pdgmqry-sparql-fv language lpref pvalcluster)
                           :else (sparql/pdgmqry-sparql-nfv language lpref valstring))
        query-sparql-forms-pr (replace query-sparql-forms #"<" "&lt;")
        req-form (http/get aama
                      {:query-params
                       {"query" query-sparql-forms ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "csv"}})
        ;; find and eliminate single-value columns
        csv (:body req-form)
        phead (split (first (split csv #"\n")) #",")
        pmapkeys (into [] (for [ph phead] (keyword (trim (lower-case ph)))))
        pmapvec1 (into [] (parse-csv csv :key :keyword))
        pmap1row (first pmapvec1)
        pcolred (pdgmcolred pmapvec1 pmapkeys)
        sgvalkeys (apply clojure.set/union (into [] pcolred))
        sgvalvec (vec sgvalkeys)
        ;; make map of difference set of keys
        selkeys (into [] (clojure.set/difference (set pmapkeys) sgvalkeys))
        pmapvec2 (for [mpv pmapvec1] (select-keys mpv selkeys))
        csvred (write-csv pmapvec2)
        ;;have to make sure that there is content in each cell for drag-col
        ;;following two can probably be combined
        csvpdgm1 (clojure.string/replace csvred #",," ", ,")
        csvpdgm2 (clojure.string/replace csvred #",\n" ", \n")
        psplit (split csvpdgm2 #"\n")
        header (first psplit)
        pdgmrows (rest psplit)
        pheads (split header #",")
        ;; from here to 'pdgmcmmt', steps to obtain pdgmLabel (dataID) 
        ;; and pdgmcmmt
        pdgmmap (if (re-find #"Finite" pdgmType)
                 (read-string (slurp (str "pvlists/vlcl-dataID-" language "-fv.edn")))
                 (read-string (slurp (str "pvlists/vlcl-dataID-" language "-" pos ".edn"))))
        vlcllistkey (if (re-find #"Finite" pdgmType)
                 (read-string (str ":" (replace valstring #"," "_")))
                 (read-string (str ":" pdgmType)))
        ;; or simply include this in the previous, or next, query?
        dataID (read-string (vlcllistkey pdgmmap))
        query-sparql-pdgmcmmt (sparql/pdgmqry-sparql-comment dataID)
        query-sparql-pdgmcmmt-pr (replace query-sparql-pdgmcmmt #"<" "&lt;")
        req-pdgmcmmt (http/get aama
                      {:query-params
                       {"query" query-sparql-pdgmcmmt ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "csv"}})
        pdgmcmmt (last (split (:body req-pdgmcmmt) #"\n" 2))
        ]
         (log/info "sparql result status: " (:status req-form))
         (layout/common
           ;; insert repeat of language/pdgm-type choice form
    ;;           (form-to [:post "/pdgmqry"]
    ;;                    [:table
    ;;                     [:tr [:td "PDGM Type: "]
    ;;                      [:td [:select#pos.required
    ;;                            {:title "Choose a pdgm type.", :name "pos"}
    ;;                            [:option {:value "verb" :label "Verb"}]
    ;;                            [:option {:value "pro" :label "Pronoun"}]
    ;;                            [:option {:value "noun" :label "Noun"}]
    ;;                            ]]]
    ;;                     [:tr [:td "PDGM Language: " ]
    ;;                      [:td [:select#language.required
    ;;                            {:title "Choose a language.", :name "language"}
    ;;                            (for [language languages]
    ;;                              [:option {:value language} language])]]]
    ;;                     [:tr 
    ;;                      [:td {:colspan "2"} [:input#submit
    ;;                        {:value "Value Clusters: ", :name "submit", :type "submit"}]]]])

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
                            (for [valcluster valclusterset]
                              [:option  valcluster])]]]
                     [:tr
                      [:td [:input#submit
                            {:value "Paradigm: ", :name "submit", :type "submit"}]]]])

           ;;back to main pdgm display
           [:h3#clickable  language " / " valstring]
           [:h4 "Common Value(s):"]
           [:ul 
            (for [sgval sgvalvec]
              [:li sgval " = " (sgval pmap1row)])]
           [:h4 "Paradigm:"]
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
           [:p "Paradigm Label: "] [:ul [:li dataID]]
           [:p "Pdgmcmmt: "] [:ul [:li pdgmcmmt]]
           [:p "  "]
           [:hr]
           [:h3#clickable "Query:"]
           [:pre query-sparql-forms-pr]
           [:div [:h4 "======= Debug Info: ======="]
            [:h4 "Query Response:"]
            [:pre (:body req-form)]
            [:p "language: " language]
            [:p "lang: " lang]
            [:p "lpref: " lpref]
            [:p "pdgmType: " pdgmType]
            [:p "valstring: "  valstring]
            [:p "vlcllistkey: "  vlcllistkey]
            [:p "pdgmmap: " [:pre pdgmmap]]
            [:p ": " ]
           [:p "pcolred: " pcolred]
            [:p "pmapkeys: " (str pmapkeys)]
            [:p "sgvalkeys: " sgvalkeys]
            [:p "sgvalvec: " (str sgvalvec)]
            [:p "pmap1row: " pmap1row]
            [:p "selkeys: " (str selkeys)]
            [:p "csvred: " [:pre csvred]]
            [:h4 "============================="]]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"])))

(defroutes pdgm-routes
  (GET "/pdgm" [] (pdgm))
  (POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  (POST "/pdgmdisplay" [language valstring pos] (handle-pdgmdisplay language valstring pos))
  )
