(ns webapp.handler
  (:require [compojure.core :refer [defroutes routes]]
            [noir.util.middleware :as noir-middleware]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [webapp.routes.home :refer [home-routes]]
            [webapp.routes.langInfo :refer [langInfo-routes]]
            [webapp.routes.bibInfoSpecial :refer [bibInfoSpecial-routes]]
            [webapp.routes.bibInfoMaster :refer [bibInfoMaster-routes]]
            [webapp.routes.pdgm :refer [pdgm-routes]]            
            [webapp.routes.multipdgmseq :refer [multipdgmseq-routes]]
            [webapp.routes.multipdgmmod :refer [multipdgmmod-routes]]
            [webapp.routes.pvlgpr :refer [pvlgpr-routes]]
            [webapp.routes.pvprvllg :refer [pvprvllg-routes]]
            [webapp.routes.pvlgvl :refer [pvlgvl-routes]]
            [webapp.routes.update :refer [update-routes]]
            [webapp.routes.upload :refer [upload-routes]]
            [webapp.routes.listlgpr :refer [listlgpr-routes]]
            [webapp.routes.listmenulpv :refer [listmenulpv-routes]]
            [webapp.routes.listvlclplex :refer [listvlclplex-routes]]
            [webapp.routes.listlpv :refer [listlpv-routes]]
            [webapp.routes.aamaApp :refer [aamaApp-routes]]
            [webapp.routes.helppdgms :refer [helppdgms-routes]]
            [webapp.routes.helppvdisp :refer [helppvdisp-routes]]
            [webapp.routes.helplistgen :refer [helplistgen-routes]]
            [webapp.routes.helpwebupdate :refer [helpwebupdate-routes]]
            [webapp.routes.helpclupdate :refer [helpclupdate-routes]]))

(defn init []
  (println "webapp is starting"))

(defn destroy []
  (println "webapp is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes  aamaApp-routes langInfo-routes bibInfoSpecial-routes bibInfoMaster-routes pdgm-routes pvlgpr-routes pvlgvl-routes pvprvllg-routes listmenulpv-routes update-routes upload-routes listlgpr-routes listvlclplex-routes listlpv-routes multipdgmseq-routes multipdgmmod-routes helppdgms-routes helppvdisp-routes helplistgen-routes helpwebupdate-routes helpclupdate-routes app-routes)
      (handler/site)
      (wrap-base-url)))

;;(def app (noir-middleware/app-handler
;;          [home-routes
;;           pdgm-routes
;;           app-routes]))

