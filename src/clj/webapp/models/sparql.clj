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
            
(defn pdgmqry-sparql-gen-vrbs [lvalcluster]
  "This version, is to be called for all archive pdgm types. Presumes verbose version of pdgm index (all pdgm values, except lex, have 'prop=val')." 
  (let [lprefmap (read-string (slurp "pvlists/lprefs.clj"))
        ;; parse valcluster
        vals (split lvalcluster #"," 2)
        language (first vals)
        vcs (split (last vals) #"," 2)
        pos (first vcs)
        mvalsprops (split (last vcs) #"%" 2)
        mv (first mvalsprops)
        proplex (last mvalsprops)
        morphclass (first (split mv #"," 2))
        props (if (re-find #"," mv)
                (last (split mv #"," 2))
                "")
        valsLex (split proplex #":" 2)
        valstr (first valsLex)
        lex (if (re-find #":" proplex)
                (last (split proplex #":" 2))
                "")
        valvec (split valstr #",")
        selection (str "?" (clojure.string/replace valstr #"," " ?"))
        selOrder (clojure.string/replace selection #"\?number|\?gender|\?nmbObj|\?gndObj" {"?number" "DESC(?number)" "?gender" "DESC(?gender)" "?nmbObj" "DESC(?nmbObj)" "?gndObj" "DESC(?gndObj)"})
        lang (read-string (str ":" language))
        lpref (lang lprefmap)
        Language (capitalize language)
        ]
    (str PREFIXES
         (tmpl/render-string 
          (str "
	PREFIX {{lpref}}:   <http://id.oi.uchicago.edu/aama/2013/{{language}}/> 
	SELECT {{selection}} ?token
	WHERE
        { 
	 { 
	  GRAPH aamag:{{language}}  
          { 
	   ?s {{lpref}}:pos {{lpref}}:{{pos}} .  
	   ?s aamas:lang aama:{{Language}} . 
	   ?s aamas:lang / rdfs:label ?langLabel .
           ?s ?QmorphClass {{lpref}}:{{morphclass}}. ")
          {:lpref lpref
           :selection selection
           :pos pos
           :morphclass morphclass
           :language language
           :Language Language})
         (if (re-find #"\w" lex)
           (tmpl/render-string 
            (str "
           ?s aamas:lexeme ?lexeme .
           ?lexeme rdfs:label \"{{lex}}\" .")
            {:lex lex}))
         (if (re-find #"\w"  props)
           (apply str  
                  (for [prop (split props #",")]
                    (let [pv (split prop #"=")
                          property (first pv)
                          value (last pv)]
                      (tmpl/render-string 
                       (str "
           ?s {{lpref}}:{{property}}  {{lpref}}:{{value}} .  ")
                       {:lpref lpref
                        :property property
                        :value value} )))))
         (apply str
                (for [val valvec]
                  (tmpl/render-string
                   (str "
	   ?s {{lpref}}:{{val}} / rdfs:label ?{{val}} .  " )
                   {:lpref lpref
                    :val val})))
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
	ORDER BY {{selOrder}} ")
          {:lpref lpref
          :selOrder selOrder})
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
	{{lpref}}:vmorphClass  ?QmorphClass .
        ?QmorphClass rdfs:label ?morphClass .
        ?p rdfs:label ?property .
        FILTER (CONTAINS (str(?morphClass), \"Finite\" ))
 	FILTER (?p NOT IN ( aamas:lang, {{lpref}}:pngShapeClass, {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) )
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
SELECT DISTINCT ?property
WHERE {
GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
	?s ?p ?o ;
	{{lpref}}:vmorphClass  ?QmorphClass .
        ?QmorphClass rdfs:label ?morphClass .
        ?p rdfs:label ?property .
        FILTER (!CONTAINS (str(?morphClass), \"Finite\" ))
        FILTER (?p NOT IN ( aamas:lang, {{lpref}}:pngShapeClass, {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) ) 	
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
	{{lpref}}:proClass  ?QmorphClass .
        #?QmorphClass rdfs:label ?morphClass .
        ?p rdfs:label ?property .
        FILTER (?p NOT IN ( aamas:lang, {{lpref}}:pngShapeClass, {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) ) 	
        #FILTER (?p NOT IN ( aamas:lang, {{lpref}}:gender, {{lpref}}:number, {{lpref}}:pngShapeClass, {{lpref}}:person, {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) )
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
SELECT DISTINCT    ?property
WHERE {
GRAPH <http://oi.uchicago.edu/aama/2013/graph/{{lang}}> {
	?s ?p ?o ;
	{{lpref}}:nmorphClass  ?QmorphClass .
        #?QmorphClass rdfs:label ?morphClass .
        ?p rdfs:label ?property .
        FILTER (?p NOT IN ( aamas:lang, {{lpref}}:pngShapeClass,  {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) ) 	
        #FILTER (?p NOT IN ( aamas:lang, {{lpref}}:gender, {{lpref}}:number, {{lpref}}:pngShapeClass, {{lpref}}:person, {{lpref}}:pos, {{lpref}}:token, rdf:type, {{lpref}}:multiLex ) )
}
}
ORDER BY ASC(?property) ")
{:lang language
 :lpref lpref})))

(defn listmenu-sparql-prop []
  (str PREFIXES
  (str "
SELECT DISTINCT  ?property
WHERE {
	?s ?p ?o ;
            aamas:memberOf ?pdgm .
        OPTIONAL {?p rdfs:label ?prop .}
        BIND (IF(!bound(?prop) ,
                 str(?p),
                 ?prop
                ) AS ?property
             ) .
        FILTER (!CONTAINS (str(?p), \"token\" ))
 	FILTER (?p NOT IN ( aamas:lang, rdf:type ) )
}
ORDER BY ASC(?property) ")))

(defn listmenu-sparql-val []
  (str PREFIXES
  (str "
SELECT DISTINCT  ?value
WHERE {
	?s ?p ?o ;
            aamas:memberOf ?pdgm .
        OPTIONAL {?o rdfs:label ?val .}
        BIND (IF(!bound(?val) ,
                 str(?o),
                 ?val
                ) AS ?value
             ) .
        FILTER (!CONTAINS (str(?p), \"token\" ))
 	FILTER (?p NOT IN ( aamas:lang, aamas:memberOf, aamas:lexeme, rdf:type) )
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

(defn formmclass-sparql [tokenID morphprop]
    (let [token (last (split tokenID #","))
          dataID (first (split tokenID #","))]
      (str PREFIXES
       (tmpl/render-string 
        (str "
       SELECT DISTINCT ?mclass
       WHERE { 
              ?s ?p ?o .
              ?s ?token \"{{token}}\" .
              ?s  aamas:memberOf ?pdgm .
              ?pdgm rdfs:label \"{{dataID}}\".
              ?s ?morphprop ?morphvalue .
              ?morphprop rdfs:label \"{{morphprop}}\" .
              ?morphvalue rdfs:label ?mclass .
         } ")
        {:token token
         :dataID dataID
         :morphprop morphprop}))))

