'''
Comma-separted strings of property values in listbox are keys to full 
pdgm-defining prop:val strings (pvstrings/pdgm-dict-LANG.py). SPARQL query
 for pdgm  derived from that in pdgmDisp.query. Widgets and grid modeled 
on Mark Roseman, Modern Tkinter -- cf. esp. pp. 67-70.
NOTE: In this one-frame version of pdgmDispUI, paradigm and res
[=SPARQL query formed in pdgmDisp.query()] are inserted into the Tk text
 widgets lpdgm and lquery.

'''

from tkinter import *
from tkinter import ttk
from SPARQLWrapper import SPARQLWrapper, JSON
from tabulate import tabulate
import pandas as pd
from pandas import Series, DataFrame
from io import StringIO
#import pdgmDisp     #import for pdgm-display query code
import shelve
# import re


f = ('times',16) #'the pleasing font'

def query(pvstring,lang):

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
        # print(str("triple = " + triple))
        triples = triples + triple
        #print(str("triples = " + triples))

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
        # print(str("triple = " + triple))
        triples = triples + triple
        #print(str("triples = " + triples))
    triples = str(triples + "}\n")
    #pvstring = 'pos=Verb,conjClass=Prefix,tam=Aorist,polarity=Affirmative,rootClass=CVC,lexeme=\'bis\'%number,person,gender,token'
    # print(str("pvstring: " + pvstring))
    # print(str("lang:     " + lang))
        
    #order statement
    selection = selection.replace("?number", "DESC(?number)")
    selection = selection.replace("?gender ", "DESC(?gender)")
    order = str("ORDER BY " + selection)

    query = str(prefixes + select + triples + order +  "\n")

    # print("query: \n" + query)

    return query

 
# Called when the selection in the lbox1 changes; figure out
# which language is currently selected, and then lookup its lcode,
# and from that, its lfile.  As well, clear the selected pdgmmessage, 
# so it doesn't stick around after we start doing
# other things.
def showLfile(*args):
    idxs = lbox.curselection()
    if len(idxs) == 1:
        idx = int(idxs[0])
        lname = languagenames[idx]
        lmsg1.set("Paradigms for %s" % lname)
        lmsg2.set(lname)
        lfilename = str("pvlists/pdgm-keys-" + lname + ".txt")
        lfile = open(lfilename, "r")
        pdkeys = lfile.read()
        # if retrieve pdgmdict from dbm
        #  pdkeys = lpdgmdict.keys()
        # works in interactive: for k in lpdgmdict.keys(): print(k, end='')
        pvar.set(pdkeys)
        pmsg.set('')

# Called when the user  clicks an item in the lbox2.
def choosePdgm(*args):
    idxs = pbox.curselection()
    if len(idxs)==1:
        idx = int(idxs[0])
        lbox.see(idx)
        pname = pbox.get(idx)
        # pmsg.set("PDGM: %s (# %s)" % (pname, idx))
        pmsg.set(pname)
        # print(pmsg)

