 (ns webapp.routes.helppdgms
 (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [capitalize split]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]))

;; whole file needs to be updated wrt current layout.clj

(defn helppdgms []
  (layout/common 
   [:div {:class "info-page"}
   [:h3 "Help: Paradigms"]
    [:p "These pages experiment with different possibilities for display and comparison of paradigms. The comparisons for the moment are oriented to png-centered displays, and thus work reasonably well for finite verb and pronominal paradigms. Their application is less clear for non-finite verbs. Note that the present datastore contains " [:em "very "] "little material for nominal inflection."]
    [:ul
      [:li (link-to "/pdgm" "Individual Paradigms")
       [:ul [:li [:p "This query-type prompts for a \"paradigm-type\" (Finite Verb, Non-finite Verb, Pronoun, Noun) and a language; it then shows a drop-down select list of paradigms in that language of that type, and returns a table-formatted display of the selected paradigm, followed by the query that produced it."]]]]
      [:li "Multiple Paradigms"
       [:p
       [:ul
      [:li (link-to "/multipdgmseq"  "Multiparadigm Sequential Fixed Display")
       [:ul [:li [:p "A first checkbox allows the selection of one or more languages, and a second the selection of one or more paradigms from each of these languages. A sequence of default table-formatted displays of the selected paradigms is returned."]
             [:p "NB: The \"Select All\" option is principally to allow print-outs for proof-reading purposes."]]]
      [:li [:p  "Multiparadigm Combined Modifiable Display. Thereare two sub-possibilities:"]
       [:ol [:li [:p (link-to "/multipdgmmod"  "Multiparadigm Display from \"Value-Cluster\" list.") " A first checkbox allows the selection of one or more languages, and a second the selection of one or more paradigms (\"value-clusters\") from each of these languages. The routine first returns a single sortable table display of the selected paradigm(s) with draggable columns. A selection button permits the reformatting of this table into a (sortable, draggable) table with the paradigms in parallel columns. (Defined currently only for finite-verb and pronominal Number-Person-Gender-Token paradigms.) "]]
        [:li [:p (link-to "/multipdgmsort"  "Multiparadigm Display from sortable Property-Value table.") " The first page is checkbox which allows a selection of one or more languages. The second gives a sortable table where each morphological value is in a column headed by the relevant property; a checkbox is associated with each paradigm-defining row. The single and parallel token-column displays are as above."]] 
]]]]]]]]))

(defroutes helppdgms-routes
  (GET "/helppdgms" [] (helppdgms)))


