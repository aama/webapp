(ns webapp.routes.listvlcl
  (:refer-clojure :exclude [filter group-by max min  replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split lower-case replace join]]
            [clojure.set :refer [difference]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            [clojure.java.io :as io]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn listvlcl []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")]
    (layout/common 
     [:h3 "Create value-cluster lists and maps to and from value-clusters to input paradigm IDs"]
     ;;[:p "Will write list of paradigm-specifying value-clusters to file(s) pvlists/pname-POS-LANG.txt for selected language(s)."]   [:hr]
     (form-to [:post "/listvlcl-gen"]
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
                      [:option {:value "fv" :label "Verb Finite"}]
                      [:option {:value "nfv" :label "Verb Other"}]
                      [:option {:value "pro" :label "Pronoun"}]
                      [:option {:value "sel" :label "Selector"}]
                      [:option {:value "noun" :label "Noun"}]]]]

               ;;(submit-button "Get pdgm")
               [:tr [:td ]
                [:td [:input#submit
                      {:value "Make Data Label / PDGM Value-Cluster Map", :name "submit", :type "submit"}]]]]))))

;; In case want to impose order on fv properties [not used here]
(defn normorder1
  "Takes property list output by listlgpr-sparql-POS and returns string with properties in (partial) order specified by porder."
  [pstring porder]
  (let [
        pordervec (split porder #",")
        pstringvec (split pstring #",")
        diffset (difference (set pstringvec) (set pordervec))
        ;;diffvec (into [] diffset)
        ]
    (join "," (into [] diffset))
    ;;(str porder "," (join "," diffvec))
    ))

(defn req2vlist1
  "For fv: Takes off header row, deletes interior brackets and quotes, makes into list, and separates plabel from morpho-syntactic values with ':'"
  [vlist]
  (let [vlist1 (replace vlist #"\r\n$" "")
        reqq (split vlist1 #"\r\n")
        ;; need final \n so that reqqe env. works on final line
        reqqb (str (rest reqq) "\n")
        reqqc  (replace reqqb #"\B,|[\(\)\]\[\"]" "")
        reqqd (replace reqqc #" " "\n")
        reqqe (replace reqqd #"(.*),(.*?\n)" "$1:$2")]
    (apply str reqqe)
    ))

;; For reference: collapses csv sparql output 
(defn propmerge
  "Takes list with 'pdgmType,prop1,prop2,...,propn' makes  map {{:pdgmType [prop1,...]}...{ and then does (merge-with into). Cf. req2vlist3."
  [vlist]
  (let [;; split into pdgmType (key) and  property-list
        vvec (for [vl vlist] (split vl #"," 2))
        ;; make a hash-map of 'pdgmType property-list'
        vmap (for [vec vvec] (hash-map (first vec) (split (last vec) #",")))
        ;; merge maps with identical key
        vmerge1 (sort (apply merge-with into  vmap))
        ;; put it back into a list
        ;;vmerge2 (for [vm vmerge1] (into [] vm))
        ]
    ;; make key ('pdgmType') and sorted property-list back into a string
    (join "\n" (for [vm vmerge1] (str (first vm) "," (join "," (apply sorted-set (last vm))))))))

(defn req2vlist2
  "Takes off header row and merges csv output for rows with same pname (as in propmerge)"
  [vlist]
  (let [vlist1 (replace vlist #"\n$" "")
        reqq (split vlist1 #"\n")
        ;;reqqa (first reqq)
        reqqb (rest reqq)
        ;; make joint key of pdgmID and pdgmType
        reqqc (for [req reqqb] (replace req #"^(.*?)," "$1+"))
        ;; split off the property
        vvec (for [req reqqc] (split req #","))
        ;; make a hash-map of ':pdgmID+pdgmType property'
        vmap (for [vvc vvec] (apply hash-map vvc))
        ;; merge maps with identical key
        vmerge (apply merge-with str vmap)
        reqq2 (into [] (for [vm vmerge] (join "," vm)))
        ;; This works because earlier split mention only '\n'
        reqq3 (for [r2 reqq2] (replace r2 #"[\r\+]" ","))
        reqq4 (for [r2 reqq3] (replace r2 #",$" ""))]
    (join "\n" reqq4)))

;; next three make dataID~vcluster lists
(defn compact-list
  "Takes string representing sorted bipartite list of dataID and pdgm value-cluster, with divider ',', and builds up list with single mention of dataID (key) paired with space-separated sting  of comma-separated value sub-strings."
  [csvlist]
  (let  [curpart1 (atom "")
         csvvec (split csvlist #"\n")]
    (for [csv csvvec]
      (let [partmap (zipmap [:part1 :part2] (split csv #"," 2))]
        (if (= (:part1 partmap) @curpart1)
          (str " " (:part2 partmap))
          (do (reset! curpart1 (:part1 partmap))
              (str "\n" @curpart1 "," (:part2 partmap))))))))

(defn csv2map1
  "Maps the first col of a csv to the string consisting of the content of the other cols"
  [csvstring]
  (let [reqcompact (apply str (compact-list csvstring))
        reqcomp (replace reqcompact #"^\n" "")
        reqvec (split reqcomp #"\n")
        reqmap (for [req reqvec] (hash-map (first (split req #"," 2)) (last (split req #"," 2))))
        reqmap2 (if (> (count reqmap) 1)
                  (into (sorted-map) (apply conj (clojure.walk/keywordize-keys reqmap))))]
    (join ",\n" (split (str reqmap2) #", "))
    ))

(defn csv2map2-old
  "Maps the content of all the cols after the first (substituting '_' for ',') to the content of the first col"
  [csvstring]
  (let [;;reqcompact (apply str (compact-list csvstring))
        ;;reqcomp (replace reqcompact #"^\n" "")
        reqvec (split csvstring #"\n")
        ;; remember to get rid of commas in key!
        reqmap (for [req reqvec] (hash-map (replace (last (split req #"," 2)) #"," "_") (first (split req #"," 2))))
        reqmap2 (if (> (count reqmap) 1)
                  (into (sorted-map) (apply conj (clojure.walk/keywordize-keys reqmap)))
                  (into (sorted-map) (clojure.walk/keywordize-keys reqmap)))]
    (join ",\n" (split (str reqmap2) #", "))
    ))

(defn csv2map2
  "Maps the XmorphClass to the content of the first col (dataID)"
  [csvstring]
  (let [;;reqcompact (apply str (compact-list csvstring))
        ;;reqcomp (replace reqcompact #"^\n" "")
        reqvec (split csvstring #"\n")
        ;; remember to get rid of commas in key!
        reqmap (for [req reqvec] (hash-map (first (rest (split req #","))) (first (split req #","))))
        reqmap2 (if (> (count reqmap) 1)
                  (into (sorted-map) (apply conj (clojure.walk/keywordize-keys reqmap)))
                  (into (sorted-map) (clojure.walk/keywordize-keys reqmap)))]
    (join ",\n" (split (str reqmap2) #", "))
    ))

;;    (if (> (count reqmap) 1)
;;    (into (sorted-map) (apply conj (clojure.walk/keywordize-keys reqmap))))))

;; next two provide for table display of pdgm props and vals (for fv only)
(defn addvstring
  "For each row of array, turns row into string of values (minus pdgmID) and adds in front of string of row vals."
  [vrows]
  (for [vrow vrows]
    (let [pstring0 (replace vrow #"^.*?," "")
          pstring1 (replace pstring0 #",+" "+")
          pstring2 (replace pstring1 #"(.*)\+(.*?)" "$1:$2")]
      (apply str pstring2 "," pstring0  "\r\n"))))

(defn req2vtable
  "For fv: adds 'PDGM' to header, and compacted value string to each row"
  [vtable]
  (let [vtable2 (split vtable #"\r\n")
        ;;vheader (first vtable2)
        vheader (replace (first vtable2) #"^.*?," "" )
        vheader2 (str "PDGM," vheader)
        vrows  (rest vtable2)
        vrows2 (addvstring vrows)]
    (apply str vheader2 "\r\n" vrows2)))

;; next two fn for vcluster of pro, sel, nfv
(defn makepng 
  "Assuming that the porder string of properties is in a specific desired order, it produces a substring containing only those properties which occur in the pstring in question."
  [pstring pordervec] 
  (let [pngvec (atom [])
        newvec (for [pv pordervec] 
                 (if (.contains pstring pv) 
                   (swap! pngvec conj (str pv))))]
    (join "," (last newvec))))

(defn normorder2
  "Takes string of vlcl values (req-vlcllist)  and returns string with a subset of properties in (partial) desired order specified by porder."
  [vlcllist porder]
  (let [
        pordervec (split porder #",")
        vclvec (for [pstring vlcllist]
                 (let [pngvec (atom [])
                       pvec (split pstring #",")
                       pname (first pvec)
                       pvals (rest pvec)
                       diffstr (join "," (into [] (difference (set pvals) (set pordervec))))
                       diffstring (if (re-find #"\w" diffstr)
                                    (str pname ","  diffstr)
                                    (str pname))
                       vclpng (makepng pstring pordervec)]
                   (if (re-find #"\w" vclpng)
                     (str diffstring "," vclpng)
                     (str diffstring))))]
    (join "\n" vclvec)))
                   
(defn handle-listvlcl-gen
  [ldomain pos]
  (layout/common
   [:body
    ;;[:h3#clickable "Value-clusters used in " pos " pdgms for: " ldomain]
    (let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))
          langs (split ldomain #",")
          ;;posvec ["fv" "nfv" "sel" "pro" "noun"]
          ]
      (for [language langs]
        ;;(for [pos posvec]
        (let [lang (read-string (str ":" language))
              lpref (lang lprefmap)
              ;; The following are possible lists and tables of pdgm values
              vlcllist (str "pvlists/vlcl-list-" language "-" pos ".txt")
              ;; 'dataID' is the datastore's pdgm name. For the moment only the
              ;; valueCluster - dataID map is generated, to retrieve comments
              ;; about a datastore pdgm. Code is present to generate dataID - valueCluster
              ;; maps should that ever become relevant.
              dataIDvlcl (str "pvlists/dataID-vlcl-" language "-" pos ".edn")
              vlcldataID (str "pvlists/vlcl-dataID-" language "-" pos ".edn")
              ;; a sortable table of pdgm values is at present generated for
              ;; the finite verb only, the only case where there are a large number
              ;; of value combinations for a given set of pdgm categories.
                 vlcltable (str "pvlists/vlcl-table-" language "-" pos ".txt")
              query-sparql1 (cond
                             (= pos "pro")
                             (sparql/listlgpr-sparql-pro language lpref)
                             (= pos "sel")
                             (sparql/listlgpr-sparql-sel language lpref)
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
              properties (set pstring)
              query-sparql2 (cond 
                             (= pos "pro")
                             (sparql/listvlcl-sparql-pro language lpref)
                             (= pos "sel")
                             (sparql/listvlcl-sparql-sel language lpref)
                             (= pos "nfv")
                             (sparql/listvlcl-sparql-nfv language lpref)
                             (= pos "noun")
                             (sparql/listvlcl-sparql-noun language lpref)
                             :else (sparql/listvlcl-sparql-fv language lpref pstring))
              query-sparql2-pr (replace query-sparql2 #"<" "&lt;")
              req2 (http/get aama
                             {:query-params
                              {"query" query-sparql2 ;;generated sparql
                               ;;"format" "application/sparql-results+json"}})
                               "format" "csv"}})
              req2-body (replace (:body req2) #",+" ",")
              req2-out   (if (= pos "fv")
                          (req2vlist1 req2-body)
                          (req2vlist2 req2-body))
              req3-out (apply str req2-out)
              req4-out (if (re-find #"\w" req3-out)
                         (replace req3-out #"^\s*\n" "")
                         (str "EmptyList"))
              ;;make map between dataID and value-cluster, and vice-versa
              req-dataIDvlcl (csv2map1 req4-out)
              req-vlcldataID  (csv2map2 req4-out)
              req4-vec (split req4-out #"\n")
              ;; now get rid of dataID in req4-vec
              req-vlcllist (sort (for [rq4 req4-vec] (replace rq4 #"^.*?," "" )))
              req2-table (if (= pos "fv")
                           (req2vtable (:body req2)))
              ;; for non-fv need to normalize npg order
              porder (str "number,person,gender")
              normstring (if (= pos "fv")
                            (str (join "\n"  req-vlcllist) "\n")
                            (normorder2 req-vlcllist porder))
              ]
          (log/info "sparql result status: " (:status req2))
          ;;(if (not (clojure.string/blank? (str req-dataIDvlcl)))
          ;;(doall (
          (spit dataIDvlcl req-dataIDvlcl)
          (spit vlcldataID req-vlcldataID)
          (spit vlcllist normstring)
          (if
              (= pos "fv")
            (spit vlcltable req2-table))
          ;;)))
          [:div 
           [:p [:b "Language: "] language]
           [:p [:b "POS: " ] pos]
           [:p [:b "Properties: "] pstring]
           [:p [:b "File vlcl-lst:    "] [:pre req-vlcllist]]
           [:p [:b "File normstring:    "] [:pre normstring]]
           [:p [:b "File vlcl-table:    "] [:pre req2-table]]
           ;;[:p [:b "File data-vlcl:     "] [:br] req-dataIDvlcl]
           [:p [:b "File vlcl-datalID:     "] [:br] req-vlcldataID]
           [:h4 "======= Debug Info: ======="] 
           [:h3#clickable "Query1:"] 
           [:pre query-sparql1-pr]
           [:h3#clickable "Query2:"] 
           [:pre query-sparql2-pr] 
           [:hr] 
           [:p "Query Output (:body req2): " 
            [:pre (:body req2)]] 
           [:h4 "Value Clusters: " ] 
           [:p "req2-body: " [:pre req2-body]]
           [:p "req2-out: " [:pre req2-out]]
           [:p "req4-out: " [:pre req4-out]] 
           [:p "req4-vec: " (join "\n" req4-vec)] 
           [:p [:b "File vlcl-list:    "] [:pre req-vlcllist]]
           [:p
            "==========================="]]))
      ;; ) [this is the parens for posvec]
      )
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]]))

(defroutes listvlcl-routes
  (GET "/listvlcl" [] (listvlcl))
  (POST "/listvlcl-gen" [ldomain pos] (handle-listvlcl-gen ldomain pos)))


