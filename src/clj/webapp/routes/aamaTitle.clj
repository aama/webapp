(ns webapp.routes.aamaTitle
  (:require [compojure.core :refer :all]
            [hiccup.element :refer [link-to]]
            [webapp.views.layout :as layout]))

(defn aamaTitle []
  (layout/common
   [:div {:class "info-page"}
    ;;[:h1#clickable "Afroasiatic Morphological Archive"]
    ;;[:h1 "AAMA"]
    [:h1 "Afroasiatic Morphological Archive:"]
    [:h3 "A Paradigm Datastore Project"]
    [:p  "Gene Gragg" [:br]
         "Oriental Institute, University of Chicago" [:br]
         [:em "For more information see: " (link-to "https://aama.github.io" "https://aama.github.io")]]]

   [:script {:src "js/goog/base.js" :type "text/javascript"}]
   [:script {:src "js/webapp.js" :type "text/javascript"}]
   [:script {:type "text/javascript"}
    "goog.require('webapp.core');"]))


(defroutes aamaTitle-routes
  (GET "/aamaTitle" [] (aamaTitle)))
