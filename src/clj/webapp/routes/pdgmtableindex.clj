(ns webapp.routes.pdgmtableindex
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

(defn pdgmtableindex []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
    (layout/common 
     [:h3 "Paradigm Source-Property Index: Sortable Display "]
     [:p "Use this option to sort paradigms from a given language according to index properties or source."]
     [:p "Choose Language"]
     (form-to [:post "/pdgmtableindexqry"]
              [:table
               [:tr [:td "PDGM Language: " ]
                [:td [:select#language.required
                      {:title "Choose a language.", :name "language"}
                      (for [language languages]
                        [:option {:value (lower-case language)} language])]]]
               [:tr [:td ]
                [:td [:input#submit
                      {:value "Get PDGM Value Clusters", :name "submit", :type "submit"}]]]]
              )
     [:hr])))

;;(def tableheads [:source,:pos,:morphClass,:clauseType,:conjClass,:gender,:lexeme,:nonFiniteForm,:number,:person,:polarity,:proClass,:rootClass,:stemClass,:strength,:tam])
;;(def headprops (for [tablehead tableheads] (clojure.string/replace tablehead #":" "")))

(defn maketableheads 
  ;; Read in table heads (from pdgm-table- file or from pdgm-index?) and merge
  [language]
  (let [vlcltableheadsfile (str "pvlists/pdgm-table-source-" language ".txt")
        vlcltableheads (slurp vlcltableheadsfile)
        ;;headprops (split vlcltableheads #",")
        headprops (for [tablehead (split vlcltableheads #",")]
                    (clojure.string/replace tablehead #":" ""))
        ]
    (vec headprops)))

(defn maketablerows
  ;; Read in table rows from pdgm-index and make csv table for propvals
  ;; Typical source line:
  ;; Wedekind-etal2007 p. 103,Verb,Finite,conjClass=Prefix,lexeme=diy,polarity=Negative,rootClass=Ciy,tam=Present%number,person,gender,token
  [language tableheads]
  (let [valclusterfile (str "pvlists/pdgm-source-" language ".txt")
        valclusterlist (slurp valclusterfile)
        vlclvec (split valclusterlist #"\n")]
    ;;parse each source text entry
    (for [vlcl vlclvec]
      (let [
            vcs (split vlcl #"," 2)
            ref (first vcs)
            mvalsprops (split (last vcs) #"%" 2)
            mv (first mvalsprops)
            pos (first (split mv #"," 2))
            mcprop (last (split mv #"," 2))
            morphclass (first (split mcprop #"," 2))
            props (if (re-find #"," mcprop) 
                    (last (split mcprop #"," 2)) 
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
            propmap2 (into (sorted-map) propmap)
            propseq (apply str 
                           (for [tablehead tableheads] 
                             (if ( (keyword tablehead) propmap2) 
                               ( str  ( (keyword tablehead) propmap2) ", ") 
                               ( str ", " ))))
            ;; proseq begins with two redundant commas and ends with one
            ;; w. need to be deleted [why?] 
            propseq2 (clojure.string/replace propseq #"^, , " "")
            propseq3 (clojure.string/replace propseq2 #",\s*$" "")
            ;;tline2  (str ref "," pos "," morphclass   propseq3  "\r\n")
            ]
        (str ref "," pos "," morphclass   propseq3  "\r\n")))))                      

(defn handle-pdgmtableindexqry
  [language]
  (let [tableheads (maketableheads language)
        tablerows (maketablerows language tableheads)
        valrows  (split (apply str tablerows) #"\r\n")
        ]
    (layout/common
     [:h3 "PDGM Source Table For: "]
     [:li (capitalize language)]
     [:p " "]
     [:table {:id "handlerTable" :class "tablesorter sar-table"}
      [:thead
       [:tr
        (for [tablehead tableheads]
          [:th [:div {:class "some-handle"} [:br] (capitalize tablehead)]]
          )
        ]]
      [:tbody 
       (for [valrow valrows]
         [:tr
          (let [;;tablecells (split valrow #"&&" 2)
                ;;langPname (first tablecells)
                ;;language (first (split langPname #"," 2 ))
                ;;pdgmname (last (split langPname #"," 2 ))
                ;;pdgmprops (last tablecells)
                propcells (split valrow #",")
                ;;pos (first (split pdgmprops #"," 2))
                ;;morphprops (next (split pdgmprops #"," 2 ))
                ;;morphclass (first (split morphprops #"," ))
                ;;propcells (next (split morphprops #"," ))
                ]
            [:div
             (for [propcell propcells]
               [:td propcell]
               )
             ;;[:td lexeme]
             ])])]]
     [:div 
      [:h4 "======= Debug Info: ======="]
      [:p [:b "Table Heads:  "] [:pre (join ", " tableheads)]]
      [:p [:b "Value Rows:  "] 
       (for [valrow valrows] [:pre valrow])]
      [:p "==========================="]
      ]
     [:script {:src "js/goog/base.js" :type "text/javascript"}]
     [:script {:src "js/webapp.js" :type "text/javascript"}]
     [:script {:type "text/javascript"}
      "goog.require('webapp.core');"])))

(defroutes pdgmtableindex-routes
  (GET "/pdgmtableindex" [] (pdgmtableindex))
  (POST "/pdgmtableindexqry" [language] (handle-pdgmtableindexqry language)))
