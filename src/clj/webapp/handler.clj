(ns webapp.handler
  (:require [compojure.core :refer [defroutes routes]]
            ;;[noir.util.middleware :as noir-middleware]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [webapp.routes.home :refer [home-routes]]
            [webapp.routes.aamaApp :refer [aamaApp-routes]]
            [webapp.routes.aamaTitle :refer [aamaTitle-routes]]
            [webapp.routes.aamapdgmdata :refer [aamapdgmdata-routes]]
            [webapp.routes.langInfo :refer [langInfo-routes]]
            [webapp.routes.langInfoTree :refer [langInfoTree-routes]]
            [webapp.routes.bibIndexGen :refer [bibIndexGen-routes]]
            [webapp.routes.bibInfoSpecial :refer [bibInfoSpecial-routes]]
            [webapp.routes.bibInfoMaster :refer [bibInfoMaster-routes]]
            [webapp.routes.pdgmIndex :refer [pdgmIndex-routes]]
            [webapp.routes.pdgmSource :refer [pdgmSource-routes]]
            [webapp.routes.pdgmindiv :refer [pdgmindiv-routes]]            
            [webapp.routes.pdgmmultdef :refer [pdgmmultdef-routes]]
            [webapp.routes.pdgmcomblist :refer [pdgmcomblist-routes]]
            [webapp.routes.pdgmcombtabl :refer [pdgmcombtabl-routes]]
            [webapp.routes.pdgmtableindex :refer [pdgmtableindex-routes]]
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
            [webapp.routes.listlpv :refer [listlpv-routes]]
            [webapp.routes.makeschemata :refer [makeschemata-routes]]
            [webapp.routes.makelexemes :refer [makelexemes-routes]]
            [webapp.routes.lextabledisp :refer [lextabledisp-routes]]
            [webapp.routes.helpaamaversions :refer [helpaamaversions-routes]]
            [webapp.routes.helpaddnewlanguage :refer [helpaddnewlanguage-routes]]
            [webapp.routes.helpinitialiizeapp :refer [helpinitializeapp-routes]]
            [webapp.routes.helppdgms :refer [helppdgms-routes]]
            [webapp.routes.helpformsearch :refer [helpormsearch-routes]]
            [webapp.routes.helppvdisp :refer [helppvdisp-routes]]
            [webapp.routes.helplistgen :refer [helplistgen-routes]]
            [webapp.routes.helpwebupdate :refer [helpwebupdate-routes]]
            [webapp.routes.helpclupdate-routes :refer [helpclupdate-routes]]))

(defn init []
  (println "webapp is starting"))

(defn destroy []
  (println "webapp is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes  aamaApp-routes aamaTitle-routes aamapdgmdata-routes langInfo-routes langInfoTree-routes bibIndexGen-routes bibInfoSpecial-routes bibInfoMaster-routes pvlgpr-routes pvlgvl-routes formsearch-routes lextabledisp-routes listmenulpv-routes listmenulang-routes update-routes pdgmindiv-routes upload-routes listlgpr-routes listvlcl-routes listlpv-routes  makelexemes-routes makeschemata-routes pdgmmultdef-routes pdgmcomblist-routes pdgmcombtabl-routes pdgmIndex-routes pdgmSource-routes pdgmtableindex-routes multipdgmsort-routes helpaamaversions-routes helpaddnewlanguage-routes helpinitializeapp-routes  helppdgms-routes helpformsearch-routes helppvdisp-routes helplistgen-routes helpwebupdate-routes helpclupdate-routes app-routes)
      (handler/site)
      (wrap-base-url)))

;;(def app (noir-middleware/app-handler
;;          [home-routes
;;           pdgm-routes
;;           app-routes]))

