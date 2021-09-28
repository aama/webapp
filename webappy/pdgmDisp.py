#!/usr/local/bin/python3
'''
Assembles sparql query from pvstring. In order:
Prefixes, Select statement, Prop-val triples, Selection triples,
Order-by statement. 
'''
def query(pvstring,lang):

    #pvstring = 'pos=Verb,conjClass=Prefix,tam=Aorist,polarity=Affirmative,rootClass=CVC,lexeme=\'bis\'%number,person,gender,token'
    # print(str("pvstring: " + pvstring))
    # print(str("lang:     " + lang))
        

    languages = {'beja-hud': 'bhu', 'afar': 'afr', 'oromo': 'orm', 'somali-standard': 'sst', 'alaaba': 'alb', 'alagwa': 'alg', 'akkadian-ob': 'aob', 'aari': 'aar', 'arabic': 'arb', 'arbore': 'abr', 'awngi': 'awn', 'bayso': 'bay', 'beja-alm': 'bal','beja-rei': 'bre', 'beja-rop': 'bro', 'beja-van': 'bva', 'beja-wed': 'bwe', 'berber-ghadames': 'bgh', 'bilin': 'bil', 'boni-jara': 'boj', 'boni-kijee-bala': 'bob', 'boni-kilii': 'bok', 'burji': 'bur', 'burunge': 'brn', 'coptic-sahidic': 'csa', 'dahalo': 'dah', 'dhaasanac': 'dha', 'dizi': 'diz', 'egyptian-middle': 'egm', 'elmolo': 'elm', 'gawwada': 'gaw', 'gedeo': 'ged', 'geez': 'gez', 'hadiyya': 'had', 'hausa': 'hau', 'hdi': 'hdi', 'hebrew': 'heb', 'iraqw': 'irq', 'kambaata': 'kam', 'kemant': 'kem', 'khamtanga': 'khm', 'koorete': 'kor', 'maale': 'mal', 'mubi': 'mub', 'rendille': 'ren', 'saho': 'sah', 'shinassha': 'shn', 'sidaama': 'sid', 'syriac': 'syr', 'tsamakko': 'tsm', 'wolaytta': 'wol', 'yaaku': 'yak', 'yemsa': 'yem'}

    lpref = str(languages.get(lang))

    # Prefixes and select statement
    prefixes = """PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
PREFIX aama: <http://id.oi.uchicago.edu/aama/2013/> 
PREFIX aamas: <http://id.oi.uchicago.edu/aama/2013/schema/>"""
    lprefix = str("\nPREFIX " + lpref + ": <http://id.oi.uchicago.edu/aama/2013/" + lang + "/>")
    prefixes = str(prefixes + lprefix + "\n")

    # Select statement
    # if no explicit selection string, use default
    if "%" in pvstring:
        propsel = pvstring.split("%")
        pvstring = propsel[0]
        valstring = propsel[1]
    else:
        valstring = "number,person,gender,token"
    selection = str("?" + valstring.replace(","," ?"))
    selection = selection.replace("-", "")
    select = str("SELECT DISTINCT " + selection + "\nWHERE\n{")
    
    # Triples
    triples = ''
    # 1. prop-val triples
    pvlist = pvstring.split(",")
    for pv in pvlist:
        # print(str(pv))
        propval = pv.split(":")
        if propval[0] == "lexeme":
            triple = str("    ?s aamas:lexeme / rdfs:label \'" + propval[1] + "\'.\n")
        elif propval[0][0:5] == "token":
            triple = str("    ?s " + lpref + ":" + propval[0] + " \'" + propval[1] +  "\'.\n")
        else:
            triple = str("    ?s " + lpref + ":" + propval[0] + " " + lpref + ":" + propval[1] + " .\n")
        triples = triples + triple
    # 2. selection triples
    sels = valstring.split(",")
    for sel in sels:
        sel2 = sel.replace("-", "")
        if sel[0:5] == "token":
            triple = str("    ?s " + lpref + ":" + sel + " ?" + sel2 + " .\n")
        #elif sel == "tokenNote":
            #triple = str("    ?s " + lpref + ":tokenNote ?tokenNote .\n")
        elif sel == "lexeme":
            triple = str("    ?s aamas:lexeme / rdfs:label ?" + sel2 + " .\n")
        else:
            triple = str("    ?s " + lpref + ":" + sel + " / rdfs:label ?" + sel2 + " .\n")
        triples = triples + triple
    triples = str(triples + "}\n")


    #order statement
    selection = selection.replace("?number", "DESC(?number)")
    selection = selection.replace("?gender ", "DESC(?gender)")
    order = str("ORDER BY " + selection)

    query = str(prefixes + select + triples + order +  "\n")

    # print("query: \n" + query)


    return query
    
 






