
(ns webapp.routes.langInfo
 (:refer-clojure :exclude [filter concat group-by max min replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize lower-case replace split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn langInfo []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        languages (split langlist #"\n")]
  (layout/common
   [:body
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
    [:hr]
    [:h3#clickable "Get Archive Data Source Information: "]
    (form-to [:post "/lgInformation"]
             [:table
              [:tr [:td "AAMA Language(s): " ]
               [:td 
                {:title "Choose one or more languages.", :name "language"}
                (for [language languages]
                 ;; (if (re-find #"_" language)
                   ;; [:div {:class "lfamily"}
                    ;;[:p language]]
                  (let [lang (read-string (str ":" (lower-case language)))
                        lpref (lang lprefmap)]
                  [:div {:class "form-group"}
                   [:label
                    (check-box {:name "languages[]" :value (lower-case language)}language) language " (" lpref ")"]]))]]
                   ;;[:p
 
                    ;; language " (" lpref ")"]]))]]
              ;; from https://groups.google.com/forum/#!topic/compojure/5Vm8QCQLsaQ
              ;; (check-box "valclusters[]" false valcluster) (str valcluster)]]
              ;;(submit-button "Get pdgm")
              [:tr [:td ]
               [:td [:input#submit
                     {:value "Get Language Information", :name "submit", :type "submit"}]]]]
             )])))

(defn handle-lgInformation
  [languages]
  ;; send SPARQL over HTTP request
    (let [bibrefmap (read-string (slurp "resources/public/bibrefs.edn"))
          lprefmap (read-string (slurp "pvlists/lprefs.clj"))]
      (layout/common
       [:body
        [:ol
        (for [language languages]
          (let [Language (capitalize language)
                lang (read-string (str ":" language))
                lpref (lang lprefmap)
                query-sparql (sparql/langInfoqry-sparql language lpref)
                query-sparql-pr (replace query-sparql #"<" "&lt;")
                req (http/get aama
                              {:query-params
                               {"query" query-sparql ;;generated sparql
                                ;;"format" "application/sparql-results+json"}})]
                                "format" "csv"}})
                ;; I have no idea why the following works; why it is necessary
                ;; to replace \r\n by something else (here &&) in order to
                ;; split (:body req).
                langInfostr (clojure.string/replace (:body req) #"\r\n" "&&")
                psplit (split langInfostr #"&&")
                header (first psplit)
                langInforow (str (rest psplit))
                langInforow2 (clojure.string/replace langInforow #"[\(\)\"]" "")
                lprops (split langInforow2 #",")
                sources (split (first lprops) #" ")
                desc (next lprops)
                descurls (split (first desc) #" ")
                txt-sf (next desc)
                desctxt (clojure.string/replace (first txt-sf) #"%%" ",")
                subfam (next txt-sf)
                ]
            (log/info "sparql result status: " (:status req))
            ;;[:div
          [:li [:p "Language: "  (capitalize language) " / Subfamily: " subfam]
             [:table {:class "linfo-table"}
              [:tbody
               ;;[:tr
                ;;[:th "Language:"] [:td Language]]
               [:tr 
                [:th "Data Source:"] [:td 
                                      (for [source sources]
                                        (let [bref (keyword (str source))
                                              ref (bref bibrefmap)]
                                          [:div [:p (str source ":")]
                                           [:p (str ref)]
                                           ]))]
                ]
               [:tr
                [:th "Description:"] [:td desctxt]
                ]
               [:tr
                [:th "Additional Information:"] [:td 
                                                 (for [descurl descurls]
                                                   [:div (link-to descurl descurl)])]
                ]]]
             ;;[:hr]
           ;;[:div [:h4 "======= Debug Info: ======="]
             ;;[:h3 "Query Response:"]
             ;;[:pre (:body req)]
             ;;[:pre langInfostr]
             ;;[:pre header]
             ;;[:pre langInforow2]
             ;;(for [lprop lprops]
             ;;[:pre lprop])
             ;;[:hr]
             ;;[:h3#clickable "Query:"]
             ;;[:pre query-sparql-pr]
            ;;[:h4 "============================="]]
           ]))]
       [:div
       [:footer
        [:p "AAMA Webapp"]]]

        [:script {:src "js/goog/base.js" :type "text/javascript"}]
        [:script {:src "js/webapp.js" :type "text/javascript"}]
        [:script {:type "text/javascript"}
         "goog.require('webapp.core');"]])))

(defroutes langInfo-routes
  (GET "/langInfo" [] (langInfo))
  (POST "/lgInformation" [languages] (handle-lgInformation languages)))
