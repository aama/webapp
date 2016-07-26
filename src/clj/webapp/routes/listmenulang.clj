(ns webapp.routes.listmenulang
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split lower-case join]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))


(def aama "http://localhost:3030/aama/query")

(defn listmenulang []
  (layout/common 
     ;;[:h1#clickable "Afroasiatic Morphological Archive"]
     [:h3 "Generate Language Indices"]
     [:p "To be invoked whenever a language or language-variety has been added to the archive or a language-name has been modified."]
     [:p "This option will (re-)generate the following indices:"
      [:ol 
       [:li [:em "pvlists/menu-langs.txt"]": a sorted list of all languages in the archive for use in language-selection menus."] 
       [:li [:em "pvlists/lprefs.clj"]": a map linking each language name in the archive with the unique language prefix used in the RDF files and SPARQL queries."]
       [:li [:em "pvlists/ldomainlist.txt"]": a sorted list of all the language groupings recognized by the archive user on whatever basis (genetic, geographic, typological, project-related, etc.). It is up to the user to maintain and modify this list by hand; for comparison a genetic (language-tree) example of an ldomainlist.txt file can be consulted in the resources/public directory. By default the language indexing routine will simply update the final 'All' domain -- very useful when producing displays and indices ranging over the whole archive."]]]
     [:p (form-to [:post "/langlist-gen"]
              [:table
               [:tr 
                [:td {:colspan "2"} [:input#submit
                                     {:value "Generate Language Files: ", :name "submit", :type "submit"}]]]])]))

(defn req2mlist
  [mlist]
  (let [mlist1 (clojure.string/replace mlist #"\r*\n$" "")
        reqq (split mlist1 #"\r*\n")
        ;;reqqa (first reqq)
        reqqb (rest reqq)
        reqqc  (clojure.string/replace reqqb #"\B,|[\(\)\"]" "")
        reqqd (clojure.string/replace reqqc #"[\]\[\"]" "")]
    (clojure.string/replace reqqd #" " "\n")))

(defn make-llist
  [mlist]
  (let [langs (split mlist #"\n")
        lnames (for [lang langs] (clojure.string/replace lang #",.*$" ""))]
     (join "\n" lnames)))

(defn make-ldom
  [llist]
  (let [langs (split llist #"\n")
        lclist (for [lang langs] (lower-case lang))]
    (join "," lclist)))
       
 (defn make-lprefmap
  [mlist]
  (let [lpvec (split mlist #"\n")
        lpmap (for [lang lpvec] (hash-map (lower-case (first (split lang #","))) (last (split lang #","))))]
    (into (sorted-map) (clojure.walk/keywordize-keys lpmap))))

(defn handle-langlist-gen
  []
  ;; send SPARQL over HTTP request
  (let [query-sparql (sparql/listmenu-sparql-lang)
        query-sparql-pr (clojure.string/replace query-sparql #"<" "&lt;")
        req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "csv"}})
        req-body (clojure.string/replace (:body req) #",+" ",")
        req-out (req2mlist req-body)             
        llist (make-llist req-out)
        ldom (make-ldom llist)
        lprefs (make-lprefmap req-out)
        ]
    (log/info "sparql result status: " (:status req))
    (spit "pvlists/menu-langs.txt" llist)
    (spit "pvlists/lprefs.clj" lprefs)
    (spit "pvlists/ldomainlist.txt" (str "\nAll " ldom) :append true)
    (let [ldomcontent1 (slurp "pvlists/ldomainlist.txt")
          ldomcontent2 (clojure.string/replace ldomcontent1 #"All .*\nAll " "All ")]
      (spit "pvlists/ldomainlist.txt" ldomcontent2))
    (layout/common
     [:body
      [:h3#clickable "Language lists have been written to pvlists."]
      [:h4 "Generated Files:"]
      [:ol
       [:li "pvlists/menu-langs.txt"]
       [:li "pvlists/lprefs.clj"]
       [:li "pvlists/ldomainlist.txt"]]
      [:hr]
      [:div [:h4 "===== Debug Info: ====="]
       [:p "req-out: "  req-out]
       [:p "llist: " llist]
       [:p "ldom: " ldom]
       [:p "lprefs: " lprefs]
       [:h3#clickable "Query:"]
       [:pre query-sparql-pr]
       [:p "======================="]]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))

(defroutes listmenulang-routes
  (GET "/listmenulang" [] (listmenulang))
  (POST "/langlist-gen" [] (handle-langlist-gen)))
