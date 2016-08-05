(ns webapp.routes.helpwebupdate
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))

(defn helpwebupdate []
  (layout/common 
   [:div {:class "info-page"}
     [:h3 "Help: Update [Webapp]"]
     [:p "Procedures to update local and remote datastore after an edn file has been edited:"]
      [:ul [:li (link-to "/update" "Update Local Datastore")]
       [:li (link-to "/upload" "Upload to Remote Repository") " [Requires Access Privileges]"]]
      [:p "(NB: These two procedures have not yet been incorporated into the webapp. For the moment, the command-line versions have to be used.)"]]))

(defroutes helpwebupdate-routes
  (GET "/helpwebupdate" [] (helpwebupdate)))


