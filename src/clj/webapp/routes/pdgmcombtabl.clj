(ns webapp.routes.pdgmcombtabl
  (:refer-clojure :exclude [filter  group-by max min])
  (:require 
   ;;[clojure.core/count :as count]
   [compojure.core :refer :all]
   [webapp.views.layout :as layout]
   [webapp.models.sparql :as sparql]
   [compojure.handler :as handler]
   [compojure.route :as route]
   ;;[clojure.string :as str]
   [clojure.string :refer [capitalize lower-case split join upper-case]]
   [stencil.core :as tmpl]
   [clj-http.client :as http]
   ;;[boutros.matsu.sparql :refer :all]
   ;;[boutros.matsu.core :refer [register-namespaces]]
   [clojure.tools.logging :as log]
   [hiccup.element :refer [link-to]]
   [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn pdgmcombtabl []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
    (layout/common 
     [:h3 "Combine/Manipulate Paradigms: From Table"]
     [:p "Use this option to pick from a table one or more  paradigms from a given language or set of languages to be displayed as a single paradigm."]
     [:h3 "Multiparadigm Sortable Display"]
     (form-to [:post "/pdgmcombtablqry"]
              [:table
               [:tr [:td "PDGM Language(s): " ]
                [:td
                 {:title "Choose one or more languages.", :name "language"}
                 (for [language languages]
                   ;;[:option {:value (lower-case language)} language])]]]
                   [:div {:class "form-group"}
                    [:label
                     (check-box {:name "languages[]" :value (lower-case language)} language) language]])]]
               ;; from https://groups.google.com/forum/#!topic/compojure/5Vm8QCQLsaQ
               ;; (check-box "valclusters[]" false valcluster) (str valcluster)]]
               ;;(submit-button "Get pdgm")
               [:tr [:td ]
                [:td [:input#submit
                      {:value "Get PDGM Value Clusters", :name "submit", :type "submit"}]]]]
              )
     [:hr])))

