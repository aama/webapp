(ns webapp.routes.pdgmmultdef
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require 
            ;;[clojure.core/count :as count]
            [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            ;;[clojure.string :as str]
            [clojure.string :refer [capitalize lower-case split upper-case]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))


(def aama "http://localhost:3030/aama/query")

(defn pdgmmultdef []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Multi-Paradigm: Default Display"]
     ;;[:p "Use this option to pick one or more  paradigms from a given language or set of languages to be displayed in fixed format vertical succession."]
   [:p "Choose Languages and Type"]
   ;; [:p error]
   ;;[:hr]
   (form-to [:post "/pdgmdefqry"]
            [:table
             [:tr [:td "PDGM Language(s): " ]
             [:td 
               {:title "Choose one or more languages.", :name "language"}
               (for [language languages]
                 [:div {:class "form-group"}
                  [:label 
                   (check-box {:name "languages[]" :value (lower-case language)}language) language]])]]
                 ;; from https://groups.google.com/forum/#!topic/compojure/5Vm8QCQLsaQ
                 ;; (check-box "valclusters[]" false valcluster) (str valcluster)]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get PDGM Value Clusters", :name "submit", :type "submit"}]]]]
            )
   [:hr])))

(defn handle-pdgmdefqry
  [languages]
  (layout/common 
       [:h3 "Multi-Paradigm Default Display"]
       [:p "Choose Value Clusters For: "]
       ;;[:p error]
       [:hr]
   (form-to [:post "/pdgmdefdisplay"]
            [:table  {:class "linfo-table"}
             ;; Following :tr can be commented out if not in proof-reading mode
             ;; selectall jQuery script from  http://www.sanwebe.com/2014/01/how-to-select-all-deselect-checkboxes-jquery
              [:tr [:td "Scope"]
               [:td 
                [:div {:class "scope"} (check-box {:id "selectall"} "Select All") "Select All"]]]
                 [:tr [:td "PDGM Language(s): " ]
                   (for [language languages]
                     [:td 
                      [:div (str (capitalize language) " ")]])]
             [:tr [:td "PDGM Value Clusters: " ]
              (for [language languages]
                [:td 
                 {:title "Choose a value.", :name "valcluster"}
                 (let [valclusterfile (str "pvlists/pdgm-index-" language ".txt")
                       valclusterlist (slurp valclusterfile)
                       ;;valclusterlst (clojure.string/replace valclusterlist #":.*?\n" "\n")
                       valclusterset (into (sorted-set) (clojure.string/split valclusterlist #"\n"))]
                   (if (re-find #"EmptyList" valclusterlist)
                     [:div (str "There are no  paradigms in the " language " archive.")]
                     (for [valcluster valclusterset]
                         [:div {:class "form-group"}
                          [:label
                           (check-box {:class "checkbox1" :name "valclusters[]" :value (str language "," valcluster) } valcluster) valcluster]])))])]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Display pdgms", :name "submit", :type "submit"}]]]])))

(defn handle-pdgmdefdisplay
 [valclusters]
 (layout/common
    (for [valcluster valclusters]
      (let [query-sparql (sparql/pdgmqry-sparql-gen-vrbs valcluster)
            query-sparql-pr (clojure.string/replace query-sparql #"<" "&lt;")
            req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})
                        "format" "text"}})
            req2 (clojure.string/replace (:body req) #"%%" " + ")
            ]
        [:div
         [:hr]
         [:h4 "Valcluster: " valcluster]
         [:pre (:body req)]
         ;;[:h4 "======= Debug Info: ======="]
         ;;[:h3#clickable "Query:"]
         ;;[:pre query-sparql-pr]
         ;;[:p "req2: " [:pre req2]]
         ;;[:p "==========================="]
         ]))
        [:script {:src "js/goog/base.js" :type "text/javascript"}]
        [:script {:src "js/webapp.js" :type "text/javascript"}]
        [:script {:type "text/javascript"}
         "goog.require('webapp.core');"]))

(defroutes pdgmmultdef-routes
  (GET "/pdgmmultdef" [] (pdgmmultdef))
  (POST "/pdgmdefqry" [languages] (handle-pdgmdefqry languages))
  (POST "/pdgmdefdisplay" [valclusters] (handle-pdgmdefdisplay valclusters)))