#function for the pdgm-display button
def guipdgm(*args):
    pdisp.set('paradigm .... ')
    pkey = pmsg.get()  #get key that user selected
    l = str(lmsg2.get())
    sfile = str('pvlists/pdgmdb' + l)
    print("pkey = ")
    print(pkey)
    print("sfile = ")
    print(sfile)

    # get pvalue from pkey in (unshelved) pdgmdb
    pdgmdb = shelve.open(sfile) # open it
    pvalue = pdgmdb[pkey] # get the full prop-val string
    pdgmdb.close()  # close it right away
    print(str('PVALUE: ' + pvalue))


    # possible test for existence of pval (not used here)
    #check characters
    #if re.search('^[a-zA-Z0-9=:\-,\"\'%_/]+$',w):
    #if 1 == 1:
    #if so display it

    # pdgmDisp.query makes SPARQL query out of full prop-val
    # pdgm specification in pval
    res = query(pvalue,l)
    # For query formation, cf. DuCharme, Learning SPARQL, pp. 285ff:
    # going to localhost server for apache-jena-fuseki
    # [note: must have activated previously wjith 'bin/fuseki.sh']
    sparql = SPARQLWrapper("http://localhost:3030/aama/query")
    sparql.setQuery(res)
    sparql.setReturnFormat(JSON)
    results = sparql.query().convert()
    # print(results)
    select = results["head"]["vars"]
    select2 = []
    for s in select:
        select2.append(s.upper())
    print("Select2:")
    print(select2)
    print("Select:")
    print(select)
    # create 'select' row [= header])    
    header =  ("    ").join(select).upper()
    header3 =  (",").join(select2)
    paradigm = str(header3 + "\n")
    paradigm2 = []
    for result in results["results"]["bindings"]:
        pdgmrow = []
        pdgmrow2 = []
        for sel in select:
            sel = result[sel]["value"]
            # pdgmrow = str(pdgmrow + sel + "      ") 
            pdgmrow.append(sel)
            pdgmrow2.append(sel)
            # i.e.
            # number = result["number"]["value"]
            # person = result["person"]["value"]
            # gender = result["gender"]["value"]
            # token  = result["token"]["value"]
            # pdgmrow = (str(number +  "      " + person + "     " + gender + "     " + token + "\n"))
        pdgmrowstr = (",").join(pdgmrow)
        paradigm = str(paradigm + pdgmrowstr + "\n")
        paradigm2.append(pdgmrow2)

    #pdisp.set(paradigm)
    # qdisp.set(res)
    pnum = int(pcount.get()) + 1
    # print(pnum)
    pcount.set(pnum)
    L = l.capitalize()
    plabel = str(pcount.get() + ". " + L + ":" + pkey + "\n")

    # This gives the simple CSV
    print("CSV:")
    print(paradigm)

    # This gives 'Paradigm-6' of pdgmDisp-pd.py
    pdgmtab = tabulate(paradigm2, headers = select2)
    print("tabulate:")
    print(pdgmtab)
    # This gives 'Paradigm-8' of pdgmDisp-pd.py
    # TODO: 1) columns for pdgmpd
    #       2) why ... in token
    pdgmpd = pd.read_csv(StringIO(paradigm))
    print("\nDataFrame:")
    print(pdgmpd)
    print("\n")

    # Write the pdgm(s) to the lpdgm text widget
    lpdgm.insert('end', plabel)
    lpdgm.insert('end', "\nA-Rendered by 'tabulate':\n ")
    lpdgm.insert('end', pdgmtab)
    lpdgm.insert('end', "\n")
    lpdgm.insert('end', "\nB-Rendered as pandas DataFrame:\n")
    lpdgm.insert('end', pdgmpd)
    lpdgm.insert('end', "\n\n")
    #lpdgm.insert('end', paradigm)

    #query header
    qlabel = str(pcount.get() + ". " + L + " - " + pvalue)
    # ERROR: NameError: name 'lang' is not defined
    qtext = str(qlabel + res)

    lquery.insert('end', qtext)
    #lquery.insert('end', res)
    lquery.insert('end', "\n\n")
    print("Query:")
    print(res)
    # else:
    #pdisp.set(str('Problem with w: ' + w))

#languagenames = ('beja-hud', 'afar', 'oromo', 'somali-standard')

languagenames = ('afar', 'alaaba', 'alagwa', 'akkadian-ob', 'arabic', 'arbore', 'awngi', 'bayso', 'beja-alm', 'beja-hud', 'beja-rei', 'beja-rop', 'beja-van', 'beja-wed', 'berber-ghadames', 'bilin', 'boni-jara', 'boni-kijee-bala', 'boni-kilii', 'burji', 'burunge', 'coptic-sahidic', 'dahalo', 'dhaasanac', 'dizi', 'egyptian-middle', 'elmolo', 'gawwada', 'gedeo', 'geez', 'hadiyya', 'hausa', 'hdi', 'hebrew', 'iraqw', 'kambaata', 'kemant', 'khamtanga', 'koorete', 'maale', 'mubi', 'oromo', 'rendille', 'saho', 'shinassha', 'sidaama', 'somali-standard', 'syriac', 'tsamakko', 'wolaytta', 'yaaku', 'yemsa')

#pcount = "1"
#print(str("pcount1 = " + pcount))

# Root and Frame
root = Tk()
root.geometry('800x800')
root.title('Paradigm Display - Text')

## Create and grid the outer content frame
cframe = ttk.Frame(root, padding=(10, 10, 22, 20))
#dframe = ttk.Frame(root, padding=(10, 10, 22, 20))