(defn makecombtableheads 
  ;; Read in table heads (from pdgm-table- file or from pdgm-index?) and merge
  [languages]
  (for [language languages]
    (let [vlcltableheadsfile (str "pvlists/pdgm-table-" language ".txt")
          vlcltableheads (slurp vlcltableheadsfile)
          ;;headprops (split vlcltableheads #",")
          headprops (for [tablehead (split vlcltableheads #",")]
                      (clojure.string/replace tablehead #":" ""))
          ]
      (vec headprops))))

(defn makecombtablerows
  ;; Read in table rows from pdgm-index and make csv table for propvals
  ;; Typical pdgm name:
  ;; Verb,Finite,polarity=AffDecl,stemClass=DentalStem,tam=Imperfect%number,person,gender:qadid
  [languages combtableheads]
  (for [language languages]
    (let [valclusterfile (str "pvlists/pdgm-index-" language ".txt")
          valclusterlist (slurp valclusterfile)
          vlclvec (split valclusterlist #"\n")]
      ;;parse each pdgm name
      (for [vlcl vlclvec]
        (let [vcs (split vlcl #"," 2)
              pos (first vcs)
              mvalsprops (split (last vcs) #"%" 2)
              mv (first mvalsprops)
              morphclass (first (split mv #"," 2))
              ;;proplex (last mvalsprops)
              ;;lex (if (re-find #":" proplex)
              ;;      (last (split proplex #":" 2))
              ;;      "-")
              props (if (re-find #"," mv)
                      (last (split mv #"," 2))
                      "")
              ;; make hash-map out of props section of vlcl
              ;; e.g. props: polarity=AffDecl,stemClass=DentalStem,tam=Imperfect
              ;; with headpropkeys: 
              ;;:caseSel:clauseTypeSel:derivedStem:gender:mood:number:person:polarity:proClass:prsObj:selectorCategory:stemClass:subjSel:tam:tenseSel
              ;; want: {:polarity "AffDecl" :stemClass "DentalStem" :tam "Imperfect"}
              propvec (split props #",")
              propmap (apply merge 
                             (for [prop propvec] 
                               (hash-map (keyword (first (split prop #"="))) (str (last (split prop #"="))))))
              ;; key to prop=val part of  table
              propseq (apply str 
                             (for [combtablehead combtableheads] 
                               (if ( (keyword combtablehead) propmap)
                                 ( str "," ( (keyword combtablehead) propmap))
                                 ( str ", " ))))
              ]
          ;; make sure no redundant commas
          ;;(str language "," vlcl "&&"  pos "," morphclass   propseq  "%%" lex  "\r\n"))))))
          (str language "," vlcl "&&"  pos "," morphclass   propseq  "\r\n"))))))                      
(defn handle-pdgmcombtablqry
  [languages]
  (let [combtableheads (makecombtableheads languages)
        propheads (reduce concat combtableheads)
        propheadset1 (into (sorted-set) propheads)
        ;; limit propheadset to prop=val components
        ;; propheadset2 (disj propheadset1 "pos" "morphClass" "pdgm" "lexeme")
        propheadset2 (disj propheadset1 "pos" "morphClass" "pdgm" )
        propheadvec (into [] propheadset2)
        combtablerows (makecombtablerows languages propheadvec)
        tablerows (reduce concat combtablerows)
        valrows  (split (apply str tablerows) #"\r\n")
        ]
    (layout/common
     [:h3 "Combined PDGM Property Table For: "]
     [:ul (for [lang languages]
            [:li (capitalize lang)])] 
     (form-to [:post "/pdgmcombtabldisplay"]
              [:table {:id "handlerTable" :class "tablesorter sar-table"}
               [:thead
                [:tr
                 [:th [:div {:class "some-handle"} [:br] "Choose" ]
                  [:th [:div {:class "some-handle"} [:br] "Source"]]
                  [:th [:div {:class "some-handle"} [:br] "Pos"]]
                  (for [prophead propheadvec]
                    [:th [:div {:class "some-handle"} [:br] (capitalize prophead)]]
                    )
                  ]]]
               [:tbody 
                (for [valrow valrows]
                  [:tr
                   (let [tablecells (split valrow #"&&" 2)
                         langPname (first tablecells)
                         language (first (split langPname #"," 2 ))
                         pdgmname (last (split langPname #"," 2 ))
                         pdgmprops (last tablecells)
                         propcells (split pdgmprops #",")
                         ]
                     [:div
                      [:td 
                       [:div {:class "form-group"}
                        [:label 
                         (check-box {:name "lvalclusters[]" :value (str language ","  pdgmname) }  language) language ]]]
                      (for [propcell propcells]
                        [:td propcell])])])
                ;;(if (re-find #"EmptyList" valclusterlist)
                ;; [:div (str "There are no " pos " paradigms in the " language " archive.")]
                ;;(submit-button "Get p/dgm")
                [:tr 
                 [:td [:input#submit
                       {:value "Disnplay pdgms", :name "submit", :type "submit"}]]]]]
              [:hr]
              [:div [:h4 "======= Debug Info: ======="]
               [:p "combtableheads: " [:p combtableheads]] 
               [:p "propheads: " [:p propheads]] 
               [:p "propheadset1: " [:p propheadset1]] 
               [:p "propheadvec: " [:p propheadvec]]
               [:p "combtablerows: " [:p combtablerows]]
               [:p "tablerows: " [:p tablerows]]
               [:p "valrows: " [:p valrows]]  
               [:h4 "==========================="]])
     [:script {:src "js/goog/base.js" :type "text/javascript"}]
     [:script {:src "js/webapp.js" :type "text/javascript"}]
     [:script {:type "text/javascript"}
      "goog.require('webapp.core');"])))


(defn vc2req
  "Makes the requests that output a vector of csv string representing each of the pdgms."
  [pdgmclusters]
  (let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))]
    (for [pdgmcluster pdgmclusters]
      (let [vals (split pdgmcluster #"-" 2)
            pnum (first vals)
            lvalcluster (last vals)
            query-sparql (sparql/pdgmqry-sparql-gen-tokenmerge lvalcluster)
            req (http/get aama
                          {:query-params
                           {"query" query-sparql ;;generated sparql
                            ;;"format" "application/sparql-results+json"}})]
                            "format" "csv"}})
            pbody1 (:body req)
            pbody1a (clojure.string/replace pbody1 #"\r\"" "")
            pbody2 (str "Pdgm," (clojure.string/replace pbody1a #" " "_"))
            ]
        ;; add pdgm number to each row of pbody as first value
        (clojure.string/replace pbody2 #"\r\n(\S)" (str "\r\n" pnum ",$1"))))))

(defn csv2pmap
  "Takes vector of pdgm strings and returns unified pmap of vector by splitting off header from rows, and then interleaving headervec and rowvec."
  [pdgmstrvec]
  (for [pdgmstr pdgmstrvec]
    (let [headerstr (first (split pdgmstr #"\r\n" 2))
          headervec (for [header (split headerstr #",")] (keyword header))
          rows (last (split pdgmstr  #"\r\n" 2))
          rowvec (split rows #"\r\n")]
      (for [row rowvec] (apply assoc {} (interleave headervec (split row #",")))))))

(defn csvcombine
  "Takes vector of combined pdgm heads (pheads), a poperty-map of pdgm rows (pmap) and vector of value keys (keyvec), and outputs combined row-value csv, with '_' for properties not represented in source pdgm. (the 'join' - 'vec' combined functions keeps output from being LazySequence -- IS THERE A BETTER WAY?)."
  [pheads pmap keyvec]
  (let [heads (str (join #"," (vec (for [head pheads]  head ))) " ")
        cells (join #" " (vec (for [map pmap] 
                                (join #" " (vec (for [submap map] 
                                                  (join #"," (vec (for [key keyvec]
                                                                    (if (key submap )
                                                                      (key submap) 
                                                                      (str "_")))))))))))]
    (apply str heads cells)))

(defn csv2pdgm
  "Takes vectorized output of csvcombine and outputs combined html table of pdgms with first col for pdgm ref; cols are draggable and sortable."
  [newpdgmvec]
  (let [header (first newpdgmvec)
        rows (rest newpdgmvec)]
    [:div
     [:table {:id "handlerTable" :class "tablesorter sar-table"}
      [:thead
       [:tr 
        (let [heads (split header #",")]
          (for [head heads]
            [:th [:div {:class "some-handle"}  [:br] (capitalize head)]]))]]
      [:tbody 
       (for [row rows]
         [:tr
          (let [cellvec (split row #",")]
            (for [cell cellvec]
              [:td cell]))])]]]))

(defn addpnum
  "Provides for convenience of reference in table an index number for each of the pdgms." 
  [pnames]
  (for [pname pnames]
    (str "P" (.indexOf pnames pname) "-" pname)))

(defn handle-pdgmcombtabldisplay
  "Takes pdgm names (with language-name added) and combines into single csv; displays as table, then asks for pivot category/ies."
  [lvalclusters]
  (let [;; note problem with using atom for addpnum: gives LazySequence
        pdgmclusters (addpnum lvalclusters)
        ;; make string of pnames
        pdgmnames (apply pr-str pdgmclusters)
        pnamestr1 (clojure.string/replace pdgmnames #"[\[\]\"]" "")
        pnamestr2 (clojure.string/replace pnamestr1 #"%" ".")
        ;; get content csv for each pdgm
        pdgmvec (vc2req pdgmclusters)
        ;; provide property-value hash-map for each csv
        pmap (csv2pmap pdgmvec)
        pmapstr (into [] (for [pm pmap] (apply str pm)))
        ;; make pdgm csv's into string
        pdgmstrvec1 (apply pr-str pdgmvec)
        pdgmstrvec2 (clojure.string/replace pdgmstrvec1 #"[\(\)\"]" "")
        pdgmvec2 (split pdgmstrvec2 #" ")
        ;; pool headers into set
        headerrows  (join #"," (for [pdgm pdgmvec2] (first (split pdgm #"\\r\\n" 2))))
        headerset (into (sorted-set) (split headerrows #","))
        pheads (into [] headerset)
        keyvec (for [head pheads] (keyword head))
        ;; make single csv array out of combined pdgm csv's, and display as HTML table
        newpdgm (csvcombine pheads pmap keyvec)
        newpdgmvec (split newpdgm #" ")
        newpdgmstring (apply str newpdgmvec)
        newpdgmstr (clojure.string/replace newpdgmstring #"[\(\)\"]" "")
        pdgmtable (csv2pdgm newpdgmvec)
        ;; take away 'token' as possible pivot
        pivots (pop pheads)
        ]
    (layout/common
     [:h3#clickable "Combined Paradigms: Sequential Display " ]
     [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
     [:hr]
     [:p "Paradigms:"
      [:ul
       (for [pname pdgmclusters]
         ;; make pnamemap of this so as to be able to pass it to different functions
         ;; USE RECORDS?
         (let [pnumber (first (split pname #"-" 2))
               values1 (last (split pname #"-" 2))
               language (first (split values1 #"," 2))
               values2 (last (split values1 #"," 2))
               srce (first (split values2 #"," 2))
               values3 (last (split values2 #"," 2))
               propsstr (first (split values3 #"%" 2))
               valstr (last (split values3 #"%" 2))]
           [:li (str pnumber ": ") 
            [:ul [:li  (str language " (" srce ")")]
             [:li propsstr]
             [:li valstr]]]))]]
     [:hr]
     pdgmtable
     [:hr]
     [:h3 "Parallel Display of Paradigms"]
     [:hr]
     [:h3 "PDGM Value List"]
     [:p "Choose Parallel Display Format"]
     (form-to [:post "/pdgmcombtablplldisplay"]
              [:table
               [:tr [:td "PNames: "]
                [:td 
                 [:select#names.required
                  {:title "Chosen PDGMS", :name "pdgmnames"}
                  [:option {:value (do (apply str pnamestr2))} "Paradigm Names (as above)"]]]]
               [:tr [:td "Header: "]
                [:td [:select#header.required
                      {:title "Header", :name "header"}
                      [:option {:value pivots} (str pivots)] 
                      ]]]
               [:tr [:td "Pivots: "]
                [:td
                 ;;[:div {:class "form-group"}
                 [:p
                  (for [head pivots]
                    [:span
                     (check-box {:name "pivotlist[]" :value (.indexOf pivots head)} head) head])]]]
               ;;               [:tr [:td "PString: "]
               ;;                 [:td [:select#pdgms.required
               ;;                      {:title "PDGMS", :name "pdgmstrvec2"}
               ;;                      [:option {:value pdgmstrvec2} "Paradigm Forms (as above)"]]]]
               [:tr [:td "Newpdgmstr: "]
                [:td [:select#npdgms.required
                      {:title "NPDGMS", :name "newpdgmstr"}
                      [:option {:value newpdgmstr} "Paradigm Vector (as above)"]
                      ]]]
               [:tr [:td ]
                [:td [:input#submit
                      {:value "Display Paradigms in Parallel", :name "submit", :type "submit"}]]]])
     [:hr]
     [:div [:h4 "======= Debug Info: ======="]
      [:p "lvalclusters: " [:p pnamestr2]]
      [:p "pdgmclusters: " [:p pdgmclusters]]
      [:p "pdgmvec: " [:pre pdgmvec]]
      [:p "pmap: " [:pre pmap]]
      [:p "pmapstr: " [:p pmapstr]]
      [:p "pivots: " [:pre pivots]]
      [:p "pdgmstrvec2: " [:pre pdgmstrvec2]]
      [:p "newpdgm: " [:pre newpdgm]]
      [:p "newpdgmvec: " [:pre newpdgmvec]]
      [:p "newpdgmstr: " [:pre newpdgmstr]]
      [:p "header: " [:p (first newpdgmvec)]]
      [:p "rows: "  (str (rest newpdgmvec)) [:pre (rest newpdgmvec)]]
      [:h4 "==========================="]]
     [:script {:src "js/goog/base.js" :type "text/javascript"}]
     [:script {:src "js/webapp.js" :type "text/javascript"}]
     [:script {:type "text/javascript"}
      "goog.require('webapp.core');"])))

(defn cleanpdgms [pdgmstr]
  "This version also gets rid of initial header row in each pdgm substring"
  (let [pdgmstr-a (clojure.string/replace pdgmstr #"\\r\\n$" "")
        pdgmstr-b (clojure.string/replace pdgmstr-a #"^.*?\\r\\n" "")
        ;;pdgmstr-b (clojure.string/replace pdgmstr-a #"^.*? " "")
        pdgmstr-c (clojure.string/replace pdgmstr-b #":" "_")]
    ;; get rid of initial header row of each member pdgm
    (clojure.string/replace pdgmstr-c #"\\r\\n .*?(\\r\\n)" "$1")))

(defn make-pmap
  "Build up hash-map key by joining pivot-vals and val by removing pivot-vals"
  [pcell pivots]
  (let [pklist (vec (for [pivot pivots] (nth pcell pivot)))
        pkstr (join "+" pklist)]
    (hash-map  pkstr (vec (remove (set pklist) pcell)))))

;; from http://stackoverflow.com/questions/1394991
;; doesn't seem to be used
(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (clojure.core/concat (subvec coll 0 pos) (subvec coll (inc pos)))))

;;conj-in & merge-matches from http://stackoverflow.com/questions/2203213/
(defn conj-in [m map-entry]
  (update-in m [(key map-entry)] (fnil conj []) (val map-entry)))

(defn merge-matches [property-map-list]
  (reduce conj-in {} (apply clojure.core/concat property-map-list)))

(defn vec2map 
  "join all elements of vector but last with ','; last with ' '"
  [row] 
  (let [prow (clojure.string/join "," row)]
    (clojure.string/replace prow #"(.*),(.*?$)" "$1 $2")))
;;(clojure.string/replace (join "," row) #"(.*),(.*?$)" "$1 $2"))

(defn pstring2maps
  "Used in handle-multimodplldisplay4. Takes pivot property out of comma-separated properties in pdgm string, and arranges the rest as a set of {:property-list 'token'} maps" 
  [prmp] 
  (let [hmap1 (split prmp #" ")
        hmap2 (apply hash-map hmap1)]
    (clojure.walk/keywordize-keys hmap2)))

(defn join-pmaps
  "Join individual '{:values token}' maps into single map"
  [prmaps]
  (for [prmap prmaps]
    (if (> (count prmap) 1)
      (apply conj prmap)
      (apply conj (conj prmap {}))
      )))
;;(prmap))))

(defn handle-pdgmcombtablplldisplay
  "In this version pivot/keyset can be generalized beyond png any col (eventually any sequence of cols) between col-1 and token column. (Need to find out how to 'presort' cols before initial display?) [Current version has very ugly string=>list pdgms=>pnames. Simplify?]"
  [pdgmnames header pivotlist newpdgmstr]
  (let [pnamestr1 (clojure.string/replace pdgmnames #"[\[\]\"]" "")
        pnamestr2 (clojure.string/replace pnamestr1 #"%" ".")
        pnamestr3 (clojure.string/replace  pnamestr2 #"\w(P\d+-)" " $1")
        pnames (split pnamestr3 #" " )
        pivots (map read-string pivotlist)
        newpdgmstr2 (clojure.string/replace newpdgmstr #"P(\d)," "\\\\r\\\\nP$1,")
        ;; get rid of spurious line-feeds
        pdgmstr3 (cleanpdgms newpdgmstr2)
        ;; map each 'val-string-w/o-pivot-val token' to token
        prows (split pdgmstr3 #"\\r\\n")
        pcells (for [prow prows] (split prow #","))
        pivot-map (for [pcell pcells] (make-pmap pcell pivots))
        ;; group the val-tokens associated with each pivot val
        newpdgms (merge-matches pivot-map)
        pvalvec (vec (for [npdgm newpdgms] (str (key npdgm))))
        ;; e.g., ["Plural" "Singular"]
        ;; make a vector of pdgm rows for each pivot
        vvec (for [npdgm newpdgms] (val npdgm))
        pmapvec (for [vgroup vvec] (for [vrow vgroup] (vec2map vrow)))
        ;; transform pmaps to hash-maps
        prmaps (for [prmap pmapvec] (for [prmp prmap] (pstring2maps prmp)))
        pmaps (join-pmaps prmaps)
        headers2 (clojure.string/replace header #"[\[\]\"]" "")
        heads (split (str headers2) #" ")
        ;;headvec = headerset minus pivot namesn
        pivotnames (vec (for [pivot pivots] (nth heads pivot)))
        headvec (vec (remove (set pivotnames) heads))
        ;; set of lists of vaue-combination-terms
        keylists (vec (for [pmap pmaps] (keys pmap)))
        ;; replace seems to be ad hoc cluj; 
        ;; only '[' and ']' appear in one-line paradigm keyvec (but should not)
        ;; other deleted chars should not be in keys in the first place
        keystring (clojure.string/replace (str keylists) #"[#(){}\[\]]" "")
        keyvec (split keystring #" ")
        ;; set of all value combinations, as strings, in the combined pdgms
        keyset (set keyvec)
        ]
    (layout/common
     [:body
      [:p [:h3 "Paradigms: Parallel Display --  Pivot " (str pivotnames)]]
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
      [:p "Paradigms:"
       [:ul
        (for [pname pnames]
         ;; make pnamemap of this so as to be able to pass it to different functions
         ;; USE RECORDS
         (let [pnumber (first (split pname #"-" 2))
               values1 (last (split pname #"-" 2))
               language (first (split values1 #"," 2))
               values2 (last (split values1 #"," 2))
               srce (first (split values2 #"," 2))
               values3 (last (split values2 #"," 2))
               propsstr (first (split values3 #"\." 2))
               valstr (last (split values3 #"\." 2))]
           [:li (str pnumber ": ")[:ul [:li  (str language " (" srce ")")]
                 [:li propsstr]
                 [:li valstr]]]))]]
      [:hr]
      [:table {:id "handlerTable" :class "tablesorter sar-table"}
       [:thead
        (for [head headvec]
          [:th [:div {:class "some-handle"} [:br] head]])
        (for [pval pvalvec]
          ;;[:div 
          (let [pvals (split pval #"\+")]
            [:th [:div {:class "some-handle"} [:br]
                  (for [pv pvals]
                    [:div  [:em pv] ])]]))]
       [:tbody
        (for [keys keyset]
          [:tr
           (let [kstring (clojure.string/replace keys #"^:" "")
                 npgs (split kstring #",")
                 kstrkey (keyword kstring)]
             [:div
              (for [npg npgs]
                [:td npg])
              (for [pmap pmaps]
                ;; following creates problems for forms w/o '_'
                ;;(let [pmap1 (clojure.string/replace (kstrkey pmap) #"_" " ")] 
                [:td (kstrkey pmap)])])]) ]]
      [:p " "]
      [:p " "]
      [:div [:h4 "======= Debug Info: ======="]
       [:p "pdgmnames: " [:pre pdgmnames]]
       [:p "pnamestr3: " [:pre pnamestr3]]
       [:p "pnames: " [:pre pnames]]
       [:p "pivotlist: " (str pivotlist)]
       [:p "prows: "  (str prows) [:pre prows]]
       [:p "pcells: " (apply str pcells) [:pre pcells]]
       [:p "pivot-map: " [:pre pivot-map]]
       [:p "newpdgms: " [:pre newpdgms]]
       [:p "newpdgms: " (str newpdgms)]
       [:p "pvalvec: " (str pvalvec)]
       [:p "pdgmstr3: " [:pre pdgmstr3]]  
       [:p "newpdgmstr: " [:p newpdgmstr]]
       [:p "newpdgmstr2: " [:pre newpdgmstr2]]
       [:p "pmapvec: " [:pre pmapvec]]
       [:p "pmapvec: " (str pmapvec)]
       [:p "prmaps: " [:pre prmaps]]
       [:p "pmaps: " [:pre pmaps]]
       [:p "header: " [:pre header]]
       [:p "headers2: " [:pre headers2]] 
       [:p "heads: " (str heads)]
       [:p "pivotnames: " (str pivotnames)]
       [:p "headvec: " (str headvec)]
       [:p "keylists: " (str keylists)]
       [:p "keystring: " [:pre keystring]]
       [:p "keyvec: " [:pre keyvec]]
       [:p "keyvec: " (str keyvec)]

       [:p "keyset: " [:pre keyset]]
       [:p "==========================="]]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))


(defroutes pdgmcombtabl-routes
  (GET "/pdgmcombtabl" [] (pdgmcombtabl))
  (POST "/pdgmcombtablqry" [languages] (handle-pdgmcombtablqry languages))
  (POST "/pdgmcombtabldisplay" [lvalclusters] (handle-pdgmcombtabldisplay lvalclusters))
  (POST "/pdgmcombtablplldisplay" [pdgmnames header pivotlist newpdgmstr] (handle-pdgmcombtablplldisplay pdgmnames header pivotlist newpdgmstr)))
