(ns webapp.routes.home
  (:require [compojure.core :refer :all]
            [hiccup.element :refer [link-to]]
            [webapp.views.layout :as layout]))

(defn home []
  (layout/common
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]))
   

(defroutes home-routes
  (GET "/" [] (home)))
