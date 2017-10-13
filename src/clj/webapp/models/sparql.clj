(ns webapp.models.sparql
(:refer-clojure :exclude [filter concat group-by max min count])
  (:require [compojure.core :refer :all]
            ;;[compojure.handler :as handler]
            ;;[compojure.route :as route]
            [clojure.string :refer [capitalize split]]
            [stencil.core :as tmpl]
            ;;[clj-http.client :as http]
            ;;[boutros.matsu.sparql :refer :all]
            ;;[boutros.matsu.core :refer [register-namespaces]]
            [clojure.tools.logging :as log])
  (:use [hiccup.page :only [html5]])
)

;; see notes/query-ext.clj for matsu and other formats
;; and for pdgmqry-sparql-alt

(def PREFIXES "
	PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
	PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
	PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/> 
	PREFIX aamag:	 <http://oi.uchicago.edu/aama/2013/graph/>") 


;; Get bibref(s) and geo/demo URL(s) and TXT
(defn langInfoqry-sparql [language lpref]
  (let [Language (capitalize language)]
    (str PREFIXES
     (tmpl/render-string 
      (str "
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT ?bibref ?lurl ?ldesc ?subfam
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language}}  
          { 
            aama:{{Language}} aamas:dataSource ?bibref .
            aama:{{Language}} aamas:geodemoURL ?lurl  .
            aama:{{Language}} aamas:geodemoTXT ?ldesc .
            OPTIONAL { aama:{{Language}} aamas:subfamily ?subfam .}
          }}}
           ")
      {:lpref lpref
       :language language
       :Language Language}))))

