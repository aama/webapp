(def termclusters [{:label "beja_W-VPrefCiCiCAffAor-sibib"
                 :note "Wedekind-etal2007 p. 110"
                 :common {:vmorphClass :Finite
                          :conjClass :Prefix,
                          :lexeme :sibib,
                          :polarity :Affirmative,
                          :pos :Verb,
                          :rootClass :CiCiC,
                          :tam :Aorist}
                 :terms [[:number :person :gender :token],
                         [:Singular :Person1 :Common "íišbíb"],
                         [:Singular :Person2 :Masc "tíišbíba"],
                         [:Singular :Person2 :Fem "tíišbíbi"],
                         [:Singular :Person3 :Masc "išbíb"],
                         [:Singular :Person3 :Fem "t’išbí"],
                         [:Plural :Person1 :Common "níišbib"],
                         [:Plural :Person2 :Common "tíišbibna"],
                         [:Plural :Person3 :Common "íišbibna"]]
                 }
                {:label "beja_W-VPrefCiCiCAffPast-sibib"
                 :note "Wedekind-etal2007 p. 109"
                 :common {:vmorphClass :Finite
                          :conjClass :Prefix,
                          :lexeme :sibib,
                          :polarity :Affirmative,
                          :pos :Verb,
                          :rootClass :CiCiC,
                          :tam :Past}
                 :terms [[:number :person :gender :token],
                         [:Singular :Person1 :Common "ašbíb"],
                         [:Singular :Person2 :Masc "tíšb’ba"],
                         [:Singular :Person2 :Fem "tíšbíbi"],
                         [:Singular :Person3 :Masc "išbíb"],
                         [:Singular :Person3 :Fem "tišbíb"],
                         [:Plural :Person1 :Common "níšbib"],
                         [:Plural :Person2 :Common "tíšbibna"],
                         [:Plural :Person3 :Common "íšbibna"]]
                 }])
                {:label "beja_W-VPrefCiCiCAffPres-sibib"
                 :note "Wedekind-etal2007 p. 109"
                 :common {:vmorphClass :Finite
                          :conjClass :Prefix,
                          :lexeme :sibib,
                          :polarity :Affirmative,
                          :pos :Verb,
                          :rootClass :CiCiC,
                          :tam :Present}
                 :terms [[:number :person :gender :token],
                         [:Singular :Person1 :Common "ašánbíib"],
                         [:Singular :Person2 :Masc "šánbíiba"],
                         [:Singular :Person2 :Fem "šánbíibi"],
                         [:Singular :Person3 :Masc "šánbíib"],
                         [:Singular :Person3 :Fem "šánbíib"],
                         [:Plural :Person1 :Common "nišábib"],
                         [:Plural :Person2 :Common "tišábibna"],
                         [:Plural :Person3 :Common "išábibna"]]
                 }]))


