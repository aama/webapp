(ns webapp.routes.multipdgmmod
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require 
            ;;[clojure.core/count :as count]
            [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize lower-case split upper-case]]
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
   ;; [:p error]
   ;;[:hr]
   (form-to [:post "/multimodqry"]
            [:table
             [:tr [:td "PDGM Type: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "fv" :label "Finite Verb"}]
                    [:option {:value "nfv" :label "Non-finite Verb"}]
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
       ;;[:h3 "Paradigms"]
       ;;[:p "Choose Value Clusters For: " language "/" pos]
       ;;[:p error]
       ;;[:hr]
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
                   (for [language languages]
                  [:td 
                   {:title "Choose a value.", :name "valcluster"}
                     (let [valclusterfile (str "pvlists/plexname-" pos "-list-" language ".txt")
                           valclusterlist (slurp valclusterfile)
                           valclusters (split valclusterlist #"\n")]
                       ;; For pdgm checkboxes, if pos is 'fv', there will be a
                       ;; label for the valcluster, then actual checkboxes will be 
                       ;; placed at different lexitems having the same valcluster. 
                       ;; Otherwise each valcluster will be a separate checkbox.
                       ;; The 'fv' type may be extended to other kinds of pdgms
                       ;; showing identical valclusters with different lex items
                       ;; (e.g., nominal paradigms with inflectional case of the
                       ;; Latin or Greek type).
                       (for [valcluster valclusters]
                         (if (= pos "fv")
                           (let [clusters (split valcluster #":")
                                 clustername (first clusters)
                                 plex (last clusters)
                                 lexitems (split plex #",")]
                             [:div {:class "form-group"}
                              [:label (str clustername ": ")
                               (for [lex lexitems]
                                 [:span 
                                  (check-box {:name "valclusters[]" :value (str language "," clustername ":" lex) } lex) lex])]])
                           [:div {:class "form-group"}
                            [:label
                             (check-box {:class "checkbox1" :name "valclusters[]" :value (str language "," valcluster) } valcluster) valcluster]])))])]
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
            language (first vals)
            vcluster (last vals)
            lang (read-string (str ":" language))
            lpref (lang lprefmap)
            valstrng (clojure.string/replace vcluster #",*person|,*gender|,*number" "")
            valstr (clojure.string/replace valstrng #":," ":")
            query-sparql (cond 
                          (= pos "pro")
                          (sparql/pdgmqry-sparql-pro language lpref valstr)
                          (= pos "nfv")
                          (sparql/pdgmqry-sparql-nfv language lpref vcluster)
                          (= pos "noun")
                          (sparql/pdgmqry-sparql-noun language lpref vcluster)
                          :else (sparql/pdgmqry-sparql-fv language lpref vcluster))
            req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "csv"}})
            ]
        ;;(str (:body req))))
        (clojure.string/replace (:body req) #" " "_"))
    )))

(defn csv2pdgm
"Takes sorted 4-col csv list with vectors of pnames and headers, and outputs 5-col html table with first col for pname ref; cols are draggable and sortable."
 [pdgmstr2 valclusters]
(let  [pdgms (str valclusters)
       pnamestr (clojure.string/replace pdgms #"[\[\]\"]" "")
       pnames (split pnamestr #" ")
       ;; pdgmstr2 is a string of space-separated pdgmstrings, whose rows are
       ;; separated by \r\n and cells separated by ","
       ;; Take off the top header
       ;; If pdgms are to be comparable
       ;; all header strings will be same
       psplit (split pdgmstr2 #"\\r\\n" 2)
       header (first psplit)
       header2 (str "pdgm," header)
       pheads (split header2 #",")
       pstrings (split pdgmstr2 #" ")
       pdgmnum (atom 0)
       ]
  [:div
   [:p "Paradigms:"
    [:ol
     (for [pname pnames]
       [:li pname])]]
   [:hr]
   ;;[:pre pdgmstr2]
   ;;[:hr]
   ;; For visible borders set {:border "1"}.
   [:table {:id "handlerTable" :class "tablesorter sar-table"}
    [:thead
     [:tr
      (for [head pheads]
        [:th [:div {:class "some-handle"}] head])]]
    [:tbody 
     (for [pdgm pstrings]
       (let [pdgm-sp (split pdgm #"\\r\\n" 2)
             pheader (first pdgm-sp)
             pbody (last pdgm-sp)
             pdgmrows (split pbody #"\\r\\n")
             pnum (swap! pdgmnum inc)
             ]
         (if (= header pheader)
           (for [pdgmrow pdgmrows]
             [:tr
              [:td (str "P-" pnum)]
              (let [pdgmrow2 (clojure.string/replace pdgmrow #"_" " ")
                    pdgmcells (split pdgmrow2 #",")]
                (for [pdgmcell pdgmcells]
                  [:td pdgmcell]))])
           ;;([:tr [:td (str "P-" pnum " does not have the header: " header)]])
           )))]]]))
        

(defn handle-multimoddisplay
  [valclusters pos]
  ;; send SPARQL over HTTP request
  (let [headerset1 (str "Paradigm " "Number " "Person " "Gender " "Token ")
        headerset2 (str "Number " "Person " "Gender " "Token ")
        headers (split headerset1 #" ")
        pdgmvec (map #(vc2req  % pos) valclusters)
        pdgmstr1 (apply pr-str pdgmvec)
        pdgmstr2 (clojure.string/replace pdgmstr1 #"[\(\)\"]" "")
        pdgmtable (csv2pdgm pdgmstr2 valclusters)
        pdgms (str valclusters)
        pnamestr (clojure.string/replace pdgms #"[\[\]\"]" "")
        pnames (split pnamestr #" ")
        ]
         (layout/common
           [:h3#clickable "Paradigms " pos ": "  ]
           [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
           [:hr]
           pdgmtable
           [:hr]
           [:h3 "Parallel Display of Paradigms (Personal Pronoun and Finite Verb Only)"]
           [:p "At present only accommodates parallel display of paradigms with columns 'Number Person Gender Token' -- to be generalized."]
           [:hr]
           (form-to [:post "/multimodplldisplay"]
                    [:table
                     [:tr [:td "PNames: "]
                      [:td 
                       ;;[:ol
                       ;; (for [pname pnames]
                       ;;   [:li pname])]]]
                            [:select#names.required
                            {:title "Chosen PDGMS", :name "pdgmnames"}
                            [:option {:value (str valclusters)} "Paradigm Names (as above)"]]]]
                     [:tr [:td "Header: "]
                      [:td [:select#header.required
                            {:title "Header", :name "header"}
                            [:option {:value headerset2} headerset2] 
                            ]]]
                     [:tr [:td "PString: "]
                     [:td [:select#pdgms.required
                            {:title "PDGMS", :name "pdgmstr2"}
                            [:option {:value pdgmstr2} "Paradigm Forms (as above)"]
                            ;;[:option {:value valclusters} (str valclusters)]
                            ]]]
                     ;; current algorithm will simply take png enumerations
                     ;; already listed in pvlists/npg.clj
                     ;; [MAKE SURE EVERYTHING RELEVANT IS THERE!]
                     ;; second step will be to combine actual png val configs
                     ;; into png vector made on the fly
                     ;; third step is to allow choice of png vals as per
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
     [:script {:src "js/goog/base.js" :type "text/javascript"}]
     [:script {:src "js/webapp.js" :type "text/javascript"}]
     [:script {:type "text/javascript"}
      "goog.require('webapp.core');"])))

(defn pstring2map
  [pdgm]
  (let [pdgm1 (clojure.string/replace pdgm #"^.*?\\r\\n" "") ;; header out
        pdgmstring (clojure.string/replace pdgm1 #"\\r\\n" "%%") 
        pdgmstr (clojure.string/replace pdgmstring #",([^,]*%%)" "&$1")
        pdgm2 (clojure.string/replace pdgmstr #"&" " ")
        pdgm3  (clojure.string/replace pdgm2 #"%%" " ")
        plist (split pdgm3 #" ")
        pmap (apply hash-map plist)
        ]
    (clojure.walk/keywordize-keys pmap)
    ))

(defn handle-multimodplldisplay
  "This version relies on external npg.clj file for sort order; can be eliminated as soon as is clear that handle-multimodplldisplay2 is adequate"
  [pdgms headerset2 pdgmstr2]
  (let [pngstring (slurp "pvlists/npg.clj")
        pngs (split pngstring #" ")
        pnamestr (clojure.string/replace pdgms #"[\[\]\"]" "")
        pnames (split pnamestr #" ")
        pstrings (split pdgmstr2 #" ")
        pmaps (for [pdgm pstrings] (pstring2map pdgm))
        headerset (str "Number " "Person " "Gender ")
        heads (split headerset #" ")
        pdgmnums (into [] (take (clojure.core/count pnames) (iterate inc 1)))
        ;; There has to be an easier way to get to keyset!
        keylists (set (for [pmap pmaps] (keys pmap)))
        keystring (clojure.string/replace (str keylists) #"[#(){}]" "")
        keyvec (split keystring #" ")
        keyset (set keyvec)
        ]
    (layout/common
     [:body
      [:h3 "Parallel Display of Paradigms:" ]
      ;;[:p "pmaps: " [:pre pmaps]]
      ;;[:p "keylists: " [:pre keylists]]
      ;;[:p "keystring: " [:pre keystring]]
      ;;[:p "keyset: " [:pre keyset]]
     [:p "Paradigms:"
      [:ol
      (for [pname pnames]
        [:li pname])]]
      [:hr]
      [:table {:id "handlerTable" :class "tablesorter sar-table"}
         [:thead
          (for [head heads]
            [:th [:div {:class "some-handle"}] head])
          (for [pdgmnum pdgmnums]
            [:th [:div {:class "some-handle"}] (str "P-" pdgmnum)])]
       [:tbody
        (for [png pngs]
          (if (contains? keyset (str (keyword png)))
            [:tr
             (let [npgs (split png #",")
                   pngk (keyword png)]
               [:div
                (for [npg npgs]
                  [:td npg])
                (for [pmap pmaps]
               [:td (pngk pmap)])])]))]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]]])))
    
(defn handle-multimodplldisplay2
  "This version does not rely on external png file for sort order; keyset should be generalized beyond png to any sequence of cols between col-1 ('pivot', here limited to paradigms) and token column. Also need to 'presort' cols before initial display."
  [pdgms headerset2 pdgmstr2]
  (let [pnamestr (clojure.string/replace pdgms #"[\[\]\"]" "")
        pnames (split pnamestr #" ")
        pstrings (split pdgmstr2 #" ")
        pmaps (for [pdgm pstrings] (pstring2map pdgm))
        ;; headerset should be derived from pdgm query (=headerset2)
        headerset (str "Number " "Person " "Gender ")
        heads (split headerset #" ")
        pdgmnums (into [] (take (clojure.core/count pnames) (iterate inc 1)))
        ;; There has to be an easier way to get to keyset!
        keylists (set (for [pmap pmaps] (keys pmap)))
        keystring (clojure.string/replace (str keylists) #"[#(){}]" "")
        keyvec (split keystring #" ")
        keyset (set keyvec)
        ]
    (layout/common
     [:body
      [:h3 "Parallel Display of Paradigms:" ]
      [:p "Click on column to sort (multiple sort by holding down shift key). Columns can be dragged by clicking and holding on 'drag-bar' at top of column."]
      ;;[:p "pmaps: " [:pre pmaps]]
      ;;[:p "keylists: " [:pre keylists]]
      ;;[:p "keystring: " [:pre keystring]]
      ;;[:p "keyset: " [:pre keyset]]
     [:p "Paradigms:"
      [:ol
      (for [pname pnames]
        [:li pname])]]
      [:hr]
      [:table {:id "handlerTable" :class "tablesorter sar-table"}
         [:thead
          (for [head heads]
            [:th [:div {:class "some-handle"}] head])
          (for [pdgmnum pdgmnums]
            [:th [:div {:class "some-handle"}] (str "P-" pdgmnum)])]
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
               [:td (kstrkey pmap)])])])]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]]])))
    

(defroutes multipdgmmod-routes
  (GET "/multipdgmmod" [] (multipdgmmod))
  (POST "/multimodqry" [languages pos] (handle-multimodqry languages pos))
  (POST "/multimoddisplay" [valclusters pos] (handle-multimoddisplay valclusters pos))
  (POST "/multimodplldisplay" [pdgmnames header pdgmstr2] (handle-multimodplldisplay2 pdgmnames header pdgmstr2)))