(defn pdgmqry-sparql-comment [dataID]
  "Query to retrieve comment from any term-cluster whose edn-file ':label' is known"
  (let [dataIDstr (str "/"" dataID "/"")]
  (str PREFIXES
       (tmpl/render-string
        (str "
          SELECT ?comment
          WHERE
          {
      	   ?s rdfs:label \"{{label}}\" ;
              rdfs:comment ?comment . } ")
        {:label dataID}))))
            
(defn pdgmqry-sparql-fv [language lpref valstring]
  "This version, for the moment only called by the single pdgm display option, which is designed to give the most information about an individual paradigm, includes information about input paradigm notes, lex, and token-... . Note info should eventually be displayed in the paradigm-label listing."
    (let [;; if assume last value is lex (generalize to other pos?)
          vals (clojure.string/replace valstring #"(.*):.*?$" "$1")
          lex (clojure.string/replace valstring #".*:(.*?)$" "$1")
          values (split vals #",")
          Language (capitalize language)
          ]
      ;;(for [lex lexvals]
      (str PREFIXES
      (tmpl/render-string 
       (str "
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT ?num ?pers ?gen ?token
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language}}  
          { 
	   ?s {{lpref}}:pos {{lpref}}:Verb .  
	   ?s aamas:lang aama:{{Language}} . 
	   ?s aamas:lang / rdfs:label ?langLabel .  
           ?s aamas:lexeme ?lexeme .
           ?lexeme rdfs:label \"{{lex}}\" . ")
       {:lpref lpref
        :language language
        :Language Language
        :lex lex})
      (apply str  
             (for [value values]
               (tmpl/render-string 
                (str "
           ?s ?Q{{value}}  {{lpref}}:{{value}} .  ")
                {:value value
                 :lpref lpref})))
      (tmpl/render-string
       (str "
	   OPTIONAL { ?s {{lpref}}:number / rdfs:label ?num . } 
	   ?s {{lpref}}:person ?person .  
	   ?person rdfs:label ?pers .  
	   OPTIONAL { ?s {{lpref}}:gender / rdfs:label ?gen . } 
	   ?s {{lpref}}:token ?tkn .
           OPTIONAL { ?s ?t ?o . FILTER (CONTAINS(str(?t), \"token-note\"))}
           BIND((IF(BOUND(?o),
                    CONCAT(?tkn, \"  [\", ?o, \"]\"),
                    ?tkn))
                   AS ?token) .
	  } 
	 } 
	} 
	ORDER BY ?lex DESC(?num) ?pers DESC(?gen) ")
       {:lpref lpref})
       );;str
))

(defn pdgmqry-sparql-pro [language lpref valstr]
    (let [values (split valstr #"[:,]")
          proclass (first values)
          vals (vec (rest values))
          propstring (clojure.string/replace valstr #"^.*?:" "")
          propstr (clojure.string/replace propstring #"^," "")
          ;;qpropstring (clojure.string/replace propstring #"-|," {"-" "" "," " ?"})
          qprops (clojure.string/replace propstr "-" "")
          qpropstring (if (re-find #"\S" qprops)
                        (str "?" (clojure.string/replace qprops "," " ?"))
                        qprops)
          Language (capitalize language)
          ]
      (str PREFIXES
      (tmpl/render-string 
       (str "
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT  ?num ?pers ?gen ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language}}  
          { 
	   ?s {{lpref}}:pos {{lpref}}:Pronoun .  
	   ?s aamas:lang aama:{{Language}} .
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  ")
       {:lpref lpref
        :language language
        :Language Language})
      (apply str  
             (for [value values]
                 (tmpl/render-string 
                  (str "
                   ?s ?Q{{value}}  {{lpref}}:{{value}} .  ")
                  {:lpref lpref
                   :value value})))
      (tmpl/render-string
       (str " 
	   OPTIONAL { ?s {{lpref}}:number ?number .  
	   ?number rdfs:label ?num . } 
	   OPTIONAL {?s {{lpref}}:person ?person .  
	   ?person rdfs:label ?pers .  }
	   OPTIONAL { ?s {{lpref}}:gender ?gender .  
	   ?gender rdfs:label ?gen . } 
	   ?s {{lpref}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY DESC(?num) ?pers DESC(?gen) ")
       {:lpref lpref})
       );;str
))

(defn pdgmqry-sparql-nfv [language lpref valstring]
    (let [valstrng (clojure.string/replace valstring #",$" "")
          values (split valstring #"," 2)
          pdgmType (first values)
          props (apply str (rest values))
          propvec (split props #",")
          ;;propstring (clojure.string/replace valstrng #"^.*?:" ",")
          propstring (str "?" props)
          qpropstring (clojure.string/replace propstring #"-|," {"-" "" "," " ?"})
          ;;qprops (clojure.string/replace propstring "-" "")]
          ;;qpropstring (clojure.string/replace qprops "," " ?")
          Language (capitalize language)
          ]
      (str PREFIXES
      (tmpl/render-string 
       (str "
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT  {{selection}} ?lex  ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language}}  
          { 
	   ?s {{lpref}}:pos {{lpref}}:Verb .  
           #NOT EXISTS {?s {{lpref}}:person ?person } .
	   ?s aamas:lang aama:{{Language}} .
           ?s aamas:memberOf ?pdgm .
           ?pdgm aamas:pdgmType {{lpref}}:{{pdgmType}} .
	   ?s aamas:lang ?lang . 
	   ?lang rdfs:label ?langLabel .  
           OPTIONAL {?s aamas:lexeme ?Qlex .
                     ?Qlex rdfs:label ?lex .}")
       {:lpref lpref
        :language language
        :Language Language
        :selection qpropstring
        :pdgmType pdgmType})
      (apply str  
             (for [prop propvec]
               (let [qprop (clojure.string/replace prop "-" "")]
               (if (re-find #"token" prop)
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref}}:{{prop}} ?{{qprop}} . }")
                  {:lpref lpref
                   :prop prop
                   :qprop qprop})
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref}}:{{prop}} ?Q{{qprop}} .
                      ?Q{{qprop}} rdfs:label ?{{qprop}} . }") 
                  {:lpref lpref
                   :prop prop
                   :qprop qprop})
                 );;if
               );;let
               ))
      (tmpl/render-string
       (str " 
	   ?s {{lpref}}:token ?tkn .  
           OPTIONAL { ?s ?t ?o . FILTER (CONTAINS(str(?t), \"token-note\"))}
           BIND((IF(BOUND(?o),
                    CONCAT(?tkn, \"  [\", ?o, \"]\"),
                    ?tkn))
                   AS ?token) .
	  } 
	 } 
	} 
	ORDER BY {{selection}} ?lex ")
       {:lpref lpref
        :selection qpropstring})
       );;str
))

(defn pdgmqry-sparql-noun [language lpref valstring]
  (let [valstrng (clojure.string/replace valstring #",$" "")
        values (split valstrng #"," 2)
        pdgmType (first values)
        props (apply str (rest values))
        propvec (split props #",")
        ;;propstring (clojure.string/replace valstrng #"^.*?:" ",")
        propstring (str "?" props)
        qpropstring (clojure.string/replace propstring #"-|," {"-" "" "," " ?"})
        ;;qprops (clojure.string/replace propstring "-" "")]
        ;;qpropstring (clojure.string/replace qprops "," " ?")
        Language (capitalize language)
        ]
      (str PREFIXES
      (tmpl/render-string 
       (str "
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT  {{selection}}  ?token  
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language}}  
          { 
	   ?s {{lpref}}:pos {{lpref}}:Noun ; 
	      aamas:lang aama:{{Language}} ;
              aamas:memberOf ?pdgm .
	   ?pdgm aamas:pdgmType {{lpref}}:{{pdgmType}} . ")
       {:lpref lpref
        :language language
        :Language Language
        :selection qpropstring
        :pdgmType pdgmType})
      (apply str  
             (for [prop propvec]
               (let [qprop (clojure.string/replace prop "-" "")]
               (if (re-find #"token" prop)
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref}}:{{prop}} ?{{qprop}} . }")
                  {:lpref lpref
                   :prop prop
                   :qprop qprop})
                 (tmpl/render-string 
                  (str "
           OPTIONAL { ?s {{lpref}}:{{prop}} ?Q{{qprop}} .
                      ?Q{{qprop}} rdfs:label ?{{qprop}} . }") 
                  {:lpref lpref
                   :prop prop
                   :qprop qprop})
                 );;if
               );;let
               ))
      (tmpl/render-string
       (str " 
	   OPTIONAL { ?s {{lpref}}:number ?number .  
	   ?number rdfs:label ?num . } 
	   OPTIONAL {?s {{lpref}}:person ?person .  
	   ?person rdfs:label ?pers .  }
	   OPTIONAL { ?s {{lpref}}:gender ?gender .  
	   ?gender rdfs:label ?gen . } 
	   ?s {{lpref}}:token ?token .  
	  } 
	 } 
	} 
	ORDER BY {{selection}} DESC(?num) ?pers DESC(?gen) ")
       {:lpref lpref
        :selection qpropstring})
       );;str
))


(defn lgpr-sparql [ldomain prop]
  (let [ldoms (split ldomain #",")]
  (str PREFIXES
    (str "
       SELECT DISTINCT ?language ?valuelabel
       WHERE { ")
      (apply str  
             (for [ldom ldoms]
               (tmpl/render-string 
                  (str "
         {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
          <http://id.oi.uchicago.edu/aama/2013/{{lang}}/{{type}}> rdfs:range ?Type .
          ?value rdf:type ?Type .
          ?value rdfs:label ?valuelabel .
          ?value aamas:lang ?lang .
          ?lang rdfs:label ?language .
          }}  "
                       (if (not (= (last ldoms) ldom))
                         (str " 
          UNION")))
    {:lang ldom
     :type prop})))
      (str "}
       ORDER BY ?language ?valuelabel  "))))

(defn listlgpr-sparql-checkall [language lpref]
(str PREFIXES
  (tmpl/render-string
   (str "
PREFIX {{lpref}}: <http://id.oi.uchicago.edu/aama/2013/{{lang}}/>
SELECT DISTINCT  ?property 
WHERE {
GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
	?s ?p ?o ;
        aamas:memberOf ?pdgm .
        #?pdgm aamas:pdgmType ?pdgmType .
        OPTIONAL {?p rdfs:label ?prop .}
        BIND (IF(!bound(?prop) ,
                 str(?p),
                 ?prop
                ) AS ?property
             ) .
        #FILTER (CONTAINS (str(?pdgmType), \"Finite\" ))
 	FILTER (?p NOT IN ( aamas:lang, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) )
}
}
ORDER BY ASC(?property) ")
{:lang language
 :lpref lpref})))

(defn listlgpr-sparql-fv [language lpref]
(str PREFIXES
  (tmpl/render-string
   (str "
PREFIX {{lpref}}: <http://id.oi.uchicago.edu/aama/2013/{{lang}}/>
SELECT DISTINCT  ?property 
WHERE {
GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
	?s ?p ?o ;
	{{lpref}}:pos  {{lpref}}:Verb ;
        aamas:memberOf ?pdgm .
        ?pdgm aamas:pdgmType ?pdgmType .
        #?s aamas:pdgmType {{lpref}}:Finite .
        #?s aamas:pdgmType ?pdgmType .
        ?p rdfs:label ?property .
        FILTER (CONTAINS (str(?pdgmType), \"Finite\" ))
 	FILTER (?p NOT IN ( aamas:lang, {{lpref}}:gender, {{lpref}}:number, {{lpref}}:pngShapeClass, {{lpref}}:person, {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) )
}
}
ORDER BY ASC(?property) ")
{:lang language
 :lpref lpref})))

(defn listlgpr-sparql-nfv [language lpref]
  (str PREFIXES
  (tmpl/render-string
   (str "
PREFIX {{lpref}}: <http://id.oi.uchicago.edu/aama/2013/{{lang}}/>
SELECT DISTINCT  ?property
WHERE {
GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
	?s ?p ?o ;
	{{lpref}}:pos  {{lpref}}:Verb ;
        aamas:memberOf ?pdgm .
        ?pdgm aamas:pdgmType ?pdgmType .
	#NOT EXISTS {?s {{lpref}}:person ?person }
        ?p rdfs:label ?property .
        FILTER (!CONTAINS (str(?pdgmType), \"Finite\" ))
        FILTER (?p NOT IN ( aamas:lang, {{lpref}}:pngShapeClass, aamas:pdgmType, {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) ) 	
        #FILTER (?p NOT IN ( aamas:lang, {{lpref}}:gender, {{lpref}}:number, {{lpref}}:pngShapeClass, {{lpref}}:person, {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) )
}
}
ORDER BY ASC(?property) ")
{:lang language
 :lpref lpref})))


(defn listlgpr-sparql-pro [language lpref]
  (str PREFIXES
   (tmpl/render-string
   (str "
PREFIX {{lpref}}: <http://id.oi.uchicago.edu/aama/2013/{{lang}}/>
SELECT DISTINCT  ?property
WHERE {
GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
	?s ?p ?o ;
	{{lpref}}:pos  {{lpref}}:Pronoun .
        #?s {{lpref}}:person ?person .
        ?p rdfs:label ?property .
 	FILTER (?p NOT IN ( aamas:lang, {{lpref}}:gender, {{lpref}}:number, {{lpref}}:pngShapeClass, {{lpref}}:person, {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) )
}
}
ORDER BY ASC(?property) ")
{:lang language
 :lpref lpref})))


(defn listlgpr-sparql-noun [language lpref]
  (str PREFIXES
  (tmpl/render-string
   (str "
PREFIX {{lpref}}: <http://id.oi.uchicago.edu/aama/2013/{{lang}}/>
SELECT DISTINCT  ?property
WHERE {
GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
	?s ?p ?o ;
	#rdf:type aamas:Muterm ;
	{{lpref}}:pos  {{lpref}}:Noun .
        ?p rdfs:label ?property .
  	FILTER (?p NOT IN ( aamas:lang, {{lpref}}:pos, aamas:pdgmType, rdf:type))
}}
ORDER BY ASC(?property) ")
{:lang language
 :lpref lpref})))

(defn listmenu-sparql-prop []
  (str PREFIXES
  (str "
SELECT DISTINCT  ?property
WHERE {
	?s ?p ?o .
        ?p rdfs:label ?property .
 	FILTER (?p NOT IN ( aamas:lang) )
}
ORDER BY ASC(?property) ")))

(defn listmenu-sparql-val []
  (str PREFIXES
  (str "
SELECT DISTINCT  ?value
WHERE {
	?s ?p ?o .
        ?o rdfs:label ?value .
 	FILTER (?p NOT IN ( aamas:lang, aamas:memberOf, aamas:lexeme) )
}
ORDER BY ASC(?value) ")))

(defn listmenu-sparql-lang []
  (str PREFIXES
  (str "
SELECT DISTINCT  ?language ?lpref
WHERE {
	?s aamas:lang ?lang .
        ?lang rdfs:label ?language .
        ?lang aamas:lpref ?lpref.
}
ORDER BY ASC(?language) ")))


(defn listlpv-sparql2 [language]
  (str PREFIXES
  (tmpl/render-string
   (str "
SELECT DISTINCT  ?lang ?prop ?val
WHERE {
GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
	aamas:lang  ?language .
	?language rdfs:label ?lang .
   ?p rdfs:label ?prop .
   ?o rdfs:label ?val .
 	FILTER (?p NOT IN ( aamas:lang ) )
}}
ORDER BY ASC(?prop) ASC(?val)
 ")
{:language language})))

(defn listptype-sparql [language]
  "In case one wants simply a list of pdgmType for a specific language"
  (let [Language (capitalize language)]
    (str PREFIXES
         (str "
    SELECT DISTINCT  ?ptype 
    WHERE { ")
         (apply str
                (tmpl/render-string
                 (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s aamas:lang  aama:{{Language}} ;
        aamas:pdgmType ?QpdgmType .
        OPTIONAL {?QpdgmType rdfs:label ?type .}
        BIND (IF (!bound(?type) ,
                 str(?QpdgmType),
                 ?type 
                ) AS ?ptype
             ) .
     }} }
   ORDER BY ASC(?ptype) ")
                 {:language language
                  :Language Language})))))

(defn listlptype-sparql [ldomain]
;; In case one wants simply a list of pdgmType 
  (let [langs (split ldomain #",")]
    (str PREFIXES
     (str "
    SELECT DISTINCT  ?lang ?ptype 
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s aamas:lang  ?language ;
        aamas:pdgmType ?QpdgmType .
	?language rdfs:label ?lang .
        OPTIONAL {?QpdgmType rdfs:label ?type .}
        BIND (IF (!bound(?type) ,
                 str(?QpdgmType),
                 ?type 
                ) AS ?ptype
             ) .

     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY ASC(?lang) ASC(?ptype) "))))

(defn listlptypepdgm-sparql [ldomain]
;; In case one wants pdgmType to be only a property of specific pdgm
  (let [langs (split ldomain #",")]
    (str PREFIXES
     (str "
    SELECT DISTINCT  ?lang ?ptype ?pname
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s a aamas:Termcluster ;
	aamas:lang  ?language ;
        aamas:pdgmType ?QpdgmType .
        ?s rdfs:label ?pname .
	?language rdfs:label ?lang .
        ?QpdgmType rdfs:label ?ptype .
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY ASC(?lang) ASC(?ptype) ASC(?pname)"))))

(defn listptypelpdgm-sparql [ldomain]
;; In case one wants pdgmType to be only a property of specific pdgm
  (let [langs (split ldomain #",")]
    (str PREFIXES
     (str "
    SELECT DISTINCT   ?ptype ?lang ?pname
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s a aamas:Termcluster ;
	aamas:lang  ?language ;
        aamas:pdgmType ?QpdgmType .
        ?s rdfs:label ?pname .
	?language rdfs:label ?lang .
        ?QpdgmType rdfs:label ?ptype .
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY ASC(?ptype) ASC(?lang) ASC(?pname)"))))


(defn listlpv-check-sparql [ldomain]
  (let [langs (split ldomain #",")]
    (str PREFIXES
     (str "
    SELECT DISTINCT  ?lang ?prop ?value
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
            aamas:memberOf ?pdgm ;
            aamas:lang  ?language .
	?language rdfs:label ?lang .
        ?p rdfs:label ?prop . 
        OPTIONAL {?o rdfs:label ?val . }
        BIND (IF(!bound(?val) ,
                 str(?o),
                 ?val
                ) AS ?value
             ) .
 	FILTER (?p NOT IN ( aamas:lang ) )
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY ASC(?lang) ASC(?prop) ASC(?value)"))))

(defn makeschemata-sparql [lang]
  (str PREFIXES
       (str "
    SELECT DISTINCT  ?lang ?property ?value
    WHERE { ")
       (apply str
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
            aamas:memberOf ?pdgm ;
            aamas:lang  ?language .
	?language rdfs:label ?lang .
        OPTIONAL {?p rdfs:label ?prop . }
        BIND (IF(!bound(?prop) ,
                 str(?p),
                 ?prop
                ) AS ?property
             ) .
        OPTIONAL {?o rdfs:label ?val . }
        BIND (IF(!bound(?val) ,
                 str(?o),
                 ?val
                ) AS ?value
             ) .
 	FILTER (?p NOT IN ( aamas:lang, aamas:lexeme, aamas:memberOf, rdf:type ) )
        FILTER (!CONTAINS (str(?p), \"token\" ))

     }}}
   ORDER BY ASC(?lang) ASC(?prop) ASC(?value)")
               {:language lang}))))



(defn listlpv-sparql [ldomain]
  (let [langs (split ldomain #",")]
    (str PREFIXES
     (str "
    SELECT DISTINCT  ?lang ?prop ?val
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
            aamas:memberOf ?pdgm ;
            aamas:lang  ?language .
	?language rdfs:label ?lang .
        ?p rdfs:label ?prop .
        ?o rdfs:label ?val .
 	FILTER (?p NOT IN ( aamas:lang ) )
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY ASC(?lang) ASC(?prop) ASC(?val)"))))

(defn listpvl-sparql [ldomain]
  (let [langs (split ldomain #",")]
    (str PREFIXES
     (str "
    SELECT DISTINCT  ?prop ?val ?lang
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
	aamas:lang  ?language .
	?language rdfs:label ?lang .
        ?p rdfs:label ?prop .
        ?o rdfs:label ?val .
 	FILTER (?p NOT IN ( aamas:lang ) )
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY  ASC(?prop) ASC(?val) ASC(?lang)"))))

(defn listcpvl-sparql [ldomain]
  (let [langs (split ldomain #",")]
    (str PREFIXES
     (str "
    SELECT DISTINCT  ?pclass ?prop ?lang ?val
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
	aamas:lang  ?language .
	?language rdfs:label ?lang .
        ?p rdfs:label ?prop .
        ?o rdfs:label ?val .
        OPTIONAL { ?p aamas:pclass / rdfs:label ?pclass . }
 	FILTER (?p NOT IN ( aamas:lang ) )
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY  ASC(?pclass) ASC(?prop) ASC(?lang) ASC(?val)"))))

(defn listvpl-sparql [ldomain]
  (let [langs (split ldomain #",")]
    (str PREFIXES
     (str "
    SELECT DISTINCT  ?val ?prop ?lang
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
	aamas:lang  ?language .
	?language rdfs:label ?lang .
        ?p rdfs:label ?prop .
        ?o rdfs:label ?val .
 	FILTER (?p NOT IN ( aamas:lang ) )
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY  ASC(?val) ASC(?prop) ASC(?lang)"))))

(defn listplv-sparql [ldomain]
  (let [langs (split ldomain #",")]
    (str PREFIXES
     (str "
    SELECT DISTINCT  ?prop ?lang ?val
    WHERE { ")
     (apply str
            (for [language langs]
              (tmpl/render-string
               (str "
    {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{language}}> {
	?s ?p ?o ;
	aamas:lang  ?language .
	?language rdfs:label ?lang .
        ?p rdfs:label ?prop .
        ?o rdfs:label ?val .
 	FILTER (?p NOT IN ( aamas:lang ) )
     }} "
                    (if (not (= (last langs) language))
                      (str " 
          UNION")))
               {:language language})))
     (str "}
   ORDER BY  ASC(?prop) ASC(?lang) ASC(?val)"))))

(defn listvlcl-sparql-fv [language lpref propstring]
  (let [;;qpropstring1 (clojure.string/replace propstring #"^.*?," "?")
        qpropstring1 (str "?" propstring)
        qpropstring2 (clojure.string/replace qpropstring1 #",$" "")
        selection (clojure.string/replace qpropstring2 #"," " ?")
        propstring2 (clojure.string/replace qpropstring2 #"^\?" "")
        proplist2 (split propstring2 #",")
        ;;Language (capitalize language)
        ]
    (str PREFIXES
         (tmpl/render-string
          (str "
       PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/>
       SELECT DISTINCT ?pdgmLabel ?pdgmTypeLabel {{selection}} ?lex
       WHERE{
         {   
          GRAPH aamag:{{language}} {
             ?s {{lpref}}:pos {{lpref}}:Verb ;
                {{lpref}}:person ?person ;
                 aamas:memberOf ?pdgm .
            ?pdgm rdfs:label ?pdgmLabel ;
                 aamas:pdgmType ?pdgmType .
             FILTER (CONTAINS (str(?pdgmType), \"Finite\" ))
             ?pdgmType rdfs:label ?pdgmTypeLabel .
             OPTIONAL {?s aamas:lexeme ?lexeme .
                       ?lexeme rdfs:label ?lex .} ")
          {:language language
           ;;:Language Language
           :lpref lpref
           :selection selection})
         (apply str
                (for [prop proplist2]
                  (tmpl/render-string
                   (str "
            OPTIONAL {?s {{lpref}}:{{prop}} ?Q{{prop}} . 
	              ?Q{{prop}} rdfs:label ?{{prop}} . } ")
                   {:prop prop
                    :lpref lpref})))
         (tmpl/render-string 
          (str "}}}
       ORDER BY ?pdgmLabel {{selection}}  ")
          {:selection selection})
         )))

(defn listvlcl-sparql-nfv [language lpref propstring]
    (str PREFIXES
     (tmpl/render-string
      (str "
       PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/>
       SELECT DISTINCT  ?pdgmLabel ?pdgmTypeLabel ?property
       WHERE{
         {   
          GRAPH aamag:{{language}} {
        	?s ?p ?o ;
	             {{lpref}}:pos  {{lpref}}:Verb ;
                     aamas:memberOf ?pdgm .
                ?pdgm aamas:pdgmType ?pdgmType ; 
	              rdfs:label ?pdgmLabel .
                ?pdgmType rdfs:label ?pdgmTypeLabel .
                ?p rdfs:label ?property .
                FILTER (!CONTAINS (str(?pdgmType), \"Finite\" ))
                #FILTER (?pdgmType NOT IN ({{lpref}}:Finite, {{lpref}}:FinitePartial)) 
        	FILTER (?p NOT IN ( aamas:lang, aamas:pdgmType, {{lpref}}:pos, rdf:type))
      }}
     }
      ORDER BY ?pdgmLabel ASC(?pdgmTypeLabel) ASC(?property) ")
      {:language language
       :lpref lpref})
    ))

(defn listvlcl-sparql-pro [language lpref propstring]
  (let [qpropstring1 (clojure.string/replace propstring #"^.*?," "?")
        qpropstring2 (clojure.string/replace qpropstring1 #",$" "")
        selection (clojure.string/replace qpropstring2 #"," " ?")
        propstring2 (clojure.string/replace qpropstring2 #"^\?" "")
        proplist2 (split propstring2 #",")
        Language (capitalize language)]
    (str PREFIXES
     (tmpl/render-string
      (str "
       PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/>
       SELECT DISTINCT ?pdgmLabel ?proClass  {{selection}}
       where{
         {   
          graph aamag:{{language}} {
             ?s {{lpref}}:pos {{lpref}}:Pronoun . 
             #?s {{lpref}}:person ?person .
       ?s aamas:lang aama:{{Language}} .
       ?s aamas:lang ?lang .
        ?s  aamas:memberOf ?pdgm .
        ?pdgm rdfs:label ?pdgmLabel .
       #?s aamas:lexeme ?lexeme .
       #?lexeme rdfs:label ?lex .
       ?lang rdfs:label ?langLabel . ")
      {:language language
       :Language Language
       :lpref lpref
       :selection selection})
     (apply str
            (for [prop proplist2]
              (tmpl/render-string
               (str "
	OPTIONAL { ?s {{lpref}}:{{prop}} ?Q{{prop}} . 
	 ?Q{{prop}} rdfs:label ?{{prop}} . } ")
               {:prop prop
                :lpref lpref})))
            (tmpl/render-string 
             (str "}}}
       ORDER BY ?pdgmLabel  {{selection}}  ")
             {:selection selection})
     )))

(defn listvlcl-sparql-noun [language lpref propstring]
    (str PREFIXES
     (tmpl/render-string
      (str "
       PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/>
       SELECT DISTINCT  ?pdgmLabel ?pdgmTypeLabel ?property 
       WHERE{
         {   
          GRAPH aamag:{{language}} {
        	?s ?p ?o ;
        	     aamas:memberOf ?pdgm ;
	             {{lpref}}:pos  {{lpref}}:Noun .
                ?pdgm aamas:pdgmType ?pdgmType . 
	        ?pdgmType rdfs:label ?pdgmTypeLabel .
                ?pdgm rdfs:label ?pdgmLabel .
                ?p rdfs:label ?property .
  	        FILTER (?p NOT IN ( aamas:lang, aamas:muterm, {{lpref}}:pos, aamas:pdgmType, rdf:type, {{lpref}}:token))
       }}
       }
       ORDER BY ?pdgmLabel ASC(?pdgmTypeLabel) ASC(?property) ")
      {:language language
       :lpref lpref})
     ))

(defn lgvl-sparql [ldomain lval]
  (let [ldoms (split ldomain #",")]
  (str PREFIXES
    (str "
       SELECT DISTINCT ?language ?predlabel
       WHERE { ")
      (apply str  
             (for [ldom ldoms]
               (tmpl/render-string 
                  (str "
         {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
          ?value rdfs:label \"{{type}}\" .
          ?value rdf:type ?predexp .
          ?pred rdfs:range ?predexp .
          ?pred rdfs:label ?predlabel .
          ?pred aamas:lang ?lang .
          ?lang rdfs:label ?language .
          }}  "
                       (if (not (= (last ldoms) ldom))
                         (str " 
          UNION")))
    {:lang ldom
     :type lval})))
      (str "}
       ORDER BY ?language ?predlabel  "))))

(defn prvllg-sparql [languages qstring filter]
  (let [pvals (split qstring #",")
        selection (apply str
		      (for [pval pvals]
                        (if (re-find #"\?" pval)
                          (let [qpval (clojure.string/split pval #"=")
                                qval (clojure.string/replace (last qpval) #"-" "")]
                            (str qval "Label ")))))
        ]
  (str PREFIXES
               (tmpl/render-string 
                  (str "
       SELECT DISTINCT ?language {{selection}}  ?pdgmLabel ?token
       #SELECT DISTINCT ?language {{selection}}  ?token
       WHERE { ")
                  {:selection selection})
               (apply str 
                      (for [language languages]
                        (str
                        (tmpl/render-string 
                  (str "
         {GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
          ?s ?p ?o .")
                  {:lang language})
      (apply str  
             (for [pval pvals]
                 (let [selpval (split pval #"=")
                       selprop (first selpval)
                       selval (last selpval)
]
               (if (re-find #"\?" selval)
                 (let [qselval (clojure.string/replace selval "-" "")
                       qselvalLabel (str qselval "Label")]
                   (tmpl/render-string 
                  (str "
         ?s <http://id.oi.uchicago.edu/aama/2013/{{lang}}/{{selprop}}> 
                {{qselval}} .
           {{qselval}} rdfs:label {{qselvalLabel}} ." )
                  {:lang language
                   :selprop selprop
                   :qselval qselval
                   :qselvalLabel qselvalLabel}))
                 (tmpl/render-string 
                  (str "
         ?s <http://id.oi.uchicago.edu/aama/2013/{{lang}}/{{selprop}}>
	  <http://id.oi.uchicago.edu/aama/2013/{{lang}}/{{selval}}> . ")
                  {:lang language
                   :selprop selprop
                   :selval selval})))))
             (tmpl/render-string 
              (str "
         ?s <http://id.oi.uchicago.edu/aama/2013/{{lang}}/token> ?token .
          FILTER (regex(?token, \"{{filterval}}\"))")
              {:lang language
               :filterval filter})
      (str "
        ?s  aamas:memberOf ?pdgm .
        ?pdgm rdfs:label ?pdgmLabel .
        ?s  aamas:lang ?lng .
        ?lng rdfs:label ?language .
          }}  "
           (if (not (= (last languages) language))
             (str " 
          UNION"))))))
               (tmpl/render-string 
                (str "}
       ORDER BY ?language {{selection}}  ")
                {:selection selection}))))

(defn formpv-sparql [tokenID]
  ;;(for [tokenID tokenIDs]
    (let [token (last (split tokenID #","))
          dataID (first (split tokenID #","))]
      (str PREFIXES
       (tmpl/render-string 
        (str "
       SELECT DISTINCT ?property ?value
       WHERE { 
              ?s ?p ?o .
              ?s ?token \"{{token}}\" .
              ?s  aamas:memberOf ?pdgm .
              ?pdgm rdfs:label \"{{dataID}}\".
              ?p rdfs:label ?property .
              ?o rdfs:label ?value .
         }
       ORDER BY ?property  ")
        {:token token
         :dataID dataID}))))

(defn formpos-sparql [tokenID]
    (let [token (last (split tokenID #","))
          dataID (first (split tokenID #","))]
      (str PREFIXES
       (tmpl/render-string 
        (str "
       SELECT DISTINCT ?pos
       WHERE { 
              ?s ?p ?o .
              ?s ?token \"{{token}}\" .
              ?s  aamas:memberOf ?pdgm .
              ?pdgm rdfs:label \"{{dataID}}\".
              ?p rdfs:label \"pos\" .
              ?o rdfs:label ?pos .
         } ")
        {:token token
         :dataID dataID}))))

(defn formptype-sparql [tokenID]
    (let [token (last (split tokenID #","))
          dataID (first (split tokenID #","))]
      (str PREFIXES
       (tmpl/render-string 
        (str "
       ASK
       WHERE { 
              ?s ?p ?o .
              ?s ?token \"{{token}}\" .
              ?s  aamas:memberOf ?pdgm .
              ?pdgm rdfs:label \"{{dataID}}\".
              ?p rdfs:label \"person\" .
         } ")
        {:token token
         :dataID dataID}))))