(def headprops (clojure.string/join "," (for [termcluster termclusters] (clojure.string/join "," (keys (termcluster :common))))))
              ;; then disj props from pos, v/nmorphClass, proClass, lexeme
              headpropvec1 (into (sorted-set) (split headprops #","))
              headpropvec2 (disj headpropvec1 ":pos" ":lexeme" ":nmorphClass" ":pmorphClass" ":vmorphClass")
              headpropstr (join "," headpropvec2)


(def note2 "Wedekind-etal2007 p. 154")

(def note1 "Wedekind-etal2007 p. 154 :: (cf. also 152) (iikta become frm aka,kaya ?). Tokens to be glossed: 'I will become a teacher', etc.")

user=> (def note3 " :: (cf. also 152) (iikta become frm aka,kaya ?). Tokens to be glossed: 'I will become a teacher', etc.")
#'user/note3

user=> (clojure.string/split note1 #"::")
["Wedekind-etal2007 p. 154 " " (cf. also 152) (iikta become frm aka,kaya ?). Tokens to be glossed: 'I will become a teacher', etc."]

user=> (clojure.string/split note2 #"::", 2)
["Wedekind-etal2007 p. 154 "]
[user=> (apply str (clojure.string/split note2 #"::", 2))
"Wedekind-etal2007 p. 154 "]
user=> (nth (clojure.string/split note2 #"::", 2) 0)
"Wedekind-etal2007 p. 154 "
user=> (nth (clojure.string/split note1 #"::", 2) 0)
"Wedekind-etal2007 p. 154"
user=> (clojure.string/split note3 #"::", 2)
[" " " (cf. also 152) (iikta become frm aka,kaya ?). Tokens to be glossed: 'I will become a teacher', etc."]
user=> (nth (clojure.string/split note3 #"::", 2) 0)
" "
user=> (nth (clojure.string/split note3 #"::", 2) 1)
" (cf. also 152) (iikta become frm aka,kaya ?). Tokens to be glossed: 'I will become a teacher', etc."

;;NO - 17 / 19
(def pdgm2 "Wedekind-etal2007 Table 33,Verb,Overview_Aux%lexeme,token,token-note-auxGloss")
"Wedekind-etal2007 Table 33,Verb,Overview_Aux, , , , , , , , , , , , , , \r\n"
"Wedekind-etal2007 Table 33,Verb,Overview_Aux, , , , , , , , , , , , , , , , \r\n"

;;NO - 12 (12+13 together) / 14 (12+13 together)
(def pdgm2 "Wedekind-etal2007 p. 103,Verb,Finite,conjClass=Prefix,lexeme=diy,polarity=Negative,rootClass=Ciy,tam=Past%number,person,gender,token")
"Wedekind-etal2007 p. 103,Verb,Finite, , Prefix, , , , Negative, Ciy, , Pastdiy\r\n"
"Wedekind-etal2007 p. 103,Verb,Finite, , , , Prefix, , , , Negative, Ciy, , Pastdiy\r\n"

;;NO - 16 / 18
(def pdgm2 "Wedekind-etal2007 Table 41 ,Verb,Affix_RelSuff,clauseType=Relative%caseHead,formFrequency,genHead,headPosition,token")
"Wedekind-etal2007 Table 41 ,Verb,Affix_RelSuff, Relative, , , , , , , , , , , , \r\n"
"Wedekind-etal2007 Table 41 ,Verb,Affix_RelSuff, , , Relative, , , , , , , , , , , , \r\n"

(def tableheads [:source,:pos,:morphClass,:clauseType,:conjClass,:gender,:lexeme,:nonFiniteForm,:number,:person,:polarity,:proClass,:rootClass,:stemClass,:strength,:tam])

(def headprops (for [tablehead tableheads] (clojure.string/replace tablehead #":" "")))

(def vcs2 (clojure.string/split pdgm2 #"," 2))
(def ref2 (first vcs2))
(def mvalsprops2 (clojure.string/split (last vcs2) #"%" 2))
(def mv2 (first mvalsprops2))
(def pos2 (first (clojure.string/split mv2 #"," 2)))
(def mcprop2 (last (clojure.string/split mv2 #"," 2)))
(def morphclass2 (first (clojure.string/split mcprop2 #"," 2)))
(def props2 (if (re-find #"," mcprop2) (last (clojure.string/split mcprop2 #"," 2)) ""))
(def propvec2 (clojure.string/split props2 #","))
(def propmap2 (apply merge (for [prop propvec2] (hash-map (keyword (first (clojure.string/split prop #"="))) (str (last (clojure.string/split prop #"=")))))))
(def propmap3 (into (sorted-map) propmap2))
(def propseq12 (apply str (for [tablehead tableheads] (if ( (keyword tablehead) propmap2) ( str  ( (keyword tablehead) propmap3) ", ") ( str ", " )))))
(def propseq22 (last (clojure.string/split propseq12 #", , " 2)))
(def tline2  (str ref2 "," pos2 "," morphclass2   propseq22  "\r\n"))

(def tline (str  ref "," pos "," morphclass   propseq  "\r\n"))








(def vcs (clojure.string/split pdgm1 #"," 2))
(def ref (first vcs))
(def mvalsprops (clojure.string/split (last vcs) #"%" 2))
(def mv (first mvalsprops))
(def pos (first (clojure.string/split  #"," 2)))
(def mcprop (last (clojure.string/split mv #"," 2)))
(def morphclass (first (clojure.string/split mcprop #"," 2)))
(def props (if (re-find #"," mcprop) (last (split mcprop #"," 2)) ""))(def propvec (clojure.string/split props #","))
(def propmap (apply merge (for [prop propvec] (hash-map (keyword (first (clojure.string/split prop #"="))) (str (last (clojure.string/split prop #"=")))))))
(def propseq (apply str (for [tablehead tableheads] (if ( (keyword tablehead) propmap) ( str "," ( (keyword tablehead) propmap)) ( str ", " )))))