# State variables
lnames = StringVar(value=languagenames) # lbox1
pvar = StringVar()
pmsg = StringVar()
lmsg1 = StringVar()
lmsg2 = StringVar()
pcount = StringVar()
pdisp = StringVar()
qdisp = StringVar()

# Create  widgets

llablgen = ttk.Label(cframe, text="Choose Language:")
lbox = Listbox(cframe, listvariable=lnames, height=5)

lplabl = ttk.Label(cframe, textvariable=lmsg1, anchor=W)
pbox = Listbox(cframe, listvariable=pvar, height=5)

pcbutton = ttk.Button(cframe, text='Choose Paradigm', command=choosePdgm, default='active')

llabel1 = ttk.Label(cframe, text="Language:", font=f)
llabel2 = ttk.Label(cframe, textvariable=lmsg2, anchor=W)
plabel1 = ttk.Label(cframe, text="Paradigm:", font=f)
plabel2 = ttk.Label(cframe, textvariable=pmsg, anchor='center')
# pdgm content displayed here in text widget
lpdgm = Text(cframe, state='normal', width=80, height=600, wrap='none')
qlabel = ttk.Label(cframe, text="Query:", font=f)
# query content displayed here in text widget
lquery = Text(cframe, state='normal', width=80, height=30, wrap='none')
pdbutton = ttk.Button(cframe, text="Display Paradigm", command=guipdgm)


# Grid  widgets [["Gentlemen, grid your widgets!"]]

cframe.grid(column=0, row=0, sticky=(N,W,E,S))
# from A
cframe.grid_columnconfigure(0, weight=1)
cframe.grid_rowconfigure(5, weight=1)

#dframe.grid(column=1, row=0, sticky=(N,W,E,S))

llablgen.grid(column=0, row=0, padx=10, pady=5)
lbox.grid(column=0, row=1, rowspan=4, sticky=(N,S,E,W), pady=5, padx=5)
lplabl.grid(column=0, row=5, sticky=(W,E))
pbox.grid(column=0, row=6, rowspan=10, sticky=(N,S,E,W), pady=5, padx=5)
# lbl2.grid(column=2, row=0, padx=10, pady=5)
pcbutton.grid(column=0, row=16, sticky=W)
pdbutton.grid(column=0, row=16, sticky=E)

llabel1.grid(column=1, row=0, sticky=(N,E))
llabel2.grid(column=2, row=0,  sticky=W)
 #tlabel.grid(column=1, row=1, sticky=(N,E))
plabel1.grid(column=1, row=1, sticky=(N,E))
plabel2.grid(column=2, row=1,  sticky=W)
lpdgm.grid(column=2, row=2, rowspan=20, sticky=(W, E))
qlabel.grid(column=0, row=17, sticky=(N,W))
lquery.grid(column=0, row=18, rowspan=4, sticky=(W, E))

# Configure cols and rows
#root.columnconfigure(0, weight=1)
#root.rowconfigure(0,weight=1)
# from UIa
root.grid_columnconfigure(0, weight=1)
root.grid_rowconfigure(0,weight=1)


#cframe.columnconfigure(0, weight=1)
#cframe.rowconfigure(0, weight=1)
# from UIa
cframe.grid_columnconfigure(0, weight=1)
cframe.grid_rowconfigure(5, weight=1)

#dframe.columnconfigure(0, weight=1)
#dframe.rowconfigure(5, weight=1)

for child in  cframe.winfo_children(): child.grid_configure(padx=5, pady=5)

# Set event bindings for when the selection in lbox1 changes,
# when the user double clicks the list, and when they hit the Return key
lbox.bind('<<ListboxSelect>>', showLfile)
lbox.bind('<Double-1>', showLfile)
root.bind('<Return>', showLfile)


# Colorize alternating lines of the lbox1
for i in range(0,len(languagenames),2):
    lbox.itemconfigure(i, background='#f0f0ff')

# Set the starting state of the interface
#  including clearing the messages.
#  Select the first language in the list; because the 
# <<ListboxSelect> event is only
# fired when user makes a change, we explicitly call showLfile.
pmsg.set('')
lmsg1.set('')
pcount.set('0')
pdisp.set('paradigm ....')
qdisp.set('query    ....')
lbox.selection_set(0)
showLfile()


root.mainloop()


