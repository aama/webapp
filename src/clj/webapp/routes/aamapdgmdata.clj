(ns webapp.routes.aamapdgmdata
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

(defn aamapdgmdata []
  (layout/common 
   [:div {:class "info-page"}

    [:h2 "Note on Paradigm Data"]
    [:p
    [:em  "\"Paradigm\""]" is here taken in its simplest and most  obvious sense as"
[:em " any presentation of 
one or more linguistic forms (\"tokens\": words, affixes, clitics, stems, etc.), along with a 
set of its morphological properties and values. Typically the shared property=value features of
 the set of forms in a paradigm are given in some string/label form (technically an indexed 
list or \"hash-map\"), while the property=value features in which they differ are presented in 
table-format with property-defined columns and and value-defined rows:"]]

    [:p "Thus, in informal notation, the paradigm: "] 

   [:pre "
                        (number)  (person)  (gender)   (token)
                        Singular  Person1    Common	xaw
                        Singular  Person2    Common	xaydă
                        Singular  Person3    Masc	xay
                        Singular  Person3    Fem	xaydă
                        Plural	  Person1    Common	xaynă
                        Plural	  Person2    Common	xayday
                        Plural	  Person3    Common	xayay"
      ]


    [:p " might be labeled: "
     [:ul
      [:li "Informally: \"the number, person gender paradigm of imperfect affirmative of the 
Burunge glide verb "  [:em "xaw-"] "'come'\""]
      [:li "Or less informally: \"paradigm of the " 
       [:em "number, person, gender, token"] 
" values of forms whose property=value features are "
       [:em "language=Burunge, pos=Verb, polarity=Affirmative, stemClass=Glide, lexeme=:xaw, tam=Imperfect"]\" ""]]]

    [:p "Information corresponding to this paradigm in EDN format:"



    [:pre
"
                {:label \"burunge-VBaseImperfGlideStemBaseForm-xaw\"
                 :note \"Kiessling1994 ## 7.2.2,7.2.3\"
                 :common {:morphClass :Finite
                          :polarity :Affirmative,
                          :lexeme :xaw,
                          :pos :Verb,
                          :stemClass :GlideStem,
                          :tam :Imperfect}
                 :terms [[:number :person :gender :token],
                         [:Singular :Person1 :Common \"xaw\"]
                         [:Singular :Person2 :Common \"xaydă\"],
                         [:Singular :Person3 :Masc \"xay\"]
                         [:Singular :Person3 :Fem \"xaydă\"]
                         [:Plural :Person1 :Common \"xaynă\"],
                         [:Plural :Person2 :Common \"xayday\"],
                         [:Plural :Person3 :Common \"xayay\"]]
                 }

"] 
"where 'morphClass' is a utility category whose purpose is to give the user an idea about the 
morphological content (affix, stem, clitic, word) of a particular paradigm, out of what is 
often a very long list; 'Finite' is the designation for a standard finite verb paradigm."]
    [:p 
"It should be obvious that, whether informally or in EDN noatation, this set of seven 
property-value associations could also be formatted, with a  fair amount of redundancy, entirely 
as a table;  or, with even more redundancy (which, as it happens is close to the way it is 
represented in an RDF datastore), entirely as an indexed list. "]


[:h2 "Paradigm Labels in the AAMA archive"]
[:p "In this application, for the purposes of display, comparison, modification, in the various
 select-lists, checkbox-lists, and text-input  fields, paradigms are labeled as a comma-separated
 string of shared property=value components, followed,  after a delimiter '%' by a comma-separated
 list of the properties whose values  consititute the rows of the paradigm. In the, frequently 
long, paradigm lists automatically generated from the EDN file by the " (link-to "/pdgmIndex" , 
"Create Paradigm Lists") " utility, for ease in processing the first two properties are always 
pos (part-of-speech) and morphClass, and, for ease in reading the 'property=' part of the label 
is omitted. Thus the label of above illustrated paradigm would be: "]
[:p 
[:pre 
"Verb,Finite,lex=:xaw,polarity=Affirmative,stemClass=Glide,tam=Imperfect&number,person,gender"]]
[:p "and might occur in a list as:"
    [:pre "
.  .  .
Verb,Finite,lex=:qadid,polarity=Affirmative,stemClass=DentalStem,tam=Perfect%number,person,gender
Verb,Finite,lex=:qadid,polarity=Affirmative,stemClass=DentalStem,tam=Subjunctive%number,person,gender
Verb,Finite,lex=:xaw,polarity=Affirmative,stemClass=GlideStem,tam=Imperfect%number,person,gender
Verb,Finite,lex=:xaw,polarity=Affirmative,stemClass=GlideStem,tam=Perfect%number,person,gender
Verb,Finite,lex=:xaw,polarity=Affirmative,stemClass=GlideStem,tam=Subjunctive%number,person,gender
. . .
"
] "(See for example, the select-lists in "  (link-to "/pdgmindiv" "Individual Paradigm Display") ", or checkbox-lists in "  (link-to "/pdgmmultdef"  "Multiple Paradigm  Display")".)"]
       [:div
       [:footer
        [:p "AAMA Webapp Help"]]]]
))

(defroutes aamapdgmdata-routes
  (GET "/aamapdgmdata" [] (aamapdgmdata)))


