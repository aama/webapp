(ns webapp.routes.bibInfoSpecial
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split lower-case upper-case replace]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn bibInfoSpecial []
  (let [biblioglist (slurp "pvlists/bibref-keyword-list.txt")
        bibliogrefs (split biblioglist #"\n")]
  (layout/common 
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
   [:h3 "Bibliographic Information by Keyword"]
   ;;[:p "(This option will enable a user to display full bibliographic information, given a bibref string.)"]
   [:hr]
   (form-to [:post "/bibInfoSpecial"]
            [:table
             [:tr [:td "Keyword: " ]
              [:td [:select#bibliogref.required
                    {:title "Choose a Keyword.", :name "bibliogref"}
                    (for [bibliogref bibliogrefs]
                        [:option {:value bibliogref :label bibliogref} bibliogref])]]]
             [:tr 
              [:td {:colspan "2"} [:input#submit
                    {:value "Display Bibliography: ", :name "submit", :type "submit"}]]]]))))


(defn handle-bibInfoSpecial
  [bibliogref]
  (let [biblioglist (slurp"pvlists/bibref-keyword-list.txt")
        bibliogrefs (split biblioglist #"\n")
        bibkwindex (read-string (slurp "pvlists/bibkwindex.edn"))
        oldbref (str bibliogref)
        bibkey (read-string (str ":" bibliogref))
        reflist (bibkey bibkwindex)
        bibrefs (split reflist #" ")
        bibrefmap (read-string (slurp "pvlists/bibrefs.edn"))]
  (layout/common
   [:body
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
   [:h3 "Bibliographic Information by Keyword"]
   ;;[:p "Form repeated here to enable successive searches. How make cumulative on page?"]
   [:hr]
   (form-to [:post "/bibInfoSpecial"]
            [:table
             [:tr [:td (str "Keyword: ") ]
              [:td [:select#bibliogref.required
                    {:title "Choose a bibliography.", :name "bibliogref"}
                    (for [bibliogref bibliogrefs]
                      (if (= (str bibliogref) oldbref)
                        [:option {:value bibliogref, :selected "selected"} bibliogref]
                        [:option {:value bibliogref :label bibliogref} bibliogref]))]]]
             [:tr 
              [:td {:colspan "2"} [:input#submit
                                   {:value "Display Bibliography: ", :name "submit", :type "submit"}]]]])
    [:h4#clickable (str "Bibliographic Information for " bibliogref ":")]
    [:p]
    [:table {:class "linfo-table"} 
     [:tbody
      (for [bibref bibrefs]
        (let [bibid (clojure.string/replace bibref #"^:" "")
              bref  (read-string bibref)
             ref (bref bibrefmap)]
          [:tr
        ;;[:p (str bibref)])
           [:th bibid] [:td (str ref) ]]))]]
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]])))


(defroutes bibInfoSpecial-routes
  (GET "/bibInfoSpecial" [] (bibInfoSpecial))
  (POST "/bibInfoSpecial" [bibliogref] (handle-bibInfoSpecial bibliogref)))
