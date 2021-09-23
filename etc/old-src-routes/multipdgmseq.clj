(ns webapp.routes.multipdgmseq
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require 
            ;;[clojure.core/count :as count]
            [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            ;;[clojure.string :as str]
            [clojure.string :refer [capitalize lower-case split upper-case]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))


(def aama "http://localhost:3030/aama/query")

(defn multipdgmseq []
  (let [langlist (slurp "pvlists/menu-langs.txt")
        languages (split langlist #"\n")]
  (layout/common 
   [:h3 "Multi-Paradigm: Fixed Sequential Display"]
     ;;[:p "Use this option to pick one or more  paradigms from a given language or set of languages to be displayed in fixed format vertical succession."]
   [:p "Choose Languages and Type"]
   ;; [:p error]
   ;;[:hr]
   (form-to [:post "/multiseqqry"]
            [:table
             [:tr [:td "PDGM Type: "]
              [:td [:select#pos.required
                    {:title "Choose a pdgm type.", :name "pos"}
                    [:option {:value "fv" :label "Finite Verb"}]
                    [:option {:value "nfv" :label "Other Verb"}]
                    [:option {:value "sel" :label "Selector"}]
                    [:option {:value "pro" :label "Pronoun"}]
                    [:option {:value "noun" :label "Noun"}]
                    ]]]
             [:tr [:td "PDGM Language(s): " ]
             [:td 
               {:title "Choose one or more languages.", :name "language"}
               (for [language languages]
                 [:div {:class "form-group"}
                  [:label 
                   (check-box {:name "languages[]" :value (lower-case language)}language) language]])]]
                 ;; from https://groups.google.com/forum/#!topic/compojure/5Vm8QCQLsaQ
                 ;; (check-box "valclusters[]" false valcluster) (str valcluster)]]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Get PDGM Value Clusters", :name "submit", :type "submit"}]]]]
            )
   [:hr])))

(defn handle-multiseqqry
  [languages pos]
  (layout/common 
       [:h3 "Multi-Paradigm Fixed Sequential Display"]
       [:p "Choose Value Clusters For: "]
       ;;[:p error]
       [:hr]
   (form-to [:post "/multiseqdisplay"]
            [:table  {:class "linfo-table"}
             [:tr [:td "PDGM Type: " ]
              [:td
               (check-box {:name "pos" :value pos :checked "true"} pos) (str (upper-case pos))]]
             ;; Following :tr can be commented out if not in proof-reading mode
             ;; selectall jQuery script from  http://www.sanwebe.com/2014/01/how-to-select-all-deselect-checkboxes-jquery
              [:tr [:td "Scope"]
               [:td 
                [:div {:class "scope"} (check-box {:id "selectall"} "Select All") "Select All"]]]
                 [:tr [:td "PDGM Language(s): " ]
                   (for [language languages]
                     [:td 
                      [:div (str (capitalize language) " ")]])]
             [:tr [:td "PDGM Value Clusters: " ]
              (for [language languages]
                [:td 
                 {:title "Choose a value.", :name "valcluster"}
                 (let [valclusterfile (str "pvlists/vlcl-list-" language "-" pos ".txt")
                       valclusterlist (slurp valclusterfile)
                       ;;valclusterlst (clojure.string/replace valclusterlist #":.*?\n" "\n")
                       valclusterset (into (sorted-set) (clojure.string/split valclusterlist #"\n"))]
                   ;; For pdgm checkboxes, if pos is 'fv', there will be a
                   ;; label for the valcluster, then actual checkboxes will be 
                   ;; placed at different lexitems having the same valcluster. 
                   ;; Otherwise each valcluster will be a separate checkbox.
                   ;; The 'fv' type may be extended to other kinds of pdgms
                   ;; showing identical valclusters with different lex items
                   ;; (e.g., nominal paradigms with inflectional case of the
                   ;; Latin or Greek type).
                   (if (re-find #"EmptyList" valclusterlist)
                     [:div (str "There are no " pos " paradigms in the " language " archive.")]
                     (for [valcluster valclusterset]
                         [:div {:class "form-group"}
                          [:label
                           (check-box {:class "checkbox1" :name "valclusters[]" :value (str language "," valcluster) } valcluster) valcluster]])))])]
             ;;(submit-button "Get pdgm")
             [:tr [:td ]
              [:td [:input#submit
                    {:value "Display pdgms", :name "submit", :type "submit"}]]]])))

(defn handle-multiseqdisplay
 [valclusters pos]
 (layout/common
  (let 
      [lprefmap (read-string (slurp "pvlists/lprefs.clj"))]
    (for [valcluster valclusters]
      (let [vals (split valcluster #"," 2)
            language (first vals)
            vcluster (last vals)
            vcs (split vcluster #"," 2)
            pdgmType (first vcs)
            pvalues (last vcs)
            lang (read-string (str ":" language))
            lpref (lang lprefmap)
            ;;valstrng (clojure.string/replace vcluster #",*person|,*gender|,*number" "")
            valstr (clojure.string/replace vcluster #":," ":")
            query-sparql (cond 
                          (= pos "pro")
                          (sparql/pdgmqry-sparql-pro language lpref valstr)
                          (= pos "sel")
                          (sparql/pdgmqry-sparql-sel language lpref valstr)
                          (= pos "noun")
                          ;; should be able to combine next 2 cases w. disjunct or 'begins-with'
                          (sparql/pdgmqry-sparql-noun language lpref vcluster)
                          ;;(= pdgmType "Finite")
                          (re-find #"Finite" pdgmType)
                           (sparql/pdgmqry-sparql-fv language lpref pvalues)
                          :else (sparql/pdgmqry-sparql-nfv language lpref vcluster))
            query-sparql-pr (clojure.string/replace query-sparql #"<" "&lt;")
            req (http/get aama
                      {:query-params
                       {"query" query-sparql ;;generated sparql
                        ;;"format" "application/sparql-results+json"}})]
                        "format" "text"}})
            req2 (clojure.string/replace (:body req) #"%%" " + ")
            ]
        [:div
         [:hr]
         [:h4 "Valcluster: " valcluster]
         [:pre (:body req)]
         ;;[:h4 "======= Debug Info: ======="]
         ;;[:p "pdgmType: " [:pre pdgmType]]
         ;;[:p "pvalues: " [:pre pvalues]]
         ;;[:h3#clickable "Query:"]
         ;;[:pre query-sparql-pr]
         ;;[:p "req2: " [:pre req2]]
         ;;[:p "==========================="]
         ])))
        [:script {:src "js/goog/base.js" :type "text/javascript"}]
        [:script {:src "js/webapp.js" :type "text/javascript"}]
        [:script {:type "text/javascript"}
         "goog.require('webapp.core');"]))

(defroutes multipdgmseq-routes
  (GET "/multipdgmseq" [] (multipdgmseq))
  (POST "/multiseqqry" [languages pos] (handle-multiseqqry languages pos))
  (POST "/multiseqdisplay" [valclusters pos] (handle-multiseqdisplay valclusters pos)))
