(ns webapp.routes.aamaApp
  (:require [compojure.core :refer :all]
            [hiccup.element :refer [link-to]]
            [webapp.views.layout :as layout]))

(defn aamaApp []
  (layout/common
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
   [:h1 "Query & Display Tool"]
   [:p "The purpose of this application is to develop and test various general formats of SPARQL queries that can be used to explore the morphological data registered in the Afroasiatic Morphological Archive. For the purposes of this provisional tool these queries are divided into three large groups:"]
    [:ol
    [:li "Paradigm Displays:"]
    [:li "Property-value Displays:"]
    [:li "Utilities:"]]
    [:p "In each case the query parameters will be specified by pick-lists or text-input boxes, and wherever feasible the query response returned from the datastore will be optionally followed by a listing of the query which produced that response."]
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]))
   

(defroutes aamaApp-routes
  (GET "/aamaApp" [] (aamaApp)))
