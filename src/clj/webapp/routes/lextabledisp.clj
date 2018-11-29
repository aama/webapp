(ns webapp.routes.lextabledisp
  (:refer-clojure :exclude [filter group-by max min  replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split lower-case replace join capitalize]]
            [clojure.set :refer [difference]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            [clojure.java.io :as io]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(defn lextabledisp []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
    (layout/common 
     [:h3 "Display lexemes:"]
     [:p "Choose Language"]
     (form-to [:post "/makelextable"]
              [:table
               [:tr [:td "Lexeme for  Language: " ]
                [:td [:select#language.required
                      {:title "Choose a language.", :name "language"}
                      (for [language languages]
                        [:option {:value (lower-case language)} language])]]]
               [:tr [:td ]
                [:td [:input#submit
                      {:value "Get Lexeme Table", :name "submit", :type "submit"}]]]]
              )
     [:hr])))

(defn makelextablerows
  [lexemes propheadvec]
  (let [lexitems (into [] (keys lexemes))
        ]
    ;;parse each pdgm name
    (for [lex lexitems]
      (let [props (lex lexemes)
            pos (replace (:pos props) #":" "")
            lemma (:lemma props)
            gloss (:gloss props)
            propseq (apply str 
                           (for [prophead propheadvec] 
                             (if ( (keyword prophead) props)
                               ( str "%" (replace ((keyword prophead) props) #":" ""))
                               ( str "% " ))))
            ]
        ;; make sure no redundant commas
        (str lex "%" pos "%\""  lemma "\"%\"" gloss "\""   propseq  "\r\n")))))

(defn handle-lextabledisp
  [language]
  (layout/common
    ;;[:div
     ;; the following is adapted from pdgmIndex.clj
     (let [inputfile ( str "../aama-data/data/" language "/" language "-pdgms.edn")
           pdgmstring (slurp inputfile)
           pdgm-map (read-string pdgmstring)
           lexemes (:lexemes pdgm-map)
           lexitems (into [] (keys lexemes))
           lexproplists (for [lex lexitems] (keys (lex lexemes)))
           ;;lexprops (sort (into #{} (flatten lexproplists)))
           lexprops (into (sorted-set) (flatten lexproplists))
           headpropset (disj lexprops :pos :lemma :gloss)
           propheadvec (into [] headpropset)
           ;;user=> props
           ;;(:conjClass :derivedStem :gloss :lemma :pluralClass :pluralform :pos :rootClass)
           lextablerows (makelextablerows lexemes propheadvec)
           lexrows (reduce concat lextablerows)
           lexvalrows  (split (apply str lexrows) #"\r\n")
           ]
   [:body
    [:h3 "Paradigm Lexeme List For: "] 
    [:h4 (capitalize language) ]
       [:table {:id "handlerTable" :class "tablesorter sar-table"}
        [:thead
         [:tr
          [:th [:div {:class "some-handle"} [:br] "Lexeme" ]]
          [:th [:div {:class "some-handle"} [:br] "Pos"]]
          [:th [:div {:class "some-handle"} [:br] "Lemma"]]
          [:th [:div {:class "some-handle"} [:br] "Gloss"]]
          (for [prophead propheadvec]
            [:th [:div {:class "some-handle"} [:br] (capitalize (str (clojure.string/replace prophead #":" "")))]]
            )
          ]]
        [:tbody 
         ;;(str lex "," pos ","  lemma "," gloss ","   propseq  "\r\n"
         (for [valrow lexvalrows]
           [:tr
            (let [propcells (split valrow #"%")]
                 [:div
                  (for [propcell propcells]
                    [:td propcell])
                  ])])]]])
       
       [:script {:src "js/goog/base.js" :type "text/javascript"}]
       [:script {:src "js/webapp.js" :type "text/javascript"}]
       [:script {:type "text/javascript"}
        "goog.require('webapp.core');"]))

(defroutes lextabledisp-routes
  (GET "/lextabledisp" [] (lextabledisp))
  (POST "/makelextable" [language] (handle-lextabledisp language))
  )

