(ns webapp.routes.aamaApp
  (:require [compojure.core :refer :all]
            [hiccup.element :refer [link-to]]
            [webapp.views.layout :as layout]))

(defn aamaApp []
  (layout/common
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
   [:h1 "Query & Display Tool"]
   [:p "The purpose of this application is to develop and test various general formats of SPARQL queries that can be used to explore the morphological data registered in the Afroasiatic Morphological Archive. It presupposes that an RDF datastore has been installed and launched, as described in in the " (link-to "http://aama.github.io" "AAMA github") " page, and that the necessary application-specific lists and indices have been generated, as explained in the " (link-to "/helpinitializeapp" "Initialization Instructions") " section."]
[:p "For the purposes of this provisional tool the implemented queries are divided into three large groups:"]
   [:ol
    [:li "Search for forms meeting some set of morphosyntactic criteria"]
    [:li "Search for paradigms"]
    [:li "Search for morphosyntactic properties and values"]]
   [:p "In each case the query parameters will be specified by pick-lists (generated in \"Utilities\") or text-input boxes;  where feasible, the query response returned from the datastore will be optionally followed by a listing of the query which produced that response."]
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]))
   

(defroutes aamaApp-routes
  (GET "/aamaApp" [] (aamaApp)))
