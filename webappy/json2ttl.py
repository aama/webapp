#!/usr/local/bin/python3
'''
Transform LANG-pdgms.json file into LANG-pdgms.ttl file,
with every assertion of the json file translated into
a set of triples
'''

import sys
import json
import uuid


def doPrelude(lang, jdata):
    # lang.title() doesn't work with hyphens. Revise?
    Lang = lang[0].upper() + lang[1::]
    aamaLang = str("aama:" + Lang)
    header = str("#TTL FROM INPUT FILE:\n#data/" + lang + "-pdgms.json\n\n")
    prefixes = """
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix dc:      <http://purl.org/dc/elements> .
@prefix dcterms: <http://purl.org/dc/terms> .
@prefix aama:    <http://id.oi.uchicago.edu/aama/2013/> .
@prefix aamas:	 <http://id.oi.uchicago.edu/aama/2013/schema/> .
"""
    lprefix = str('@prefix ' + jdata["sgpref"] + ':     <http://id.oi.uchicago.edu/aama/2013/' + lang + '/> .\n\n')
    linfo = str('#LANG INFO:\n\n' + aamaLang + ' a aamas:Language.\n')
    linfo = linfo + str(aamaLang + ' rdfs:label "' + Lang + '" .\n')
    linfo = linfo + str(aamaLang + ' aamas:subfamily "' + jdata["subfamily"] + '" .\n')
    linfo = linfo + str(aamaLang + ' aamas:lpref "' +  jdata["sgpref"] + '" .\n')
    linfo = linfo + str(aamaLang + ' aamas:dataSource "' +  jdata["datasource"] + '" .\n')
    linfo = linfo + str(aamaLang + ' aamas:geodemoURL "' +  jdata["geodemoURL"] + '" .\n')
    linfo = linfo + str(aamaLang + ' aamas:geodemoTxt "' +  jdata["geodemoTXT"] + '" .\n')

    prelude = header + prefixes + lprefix + linfo

    return(prelude)

def doSchema(lang, jdata):
    # 'lang.title()' doesn't work with hyphenated names. Revise?
    Lang = lang[0].upper() + lang[1:]
    lpref = jdata["sgpref"]
    schema = jdata["schemata"]
    schemata = ''
    for prop in schema.keys():
        Prop = prop.title()
        pline = str(lpref + ':' + prop)
        Pline = str(lpref + ':' + Prop)
        sec = str('\n#SCHEMATA: ' + prop + '\n')
        sec += str(pline + ' aamas:lang aama:' + Lang + ' .\n') 
        sec += str(Pline + ' aamas:lang aama:' + Lang + ' .\n')
        sec += str(pline + ' rdfs:domain aamas:Term .\n')
        sec += str(Pline + ' rdfs:label "' + prop + ' exponents" .\n')
        sec += str(pline + ' rdfs:label "' + prop + '" .\n')
        sec += str(pline + ' rdfs:range ' + Pline + ' .\n')
        sec += str(Pline + ' rdfs:subClassOf ' + lpref + ':MuExponent .\n')
        sec += str(pline + ' rdfs:subPropertyOf ' + lpref + ':muProperty .\n')
        vals = schema.get(prop)
        for val in vals:
            vline = str(lpref + ':' + val)
            sec += str(vline + ' aamas:lang aama:' + Lang + ' .\n') 
            sec += str(vline + ' rdf:type aamas:' + Prop + ' .\n')
            sec += str(vline + ' rdfs:label "' + val + '" .\n')
        schemata = schemata + sec

    return(schemata)

def doLexprops(lang, jdata):
    """
    There are as of now only empty lexprops keys in the LANG-pdgms.json
    files.
    This function will simply print 'LEXEMES' to the ttl file and go
    on to the individual lexemes. Whole function of lexprops needs to
    be thought out. The following is clearly a simple calk on doSchema.
    """
    Lang = lang[0].upper() + lang[1::]
    lpref = jdata["sgpref"]
    lprops = {} # jdata["lexprops"]
    lexprops  = str('\n#LEXEMES: ' + Lang + '\n')
    for prop in lprops.keys():
        Prop = prop.title()
        pline = str('aamas:' + prop)
        Pline = str('aamas:' + Prop)
        sec = str('\n#LexSchema: ' + prop + '\n')
        sec += str(pline + ' aamas:lang aama:' + Lang + ' .\n') 
        sec += str(Pline + ' aamas:lang aama:' + Lang + ' .\n')
        sec += str(pline + ' rdfs:domain aamas:Lexeme  \n')
        sec += str(Pline + ' rdfs:label "' + prop + ' exponents" .\n')
        sec += str(pline + ' rdfs:label "' + prop + '" .\n')
        sec += str(pline + ' rdfs:range aamas:' + Prop + ' .\n')
        sec += str(Pline + ' rdfs:subClassOf ' + lpref + ':LexEponent .\n')
        sec += str(pline + ' rdfs:subPropertyOf ' + lpref + ':lexProperty .\n')
        lexprops = lexprops + sec

    return(lexprops)

