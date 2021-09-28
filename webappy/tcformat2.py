#!/usr/local/bin/python3
'''
Script to reformat e2jfiles/LANG-pdgms1.json 
  1. 'termclusters[terms]'into compact list of lists, 
      (unsorted) using pprint.PrettyPrinter.pformat
  2. other top-level properties (sorted?), 
      using pprint.pformat
and write out to e2jfiles/LANG-pdgms2.json.
'''
import json
import pprint


# Get language name and load up to 'termclusters' into output file.
# Then save original \' in termclusters before json.load 
# (which will change all \" to \'.). Reformat termclusters
# into desired output form, change \' to \" to get
# well-formed json, restore original \', and write to output file.

lang = input('Type language name: ')
infile = str('e2jfiles/' + lang + '-pdgms1.json') # original
infile2 = str('e2jfiles/' + lang + '-pdgms2.json') # fixed for '=>"
ffile =str('e2jfiles/' + lang + '-pdgms.json') # output

file = open(infile, "r")
ldata = file.read() 
file.close()

# Fix the ldata and separate out the pre-termcluster part
if ldata.find('>'):
    ldata = ldata.replace('>', '&gt;')
    print('GT')
#if ldata.find('\"'):
#    ldata = ldata.replace('\"', 'ZZ')
#    print('DQUOTE')
if ldata.find('\''):
    ldata = ldata.replace('\'', 'ZZ')
    print('SQUOTE')
parts = ldata.split('"termclusters": [')

# Open the outfile and write the pre-termcluster part to it
outfile = open(ffile, "w")
outfile.write(parts[0]) # first part of outfile is now fine

# Write the revised ldata to a new input file and json.load it
file2 = open(infile2, "w")
file2.write(ldata)
file2.close()
jdata = json.load(open(infile2))   

termclusters = jdata['termclusters']
lentc = len(termclusters)
pp1 = pprint.PrettyPrinter(indent=1, width=40, sort_dicts=True)
pp2 = pprint.PrettyPrinter(indent=1, width=120, sort_dicts=False)
tcstr = '"termclusters": ['
count = 0

for tc in termclusters:
    tcstr += '\n{\n'
    #lbl = pp1.pformat(tc['label'])
    lbl = tc['label']
    tcstr += '"label": "' + lbl + '",\n'
    '"label": "' + lbl + '",\n'
    #nte = pp2.pformat(tc['note'])
    nte = tc['note']
    tcstr += '"note": "' + nte + '",\n'
    cmn = pp1.pformat(tc['common'])
    #cmn = tc['common']
    tcstr += '"common": ' + cmn + ',\n'

    ## PrettyPrinter for all terms:
    tc = tc['terms']
    pdgm = pp2.pformat(tc)
    tcstr += '"terms":  \n'
    tcstr += str(pdgm)

    if count < len(termclusters)-1:
        tcstr += '\n},'
        #outfile.write(str(count))
        count = count + 1
    else:
        tcstr += '\n}'

tcstr += ']\n}'
tcstr = tcstr.replace('\'', '\"') # now make well-formed json
tcstr = tcstr.replace('ZZ', '\'') # and bring back original \'
outfile.write(tcstr)
outfile.close()


