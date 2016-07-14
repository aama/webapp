(ns edn2ttl.core
  (require [clojure.edn :as edn]
      [stencil.core :as tmpl ])
  (:gen-class :main true))

;; Old working version of edn2ttl.core with provision for :note and
;; :termcluster included, but commented-out

(defn uuid
  "Generates random UUID for pdgm terms"
  []
  (str (java.util.UUID/randomUUID))
)

(defn do-prelude
  [inputfile pdgm-map]
  (let [lang (name (pdgm-map  :lang))
        Lang (clojure.string/capitalize lang)
        sgpref (pdgm-map :sgpref)]
      (println
         (tmpl/render-string (str "#TTL FROM INPUT FILE:\n#{{inputfile}}\n\n"
          "@prefix rdf:	 <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
          "@prefix rdfs:	 <http://www.w3.org/2000/01/rdf-schema#> .\n"
          "@prefix aama:	 <http://id.oi.uchicago.edu/aama/2013/> .\n"
          "@prefix aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/> .\n"
          "@prefix {{pfx}}:   <http://id.oi.uchicago.edu/aama/2013/{{lang}}/> .\n\n"
          "#SCHEMATA:\n\n"
          "aama:{{Lang}} a aamas:Language .\n"
          "aama:{{Lang}} rdfs:label \"{{Lang}}\" .\n")
           {:pfx sgpref 
            :Lang Lang
            :inputfile inputfile})
      )
   )
)

(defn do-props
  [schemata sgpref Lang]
  (doseq [[property valuelist] schemata]
    (let [prop (name property)
          Prop (clojure.string/capitalize prop)]
          ;; NB clojure.string/capitalize gives  wrong output with
          ;; terms like conjClass: =>Conjclass rather than ConjClass
      (println
       (tmpl/render-string (str
                         (newline)
                         "#SCHEMATA: {{prop}}\n"
                         "{{pfx}}:{{prop}} aamas:lang aama:{{Lang}} .\n"
                         "{{pfx}}:{{Prop}} aamas:lang aama:{{Lang}} .\n"
                         "{{pfx}}:{{prop}} rdfs:domain aamas:Term .\n"
                         "{{pfx}}:{{Prop}} rdfs:label \"{{prop}} exponents\" .\n"
                         "{{pfx}}:{{prop}} rdfs:label \"{{prop}}\" .\n"
                         "{{pfx}}:{{prop}} rdfs:range {{pfx}}:{{Prop}} .\n"
                         "{{pfx}}:{{Prop}} rdfs:subClassOf {{pfx}}:MuExponent .\n"
                         "{{pfx}}:{{prop}} rdfs:subPropertyOf {{pfx}}:muProperty .")
                        {:pfx sgpref
                         :Lang Lang
                         :prop prop
                         :Prop Prop})
      )
      (doseq [value valuelist]
        (let [val (name value)]
          (println
           (tmpl/render-string (str
                           "{{pfx}}:{{val}} aamas:lang aama:{{Lang}} .\n"
                           "{{pfx}}:{{val}} rdf:type {{pfx}}:{{Prop}} .\n"
                           "{{pfx}}:{{val}} rdfs:label \"{{val}}\" .")
                           {:pfx sgpref
                            :Lang Lang
                            :Prop Prop
                            :val val})
           )
          )
        )
      )
    )   
)