def doLexemes(lang, jdata):
    Lang = lang[0].upper() + lang[1::]
    lpref = jdata["sgpref"]
    lexentries = jdata["lexemes"]
    lexemes  = str('\n#LexItems: ' + Lang + '\n\n')
    for lex in lexentries.keys():
        # This is what we use for ID (use uuid instead?):
        sec = str('aama:' + Lang + '-' + lex + ' a aamas:Lexeme ;\n')
        sec += str('\taamas:lang aama:' + lang + ' ;\n')
        sec += str('\trdfs:label "' + lex + '" ;\n')
        lexprops = lexentries.get(lex)
        for lexprop in lexprops:
            lexval = lexprops.get(lexprop)
            if lexprop == 'gloss' or lexprop == 'lemma':
                sec += str('\taamas:' + lexprop + ' "' + lexval + '" ; \n')
            else:
                sec += str('\t' + lpref + ':' + lexprop + ' ' + lpref + ':' + lexval + ' ; \n')
        sec += '\t.\n'
        lexemes = lexemes + sec

    return(lexemes)
    
def doTermclusters(lang, jdata):
    Lang = lang[0].upper() + lang[1::]
    lpref = jdata["sgpref"]
    tclusters = jdata["termclusters"]
    termclusters = str('\n#TERMCLUSTERS: ' + Lang + '\n\n')
    # for each termcluster
    for tcl in range(len(tclusters)):
        termcluster = tclusters[tcl]
        label = termcluster.get('label')
        note = termcluster.get('note')
        common = termcluster.get('common')
        terms = termcluster.get('terms')
        vprops = terms[0]
        data = terms[1:]
        tcltext = str('#TERMCLUSTER: ' + label + '\n\n' + lpref + ':' + label + ' a aamas:Termcluster ;\n\trdfs:label "' + label + '" ;\n\taamas:lang aama:' + Lang + ' ;\n\trdfs:comment "' + note + '" \n\t.\n\n')
        # print(tcltext)
        # for each term
        # first the pv's in common sec
        for term in data:
            # every term in termcluster has this:
            termid = str(uuid.uuid4())
            sec = str('aama:ID' + termid + ' a aamas:Term ;\n\taamas:lang aama:' + Lang + ' ;\n\taamas:memberOf ' + lpref + ':' + label + ' ;\n')
            for cprop in common:
                cval = common.get(cprop)
                if cprop == 'lexeme':
                    sec += str('\taamas:' + cprop + ' aama:' + Lang + '-' + cval + ' ;\n')
                elif cprop[0:5] == 'token':
                    sec += str('\t' + lpref + ':' + cprop + ' "' + cval + '" ;\n')
                else:
                    sec += str('\t' + lpref + ':' + cprop + ' ' + lpref + ':' + cval + ' ;\n')
            # make dictionary out of vprops for each line of data
            pdict = dict(zip(vprops,term))
            #print(pdict)
            for prop in vprops:
                if prop[0:5] == 'token':
                    sec += str('\t' + lpref + ':' + prop + ' "' + pdict[prop] + '" ;\n')
                elif prop == 'lexeme':
                    sec += str('\taamas:' + prop + ' aama:' + Lang + '-' + pdict[prop] + ' ;\n')
                else:
                    sec += str('\t' + lpref + ':' + prop + ' ' + lpref + ':' + pdict[prop] + ' ;\n')
            sec += '\t.\n\n'
            tcltext += sec
        termclusters += tcltext
   
    return(termclusters)
    

                
#lang = input('Type language name: ')
lang = sys.argv[1]
print(str("LANG: " + lang))
# ultimate data and  output file
# lfile = str('../aama-data/data/' + lang + '/' + lang + '-pdgms.json')
# outfile = str('../aama-data/data/' + lang + '/' + lang + '-pdgms.ttl')

# initial data and output file
lfile = str('../aama-data/data/' + lang + '/' + lang + '-pdgms.json')
outfile = str('../aama-data/data/' + lang + '/'  + lang + '-pdgms.ttl')

jdata = json.load(open(lfile))

prelude = doPrelude(lang, jdata)
schema = doSchema(lang, jdata)
lexprops = doLexprops(lang, jdata)
lexemes = doLexemes(lang, jdata)
termclusters = doTermclusters(lang, jdata)

ttl = prelude + schema + lexprops + lexemes + termclusters

file = open(outfile, "w")
file.write(ttl)
file.close()

#print(prelude)
#print(schema)
#print(lexprops)
#print(lexemes)
#print(termclusters)
