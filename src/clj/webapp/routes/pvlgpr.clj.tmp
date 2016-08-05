(ns webapp.routes.pvlgpr
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split lower-case replace]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pvlgpr []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")
        lproplist (slurp "pvlists/menu-props.txt")
        lprops (split lproplist #"\n")]
  (layout/common 
   [:h3 "Language-Property=>Value Cooccurrences"]
   [:h4 "Choose Language Domain and Property"]
   ;;[:p  "This family of queries returns the values, if any, associated with a specified property in a specified language or group/family of languages."]
   ;; [:p error]
   ;;[:hr]
   (form-to [:post "/lgprdisplay"]
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
             [:tr [:td "Property: " ]
              [:td 
               [:select#prop.required
               {:title "Select a property.", :name "prop"}
                (for [lprop lprops]
               [:option lprop ])
                 [:option {:disabled "disabled"} "Other"]]]
               ;;[:td 
              ;; (text-field {:placeholder "Enter a property"} "prop")
              ;; ]
              ]
             ;;(submit-button "Get values")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get language domain values", :name "submit", :type "submit"}]]]]))))

(defn handle-lgprdisplay
  [ldomain prop]
  ;; send SPARQL over HTTP request
  (let [query-sparql (sparql/lgpr-sparql ldomain prop)
        query-sparql-pr (replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h3#clickable "Language-Property-Values: " ldomain " / " prop]
           [:pre (:body req)]
           [:hr]
           [:h3#clickable "Query:"]
           [:pre query-sparql-pr]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defroutes pvlgpr-routes
  (GET "/pvlgpr" [] (pvlgpr))
  ;;(POST "/lgprqry" [ldomain prop] (handle-lgprqry ldomain prop))
  (POST "/lgprdisplay" [ldomain prop] (handle-lgprdisplay ldomain prop))
  )


