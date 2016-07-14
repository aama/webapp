(ns webapp.routes.helppvdisp
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

(defn helppvdisp []
  (layout/common 
   [:div {:class "help-page"}
    [:h3 "Help: Property Value Displays"]
    [:p "These pages are designed to permit querying for arbitrary combinations of language, property, and value."]
    [:ul
     [:li (link-to "/pvlgpr" "Language-property")
      [:ul [:li [:p "This family of queries returns the values, if any, associated with a specified property in a specified language or group/family of languages."]]]]
     [:li (link-to "/pvlgvl" "Language-value")
       [:ul [:li [:p "This family of queries returns the properties, if any, associated with a specified value in a specified language or group/family of languages"]]]]
     [:li (link-to "/pvprvllg" "Language-property-value")
       [:ul [:li [:p "This family of queries accepts a language or group/family of languages and a comma-separated string of prop=val statements (in which case it returns the languages having that set of prop=val), combined optionally with one or more prop=?val statements (in which case it also returns the values of properties which may be associated with the specified properties)."]
   [:ul [:li "[For example the query \"person=Person2,gender=Fem\" with language group \"Beja\" returns the Beja languages which have 2f forms; while the query \"person=Person2,gender=Fem,pos=?pos,number=?number\" with \"Beja\" returns a table with the language(s) having 2f forms, along with the part-of-speech values, and number values associated with these forms.]"]]]]]]]))

(defroutes helppvdisp-routes
  (GET "/helppvdisp" [] (helppvdisp)))


