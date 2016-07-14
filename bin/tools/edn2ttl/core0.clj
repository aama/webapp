(ns edn2ttl.core
	(require [clojure.edn :as edn])
	(:gen-class))

;; Oldest version. No longer relevant.

(defn uuid 
"Generates random UUID for pdgm terms"
[]
(str (java.util.UUID/randomUUID)))

(defn -main
  "Calls the functions that transform the keyed maps of a pdgms.edn to a pdgms.ttl"
  [& file]
	(def inputfile (first file))
	(println "\n#TTL FROM INPUT FILE:\n#" inputfile)
	(def pdgmstring (slurp inputfile))
	(def pdgmmap (edn/read-string pdgmstring))
	(def lang (name (pdgmmap  :lang)))
	(def Lang (clojure.string/capitalize lang))
	(def sgpref (pdgmmap :sgpref))
        (def x ( map println [
                       (format "@prefix rdf:	 <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." )
                       (format "@prefix rdfs:	 <http://www.w3.org/2000/01/rdf-schema#> ." )
                       (format "@prefix aama:	 <http://id.oi.uchicago.edu/aama/2013/> ." )
                       (format "@prefix aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/> ." )
                       (format "@prefix %s:   <http://id.oi.uchicago.edu/aama/2013/%s/> ." sgpref lang)
		])
	)	
	(doall x)

	(def x ( map println [
			(format "\n#SCHEMATA:\n")
			(format "aama:%s a aamas:Language ." Lang)
			(format  "aama:%s rdfs:label \"%s\" ." Lang Lang)
		])
	)	
	(doall x)
	(def schemata (pdgmmap :schemata))
	(def morphemes (pdgmmap :morphemes))
	(def lexemes (pdgmmap :lexemes))
	(def lexterms (pdgmmap :lxterms))	
        (def muterms (pdgmmap :muterms))
	(doseq [[property valuelist] schemata]
		(def prop (name property))
		;; NB clojure.string/capitalize gives  wrong output with 
		;; terms like conjClass: =>Conjclass rather than ConjClass
		(def Prop (clojure.string/capitalize prop))
		(def x ( map println [
					(format "\n#SCHEMATA: %s" prop  )
					(format "%s:%s aamas:lang aama:%s ." sgpref prop Lang)
					(format "%s:%s aamas:lang aama:%s ." sgpref Prop Lang)
					(format "%s:%s rdfs:domain aamas:Term ." sgpref prop)
					(format "%s:%s rdfs:label \"%s exponents\" ." sgpref Prop prop)
					(format "%s:%s rdfs:label \"%s\" ." sgpref prop prop)
					(format "%s:%s rdfs:range %s:%s ." sgpref prop sgpref Prop)
					(format "%s:%s rdfs:subClassOf %s:MuExponent ." sgpref Prop sgpref)
					(format "%s:%s rdfs:subPropertyOf %s:muProperty ." sgpref prop sgpref )
			])
		)
		(doall  x)
		(def vallist valuelist)
		(doseq [value vallist] 
			(def valname (name value))
			(def y ( map println [
					(format "%s:%s aamas:lang aama:%s ." sgpref valname Lang)
					(format "%s:%s rdf:type %s:%s ." sgpref valname sgpref Prop)
					(format "%s:%s rdfs:label \"%s\"." sgpref valname valname)
				])
			)
			(doall y)
		)
	)
	(println	(format "\n#MORPHEMES:\n"))
	(doseq [[morpheme featurelist] morphemes]
		(def morph (name morpheme))
		(def x ( map println [
				(format "aama:%s-%s a aamas:Morpheme ;" Lang morph)
				(format "\taamas:lang aama:%s ;" Lang)
				(format "\trdfs:label \"%s\" ;" morph)
			])
		)
		(doall  x)
		(doseq [[feature value] featurelist] 
			(def mprop (name feature))
			(def mval (name value))
			(def y ( map println [
					(cond (= mprop "gloss")
								(format "\trdfs:comment \"%s\" ;" mval)
                              (= mprop "lemma")
								(format "\trdfs:comment \"%s\" ;" mval)
                              (re-find #"^\"" mval)
                                 (format "\t%s:%s \"%s\" ;" sgpref mprop mval)
                              :else
								 (format "\t%s:%s %s:%s ;" sgpref mprop sgpref mval)
					)
				])
			)
			(doall y)
		)
		(println "\t.")
	)
	(println	(format "\n#LEXEMES:\n"))
	(doseq [[lexeme featurelist] lexemes]
		(def lex (name lexeme))
		(def x ( map println [
				(format "aama:%s-%s a aamas:Lexeme ;" Lang lex)
				(format "\taamas:lang aama:%s ;" Lang)
				(format "\trdfs:label \"%s\" ;" lex)
			])
		)
		(doall  x)
		(doseq [[feature value] featurelist] 
			(def lprop (name feature))
			(def lval (name value))
			(def y ( map println [
				  (cond (= lprop "gloss")
					    (format "\taamas:%s \"%s\" ;" lprop lval)
                                        (= lprop "lemma")
                                            (format "\taamas:%s \"%s\" ;" lprop lval)
				        (re-find #"^token" lprop)
					    (format "\t%s:%s \"%s\" ;" sgpref lprop lval )
                                        (re-find #"^note" lprop)
                                            (format "\t%s:%s \"%s\" ;" sgpref lprop lval)
                                        :else
                                            (format "\t%s:%s %s:%s ;" sgpref lprop sgpref lval)
				    )
				])
			)
			(doall y)
		)
		(println "\t.")
	)
	(doseq [ termcluster lexterms]
		(def label (:label termcluster))
		(def x (map println [
			(format "\n#TERMCLUSTER: %s\n"  label)])
		)
		(doall x)
		(def terms (:terms termcluster))
		(def schema (first terms))
		(def data (next terms))
		(def common (:common termcluster))
		;; Need to build up string which can then be println-ed with each term of cluster
		(doseq [term data]
			(def termid (uuid))
			(def w (map println [
				;(format "\n"  )
				(format "aama:ID%s a aamas:Term ;" termid)
				(format "\taamas:lang aama:%s ;" Lang)]))
			(doall w)
			(doseq [[feature value] common]
				(def cprop (name feature))
				(def cval (name value))
				(def x ( map println [
					 (cond (= cprop "lexeme")
					     (format "\taamas:%s aama:%s-%s ;" cprop Lang cval)
					 (re-find #"^token" cprop)
				       	     (format "\t%s:%s \"%s\" ;" sgpref cprop cval )
					 (re-find #"^note" cprop)
				       	     (format "\t%s:%s \"%s\" ;" sgpref cprop cval )

					 :else
                                             (format "\t%s:%s %s:%s ;" sgpref cprop sgpref cval)
					 )
                                       ])
				)
				(doall x)
			)
			(def termmap (apply assoc {}
				(interleave schema term)))
			(doseq [tpropval termmap]
				(def tprop (name (key tpropval)))
				(def tval (name (val tpropval)))
				(def y (map println [
                                            (cond (re-find #"^\"" tval)
                                                     (format "\t%s:%s \"%s\" ;" sgpref lprop lval)
						  ;;  following redundant if previous clause works
						  (re-find #"^token" tprop)
						     (format "\t%s:%s \"%s\" ;" sgpref tprop tval )
						  (re-find #"^note" tprop)
						     (format "\t%s:%s \"%s\" ;" sgpref tprop tval )
                                                  (re-find #"^gloss" tprop)
                                                     (format "\taamas:%s \"%s\" ;" tprop tval)
                                                  (= tprop "lexeme")
                                                     (format "\taamas:%s aama:%s-%s ;" tprop Lang tval)
						   :else
						     (format "\t%s:%s %s:%s ;" sgpref tprop sgpref tval)
                                             )
					])
				)
				(doall y)
			)
			(def z (map println [
				(format "\t." )])
			)
			(doall z)
		)
	)
	(doseq [ mutermcluster muterms]
		(def label (:label mutermcluster))
		(def x (map println [
			(format "\n#MUTERMCLUSTER: %s\n"  label)])
		)
		(doall x)
		(def terms (:terms mutermcluster))
		(def schema (first terms))
		(def data (next terms))
		(def common (:common mutermcluster))
		;; Need to build up string which can then be println-ed with each term of cluster
		(doseq [term data]
			(def termid (uuid))
			(def w (map println [
				;(format "\n"  )
				(format "aama:ID%s a aamas:Muterm ;" termid)
				(format "\taamas:lang aama:%s ;" Lang)]))
			(doall w)
			(doseq [[feature value] common]
				(def cprop (name feature))
				(def cval (name value))
				(def x ( map println [
					 (cond (= cprop "morpheme")
					     (format "\taamas:%s aama:%s-%s ;" cprop Lang cval)
					 (re-find #"^token" cprop)
				       	     (format "\t%s:%s \"%s\" ;" sgpref cprop cval )
					 (re-find #"^note" cprop)
				       	     (format "\t%s:%s \"%s\" ;" sgpref cprop cval )

					 :else
                                             (format "\t%s:%s %s:%s ;" sgpref cprop sgpref cval)
					 )
                                       ])
				)
				(doall x)
			)
			(def termmap (apply assoc {}
				(interleave schema term)))
			(doseq [tpropval termmap]
				(def tprop (name (key tpropval)))
				(def tval (name (val tpropval)))
				(def y (map println [
                                            (cond (re-find #"^\"" tval)
                                                     (format "\t%s:%s \"%s\" ;" sgpref lprop lval)
						  ;;  following redundant if previous clause works
						  (re-find #"^token" tprop)
						     (format "\t%s:%s \"%s\" ;" sgpref tprop tval )
						  (re-find #"^note" tprop)
						     (format "\t%s:%s \"%s\" ;" sgpref tprop tval )
                                                  (re-find #"^gloss" tprop)
                                                     (format "\taamas:%s \"%s\" ;" tprop tval)
                                                  (= tprop "morpheme")
                                                     (format "\taamas:%s aama:%s-%s ;" tprop Lang tval)
						   :else
						     (format "\t%s:%s %s:%s ;" sgpref tprop sgpref tval)
                                             )
					])
				)
				(doall y)
			)
			(def z (map println [
				(format "\t." )])
			)
			(doall z)
		)
	)
)
 
 

