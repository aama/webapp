#!/usr/local/bin/python3
'''
Comand-line version of pdgmDisp, should print query and pdgm to iTerm
window. For use in jupyter.
'''

from SPARQLWrapper import SPARQLWrapper, JSON
import shelve
from tabulate import tabulate

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

    #print(str("QUERY: \n" + query))
    return query
    
#lname = "beja-hud"
#pname = "Verb,Prefix,dbl,Affirmative,CCC,Aorist"

lname = input('Type language name: ')
pname = input('Type pdgm name: ')

print(str("lname: " + lname))
print(str("pname: " + pname))

sfile = str('pvlists/pdgmdb' + lname)
# get pvalue from pkey in (unshelved) pdgmdb
pdgmdb = shelve.open(sfile) # open it
pvalue = pdgmdb[pname] # get the full prop-val string
pdgmdb.close()  # close it right away
print(str('pvalue: ' + pvalue))

# pdgmDisp.query makes SPARQL query out of full prop-val
# pdgm specification in pval
res = query(pvalue,lname)
print(str("QUERY: \n" + res))

# For query formation, cf. DuCharme, Learning SPARQL, pp. 285ff:
# going to localhost server for apache-jena-fuseki
# [note: must have activated previously wjith 'bin/fuseki.sh']
sparql = SPARQLWrapper("http://localhost:3030/aama/query")
sparql.setQuery(res)
sparql.setReturnFormat(JSON)
results = sparql.query().convert()
# print(results)
select = results["head"]["vars"]
header2 = []
for s in select:
    header2.append(s.upper())
# create 'select' row [= header])    
header =  ("    ").join(select).upper()
paradigm = str(header + "\n")
paradigm2 = []
for result in results["results"]["bindings"]:
    #print('result: ')
    #print(result)
    pdgmrow = ""
    pdgmrow2 = []
    for sel in select:
        sel = result[sel]["value"]
        #print('sel: ')
        #print(sel)
        pdgmrow = str(pdgmrow + sel + "      ") 
        pdgmrow2.append(sel)
        # i.e.
        # number = result["number"]["value"]
        # person = result["person"]["value"]
        # gender = result["gender"]["value"]
        # token  = result["token"]["value"]
        # pdgmrow = (str(number +  "      " + person + "     " + gender + "     " + token + "\n"))
    #paradigm = with header (for straight csv); paradigm2 = w/o header
    paradigm = str(paradigm + pdgmrow + "\n")
    paradigm2.append(pdgmrow2)

# Just print raw CSV:
print(str("PARADIGM: \n" + paradigm))

# Using format:
print("PARADIGM: ")
colspace = "{:<12}{:<12}{:<12}{:<32}"
colheads = "NUMBER, PERSON, GENDER, TOKEN"
print("{:<12}{:<12}{:<12}{:<32}".format('NUMBER', 'PERSON', 'GENDER', 'TOKEN'))
# also works: print(colspace.format('NUMBER', 'PERSON', 'GENDER', 'TOKEN'))
# doesn't work: print(colspace.format(colheads))
#print(paradigm2)
for row in paradigm2:
    number, person, gender, token = row
    print("{:<12}{:<12}{:<12}{:<32}".format(number, person, gender, token))

# Using tabulate:
print("\nPARADIGM: ")
#print(select)
#print(tabulate(paradigm2, headers=['NUMBER', 'PERSON', 'GENDER', 'TOKEN']))
print(tabulate(paradigm2, headers=header2))
print('\n')

# Conclusion: 'format' approach hard to generalize; have to recalculate
# colspace and restate colheads every time they change;
# tabulate works for every imaginable CSV






