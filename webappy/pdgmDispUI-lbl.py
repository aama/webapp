'''
Comma-separted strings of property values in listbox are keys to full 
pdgm-defining prop:val strings (pvstrings/pdgm-dict-LANG.py). SPARQL query
 for pdgm  derived from that in pdgmDisp.query. Widgets and grid modeled 
on Mark Roseman, Modern Tkinter -- cf. esp. pp. 67-70.
NOTE: In this one-frame version of pdgmDispUI paradigm (pdisp) and 
query (qdisp) are displayed as the textvariables of the ttk label widgets 
lpdgm and lquery respectively. 
Problems: no scrolling; no copy/paste of texts.
'''

from tkinter import *
from tkinter import ttk
from SPARQLWrapper import SPARQLWrapper, JSON
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
    idxs = lbox1.curselection()
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
    idxs = lbox2.curselection()
    if len(idxs)==1:
        idx = int(idxs[0])
        lbox2.see(idx)
        pname = lbox2.get(idx)
        # pmsg.set("PDGM: %s (# %s)" % (pname, idx))
        pmsg.set(pname)
        # print(pmsg)

#function for the pdgm-display button
def guipdgm(*args):
    pdisp.set('paradigm .... ')
    pkey = pmsg.get()  #get key that user selected
    l = str(lmsg2.get())
    sfile = str('pvlists/pdgmdb' + l)
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
    # create 'select' row [= header])    
    header =  ("    ").join(select).upper()
    paradigm = str(header + "\n")
    for result in results["results"]["bindings"]:
        pdgmrow = ""
        for sel in select:
            sel = result[sel]["value"]
            pdgmrow = str(pdgmrow + sel + "      ") 
            # i.e.
            # number = result["number"]["value"]
            # person = result["person"]["value"]
            # gender = result["gender"]["value"]
            # token  = result["token"]["value"]
            # pdgmrow = (str(number +  "      " + person + "     " + gender + "     " + token + "\n"))
        paradigm = str(paradigm + pdgmrow + "\n")

    pdisp.set(paradigm)
    qdisp.set(res)
    print(paradigm)
    print(res)
    # else:
    #pdisp.set(str('Problem with w: ' + w))

#languagenames = ('beja-hud', 'afar', 'oromo', 'somali-standard')

languagenames = ('afar', 'alaaba', 'alagwa', 'akkadian-ob', 'arabic', 'arbore', 'awngi', 'bayso', 'beja-alm', 'beja-hud', 'beja-rei', 'beja-rop', 'beja-van', 'beja-wed', 'berber-ghadames', 'bilin', 'boni-jara', 'boni-kijee-bala', 'boni-kilii', 'burji', 'burunge', 'coptic-sahidic', 'dahalo', 'dhaasanac', 'dizi', 'egyptian-middle', 'elmolo', 'gawwada', 'gedeo', 'geez', 'hadiyya', 'hausa', 'hdi', 'hebrew', 'iraqw', 'kambaata', 'kemant', 'khamtanga', 'koorete', 'maale', 'mubi', 'oromo', 'rendille', 'saho', 'shinassha', 'sidaama', 'somali-standard', 'syriac', 'tsamakko', 'wolaytta', 'yaaku', 'yemsa')


# Root and Frame
root = Tk()
root.geometry('400x400')
root.title('Paradigm Display - Label')
## Create and grid the outer content frame
c = ttk.Frame(root, padding=(10, 10, 22, 20))
c.grid(column=0, row=0, sticky=(N,W,E,S))
root.grid_columnconfigure(0, weight=1)
root.grid_rowconfigure(0,weight=1)

# State variables
lnames = StringVar(value=languagenames) # lbox1
pvar = StringVar()
pmsg = StringVar()
lmsg1 = StringVar()
lmsg2 = StringVar()
tamValue = StringVar() #textvariable for entry
pdisp = StringVar()
qdisp = StringVar()

# Create  widgets

lbl1 = ttk.Label(c, text="Choose Language:")
lbox1 = Listbox(c, listvariable=lnames, height=5)

llabl1 = ttk.Label(c, textvariable=lmsg1, anchor=W)
llabl2 = ttk.Label(c, textvariable=lmsg2, anchor=W)
lbox2 = Listbox(c, listvariable=pvar, height=5)

plbl = ttk.Label(c, textvariable=pmsg, anchor='center')
pcbutton = ttk.Button(c, text='Choose Paradigm', command=choosePdgm, default='active')

llabel = ttk.Label(c, text="Language:", font=f)
#tVal = ttk.Label(c,  textvariable=tamValue, font=f)
plabel = ttk.Label(c, text="Paradigm:", font=f)
lpdgm = ttk.Label(c, textvariable=pdisp, font=f)
qlabel = ttk.Label(c, text="Query:", font=f)
lquery = ttk.Label(c, textvariable=qdisp, font=f)
pdbutton = ttk.Button(c, text="Display Paradigm", command=guipdgm)


# Grid  widgets [["Gentlemen, grid your widgets!"]]
c.grid_columnconfigure(0, weight=1)
#c.grid_rowconfigure(1, weight=1)
#c.grid_rowconfigure(4, weight=2)
c.grid_rowconfigure(5, weight=1)


lbl1.grid(column=0, row=0, padx=10, pady=5)
lbox1.grid(column=0, row=1, rowspan=3, sticky=(N,S,E,W), pady=5, padx=5)
llabl1.grid(column=0, row=4, sticky=(W,E))
lbox2.grid(column=0, row=5, rowspan=3, sticky=(N,S,E,W), pady=5, padx=5)
# lbl2.grid(column=2, row=0, padx=10, pady=5)
pcbutton.grid(column=0, row=8, sticky=W)

llabel.grid(column=1, row=0, sticky=(N,E))
llabl2.grid(column=2, row=0,  sticky=W)
# tlabel.grid(column=1, row=1, sticky=(N,E))
plabel.grid(column=1, row=1, sticky=(N,E))
plbl.grid(column=2, row=1,  sticky=W)
#lres.grid(column=2, row=2, rowspan=4, sticky=(W, E))
lpdgm.grid(column=2, row=2, rowspan=4, sticky=(W, E))
qlabel.grid(column=1, row=6, sticky=(N,E))
#lquery.grid(column=2, row=6, rowspan=4, sticky=(W, E))
lquery.grid(column=2, row=6, rowspan=4, sticky=(W, E))
pdbutton.grid(column=1, row=10, sticky=E)

for child in  c.winfo_children(): child.grid_configure(padx=5, pady=5)

# Set event bindings for when the selection in lbox1 changes,
# when the user double clicks the list, and when they hit the Return key
lbox1.bind('<<ListboxSelect>>', showLfile)
lbox1.bind('<Double-1>', showLfile)
root.bind('<Return>', showLfile)


# Colorize alternating lines of the lbox1
for i in range(0,len(languagenames),2):
    lbox1.itemconfigure(i, background='#f0f0ff')

# Set the starting state of the interface
#  including clearing the messages.
#  Select the first language in the list; because the 
# <<ListboxSelect> event is only
# fired when user makes a change, we explicitly call showLfile.
pmsg.set('')
lmsg1.set('')
pdisp.set('paradigm ....')
qdisp.set('query    ....')
lbox1.selection_set(0)
showLfile()


root.mainloop()


