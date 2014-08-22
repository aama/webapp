(ns webapp.core
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clj-http.client :as http]
            [boutros.matsu.sparql :refer :all]
            [boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log])
  (:use [hiccup.page :only [html5]]
            ))

;; local aama sparql query endpoint
(def aama "http://localhost:3030/aamadev/query")

;; some common prefixes
(register-namespaces {:ex "<http://example.org/book>"
                      :dc "<http://purl.org/dc/elements/1.1/>"})

;; PREFIX  ex: <http://example.org/book>
;; PREFIX  dc: <http://purl.org/dc/elements/1.1/>
;; SELECT ?title ?author
;; WHERE
;;   { ?book dc:title ?title .
;;      ?book dc:creator ?author . }

(defquery books-qry []
 (select :title :author)
 (where :book [:dc :title] :title \;
        [:dc :creator] :author \. ))

(defroutes app-routes
  (GET "/" []
       ;; demo clojurescript
       (html5
        [:body
         [:p#clickable "Click me!"] ;; clickable: see core.cljs
         [:script {:src "js/goog/base.js" :type "text/javascript"}]
         [:script {:src "js/webapp.js" :type "text/javascript"}]
         [:script {:type "text/javascript"}
          "goog.require('webapp.core');"]]))
  (GET "/books"
       []
       ;; send SPARQL over HTTP request
       (let [req (http/get aama
                           {:query-params
                            {"query" (books-qry)
                             "format" "application/sparql-results+json"}})]
         (log/info "sparql result status: " (:status req))
         (html5
          [:body
           [:h1#clickable "BOOKS"] ;; clickable: see core.cljs
           [:pre (:body req)]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"]])))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
