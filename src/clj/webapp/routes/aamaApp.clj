(ns webapp.routes.aamaApp
  (:require [compojure.core :refer :all]
            [hiccup.element :refer [link-to]]
            [webapp.views.layout :as layout]))

(defn aamaApp []
  (layout/common
   [:div {:class "info-page"}
    ;;[:h1#clickable "Afroasiatic Morphological Archive"]
    [:h1 "AAMA"]
    [:h2 "The Afroasiatic Morphological Archive"]
    [:p "The AAMA project has three components:"
     [:ol [:li [:p [:em "Data:"] " An extensible collection of morphological data, principally in the form of paradigms, from Afroasiatic languages. The normative/persistant data format is the json-like " [:em "edn: "] (link-to "https://github.com/edn-format/edn" "Extensible Data Notation") ". The data is application-neutral, and could be cast into any plausible datastore format, and used in conjunction with tools and query-and-display applications constructed using any appropriate programming tools."]]
      [:li [:p [:em "Tools:"] " A jar file (with source code)  to transform the data files into appropriate " (link-to "https://www.w3.org/RDF/" "RDF") " datastore ttl/rdf format, and a set of scripts  to upload aama files to a local git repository and then push to the appropriate repository in the github collection  " (link-to "https://github.com/aama" "aama")"."]]
      [:li [:p "(This) "[:em "Query & Display Application:"] " runnable as a jar file or directly from " (link-to "https://clojure.org" "Clojure") " source code"]]]]
    [:h2 "Query & Display Tool"]
    [:p "The purpose of this application is to develop and test various general formats of SPARQL queries that can be used to explore the morphological data registered in the Afroasiatic Morphological Archive."]
    [:p "Instructions for installing and configuring the required softward, downloading the data and tools, and setting up an RDF datastore as a SPARQL endpoint, as well as a description of the EDN format for the morphological data are contained in the " (link-to "http://aama.github.io" "AAMA github") " page; a more technical overview of the application code can be found in the aama/webapp repository's " (link-to "https://github.com/aama/webapp/blob/master/README.md" "README") "  page."]
    [:p "The application presupposes that the Fuseki has been launched, by default in " [:code "localhost:3030"] ", by the command, " [:code "bin/fuseki.sh"] ", cf.  " (link-to "http://aama.github.io" "aama.github.io") ",  and that the necessary application-specific lists and indices have been generated, as explained in the " (link-to "/helpinitializeapp" "Help > Initialization Instructions") " menu selection."]
    [:p "For the purposes of this provisional tool the queries implemented under \"" [:code "Menu > Search+"] "\" are divided into three large groups:"]
    [:ol
     [:li "Search for forms meeting some set of morphosyntactic criteria"]
     [:li "Search for paradigms"]
     [:li "Search for morphosyntactic properties and values"]]
    [:p "In each case the query parameters will be specified by pick-lists (generated in \"Utilities\") or text-input boxes;  where feasible, the query response returned from the datastore will be optionally followed by a listing of the query which produced that response."]
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]]))
   

(defroutes aamaApp-routes
  (GET "/aamaApp" [] (aamaApp)))
