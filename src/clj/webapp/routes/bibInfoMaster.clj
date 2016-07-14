(ns webapp.routes.bibInfoMaster
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split lower-case replace]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn bibInfoMaster []
  (let [reflist (slurp "pvlists/bibref-master-list.txt")
        bibrefs (split reflist #"\n")]
  (layout/common 
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
   [:h3 "Display General Bibliography Information"]
   ;;[:p "(This option will enable a user to display full bibliographic information, given a bibref string.)"]
   [:hr]
   (form-to [:post "/bibInfoMaster"]
            [:table
             [:tr [:td "Scope"]
              [:td 
               [:div {:class "scope"} (check-box {:id "selectall"} "Select All") "Select All"]]]
             [:tr [:td "Bibliographic Reference: " ]
              [:td 
                    {:title "Choose one or more Bibliographic References.", :name "brefs"}
                    (for [bibref bibrefs]
                      [:div {:class "form-group"}
                       [:label 
                        (check-box {:class "checkbox1" :name "brefs[]" :value bibref }bibref) bibref]])]]
             ;;(submit-button "Bibliographic Information")
             [:tr 
              [:td {:colspan "2"} [:input#submit
                    {:value "Bibliographic Information: ", :name "submit", :type "submit"}]]]]))))

;;See if can add atom reftexts which starts empty and successively gets updated
;;with new ref

(defn handle-bibInfoMaster
  [brefs]
  (let [bibrefmap (read-string (slurp "pvlists/bibrefs.clj"))]
  (layout/common
   [:body
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
   [:h3 "Selected Bibliographic Information"]
   [:hr]
    ;;[:h4#clickable "Bibliographic Information: "]
    [:p ]
    [:table {:class "linfo-table"}
     [:tbody
      (for [bibref brefs]
       (let [bref (read-string (str ":" bibref))
             ;;bref (keyword (str bibref))
             ref (bref bibrefmap)]
          [:tr
           [:th bibref] [:td ref ]]))]]
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]])))

(defroutes bibInfoMaster-routes
  (GET "/bibInfoMaster" [] (bibInfoMaster))
  (POST "/bibInfoMaster" [brefs] (handle-bibInfoMaster brefs)))
