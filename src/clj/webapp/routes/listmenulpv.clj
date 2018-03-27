(ns webapp.routes.listmenulpv
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split lower-case join]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))


(def aama "http://localhost:3030/aama/query")

(defn listmenulpv []
  (layout/common 
   [:h3 "Property Value Lists for Selection Menus"]
   ;;[:hr]
   ;;[:div
    ;;[:p "This option will generate cached datastore-wide language, property, or value lists for use in selection drop-down menus.."]]
   ;;[:hr]
   (form-to [:post "/listmenu-gen"]
            [:table
             [:tr [:td "List: "]
              [:td [:select#pos.required
                    {:title "Choose a list type.", :name "listtype"}
                    [:option {:value "val" :label "General Value List"}]
                    [:option {:value "prop" :label "General Property List"}]
                    ]]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make List", :name "submit", :type "submit"}]]]])))

(defn req2mlist
  [mlist]
  (let [mlist1 (clojure.string/replace mlist #"\r*\n$" "")
        reqq (split mlist1 #"\r*\n")
        ;;reqqa (first reqq)
        reqqb (rest reqq)
        reqqc  (clojure.string/replace reqqb #"\B,|[\(\)\"]" "")
        reqqd (clojure.string/replace reqqc #"[\]\[\"]" "")]
    (clojure.string/replace reqqd #" " "\n")))

(defn handle-listmenu-gen
  [listtype]
  (layout/common
   [:body
    [:h3#clickable "Datastore list for: " listtype]
    [:h4 "Generated File:"]
    [:li (str "pvlists/menu-" listtype "s.txt")]
    [:hr]
                    ;; send SPARQL over HTTP request
    (let [outfile (str "pvlists/menu-" listtype "s.txt")
          query-sparql (cond 
                        (= listtype "prop")
                        (sparql/listmenu-sparql-prop)
                        (= listtype "val")
                        (sparql/listmenu-sparql-val))
          query-sparql-pr (clojure.string/replace query-sparql #"<" "&lt;")
          req (http/get aama
                        {:query-params
                         {"query" query-sparql ;;generated sparql
                          ;;"format" "application/sparql-results+json"}})]
                          "format" "csv"}})
          req-body (clojure.string/replace (:body req) #",+" ",")
          req-out (req2mlist req-body)             
          ]
    (log/info "sparql result status: " (:status req))
      (spit outfile req-out)
     [:hr]
     [:div
     [:pre "req-out: "  req-out]
     [:h3#clickable "Query:"]
     [:pre query-sparql-pr]]
     )
[:script {:src "js/goog/base.js" :type "text/javascript"}]
[:script {:src "js/webapp.js" :type "text/javascript"}]
[:script {:type "text/javascript"}
 "goog.require('webapp.core');"]]))

(defroutes listmenulpv-routes
  (GET "/listmenulpv" [] (listmenulpv))
  (POST "/listmenu-gen" [listtype] (handle-listmenu-gen listtype)))
