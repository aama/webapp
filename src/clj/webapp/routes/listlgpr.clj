(ns webapp.routes.listlgpr
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split lower-case replace capitalize]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn listlgpr 
"Given language(s) and POS, list (in a col) properties for that POS.
 (sparql/listlgpr-sparql-pro language lpref)
 (sparql/listlgpr-sparql-nfv language lpref)
 (sparql/listlgpr-sparql-noun language lpref)
 (sparql/listlgpr-sparql-fv language lpref)
 (sparql/listlgpr-sparql-checkall language lpref))"
 []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")]
  (layout/common 
   [:h3 "Properties by POS for datastore languages"]
   ;;[:p "(Only 'Finite Verb' enabled at this time.)"]
   ;;[:hr]
   (form-to [:post "/listlgpr-gen"]
            [:table
             [:tr [:td "Language Domain: " ]
              [:td 
               [:select#ldomain.required
               {:title "Choose a language domain.", :name "ldomain"}
                [:optgroup {:label "Languages"} 
                (for [language languages]
                  [:option {:value (lower-case language)} language])]
              [:optgroup {:label "Language Families"} 
               (for [ldom ldoms]
                (let [opts (split ldom #" ")]
               [:option {:value (last opts)} (first opts) ]))
                 [:option {:disabled "disabled"} "Other"]]]]]
             [:tr [:td "Part of Speech: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "fv" :label "Finite Verb"}]
                    [:option {:value "nfv" :label "Other Verb"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:value "noun" :label "Noun"}]
                    [:option {:value "all" :label "Schemata Property Check"}]
                    ]]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make Language-Property List", :name "submit", :type "submit"}]]]]))))

(defn handle-listlgpr-gen
  [ldomain pos]
  (layout/common
   (let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))
         langs (split ldomain #",")]
     [:body
      [:h3#clickable "Properties used in " pos " pdgms for: " ldomain]
      [:table
       [:tr
        (for [language langs]
          [:th
           [:div (str (capitalize language) " ")]])]
       [:tr
      (for [language langs]
          [:td
        (let [lang (read-string (str ":" language))
              lpref (lang lprefmap)
              ;; send SPARQL over HTTP request
              query-sparql (cond 
                            (= pos "pro")
                            (sparql/listlgpr-sparql-pro language lpref)
                            (= pos "nfv")
                            (sparql/listlgpr-sparql-nfv language lpref)
                            (= pos "noun")
                            (sparql/listlgpr-sparql-noun language lpref)
                            (= pos "fv")
                            (sparql/listlgpr-sparql-fv language lpref)
                            :else (sparql/listlgpr-sparql-checkall language lpref))
              query-sparql-pr (replace query-sparql #"<" "&lt;")
              req (http/get aama
                            {:query-params
                             {"query" query-sparql ;;generated sparql
                              ;;"format" "application/sparql-results+json"}})]
                              "format" "csv"}})
              ;;properties (split (:body req) #"\r\n" 2)
              props (replace (:body req) #"property" "")
              proplist (split props #"\r\n") ]
          ;;(log/info "sparql result status: " (:status req))
          [:div
           
          ;;[:h3 language]
            (for [prop proplist]
              [:p prop])
         ;;[:h4 "======= Debug Info: ======="]
         ;;[:h3#clickable "Query:"]
         ;;[:pre query-sparql-pr]
         ;;[:pre (:body req)]
         ;;[:p "==========================="]
            [:hr]])])]]
            ])
            ;;)])]]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]))

(defroutes listlgpr-routes
  (GET "/listlgpr" [] (listlgpr))
  (POST "/listlgpr-gen" [ldomain pos] (handle-listlgpr-gen ldomain pos))
  ;;(POST "/lgvldisplay" [ldomain lval] (handle-lgvldisplay ldomain lval))
  )


