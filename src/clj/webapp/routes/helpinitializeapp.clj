(ns webapp.routes.helpinitializeapp
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

(defn helpinitializeapp []
  (layout/common 
   [:div {:class "info-page"}
    [:h3 "Help: Initialize Application On Remote Machine"]
    [:p "This application presupposes that the Fuseki AAMA data server has been installed, and launched through the invocation of the shell-script " [:em "bin/fuseki.sh"] ", as described in the " (link-to "http://aama.github.io" "AAMA github") " page."]
    [:p "In order to work properly, the application needs to find in its home directory a directory " [:em "pvlists"] " with application-specific lists and indices used in menus, queries and displays. In fact wihtout these, most menu-options will simply show a blank page. The following steps will properly set up the application:"]
    [:ol
     [:li "Create a directory " [:em "pvlists"] " in the home directory: <code>mkdir pvlists</code>."]
     [:li "Copy the file " [:em "resources/public/bibrefs.edn"] " to " [:em "pvlists"] ".. The version of the file in the distribution is a hash-map of the sources of current AAMA data, associating a bibrefID key with an array of two strings, one containing the text of the reference and the other a space-separated list of keywords. Format:  " [:br] "<code>{ ... :bibrefID [\"bibref text\" \"keyword-list\"] ... }</code>" [:br] " The user may add or delete items from this file at will, and insert whatever keywords are helpful for projects at hand. [Eventually this may be replaced by linking AAMA to one of the established bibliography apps such as Zotero.]"]
     [:li "Run the following utilities:"
      [:ol
       [:li (link-to "/bibKWIndexGen" "Generate Bibliography Keyword Indices") "."]
       [:li (link-to "/listmenulang" "Generate Language Lists") ". Note in particluar " [:em "pvlists/ldomainlist.txt"] " to be edited by the user to register language groups of interest for joint processing."]
       [:li (link-to "/listmenulpv" "Generate Property/Value Lists") ". Lists all morphosyntactic properties and values in the data store; for search for specific properties and values."]
       [:li (link-to "/pdgmIndex" "Generate Paradigm Value-Cluster Lists")                          ". This creates the lists which  enable search for specific paradigms in the datastore, and map the application paradigms to the data-enty paradigms. Be sure to take the language domain option \"All\"."]]]]

    [:p "Note that the appropriate  utilities need to be re-run whenever bibliography items, languages, or prop-val pairs are added, deleted, or edited"]]))

(defroutes helpinitializeapp-routes
  (GET "/helpinitializeapp" [] (helpinitializeapp)))


