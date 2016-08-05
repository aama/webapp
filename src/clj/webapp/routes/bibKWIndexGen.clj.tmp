(ns webapp.routes.bibKWIndexGen
 (:refer-clojure :exclude [filter concat group-by max min count replace])
  (:require [compojure.core :refer :all]
            [webapp.views.layout :as layout]
            [webapp.models.sparql :as sparql]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :refer [split join lower-case upper-case replace]]
            [stencil.core :as tmpl]
            [clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log]
            [hiccup.form :refer :all]))

(def aama "http://localhost:3030/aama/query")

(defn bibKWIndexGen []
    (layout/common 
     ;;[:h1#clickable "Afroasiatic Morphological Archive"]
     [:h3 "Generate Bibliography Indices"]
     [:p "To be invoked whenever 'pvlists/bibrefs.edn', the general bibliography file, has been modified."]
     [:p "This option will (re-)generate the following indices:"
      [:ol 
       [:li [:em "pvlists/bibkwindex.edn"]": a map linking each keyword used in  bibrefs.edn, with a list of the associated bibref IDs."]
       [:li [:em "pvlists/bibref-master-list.txt"]": a sorted list of all the bibref IDs [used in the general bibliography menu checkbox list]."]
       [:li [:em "pvlists/bibref-keyword-list.txt"]": a sorted list of all the keywords [used in the keyword menu selection list]."]]]
     [:p (form-to [:post "/bibKWIndexGen"]
              [:table
               [:tr 
                [:td {:colspan "2"} [:input#submit
                                     {:value "Generate Index Files: ", :name "submit", :type "submit"}]]]])]))

(defn make-kwindex  [bibrefmap]
  (for [key (keys bibrefmap)]
    (for [kw (split (last (key bibrefmap)) #" ")]
      (str kw "," key))))
  
(defn compact-list
"Takes string representing sorted bipartite list of bibrefID  keywords, with divider ',', and builds up list with single mention of each keyword paired with space-separated sting  of bibrefIDs."
 [kwlist]
 (let  [curpart1 (atom "")]
   (for [kwentry kwlist]
         (let [partmap (zipmap [:part1 :part2] (split kwentry #"," 2))]
           (if (= (:part1 partmap) @curpart1)
               (str " " (:part2 partmap))
             (do (reset! curpart1 (:part1 partmap))
                 (str ", " @curpart1 " " (:part2 partmap))))))))

(defn handle-bibKWIndexGen
  []
  (let [
        bibrefmap (read-string (slurp "pvlists/bibrefs.edn"))
        klist (sort (flatten (make-kwindex bibrefmap)))
        kwcompact (apply str (compact-list klist))
        kwcomp (clojure.string/replace kwcompact #"^, " "")
        kwvec (split kwcomp #", ")
        kwmap (for [kw kwvec] (hash-map (first (split kw #" " 2)) (last (split kw #" " 2))))
        kmap  (into (sorted-map) (clojure.walk/keywordize-keys  kwmap))
        bibkeys1 (clojure.string/replace (str (sort (keys bibrefmap))) #"[:\)\(]" "")
        bibkeys2 (clojure.string/replace bibkeys1 #" " "\n")
        bibkw1 (clojure.string/replace (str (sort (keys kmap))) #"[:\)\(]" "")
        bibkw2 (clojure.string/replace bibkw1 #" " "\n")
        ]
    (spit "pvlists/bibkwindex.edn" kmap)
    (spit "pvlists/bibref-master-list.txt" bibkeys2)
    (spit "pvlists/bibref-keyword-list.txt" bibkw2)
    (layout/common
     [:body
      ;;[:h1#clickable "Afroasiatic Morphological Archive"]
      [:h4 "A regenerated Key Word Index has been written to pvlists/bibkwindex.edn"]
      [:p "kwvec: " kwvec]
      [:p "kwmap: " [:pre kwmap]]
      [:p "kmap1: " kmap]
      [:p "kmap2: " [:pre (clojure.string/replace kmap #"," "\n")]]
      (let [kmap2 (clojure.string/replace (str kmap) #"[{}]" "")
            kmapvec (split kmap2 #",")]
      [:table
       [:tr
        [:th "Key Word"] [:th "Bibrefs"]]
       (for [map kmapvec]
         (let [map2 (clojure.string/replace map #"\"$" "")]
         [:tr
          ;;[:td map][:td map]])])
         [:td (first (split map2 #" \"" 2))]
         [:td (last (split map2 #" \"" 2))]]))])
      [:p "bibref keys: " (str (sort (keys bibrefmap)))]
      [:p "kwindex keys: " (str (keys kmap))]
      [:script {:src "js/goog/base.js" :type "text/javascript"}]
      [:script {:src "js/webapp.js" :type "text/javascript"}]
      [:script {:type "text/javascript"}
       "goog.require('webapp.core');"]])))

(defroutes bibKWIndexGen-routes
  (GET "/bibKWIndexGen" [] (bibKWIndexGen))
  (POST "/bibKWIndexGen" [] (handle-bibKWIndexGen)))

