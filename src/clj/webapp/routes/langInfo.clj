
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
        languages (split langlist #"\n")]
  (layout/common
   [:body
   ;;[:h1#clickable "Afroasiatic Morphological Archive"]
    [:hr]
   [:h3#clickable "Get Language Information: "]
   (form-to [:post "/langInfo"]
            [:table
             [:tr [:td "Language: " ]
              [:td [:select#language
                    {:title "Choose a language.", :name "language"}
                    (for [language languages]
                        [:option {:value (lower-case language)} language])]]]
             ;;(submit-button "Get langInfo")
             [:tr 
              [:td {:colspan "2"} [:input#submit
                    {:value "Language Information: ", :name "submit", :type "submit"}]]]]
              ;;[:td {:colspan "2"} [:button#selection "Language Information For: "]]]]
            )])))

(defn handle-langInfo
  [language]
  ;; send SPARQL over HTTP request
  (let [langlist (slurp "pvlists/menu-langs.txt")
        bibrefmap (read-string (slurp "pvlists/bibrefs.edn"))
        languages (split langlist #"\n")
        Language (capitalize language)
        lang (read-string (str ":" language))
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))
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
        desctxt (clojure.string/replace (last desc) #"%%" ",")
        ]
         (log/info "sparql result status: " (:status req))
         (layout/common
          [:body
           [:h3#clickable "Get Language Information: "]
           (form-to [:post "/langInfo"]
                    [:table
                     [:tr [:td "Language: " ]
                      [:td [:select#language 
                            {:title "Choose a language.", :name "language"}
                            (for [language languages]
                              (if (= language Language)
                              [:option {:value (lower-case Language), :selected "selected"} Language]
                              [:option {:value (lower-case language)} language]))]]]
                     ;;(submit-button "Get langInfo")
                     [:tr 
                      [:td {:colspan "2"} [:input#submit
                                           {:value "Language Information: ", :name "submit", :type "submit"}]]]]
                    ;;[:td {:colspan "2"} [:button#selection "Language Information For: "]]]]
            )
           [:p]
           [:table {:class "linfo-table"}
            [:tbody
             [:tr
              [:th "Language:"] [:td Language]]
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
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

(defroutes langInfo-routes
  (GET "/langInfo" [] (langInfo))
  (POST "/langInfo" [language] (handle-langInfo language))
  )
