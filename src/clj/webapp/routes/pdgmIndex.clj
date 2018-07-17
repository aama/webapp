(ns webapp.routes.pdgmIndex
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

(defn pdgmIndex []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")
        ldomlist (slurp "pvlists/ldomainlist.txt")
        ldoms (split ldomlist #"\n")]
    (layout/common 
     [:h3 "Create Paradigm Lists"]
     (form-to [:post "/pdgmIndex-gen"]
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
               ;; Can't find out why following doesn't work:
               ;; [:td [:select#language.required
               ;;       {:title "Choose a language.", :name "language"}
               ;;     (for [language languages]
               ;;     [:option {:value (lower-case language)} language])]]]
               ;;(submit-button "Make pdgm list")
               [:tr [:td ]
                [:td [:input#submit
                      {:value "PDGM Value-Cluster List", :name "submit", :type "submit"}]]]]))))

(defn makeplist
  [termclusters]
  (for [termcluster termclusters]
    (let [label (termcluster :label)
          terms (termcluster :terms)
          schema (pop (first terms))
          common (into (sorted-map) (termcluster :common))
          posval (common :pos)
          ;;lexval (common :lexeme)
          commonVSet (vals common)
          ;; combine proClass nmorphClass vmorphClass into single category morphClass
          morphClassval (cond 
                         (= posval :Pronoun) 
                         (common :pmorphClass) 
                         (= posval :Noun)
                         (common :nmorphClass)
                         :else
                         (common :vmorphClass))
          ;;pmorphTypeval (common :pmorphType) 
          pvstring (str (dissoc common :pos :vmorphClass :nmorphClass :pmorphClass))
          pvstring1 (clojure.string/replace pvstring #"(\w) :" "$1=")
          pvstring2 (clojure.string/replace pvstring1 #"[/{/}\s]" "")
          schemastr (clojure.string/replace (apply str schema) #":" ",")
          pindex1 (str label "," posval "," morphClassval "," pvstring2 "%" schemastr )
          pindex2 (clojure.string/replace pindex1 #":" "")
          ;; if want to take :lexeme out of common
          ;;pindex2 (str (clojure.string/replace pindex1 #":" "") lexval)
          ;; for next two, adjust N on pindexN acc. to choice
          ;; if want to have Prop:Val in schema
          ;;pindex3 (clojure.string/replace pindex2 #",(\w*?)=" ",$1:")
          ;; if want only Val in schema
          ;;pindex3 (clojure.string/replace pindex2 #",\w*?=" ",")
          ]
      (clojure.string/replace pindex2 #",*%,*" "%"))))

(defn csv2map
  "Maps the content of the label+pdgmValueCluster vector into a (keywordized) pdgmVC to label map"
  [vlclvec]
  (let [csvmap1 (for [vlcl vlclvec] (hash-map (replace (first (split vlcl #"," 2)) #"," "_") (last (split vlcl #"," 2))))
        csvmap2 (into (sorted-map) (clojure.walk/keywordize-keys csvmap1))]
    (join ",\n" (split (str csvmap2) #", "))))


(defn handle-pdgmIndex-gen
  [ldomain]
  (layout/common
   [:body
    ;;[:h3#clickable "Value-clusters used in " pos " pdgms for: " ldomain]
    (let [languages (split ldomain #",")]
      (for [language languages]
        (let [inputfile ( str "../aama-data/data/" language "/" language "-pdgms.edn")
              pdgmstring (slurp inputfile)
              pdgm-map (read-string pdgmstring)
              ;; The following are possible lists and tables of pdgm values
              vlclindex (str "pvlists/pdgm-index-" language ".txt")
              labellist (str "pvlists/pdgm-label-" language ".edn")
              proptable (str "pvlists/pdgm-table-" language ".txt")
              termclusters (:termclusters pdgm-map)
              ;; get set of all :common props, plus label
              vlclvec (makeplist termclusters)
              ;;get rid of label for pdgm index
              vlcllist1 (for [vlcl vlclvec] 
               (last (split vlcl #"," 2))) 
              vlcllist (join "\n" (into (sorted-set) vlcllist1))
              ;; map label to value cluster
              labelmap (csv2map vlclvec)
              headprops (join "," 
                              (for [termcluster termclusters] 
                                (join "," (keys (termcluster :common)))))
              ;; then disj props from pos, v/nmorphClass, proClass, lexeme
              headpropvec1 (into (sorted-set) (split headprops #","))
              headpropvec2 (disj headpropvec1 ":pos" ":lexeme" ":nmorphClass" ":pmorphClass" ":vmorphClass")
              headpropstr (join "," headpropvec2)
              ;;headpropkeys (for [headprop headpropvec2] 
              ;;               (clojure.string/replace headprop #":" ""))
              ;;make pdgm property csv
              ;;pdgmtablerows (makeproptablerows vlcllist headpropkeys)
              pdgmtableheads (str ":pdgm,:pos,:morphClass," headpropstr ",:lexeme")
              pdgmtable (apply str pdgmtableheads)
              ]
          (spit vlclindex vlcllist)
          (spit proptable pdgmtable)
          (spit labellist labelmap)
          [:div 
           [:p [:b "Ldomain: "] [:pre language]]
           [:p [:b "pdgm-index:    "] [:pre vlcllist]]
           [:p [:b "pdgm-table:  "] [:pre pdgmtable]]
           [:p [:b "label-index:    "] [:pre labelmap]]
           [:p " "]
           ;;[:h4 "======= Debug Info: ======="]
           ;;[:p [:b "Prop table heads1:  "] [:pre headprops]]
           ;;[:p [:b "Sorted Prop head set:  "] [:pre headpropvec2]]
           ;;[:p [:b "Prop head string:  "] [:pre headpropstr]]
           ;;[:p [:b "Prop head keys:  "] [:pre headpropkeys]]
           ;;[:p [:b "Pdgmtable Heads:  "] [:pre pdgmtableheads]]
           ;;[:p [:b "Pdgmtable Rows:  "] [:pre pdgmtablerows]]
           ;;[:p [:b "File vlclvec:    "] [:p vlclvec]]
           ;;[:p "==========================="]
           ])))
    [:script {:src "js/goog/base.js" :type "text/javascript"}]
    [:script {:src "js/webapp.js" :type "text/javascript"}]
    [:script {:type "text/javascript"}
     "goog.require('webapp.core');"]]))


(defroutes pdgmIndex-routes
  (GET "/pdgmIndex" [] (pdgmIndex))
  (POST "/pdgmIndex-gen" [ldomain] (handle-pdgmIndex-gen ldomain)))


