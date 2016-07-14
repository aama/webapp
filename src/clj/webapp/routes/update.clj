(ns webapp.routes.update
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
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn update []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")]
  (layout/common 
   [:h3 "Update Datastore"]
    [:div {:class "help-page"}
     [:hr]
   [:p "[This option will enable a user to update the datastore after an edn data file has been revised in one or more ~/aama-data/data/LANG directories. Webapp page under construction; in the meantime cf." (link-to "/helpclupdate" "Help>Update/Upload>Command Line")".]"]
   ;;[:hr]
   (form-to [:post "/update-make"]
            [:table
             [:tr [:td "PDGM Language Domain: " ]
              [:td [:select#ldomain.required
                    {:title "Choose a language domain.", :name "ldomain"}
                    [:optgroup {:label "Languages"} 
                    (for [language languages]
                      [:option {:value (lower-case language)} language])]
              [:optgroup {:label "Language Families"} 
               (for [ldom ldoms]
                (let [opts (split ldom #" ")]
               [:option {:value (last opts)} (first opts) ]))
                 [:option {:disabled "disabled"} "Other"]]]]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Update Datastore", :name "submit", :type "submit"}]]]])])))

(defn edn2rdf
  [lang]
  (str lang ".rdf"))

(defn handle-update-make
  [ldomain]
  (layout/common
   [:body
    [:h3#clickable "Updating: " ldomain]
      (let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))
            langs (split ldomain #",")]
        (for [language langs]
          (let [
                lang (read-string (str ":" language))
                lpref (lang lprefmap)
                ;; make graph-name
                graph (str  "&lt;http://oi.uchicago.edu/aama/2013/graph/" language ">"  )
                ;; make rdf file & file-name
                file (edn2rdf lang)
                ;; send SPARQL over HTTP request
                ;; (how do we do http/delete and http/post here?)
                ;;req1 (http/delete aama
                ;;                  {:query-params
                ;;                   {"graph" graph ;;generated graph-name
                ;;                    }})
                ;;req2 (http/post aama
                ;;                {:query-params
                ;;                 {"graph" graph  ;;generated graph-name
                ;;                  "body" file}}) ;; new rdf-file
                ]
            ;;(log/info "sparql result status: " (:status req2))
          [:div {:class "help-page"}
           [:h3 "Language: " lang " updated."]
           [:p file " uploaded to " graph ]
           [:hr ]
          ])))
          [:script {:src "js/goog/base.js" :type "text/javascript"}]
          [:script {:src "js/webapp.js" :type "text/javascript"}]
          [:script {:type "text/javascript"}
           "goog.require('webapp.core');"]]))


(defroutes update-routes
  (GET "/update" [] (update))
  (POST "/update-make" [ldomain] (handle-update-make ldomain))
  ;;(POST "/pdgmqry" [language pos] (handle-pdgmqry language pos))
  ;;(POST "/pdgmdisplay" [language valstring] (handle-pdgmdisplay language valstring))
  )