(defn do-morphemes
  [morphemes sgpref Lang]
  (println  "\n#MORPHEMES:\n")
  (doseq [[morpheme featurelist] morphemes]
    (let [morph (name morpheme)]
      (println
       (tmpl/render-string (str
                           "aama:{{Lang}}-{{morph}} a aamas:Muterm ;\n"
                           "\taamas:lang aama:{{Lang}} ;\n"
                           "\trdfs:label \"{{morph}}\" ;")
                          {:Lang Lang
                           :morph morph})
       )
      ) ;; (let [morph (name morpheme)]
      (doseq [[feature value] featurelist]
        (let [mprop (name feature)
              mval (name value)]
          (println
           (cond (= mprop "gloss")
               (tmpl/render-string (str "\trdfs:comment \"{{mval}}\" ;") 
                                   {:mval mval})
               (= mprop "lemma")
               (tmpl/render-string (str"\trdfs:comment \"{{mval}}\" ;") 
                                   {:mval mval})
               (re-find #"^\"" mval)
               (tmpl/render-string (str"\t{{pfx}}:{{mprop}} \"{{mval}}\" ;") 
                                   {:pfx sgpref :mval mval :mprop mprop})
               :else
               (tmpl/render-string (str"\t{{pfx}}:{{mprop}} {{pfx}}:{{mval}}s ;") 
                                   {:pfx sgpref :mval mval :mprop mprop})
           )
         )
      ) ;; (let [mprop (name feature)
     ) ;; (doseq [[feature value] featurelist]
  (println "\t.")
  ) ;; (doseq [[morpheme featurelist] morphemes]
) ;; (defn do-morphemes


(defn do-lexemes
  [lexemes sgpref Lang]
  (println  "\n#LEXEMES:")
  (doseq [[lexeme featurelist] lexemes]
    (let [lex (name lexeme)]
      (println
       (tmpl/render-string (str
                           "aama:{{Lang}}-{{lex}} a aamas:Lexeme ;\n" 
                           "\taamas:lang aama:{{Lang}} ;\n" 
                           "\trdfs:label \"{{lex}}\" ;")
                          {:Lang Lang
                           :lex lex})
      )
    )
      (doseq [[feature value] featurelist]
        (let [lprop (name feature)
              lval (name value)]
          (println
           (cond (= lprop "gloss")
              (tmpl/render-string (str "\taamas:{{lprop}} \"{{lval}}\" ;") 
                                  {:lprop lprop :lval lval})
              (= lprop "lemma")
              (tmpl/render-string (str "\taamas:{{lprop}} \"{{lval}}\" ;" )
                                  {:lprop lprop :lval lval})
              (re-find #"^token" lprop)
              (tmpl/render-string (str "\t{{pfx}}:{{lprop}} \"{{lval}}\" ;"  )
                                  {:pfx sgpref :lprop lprop :lval lval})
              (re-find #"^note" lprop)
              (tmpl/render-string (str "\t{{pfx}}:{{lprop}} \"{{lval}}\" ;" )
                                  {:pfx sgpref :lprop lprop :lval lval})
              :else
              (tmpl/render-string (str "\t{{pfx}}:{{lprop}} {{pfx}}:{{lval}} ;" )
                                  {:pfx sgpref :lprop lprop :lval lval})
           )
           )
      )
    )
    (println "\t.")
 ) ;; (doseq [[lexeme featurelist] lexemes
) ;; (do-lexemes)

(defn do-lexterms
  [lexterms sgpref Lang]
  (doseq [termcluster lexterms]
    (let [label (:label termcluster)
          terms (:terms termcluster)
          note (:note termcluster)
          schema (first terms)
          data (next terms)
          common (:common termcluster)]
          (println "\n#TERMCLUSTER: " label)
;;          (println
;;           (tmpl/render-string (str (newline)
;;                                    "{{pfx}}:{{label}} a aamas:Termcluster ;\n"
;;                                    "\trdfs:label \"{{label}}\" ;\n"
;;                                    "\trdfs:comment \"{{note}}\" \n"
;;                                    "\t.")
;;                          {:pfx sgpref
;;                           :label label
;;                           :note note}))
     ;; Need to build up string which can then be println-ed with each term of cluster
    (doseq [term data]
      (let [termid (uuid)]
        (println
         (tmpl/render-string (str (newline)
                            "aama:ID{{uuid}} a aamas:Term ;\n"
                            "\taamas:lang aama:{{Lang}} ;\n"
                            ;;"\taamas:memberOf {{pfx}}:{{label}} ;"
                            )
                          {:Lang Lang
                           :uuid termid
                           :pfx sgpref
                           :label label})
         )
        )
      (doseq [[feature value] common]
        (let [cprop (name feature)
              cval (name value)]
          (println
            (cond (= cprop "lexeme")
             (tmpl/render-string (str "\taamas:{{cprop}} aama:{{Lang}}-{{cval}} ;") 
                                  {:cprop cprop :Lang Lang :cval cval})
              (re-find #"^token" cprop)
              (tmpl/render-string (str "\t{{pfx}}:{{cprop}} \"{{cval}}\" ;")
                                  {:pfx sgpref :cprop cprop :cval cval} )
              (re-find #"^note" cprop)
              (tmpl/render-string (str "\t{{pfx}}:{{cprop}} \"{{cval}}\" ;")
                                  {:pfx sgpref :cprop cprop :cval cval} )
              :else
              (tmpl/render-string (str "\t{{pfx}}:{{cprop}} {{pfx}}:{{cval}} ;")
                                  {:pfx sgpref :cprop cprop :cval cval} )
             )
            )
          )
        )
      (let [termmap (apply assoc {} (interleave schema term))]
      (doseq [tpropval termmap]
        (let [tprop (name (key tpropval))
              tval (name (val tpropval))]
          (println
             (cond (re-find #"^\"" tval)
              (tmpl/render-string (str "\t{{pfx}}:{{tprop}} \"{{tval}}\" ;" )
                                  {:pfx sgpref :tprop tprop :tval tval})
              ;;  following redundant if previous clause works
              (re-find #"^token" tprop)
              (tmpl/render-string (str "\t{{pfx}}:{{tprop}} \"{{tval}}\" ;" )
                                  {:pfx sgpref :tprop tprop :tval tval})
              (re-find #"^note" tprop)
              (tmpl/render-string (str "\t{{pfx}}:{{tprop}} \"{{tval}}\" ;" )
                                  {:pfx sgpref :tprop tprop :tval tval})
              (re-find #"^gloss" tprop)
              (tmpl/render-string (str "\taamas:{{tprop}} \"{{tval}}\" ;" )
                                  {:pfx sgpref :tprop tprop :tval tval})
              (= tprop "lexeme")
              (tmpl/render-string (str "\taamas:{{tprop}} aama:{{Lang}}-{{tval}} ;")
                                  {:tprop tprop :Lang Lang :tval tval})
              :else
              (tmpl/render-string (str "\t{{pfx}}:{{tprop}} {{pfx}}:{{tval}} ;")
                                  {:pfx sgpref :tprop tprop :tval tval})
             )
            )
           )
          )
        ) ;; (let [termmap apply assoc {}
      (println "\t.")
    ) ;;(doseq [term data]
  ) ;;(let [terms (:terms termcluster)
) ;;(doseq [termcluster lexterms]
) ;;(defn do-lexterms

(defn do-muterms
  [muterms sgpref Lang]
  (doseq [ mutermcluster muterms]
    (let [label (:label mutermcluster)
          terms (:terms mutermcluster)
          schema (first terms)
          data (next terms)
          common (:common mutermcluster)]
   ;; Need to build up string which can then be println-ed with each term of cluster
      (println "\n#MUTERMCLUSTER: "  label)
      (doseq [term data]
	(let [termid (uuid)]
          (println
           (tmpl/render-string (str (newline)
                              "aama:ID{{termid}} a aamas:Muterm ;\n" 
		              "\taamas:lang aama:{{Lang}} ;")
                          {:Lang Lang
                           :uuid uuid})
           )
        )
	(doseq [[feature value] common]
	    (let [cprop (name feature)
                  cval (name value)]
              (println
        	 (cond (= cprop "morpheme")
             (tmpl/render-string (str "\taamas:{{cprop}} aama:{{Lang}}-{{cval}} ;") 
                                  {:cprop cprop :Lang Lang :cval cval})
                  (re-find #"^token" cprop)
                  (tmpl/render-string (str "\t{{pfx}}:{{cprop}} \"{{cval}}\" ;")
                                  {:pfx sgpref :cprop cprop :cval cval} )
                  (re-find #"^note" cprop)
                  (tmpl/render-string (str "\t{{pfx}}:{{cprop}} \"{{cval}}\" ;")
                                  {:pfx sgpref :cprop cprop :cval cval} )
                  :else
                  (tmpl/render-string (str "\t{{pfx}}:{{cprop}} {{pfx}}:{{cval}} ;")
                                  {:pfx sgpref :cprop cprop :cval cval} )
		)
               )
            )
	 )
	(let [termmap (apply assoc {} (interleave schema term))]
	(doseq [tpropval termmap]
	  (let [tprop (name (key tpropval))
                      tval (name (val tpropval))]
            (println
             (cond (re-find #"^\"" tval)
              (tmpl/render-string (str "\t{{pfx}}:{{tprop}} \"{{tval}}\" ;" )
                                  {:pfx sgpref :tprop tprop :tval tval})
              ;;  following redundant if previous clause works
              (re-find #"^token" tprop)
              (tmpl/render-string (str "\t{{pfx}}:{{tprop}} \"{{tval}}\" ;" )
                                  {:pfx sgpref :tprop tprop :tval tval})
              (re-find #"^note" tprop)
              (tmpl/render-string (str "\t{{pfx}}:{{tprop}} \"{{tval}}\" ;" )
                                  {:pfx sgpref :tprop tprop :tval tval})
              (re-find #"^gloss" tprop)
              (tmpl/render-string (str "\taamas:{{tprop}} \"{{tval}}\" ;" )
                                  {:pfx sgpref :tprop tprop :tval tval})
              (= tprop "morpheme")
              (tmpl/render-string (str "\taamas:{{tprop}} aama:{{Lang}}-{{tval}} ;")
                                  {:tprop tprop :Lang Lang :tval tval})
              :else
              (tmpl/render-string (str "\t{{pfx}}:{{tprop}} {{pfx}}:{{tval}} ;")
                                  {:pfx sgpref :tprop tprop :tval tval})
             )
            )
           )
          )
        ) ;; (let [termmap apply assoc {}
       (println "\t.")
     ) ;; (doseq [term data]
    ) ;; (let [terms (:terms mucluster)
  ) ;; (doseq [mutermcluster muterms]
) ;; (defn do-muterms

  (defn -main
   "Calls the functions that transform the keyed maps of a pdgms.edn to a pdgms.ttl"
    [& file]

    (let [inputfile (first file)
          pdgmstring (slurp inputfile)
          pdgm-map (edn/read-string pdgmstring)
          lang (name (pdgm-map  :lang))
          Lang (clojure.string/capitalize lang)
          sgpref (pdgm-map :sgpref)
          ]

      (do-prelude inputfile pdgm-map)

      (do-props (pdgm-map :schemata) sgpref Lang)

      (do-morphemes (pdgm-map :morphemes) sgpref Lang)

      (do-lexemes (pdgm-map :lexemes) sgpref Lang)

      (do-lexterms (pdgm-map :lxterms) sgpref Lang)

      (do-muterms (pdgm-map :muterms) sgpref Lang)
      )
     )
