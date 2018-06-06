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
    [:p
     "The purpose of the AAMA Project is to create a morphological
		archive whose data can be:"
     [:ul

      [:li "curated (edited/created) -- and hopefully shared!"]

      [:li "inspected"]

      [:li "manipulated"]

      [:li "queried"]]
     "on individual browsers."]
    
    [:p
     "In the first instance
		the archive aims to  make available and comparable the major 
		morphological paradigms of some forty Cushitic and Omotic languages,
		and in the longer term help situate the morphologies of these 
		two language families within Afroasiatic. In the longer run
		we hope also that the archive and its accompanying software
		may serve as a tool for exploration of typology and structure 
		of the form of linguistic organization known as the paradigm.
	      "]

    [:p
     "As presently configured tha AAMA project consists of 
		three interconnected modules:"]
    [:ol 
     [:li 
      [:p [:em "Data:"]"An extensible collection of morphological data, principally in the form 
of paradigms, from Afroasiatic languages. The normative/persistant data format is the json-like "
       [:em "edn: "] (link-to "https://github.com/edn-format/edn" "Extensible Data Notation") ". 
The data  is application-neutral, and could be cast into any plausible datastore format , and used in 
conjunction with tools and query-and-display applications constructed using any appropriate 
programming tools "]
      [:p "[N.B. The platform choices made here are: An " (link-to "https://www.w3.org/RDF/"
"RDF")  " datastore accessed by  an " (link-to "https://jena.apache.org/documentation/fuseki2/" 
 "Apache Jena Fuseki") " server, and an application coded in  "(link-to "https://clojure.org" 
 "Clojure") ", a Lisp dialect which has the virtues of compiling to Java Virtual Machine language, 
hence runnable on virtually any computer, and of providing a wide selection of easily available 
web-application libraries.]"]]
     [:li 
      [:p 
       [:em "Tools:"] " An executable jar file (with source code)  for transforming the (edn)  
data files into appropriate RDF datastore (ttl/rdf) format, and a set of scripts to upload data 
files to a local Fuseki server, and to push data and application files to the appropriate  " 
       (link-to "https://github.com/aama" "GihHub aama")" repository."]]
     [:li
      [:p 
       "(This menu-driven) "[:em "Query & Display Browser Application:"] ", which basically 
gathers requested language and morphological property and value information via the usual 
array of selection-list, checkbox, and text-input mechanisms, formulates them as a " 
(link-to "https://www.w3.org/TR/sparql11-query/"  "SPARQL") 
" query, and returns the response, usually in HTML table form. It is runnable as a jar file,
 or directly from the Clojure source code via the very user-friendly " 
(link-to "https://leiningen.org" "Leiningen") 
" tool package."]]]

    [:p 
     "Instructions for installing and configuring the required softward, downloading the
 data and tools, and setting up an RDF datastore as a SPARQL endpoint, as well as a 
description of the EDN format for the morphological data are contained in the " 
(link-to "http://aama.github.io" "AAMA github") 
" page; a more technical overview of the application code can be found in the 
aama/webapp repository's " 
(link-to "https://github.com/aama/webapp/blob/master/README.md" "README") 
"  page. "]
    [:p
     "More on the nature of the application's use of paradigms, cf. "
(link-to "/aamapdgmdata" "AAMA Paradigm Data")
" ; and the  "
(link-to "/helpinitializeapp"  "Help > Initialization Instructions") 
" section of this application contains practical notes on installation issues." ]
       [:div
       [:footer
        [:p "AAMA Webapp"]]]]

   [:script {:src "js/goog/base.js" :type "text/javascript"}]
   [:script {:src "js/webapp.js" :type "text/javascript"}]
   [:script {:type "text/javascript"}
    "goog.require('webapp.core');"]))


(defroutes aamaApp-routes
  (GET "/aamaApp" [] (aamaApp)))
