(ns webapp.routes.multipdgmmod
 (:refer-clojure :exclude [filter concat group-by max min])
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

(defn multipdgmmod []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Checkbox: Multilingual Display"]
   ;;[:p "Use this option to pick one or more  paradigms from a given language or set of languages to be displayed as a single paradigm. (NB: Will only combine paradigms with identical headers.)"]
   [:p "Choose Languages and Type"]
   (form-to [:post "/multimodqry"]
            [:table
             [:tr [:td "PDGM Type: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "verb" :label "Verb"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:value "noun" :label "Noun"}]
                    ]]]
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

(defn handle-multimodqry
  [languages pos]
  (layout/common 
   (form-to [:post "/multimoddisplay"]
            [:table
             [:tr [:td "PDGM Type: " ]
              [:td
               (check-box {:name "pos" :value pos :checked "true"} pos) (str (upper-case pos))]]
              [:tr [:td ]]
                 [:tr [:td "PDGM Language(s): " ]
                   (for [language languages]
                  [:td 
                   [:div (str (capitalize language) " ")]])]
                 [:tr [:td "PDGM Value Clusters: " ]
                  (let [pnum (atom 0)]
                   (for [language languages]
                  [:td 
                   {:title "Choose a value.", :name "valcluster"}
                     (let [valclusterfile (str "pvlists/vlcl-list-" language "-" pos ".txt")
                           valclusterlist (slurp valclusterfile)
                           ;;valclusterlst (clojure.string/replace valclusterlist #":.*?\n" "\n")
                           valclusterset (into (sorted-set) (clojure.string/split valclusterlist #"\n"))]
                   (if (re-find #"EmptyList" valclusterlist)
                     [:div (str "There are no " pos " paradigms in the " language " archive.")]
                       (for [valcluster valclusterset]
                           [:div {:class "form-group"}
                            [:label
                             (check-box {:class "checkbox1" :name "valclusters[]" :value (str language "," valcluster) } valcluster) valcluster]]))
                       )]))]
                 ;;(submit-button "Get pdgm")
                 [:tr [:td ]
                  [:td [:input#submit
                        {:value "Display pdgms", :name "submit", :type "submit"}]]]])))

(defn vc2req
 [valclusters pos]
  (let [vcvec (split valclusters #" ")
        lprefmap (read-string (slurp "pvlists/lprefs.clj"))]
    (for [valcluster vcvec]
      (let [vals (split valcluster #"," 2)
            plang (first vals)
            ;;pnum (clojure.string/replace plang #"%.*" "")
            language (clojure.string/replace plang #"^P.*?%" "")
            lang (read-string (str ":" language))
            lpref (lang lprefmap)
            vcluster (last vals)
            vcs (split vcluster #"," 2)
            pdgmType (first vcs)
            pvalcluster (last vcs)
            pvlcl (clojure.string/replace pvalcluster #"," ".")
            valstrng (clojure.string/replace pvlcl #",*person|,*gender|,*number" "")
            valstr (clojure.string/replace valstrng #":," ":")
            query-sparql (cond 
                          (= pos "pro")
                          (sparql/pdgmqry-sparql-pro language lpref pvalcluster)
                          (= pos "noun")
                          (sparql/pdgmqry-sparql-noun language lpref vcluster)
                          (= pdgmType "Finite")
                          (sparql/pdgmqry-sparql-fv language lpref pvalcluster)
                          :else (sparql/pdgmqry-sparql-nfv language lpref vcluster))
            req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "csv"}})
            ;; get rid of header
            pbody1 (:body req)
            ;;pbody1 (clojure.string/replace (:body req) #"^.*?\r\n" "\r\n")
            pbody2 (clojure.string/replace pbody1 #" " "_")
            ]
        ;;(clojure.string/replace pbody1 #" " "_")
        ;; add pdgm name to each row of pbody as first value
        (clojure.string/replace pbody2 #"\r\n(\S)" (str "\r\n" lpref "." pvlcl  ",$1"))
        ;;(str vcluster " +1 " pdgmType " +2 " pvalcluster " +3 " (:body req) " +4 " query-sparql)
        ))))

(defn csv2pdgm
"Takes sorted n-col csv list with vectors of pnames and headers, and outputs n+1-col html table with first col for pname ref; cols are draggable and sortable."
 [pdgmstr2 valclusters]
(let  [pdgms (str valclusters)
       pnamestr1 (clojure.string/replace pdgms #"[\[\]\"]" "")
       pnamestr2 (clojure.string/replace pnamestr1 #"%" ".")
       pnames (split pnamestr2 #" ")
       ;; pdgmstr2 is a string of space-separated pdgmstrings, whose rows are
       ;; separated by \r\n and cells separated by ","
       ;; Take off the top header
       ;; If pdgms are to be comparable
       ;; all header strings will be same
       pstrings (split pdgmstr2 #" ")
       pdgm1 (first pstrings)
       header (first (split pdgm1 #"\\r\\n"))
       ;;header (str "num,pers,gen,token")
       header2 (str "pdgm," header)
       pheads (split header2 #",")
       ]
  [:div
   [:p "Paradigms:"
    [:ul
     (for [pname pnames]
       [:li pname])]]
   [:hr]
   [:table {:id "handlerTable" :class "tablesorter sar-table"}
    [:thead
     [:tr
      (for [head pheads]
        [:th [:div {:class "some-handle"}  [:br] (capitalize head)]])]]
    [:tbody 
     (for [pdgm pstrings]
       (let [;;throw away header row
             pdgm-sp (split pdgm #"\\r\\n" 2)
             pheader (first pdgm-sp)
             pbody (last pdgm-sp)
             pdgmrows (split pbody #"\\r\\n")
             ]
         (for [pdgmrow pdgmrows]
           [:tr
            (let [pdgmrow2 (clojure.string/replace pdgmrow #"_" " ")
                  pdgmcells (split pdgmrow2 #",")]
              (for [pdgmcell pdgmcells]
                [:td pdgmcell]))])
         ))]]]))
        
(defn addpnum
  [pdgmvec]
  (let [pnum (atom 0)]
    (for [pdgmrow pdgmvec]
      (let [pnum (swap! pnum inc)]
        (clojure.string/replace pdgmrow #"\r\n(\S)" (str "\r\nP-"  pnum  "-$1"))))))

(defn handle-multimoddisplay
  [valclusters pos]
  (let [headerset1 (str "Paradigm " "Number " "Person " "Gender " "Token ")
        headerset2 (str "pdgm " "num " "pers " "gen ")
        headers (split headerset2 #" ")
        pdgmvec (map #(vc2req  % pos) valclusters)
        ;;pdgmvec (vc2req valclusters pos)
        ;;pdgmvec2 (map #(addpnum % ) pdgmvec)
        ;;vheader (first pdgmvec)
        pdgmstr1 (apply pr-str pdgmvec)
        pdgmstr2 (clojure.string/replace pdgmstr1 #"[\(\)\"]" "")
        pdgmtable (csv2pdgm pdgmstr2 valclusters)
        pdgms (str valclusters)
        ]
         (layout/common
           [:h3#clickable "Paradigms " pos ": "  ]
           [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
           [:hr]
           pdgmtable
           [:hr]
           [:h3 "Parallel Display of Paradigms"]
           [:p "At present only accommodates parallel display of pronominal and verbval paradigms where merged paradigms have same number of columns -- to be generalized."]         
           [:hr]
           (form-to [:post "/multimodplldisplay"]
                    [:table
                     [:tr [:td "PNames: "]
                      [:td 
                            [:select#names.required
                            {:title "Chosen PDGMS", :name "pdgmnames"}
                            [:option {:value (str valclusters)} "Paradigm Names (as above)"]]]]
                     [:tr [:td "Header: "]
                      [:td [:select#header.required
                            {:title "Header", :name "header"}
                            [:option {:value headers} (str headers)] 
                            ]]]
                     [:tr [:td "Pivots: "]
                      [:td
                       [:div {:class "form-group"}
                        [:label 
                         (for [head headers]
                           [:span
                           (check-box {:name "pivotlist[]" :value (.indexOf headers head)} head) head])]]]]
                     [:tr [:td "PString: "]
                     [:td [:select#pdgms.required
                            {:title "PDGMS", :name "pdgmstr2"}
                            [:option {:value pdgmstr2} "Paradigm Forms (as above)"]
                            ;;[:option {:value valclusters} (str valclusters)]
                            ]]]
                     ;; current algorithm combines actual png val configs
                     ;; into png vector made on the fly;
                     ;; next step is to allow choice of png vals as per
                     ;; text input fields below (for now can be left blank)
                     ;;[:tr [:td "Number: "]
                     ;; [:td [:input#num.required
                     ;;       {:title "Choose Number Values.", :name "nmbr"}
                     ;;       ]]]
                     ;;[:tr [:td "Person: " ]
                     ;; [:td [:input#pers.required
                     ;;       {:title "Choose Person Values.", :name "pers"}
                     ;;       ]]]
                     ;;[:tr [:td "Gender: " ]
                     ;; [:td [:input#gen.required
                     ;;       {:title "Choose Gender Values.", :name "gen"}
                     ;;       ]]]
                     ;;(submit-button "Get pdgm")
                     [:tr [:td ]
                      [:td [:input#submit
                            {:value "Display Paradigms in Parallel", :name "submit", :type "submit"}]]]])
           [:hr]
           [:div [:h4 "======= Debug Info: ======="]
            [:p "pdgmvec: " [:pre pdgmvec]]
            [:p "pos: " [:pre pos]]
            [:p "valclusters: " [:pre pdgms]]
            [:p "headerset2: " [:pre headerset2]]
            [:p "pdgmstr2: " [:pre pdgmstr2]]
            [:h4 "==========================="]]
           [:script {:src "js/goog/base.js" :type "text/javascript"}]
           [:script {:src "js/webapp.js" :type "text/javascript"}]
           [:script {:type "text/javascript"}
            "goog.require('webapp.core');"])))
    
(defn cleanpdgms [pdgmstr]
  "This version also gets rid of initial header row in each pdgm substring"
  (let [pdgmstr-a (clojure.string/replace pdgmstr #"\\r\\n$" "")
        pdgmstr-b (clojure.string/replace pdgmstr-a #"^.*?\\r\\n" "")
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
(defn vec-remove
  "remove elem in coll"
   [coll pos]
   (vec (clojure.core/concat (subvec coll 0 pos) (subvec coll (inc pos)))))

;; conj-in & merge-matches from http://stackoverflow.com/questions/2203213/
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

(defn handle-multimodplldisplay
  "In this version pivot/keyset can be generalized beyond png any col (eventually any sequence of cols) between col-1 and token column. (Need to find out how to 'presort' cols before initial display?)"
  [pdgms headerset2 pdgmstr2 pivotlist]
  (let [pnamestr1 (clojure.string/replace pdgms #"[\[\]\"]" "")
        pnamestr2 (clojure.string/replace pnamestr1 #"%" ".")
        pnames (split pnamestr2 #" ")        
        pivots (map read-string pivotlist)
        ;;pivot (read-string pivotname)
        ;; get rid of spurious line-feeds
        pdgmstr3 (cleanpdgms pdgmstr2)
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
        ;;pmaps (for [prmap prmaps] (apply conj prmap))
        pmaps (join-pmaps prmaps)
        headerset3 (clojure.string/replace headerset2 #"[\[\]\"]" "")
        heads (split (str headerset3) #" ")
        ;;headvec = headerset minus pivot namesn
        pivotnames (vec (for [pivot pivots] (nth heads pivot)))
        headvec (vec (remove (set pivotnames) heads))
        ;; set of lists of vaue-combination-terms
        keylists (vec (for [pmap pmaps] (keys pmap)))
        ;;keylists (set (keys pmap))
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
      [:p [:h3 "Parallel Display of Paradigms: Pivot " (str pivotnames)]]
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
     [:p "Paradigms:"
      [:ul
      (for [pname pnames]
        [:li pname])]]
      [:hr]
      [:table {:id "handlerTable" :class "tablesorter sar-table"}
         [:thead
          (for [head headvec]
            [:th [:div {:class "some-handle"} [:br] (capitalize head)]])
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
        ;;[:p "pdgms: " [:pre pdgms]]
        [:p "pnames: " (str pnames)]
        [:p "headerset2: " [:pre headerset2]]
        [:p "pivotlist: " (str pivotlist)]
        [:p "pivotnames: " (str pivotnames)]
        [:p "heads: " (str heads)]
        [:p "headvec: " (str headvec)]
        [:p "prows: "  (str prows) [:pre prows]]
        [:p "pcells: " (apply str pcells) [:pre pcells]]
        [:p "pivot-map: " [:pre pivot-map]]
        [:p "newpdgms: " [:pre newpdgms]]
        [:p "newpdgms: " (str newpdgms)]
        [:p "pvalvec: " (str pvalvec)]
        [:p "pdgmstr2: " [:pre pdgmstr2]]
        [:p "pdgmstr3: " [:pre pdgmstr3]]
        ;;[:p "vvec: " [:pre vvec]] ;;!!raises "not valid element" exception
        ;;[:p "vvec: " (str vvec)] ;; "not valid el." excp. with "!" in text
        [:p "pmapvec: " [:pre pmapvec]]
        [:p "pmapvec: " (str pmapvec)]
        [:p "prmaps: " [:pre prmaps]]
        [:p "pmaps: " [:pre pmaps]]
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


(defroutes multipdgmmod-routes
  (GET "/multipdgmmod" [] (multipdgmmod))
  (POST "/multimodqry" [languages pos] (handle-multimodqry languages pos))
  (POST "/multimoddisplay" [valclusters pos] (handle-multimoddisplay valclusters pos))
(POST "/multimodplldisplay" [pdgmnames header pdgmstr2 pivotlist] (handle-multimodplldisplay pdgmnames header pdgmstr2 pivotlist)))
