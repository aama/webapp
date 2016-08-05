(ns ednsort.core
  (require [clojure.edn :as edn]
           [clojure.set :as set]
           [clojure.string :as str])
  (:gen-class :main true))

;; adapted from edn2ttl.core
;; println output directed to lexterm output-file by either:
;;   1) lein run [LANG]-pdgms.edn > [LANG]-sorted-lexterms.edn {from ednsort directory}
;;   2) java -jar path/to/aama-ednsort.jar [LANG]-pdgms.edn > [LANG]-sorted-lexterms.edn
;; :lxterms section then to be replaced by hand after CAREFUL inspection 
;; ensures that all data preserved
;;
;; General case - 
;; 1) for each termcluster of lxterms: #{}
;;    IF: :terms[0] (i.e. schema) has [(:OTHER) :gender :number (:person) :token]
;;    THEN: order (:OTHER) :number[rev] (:person) :gender[rev] :token
;;    ELSE: order by col. left to right
;; 2) add to lxterms:[[atom []]] or sorted #{} (?)
;; 3) sort lxterms by {:label "label" . . . }
;; 4) println to [LANG]-sorted-lexterms.edn

(defn makepngvec [pngs]
  (let [pngvec (atom [])]
    (if (pngs :number) (swap! pngvec conj :number))
    (if (pngs :person) (swap! pngvec conj :person))
    (if (pngs :gender) (swap! pngvec conj :gender))
    @pngvec))

;;(defn maketokenvec [nonpngvec](let [tokenvec (atom []) propnums (range (count nonpngvec))] (for [propnum propnums] (let [prop (get nonpngvec propnum)] (if (re-find #"token" (str prop) ) (swap! tokenvec conj prop)))) @tokenvec))

(def asc compare)
(def desc #(compare %2 %1))

(defn compare-by [[k comp & more] x y]
  (let [result (comp (k x) (k y))]
    (if (and (zero? result) (seq more))
      (recur more x y)
      result)))

(defn termmap [termcluster schema] 
  (for [term termcluster] 
    (apply assoc {} (interleave schema term))))
 
(defn tmapsort [tmap schemaSort] 
  (sort #(compare-by schemaSort %1 %2) tmap))

(defn table [tmapsort schemaTable] 
  (for [term tmapsort] (vec (for [prop schemaTable] (prop term)))))

(defn do-lexterms
  [lexterms]
  (println ":lxterms [")
  (doseq [termcluster (sort-by :label lexterms)]
    (let [label (:label termcluster)
          terms (:terms termcluster)
          ;;because csv sparql req will be split by ","
          ;;note (str/replace (str (:note termcluster)) #"," "%%")
          note (:note termcluster)
          schema (first terms)
          data (next terms)
          common (:common termcluster)
          ;;asc (compare)
          ;;desc #(compare %2 %1)
          pngset #{:number :person :gender}
          pngsort [:number desc :person asc :gender desc]
          nonpngset (set/difference (set schema) pngset)
          nonpngvec (vec (sort nonpngset))
          pngs (set/difference (set schema) nonpngset)
          pngvec (makepngvec pngs)
          nonpngvec2 (str/replace nonpngvec #"]" " ]")
          tokenvecstr (vec (re-seq #"token.*? " nonpngvec2))
          tokenvec (vec (for [token tokenvecstr] (keyword (str/trimr token))))
          nonpngtvec (vec (sort (set/difference nonpngset (set tokenvec))))
          nonpngtsort (vec (interleave nonpngtvec (repeat asc)))
          schemaSort (vec (concat nonpngtsort pngsort))
          schemaTable (vec (concat nonpngtvec pngvec tokenvec))
          tmap (termmap data schema)
          tmapsrt (tmapsort tmap schemaSort)
          tbl (table tmapsrt schemaTable)
          ]
      (println (str "    {:label \"" label"\""))
      (if (:note termcluster)
        (println (str "    :note \"" note "\"")))
      (println (str "    :common " common))
      (println (str "    :terms [" schemaTable " ;; schema"))
      (prn tbl "]" )
      (println "    }")
      ))
  (println "]"))

(defn do-muterms
  [muterms]
  (println ":muterms [")
  (doseq [termcluster (sort-by :label muterms)]
    (let [label (:label termcluster)
          terms (:terms termcluster)
          ;;because csv sparql req will be split by ","
          ;;note (str/replace (str (:note termcluster)) #"," "%%")
          note (:note termcluster)
          schema (first terms)
          data (next terms)
          common (:common termcluster)
          pngset #{:number :person :gender}
          pngsort [:number desc :person asc :gender desc]
          nonpngset (set/difference (set schema) pngset)
          nonpngvec (vec (sort nonpngset))
          pngs (set/difference (set schema) nonpngset)
          pngvec (makepngvec pngs)
          nonpngvec2 (str/replace nonpngvec #"]" " ]")
          tokenvecstr (vec (re-seq #"token.*? " nonpngvec2))
          tokenvec (vec (for [token tokenvecstr] (keyword (str/trimr token))))
          nonpngtvec (vec (sort (set/difference nonpngset (set tokenvec))))
          nonpngtsort (vec (interleave nonpngtvec (repeat asc)))
          schemaSort (vec (concat nonpngtsort pngsort))
          schemaTable (vec (concat nonpngtvec pngvec tokenvec))
          tmap (termmap data schema)
          tmapsrt (tmapsort tmap schemaSort)
          tbl (table tmapsrt schemaTable)
          ]
      (println (str "    {:label \"" label"\""))
      (if (:note termcluster)
        (println (str "    :note \"" note "\"")))
      (println (str "    :common " common ))
      (println (str "    :terms [" schemaTable " ;; schema"))
      (prn tbl "]" )
      (println "    }")
      ))
  (println "]"))

  (defn -main
   "Calls the functions that sort the png  order of the :lexterm maps of a pdgms.edn "
    [& file]

    (let [inputfile (first file)
          pdgmstring (slurp inputfile)
          pdgm-map (edn/read-string pdgmstring)
          lang (name (pdgm-map  :lang))
          sgpref (pdgm-map :sgpref)
          ]

;;      (do-prelude inputfile pdgm-map)
;;      (do-props (pdgm-map :schemata) sgpref Lang)
;;      (do-pclass (pdgm-map :pclass) sgpref)
;;      (do-morphemes (pdgm-map :morphemes) sgpref Lang)
;;      (do-lexemes (pdgm-map :lexemes) sgpref Lang)
      (do-lexterms (pdgm-map :lxterms))
;;      (do-muterms (pdgm-map :muterms))
      )
     )
