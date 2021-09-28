#!/usr/local/bin/python3
'''
Comand-line version of pdgmDisp, should print query and pdgm to iTerm
window. For use in jupyter.
'''

from SPARQLWrapper import SPARQLWrapper, JSON
import shelve
from tabulate import tabulate
import pandas as pd
from pandas import Series, DataFrame
from io import StringIO

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
    
lname = "beja-hud"
pname = "Verb,Prefix,dbl,Affirmative,CCC,Present"

#lname = input('Type language name: ')
#pname = input('Type pdgm name: ')

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
# [note: must have activated previously with 'bin/fuseki.sh']
sparql = SPARQLWrapper("http://localhost:3030/aama/query")
sparql.setQuery(res)
sparql.setReturnFormat(JSON)
results = sparql.query().convert()
results2 = sparql.query()

# print raw query results
print(str("1. PARADIGM: (raw query)"))
print(results2)
print('\n')
print(str("2. PARADIGM: (raw query-output)"))
print(results)
print('\n')
print(str("3A. HEADER: (select/header2)"))
select = results["head"]["vars"]
print(str("Select="))
print(select)
header2 = []
for s in select:
    header2.append(s.upper())
print(str("Header2="))
print(header2)
print('\n')
# create 'select' row [= header])    
header =  ("    ").join(select).upper()
header3 =  (",").join(select)
paradigm = str(header3 + "\n")
paradigm2 = []
paradigm2.append(select)
for result in results["results"]["bindings"]:
    #print('result: ')
    #print(result)
    #pdgmrow = ""
    pdgmrow = []
    pdgmrow2 = []
    for sel in select:
        sel = result[sel]["value"]
        #print('sel: ')
        #print(sel)
        # pdgmrow = str(pdgmrow + sel + ",") 
        pdgmrow.append(sel)
        pdgmrow2.append(sel)
        # i.e.
        # number = result["number"]["value"]
        # person = result["person"]["value"]
        # gender = result["gender"]["value"]
        # token  = result["token"]["value"]
        # pdgmrow = (str(number +  "      " + person + "     " + gender + "     " + token + "\n"))
    #paradigm = with header (for straight csv); paradigm2 = w/o header
    pdgmrowstr = (",").join(pdgmrow)
    paradigm = str(paradigm + pdgmrowstr + "\n")
    paradigm2.append(pdgmrow2)

print("3. PARADIGM: (query-output 'value'-bindings: row=list)")
print(paradigm2)
print("\n")
# Just print simple CSV:
print(str("4. PARADIGM: (basic CSV: row=string)"))
print(paradigm)
# Basis for tabulate, format:
# print(str("PARADIGM: (basic CSV)\n" + paradigm2))

# Using format:
print("5. PARADIGM: (format)")
# 'format' output now will only work with npg-token pdgms;
# needs to be generalized
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
print("\n6. PARADIGM: (tabulate) ")
#print(select)
#print(tabulate(paradigm2, headers=['NUMBER', 'PERSON', 'GENDER', 'TOKEN']))
# Note both paradigm2 and header2 are lists
print(tabulate(paradigm2, headers=header2))

# Conclusion: 'format' approach hard to generalize; 
# have to recalculate
# colspace and restate colheads every time they change;
# tabulate works for every imaginable CSV

# Using pandas:
print("\n7. PARADIGM: (pandas, csv to dict) ")
# For simplest df, have to convert CSV col/row output into dict
# (here done by hand), need csv2dict.py

print("7A. Dictionary:")
pdata = {'number': ['Singular', 'Singular', 'Singular', 'Singular', 'Singular', 'Plural', 'Plural', 'Plural'],
        'person': ['Person1', 'Person2', 'Person2', 'Person3', 'Person3', 'Person1', 'Person2', 'Person3'],
        'gender': ['Common', 'Masc', 'Fem', 'Masc', 'Fem', 'Common', 'Common', 'Common'],
        'token': ['?-iidbíl', 't-iidbil-`a', 't-iidbil-`i', '?-iidbíl', 't-iidbíl', 'n-iidbíl', 't-iidbil-`na', '?-iidbil-`na']}
print(pdata)
print("7B. Paradigm:")
pframe = pd.DataFrame(pdata)
print(pframe)

print("\n8. PARADIGM: (pandas, by read_csv) ")
# Read any file-like csv data structure into pd.DataFrame via StringIO class
# "By file-like object, we refer to objects with a read() method, such as
#  a file handle (e.g. via builtin open function) or StringIO." See:
# https://pandas.pydata.org/pandas-docs/stable/user_guide/io.html 

# 'pcsv'  and 'pcsv2' are two csv-format strings made up by hand for
# "Verb,Prefix,dbl,Affirmative,CCC,Aorist"; 'paradigm'. can be any
# pdgm returned by a 'query(pvalue,lname)' query.

pcsv = """\
number,person,gender,token
Singular,Person1,Common,?-iidbíl
Singular,Person2,Masc,t-iidbil-`a
Singular,Person2,Fem,t-iidbil-`i
Singular,Person3,Masc,?-iidbíl
Singular,Person3,Fem,t-iidbíl
Plural,Person1,Common,n-iidbíl
Plural,Person2,Common,t-iidbil-`na
Plural,Person3,Common,?-iidbil-`na"""

pcsv2 = """\
number,person,gender,token\nSingular,Person1,Common,?-iidbíl\nSingular,Person2,Masc,t-iidbil-`a\nSingular,Person2,Fem,t-iidbil-`i\nSingular,Person3,Masc,?-iidbíl\nSingular,Person3,Fem,t-iidbíl\nPlural,Person1,Common,n-iidbíl\nPlural,Person2,Common,t-iidbil-`na\nPlural,Person3,Common,?-iidbil-`na"""

#p1 = pd.read_csv(StringIO(pcsv2))
p1 = pd.read_csv(StringIO(paradigm))

# Without StringIO, paradigm, pcsv, pcsv2 would be taken as file names
# p1 = pd.read_csv(paradigm)

# "DataFrame constructor not properly called!"
# p1 = pd.DataFrame(pcsv2)

# The following works, but would require making an explicit csv file
# for each paradigm.
# p1 = pd.read_csv('pdgms/beja-hud1.csv')

print(p1)
print('\n')








