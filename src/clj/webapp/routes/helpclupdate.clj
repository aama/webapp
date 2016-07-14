(ns webapp.routes.helpclupdate
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

(defn helpclupdate []
  (layout/common 
   [:div {:class "help-page"}
     [:h3 "Help: Update [Command-line]"]
     [:p "The following command-line versions presuppose that the edn data files are in the  ~/aama-data/data/[LANG] directories:"]
      [:ul [:li [:h4 "Datastore Update."] 
            [:p "The following script, run from the webapp dir,  will:" 
             [:ol 
              [:li "Delete current LANG sub-graph(s) from the datastore, if already in existence"]
              ;;[:li "(Optionally: Run triple-count and sub-graph-list queries to verify deletion(s))"]
              [:li "Transform the new or revised edn file to a ttl file"]
              [:li "Insert new or revised LANG sub-graph(s) into datastore"]
              ;;[:li "(Optionally: Run triple-count and sub-graph-list queries to verify insertion(s))"]
              ]]
            [:p "Usage:" 
            [:ul 
             [:li "bin/aama-datastore-update.sh ../aama-data/data/[LANG]  (for a single language)"]
             [:li "bin/aama-datastore-update.sh \"../aama-data/data/*\" (to [re-]initiate the whole datastore)"]
             [:li "[bin/fuqueries.sh (optionally, before or after update, will run triple-count and subgraph-list queries to verify deletions and/or insertions)]"]]]
       [:li [:h4 "Datastore Upload."] 
        [:p "The following scripts, run from the webapp dir, will:"
         [:ol
          [:li "Upload revised edn/ttl file(s) to aama/[LANG] repository"]
          [:li "Push the new edn/ttl file(s) to origin ("[:em "github.com/aama/[LANG]"]")"]
          [:li "Upload and push a revised aama-edn2ttl jar and source file"]]]
        [:p "Usage:"
        [:ul 
         [:li "bin/aama-cp2lngrepo.sh ../aama-data/data/[LANG] (for a single language)"]
         [:li "bin/aama-cp2lngrepo.sh \"../aama-data/data/*\" (to [re-]upload the whole datastore)"]
         [:li "bin/aama-cptools2lngrepo.sh (necessary only if aama-edn2ttl.jar has been revised)"]]]]
       [:li [:h4 "Revise aama-edn2ttl.jar."] 
        [:p "Occasionally new schemata elements and relations will be desired, or old ones will need to be corrected, in the mapping of the edn data files to ttl."]
        [:p "Procedure: "
        [:ol 
         [:li "First,  from the ~/webapp/bin/tools/edn2ttl dir:"
          [:ol
           [:li "After revisions of the source code in ~/webapp/bin/tools/edn2ttl/src/edn2ttl/core.clj, test whether a satisfactory [LANG]-pdgms.ttl is derived from a sample [LANG]-pdgms.edn file which has been copied to the ~/webapp/bin/tools/edn2ttl directory:"
            [:ul
           [:li "lein run [LANG]-pdgms.edn > [LANG]-pdgms.ttl"]]]
           [:li "When satisfied, make jar file:"
            [:ul
             [:li "lein uberjar"]]]
           [:li "Copy jar file to edn2ttl directory and to ~/.jar"
            [:ul
             [:li "cp target/edn2ttl2-0.1.0-SNAPSHOT-standalone.jar aama-edn2ttl.jar"]
             [:li "cp target/edn2ttl2-0.1.0-SNAPSHOT-standalone.jar ~/.jar/aama-edn2ttl.jar"]]]]]
         [:li "Then, if necessary, update and upload whole datastore as above (from webapp directory):"
          [:ul
           [:li "bin/aama-datastore-update.sh \"../aama-data/data/*\""]
           [:li "bin/aama-cp2lngrepo.sh \"../aama-data/data/*\""]]]]]]]
       [:li [:h4 "(Don't forget to commit and push webapp and aama-data!)"]]]
    
   [:hr]
   [:h4 "[For more detail on the above, cf. the github aama-data/bin " (link-to "https://github.com/gbgg/aama-data/blob/master/bin/README.md" "README")"]"]]))

(defroutes helpclupdate-routes
  (GET "/helpclupdate" [] (helpclupdate)))


