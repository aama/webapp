(ns webapp.routes.makeschemata
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [clojure.edn :as edn]
           [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split lower-case replace join]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]
            ;;[clojure-csv.core :as csv]
            ))

(def aama "http://localhost:3030/aama/query")

(defn makeschemata []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Make new schemata file."]
   ;;[:p "Will write requested Language-Property-Value co-occurrence list to file(s) pvlists/pname-POS-LANG.txt for selected language(s)."] 
   ;;[:hr]
   (form-to [:post "/makeschemata-gen"]
            [:table
             [:tr [:td "Language: " ]
              [:td 
               [:select#lang.required
                {:title "Choose a language.", :name "language"}
                [:optgroup {:label "Languages"} 
                 (for [language languages]
                   [:option {:value (lower-case language)} language])]]]]
             ;;(submit-button "Get values")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make Schemata", :name "submit", :type "submit"}]]]]))))


(defn csv2schemata
  "Takes sorted 3-col csv list and outputs html table with empty [:td]  for repeated col1 and string of col3 vals for repeated col2."
  [lpvs]
(let  [curcat1 (atom "")
      curcat2 (atom "")]
(do (for [lpv lpvs]
 (let [;;lpv2 (replace lpv #"^," "")
       catmap (zipmap [:cat1 :cat2 :cat3] (split lpv #","))]
   (if (= (:cat1 catmap) @curcat1)
     (if (= (:cat2 catmap) @curcat2)
       (str " :" (:cat3 catmap)  )
       (do (reset! curcat2 (:cat2 catmap))
           (str "],\r\n :"@curcat2" [:" (:cat3 catmap) "")))
     (do (reset! curcat1 (:cat1 catmap))
         (reset! curcat2 (:cat2 catmap))
         (str ":schemata {:" @curcat2 " [:" (:cat3 catmap) " "))))))))

(defn iri2localname
"Gets rid of namespace designation in req csv output"
[lpvscheme]
(for [lpv (split lpvscheme #"\r\n")]
  (replace lpv #":http:\/\/id.oi.uchicago.edu\/aama\/2013\/.*?\/" ":")))


(defn handle-makeschemata-gen
  [language]
  (layout/common
   (let [inputfile (str "../aama-data/data/" language "/" language "-pdgms.edn")
         ;; -pdgms.edn file will contain the final updated version
         ;; -pdgms-bck.edn will contain original, pre-final (but with pmorphClass
        bckfile (str "../aama-data/data/" language "/" language "-pdgms-bck.edn")
        pdgmstring (str (slurp inputfile))
         ;;pdgm-map (edn/read-string pdgmstring)
         schemafile (str "pvlists/schemata/" language "-schemata.edn")
         ;; send SPARQL over HTTP request"
         query-sparql (sparql/makeschemata-sparql language)
         query-sparql-pr (replace query-sparql #"<" "&lt;")
         req (http/get aama
                       {:query-params
                        {"query" query-sparql ;;generated sparql
                         ;;"format" "application/sparql-results+json"}})]
                         ;;"format" "text"}})
                         "format" "csv"}})
         reqvec (split (:body req) #"\n")
         header (first reqvec)
         lpvs (rest reqvec)
         lpvscheme (csv2schemata lpvs)
         lpvscheme2 (apply str lpvscheme)
         lpvscheme3 (replace (str lpvscheme2 "]\n }") #"\r" "")
         ;; get rid of namespace in prop and val designations of req csv output
         ;; (could probably do this in SPARQL with string function extensison
         ;; such as apache jena's splitIRI)
         lpvscheme4 (apply str (iri2localname lpvscheme3))
         ;; following uses pattern flag (?s) [= 'make dot all'] 
         ;; to get rid of multiline :schemata contents
         ;; (cf. https://clojuredocs.org/clojure.core/re-pattern, also
         ;; https://stackoverflow.com/questions/15020669/clojure-multiline-regular-expression/15021171)
         pdgmstring2 (replace pdgmstring #"(?s):schemata\s*\{.*?\}" ":schemata")
         ;; following puts lpvscheme4 in right place
         pdgmstring3 (replace pdgmstring2 #":schemata" lpvscheme4)
         ]
          (log/info "sparql result status: " (:status req))
          (spit schemafile lpvscheme4)
          ;;(assoc pdgm-map :schemata lpvscheme4)
          (spit bckfile pdgmstring)
          (spit inputfile pdgmstring3)
          [:div
           [:p "Language:      " language ]
           [:p "Schemata File: " schemafile]
           [:hr]
           ;;[:p "lpvscheme: " [:pre lpvscheme]]
           ;;[:p "lpvscheme2: " [:pre lpvscheme2]]
           [:p "lpvscheme4: " [:pre lpvscheme4]]
           [:p "lpvscheme3: " [:pre lpvscheme3]]
           ;;[:p "pdgmstring: " [:pre pdgmstring]]
           ;[:p "pdgmstring2: " [:pre pdgmstring2]]
           [:p "pdgmstring3: " [:pre pdgmstring3]]
           [:hr]
           [:h3 "Response:"]
           [:pre (:body req)]
           [:h3#clickable "Query:"]
           [:pre query-sparql-pr]
           ])
          [:script {:src "js/goog/base.js" :type "text/javascript"}]
          [:script {:src "js/webapp.js" :type "text/javascript"}]
          [:script {:type "text/javascript"}
           "goog.require('webapp.core');"]))

(defroutes makeschemata-routes
  (GET "/makeschemata" [] (makeschemata))
  (POST "/makeschemata-gen" [language] (handle-makeschemata-gen language)))


