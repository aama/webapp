(ns webapp.routes.upload
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


(def aama "http://localhost:3030/aama/query")

(defn upload 
"[This option will enable a user with access privileges to push the current state of the designated LANG-pdgms.edn file(s) to the appropriate github repository. Webapp page under construction; in the meantime cf. 'Help>Update/Upload>Command Line']"
 []
  (layout/common [:h3 "Upload"]
    [:div {:class "info-page"}
     [:hr]
     [:p "[This option will enable a user with access privileges to push the current state of the designated LANG-pdgms.edn file(s) to the appropriate github repository. Webapp page under construction; in the meantime cf." (link-to "/helpclupdate" "Help>Update/Upload>Command Line")".]"]]))

(defroutes upload-routes
  (GET "/upload" [] (upload))
  )
