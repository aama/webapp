(ns webapp.routes.listvlclplex
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
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
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn listvlclplex []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")]
  (layout/common 
   [:h3 "PDGM Value-Cluster List"]
   ;;[:p "Will write list of paradigm-specifying value-clusters to file(s) pvlists/pname-POS-list-LANG.txt for selected language(s)."]   [:hr]
   (form-to [:post "/listvlclplex-gen"]
            [:table
             [:tr [:td "PDGM Language Domain: " ]
              [:td [:select#ldomain.required
                    {:title "Choose a language domain.", :name "ldomain"}
                    [:optgroup {:label "Languages"} 
                    (for [language languages]
                      [:option {:value (lower-case language)} language])]
                [:optgroup {:label "Language Families"} 
               (for [ldom ldoms]
                (let [opts (split ldom #" ")]
               [:option {:value (last opts)} (first opts) ]))
                 [:option {:disabled "disabled"} "Other"]]]]]
              [:tr [:td "Part of Speech: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "fv" :label "Finite Verb"}]
                    [:option {:value "nfv" :label "Non-finite Verb"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:value "noun" :label "Noun"}]
                    ]]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Make PDGM Value-Clusters List", :name "submit", :type "submit"}]]]]))))

(defn compact-list
"Takes string representing sorted bipartite list with divider ':' and builds up new list with single mention of repeated part1 and comma-separated string of part2 vals for repeated part1."
 [lpvs]
 (let  [curpart1 (atom "")
        lpvvec (split lpvs #"\n")]
   (for [lpv lpvvec]
         (let [partmap (zipmap [:part1 :part2] (split lpv #":"))]
           (if (= (:part1 partmap) @curpart1)
               (str "," (:part2 partmap))
             (do (reset! curpart1 (:part1 partmap))
                 (str "\n"  @curpart1 ":" (:part2 partmap))))))))

(defn normorder
  "Takes property list output by listlgpr-sparql-POS and returns string with properties in (partial) order specified by porder."
  [pstring porder]
  (let [
        pordervec (split porder #",")
        pstringvec (split pstring #",")
        diffset (clojure.set/difference (set pstringvec) (set pordervec))
        diffvec (into [] diffset)]
    (str porder "," (join "," diffvec))))

(defn req2vlist1
  "For fv: Takes off header row, deletes interior brackets and quotes, makes into list, and separates plex from morpho-syntactic values with ':'"
  [vlist]
  (let [vlist1 (replace vlist #"\r\n$" "")
        reqq (split vlist1 #"\r\n")
        ;; need final \n so that reqqe env. works on final line
        reqqb (str (rest reqq) "\n")
        reqqc  (replace reqqb #"\B,|[\(\)\]\[\"]" "")
        reqqd (replace reqqc #" " "\n")
        reqqe (replace reqqd #"(.*),(.*?\n)" "$1:$2")]
    (apply str (compact-list reqqe))
))

(defn req2vlist2
  [vlist]
  (let [vlist1 (replace vlist #"\n$" "")
        reqq (split vlist1 #"\n")
        ;;reqqa (first reqq)
        reqqb (rest reqq)
        vvec (for [req reqqb] (split req #","))
        vmap (for [vvc vvec] (apply hash-map vvc))
        vmerge (apply merge-with str vmap)
        reqq2 (into [] (for [vm vmerge] (join "," vm)))
        ;; Find out why the following works
        reqq3 (for [r2 reqq2] (replace r2 #"\r" ","))]
    (join "\n" reqq3)
))

(defn req2vlist3
  "For pro: Takes off header row, deletes interior brackets and quotes, deletes line-final ','"
  [vlist]
  (let [reqq (split vlist #"\n" 2)
        reqqa (rest reqq)
        reqqb (apply str reqqa)
        ;;same Q as in req2vlist2
        reqqc (split reqqb #"\r")
        reqqd (for [req reqqc] (replace req #"\B,|[\(\)\]\[\"]" ""))]
    (for [req reqqd] (replace req #",$" ""))))

(defn handle-listvlclplex-gen
  [ldomain pos]
  (layout/common
   [:body
    ;;[:h3#clickable "Value-clusters used in " pos " pdgms for: " ldomain]
      (let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))
            langs (split ldomain #",")]
        (for [language langs]
          (let [
                lang (read-string (str ":" language))
                lpref (lang lprefmap)
                ;; send SPARQL over HTTP request
                outfile (str "pvlists/plexname-" pos "-list-" language ".txt")
                query-sparql1 (cond 
                          (= pos "pro")
                          (sparql/listlgpr-sparql-pro language lpref)
                          (= pos "nfv")
                          (sparql/listlgpr-sparql-nfv language lpref)
                          (= pos "noun")
                          (sparql/listlgpr-sparql-noun language lpref)
                          (= pos "fv")
                          (sparql/listlgpr-sparql-fv language lpref))
                query-sparql1-pr (replace query-sparql1 #"<" "&lt;")
                req1 (http/get aama
                          {:query-params
                           {"query" query-sparql1 ;;generated sparql
                            "format" "csv"}})
                            ;;"format" "application/sparql-results+json"}})
                            ;;"format" "text"}})
                propstring (if (= (:body req1) "property")
                             (str "no_" pos)
                             (replace (:body req1) #"\r\n" ","))
                pstring (replace propstring #"property,|,$" "")
                porder (str "formType,morphClass,pdgmType,conjClass,derivedStem,derivedStemAug,clauseType,tam,polarity,stemClass,rootClass")
                normstring (normorder pstring porder)
                plist (replace pstring #"," ", ")
                query-sparql2 (cond 
                          (= pos "pro")
                          (sparql/listvlcl-sparql-pro language lpref propstring)
                          (= pos "nfv")
                          (sparql/listvlcl-sparql-nfv language lpref propstring)
                          (= pos "noun")
                          (sparql/listvlcl-sparql-noun language lpref propstring)
                          :else (sparql/listvlcl-sparql-fv language lpref normstring))
                query-sparql2-pr (replace query-sparql2 #"<" "&lt;")
                req2 (http/get aama
                          {:query-params
                           {"query" query-sparql2 ;;generated sparql
                            ;;"format" "application/sparql-results+json"}})
                            "format" "csv"}})
                req2-body (replace (:body req2) #",+" ",")
                req2-out   (cond 
                            (= pos "fv")
                            (req2vlist1 req2-body)
                            (= pos "pro")
                            ;;(rest req2-body)
                            (req2vlist3 req2-body)
                            :else (req2vlist2 req2-body))
                req3-out (apply str req2-out)
                req4-out (replace req3-out #"^\s*\n" "")
              ]
        (log/info "sparql result status: " (:status req2))
        (spit outfile req4-out)
          [:div
           [:p [:b "Language: "] language]
           [:p [:b "File:     "] outfile]
           [:p [:b "Pstring: " ] pstring]
           [:p [:b "Porder:  " ] porder]
           [:p [:b "Normstring: "] normstring]
           [:h4  "Value Clusters: " ]
           [:pre req4-out]
           ;;[:hr]
           ;;[:p "propstring: " [:pre propstring]]
           [:h3#clickable "Query:"]
           [:pre query-sparql2-pr]
           ;;[:hr]
           [:hr]
           [:p "Query Output: " [:pre (:body req2)]]
           ])))
          [:script {:src "js/goog/base.js" :type "text/javascript"}]
          [:script {:src "js/webapp.js" :type "text/javascript"}]
          [:script {:type "text/javascript"}
           "goog.require('webapp.core');"]]))

(defroutes listvlclplex-routes
  (GET "/listvlclplex" [] (listvlclplex))
  (POST "/listvlclplex-gen" [ldomain pos] (handle-listvlclplex-gen ldomain pos)))


