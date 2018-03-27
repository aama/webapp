(ns webapp.handler
  (:require [compojure.core :refer [defroutes routes]]
            [noir.util.middleware :as noir-middleware]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [webapp.routes.home :refer [home-routes]]
            [webapp.routes.aamaApp :refer [aamaApp-routes]]
            [webapp.routes.langInfo :refer [langInfo-routes]]
            [webapp.routes.langInfoTree :refer [langInfoTree-routes]]
            [webapp.routes.bibIndexGen :refer [bibIndexGen-routes]]
            [webapp.routes.bibInfoSpecial :refer [bibInfoSpecial-routes]]
            [webapp.routes.bibInfoMaster :refer [bibInfoMaster-routes]]
            [webapp.routes.pdgm :refer [pdgm-routes]]            
            [webapp.routes.multipdgmseq :refer [multipdgmseq-routes]]
            [webapp.routes.pdgmmultdef :refer [pdgmmultdef-routes]]
            [webapp.routes.multipdgmmod :refer [multipdgmmod-routes]]
            [webapp.routes.multipdgmsort :refer [multipdgmsort-routes]]
            [webapp.routes.pvlgpr :refer [pvlgpr-routes]]
            [webapp.routes.formsearch :refer [formsearch-routes]]
            [webapp.routes.pvlgvl :refer [pvlgvl-routes]]
            [webapp.routes.update :refer [update-routes]]
            [webapp.routes.upload :refer [upload-routes]]
            [webapp.routes.listlgpr :refer [listlgpr-routes]]
            [webapp.routes.listmenulpv :refer [listmenulpv-routes]]
            [webapp.routes.listmenulang :refer [listmenulang-routes]]
            [webapp.routes.listvlcl :refer [listvlcl-routes]]
            [webapp.routes.pdgmIndex :refer [pdgmIndex-routes]]
            [webapp.routes.valclmod :refer [valclmod-routes]]
            [webapp.routes.listlpv :refer [listlpv-routes]]
            [webapp.routes.listptype :refer [listptype-routes]]
            [webapp.routes.makeschemata :refer [makeschemata-routes]]
            [webapp.routes.helpaamaversions :refer [helpaamaversions-routes]]
            [webapp.routes.helpinitializeapp :refer [helpinitializeapp-routes]]
            [webapp.routes.helppdgms :refer [helppdgms-routes]]
            [webapp.routes.helpformsearch :refer [helpformsearch-routes]]
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
  (-> (routes home-routes  aamaApp-routes langInfo-routes langInfoTree-routes bibIndexGen-routes bibInfoSpecial-routes bibInfoMaster-routes pdgm-routes pvlgpr-routes pvlgvl-routes formsearch-routes listmenulpv-routes listmenulang-routes update-routes upload-routes listlgpr-routes listvlcl-routes pdgmIndex-routes listlpv-routes listptype-routes makeschemata-routes multipdgmseq-routes pdgmmultdef-routes valclmod-routes multipdgmmod-routes multipdgmsort-routes helpaamaversions-routes helpinitializeapp-routes helppdgms-routes helpformsearch-routes helppvdisp-routes helplistgen-routes helpwebupdate-routes helpclupdate-routes app-routes)
      (handler/site)
      (wrap-base-url)))

;;(def app (noir-middleware/app-handler
;;          [home-routes
;;           pdgm-routes
;;           app-routes]))

