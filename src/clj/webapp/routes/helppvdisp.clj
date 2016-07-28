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
    [:h3 "Help: Morphosyntactic Category Search"]
    [:p "These pages are designed to permit querying for arbitrary combinations of language, property, and value."]
    [:ul
     [:li (link-to "/pvlgpr" "Morphosyntactic Properties")
      [:p "This family of queries returns the values, if any, associated with a specified property in a specified language or group/family of languages."]]
     [:li (link-to "/pvlgvl" "Morphosyntactic Values")
       [:p "This family of queries returns the properties, if any, associated with a specified value in a specified language or group/family of languages"]]
     [:li (link-to "/listlgpr"  "Properties by Language and POS")
      [:p "This set of queries lists, for one or more languages or language families in the datastore, the properties associated with the designated part of speech. "]]
     [:li (link-to "/listlpv" "Property-Value Indices by Language Domain")
      [:p "This set of queries will generate for a given language, language-family, or set of languages, index tables with entries:"
       [:ol 
        [:li "lang prop: val, val, val, ..." [:br]
         "(All the vals for each prop in each lang.)"]
        [:li "prop val: lang, lang, lang, ..." [:br]
         "(All the langs in which a given prop has a given val.)"]
        [:li " val prop: lang, lang, lang, ... " [:br]
         "(All the langs in which a given val is associated with a given prop.)"]
        [:li " prop lang: val, val, val, ..." [:br]
         "(All the vals associated with a given prop in a given language, set of languages, or language family.)"]
        [:li " prop-class prop lang: val, val, val, ..." [:br]
         "(As in no. 4, but where the properties are sorted by property-class. For the moment the following prop-classes are distinguished: [Not yet assigned], Inflectional-Class, PNG-Class, [Other] Syntactic/SemanticInfo-Class. A more flexible property classification scheme is being worked on.)"]
        [:li "drag/sort lang prop val" [:br]
         "(All the lang-prop-val triples in the language domain, displayed in a table with draggable columns and sortable rows. An eventual version of this table should be able to subsume tables 1-4, and perhaps also 5.)"]
        ]
            [:p "These tables provide in effect a set of complete lang-prop-val indices for the language(s) in question. The scripts use a simple SPARQL ?s ?p ?o template to generate for each lang a csv/jason file which is essentially a schemata (for lang-prop-val) or schemata index for the langs in question."]]]]]))

(defroutes helppvdisp-routes
  (GET "/helppvdisp" [] (helppvdisp)))


