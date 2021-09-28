#!/usr/local/bin/python3
'''
Makes three files out of each termcluster's 'common' sections : 
  pdgm-keys for display in list boxes, 
  pdgmdb for mapping of key-string to key-value string, 
  pdgm-dict for human-readable version of pdgmdb. 
where key is comma-separated string of pdgm's values, and value
is comma-separated string of prop-val pairs: 
      (Format:" pos:POS,...,property:Value,...") 
for all recognized termclusters in LANG-pdgms.json. 
[Desirable for more inspectable pdgm list in pdgmDispUI.py.]
'''

import json
import shelve

#def pdgmidx(lang)

# For single lang:
#languagenames = input('Type language name: ')

# For corpus:
languagenames = ('afar', 'alaaba', 'alagwa', 'akkadian-ob', 'arabic', 'arbore', 'awngi', 'bayso', 'beja-alm', 'beja-hud', 'beja-rei', 'beja-rop', 'beja-van', 'beja-wed', 'berber-ghadames', 'bilin', 'boni-jara', 'boni-kijee-bala', 'boni-kilii', 'burji', 'burunge', 'coptic-sahidic', 'dahalo', 'dhaasanac', 'dizi', 'egyptian-middle', 'elmolo', 'gawwada', 'gedeo', 'geez', 'hadiyya', 'hausa', 'hdi', 'hebrew', 'iraqw', 'kambaata', 'kemant', 'khamtanga', 'koorete', 'maale', 'mubi', 'oromo', 'rendille', 'saho', 'shinassha', 'sidaama', 'somali-standard', 'syriac', 'tsamakko', 'wolaytta', 'yaaku', 'yemsa')

for lang in languagenames:
     print(str('LANG: ' + lang))
     lfile = str('../aama-data/data/' + lang + '/' + lang + '-pdgms.json')
     jdata = json.load(open(lfile))
     outfile1 = str('pvlists/pdgm-dict-' + lang + '.py')
     outfile2 = str('pvlists/pdgm-keys-' + lang + '.txt')
     mdbfile = str('pvlists/pdgmdb' + lang)
     shelffile = shelve.open(mdbfile)

     pdgmdict = {}
     pkeys = ''
     tccount = len(jdata['termclusters'])
     for i in range(tccount):
          # read-in 'common' section
          tccommon = jdata['termclusters'][i]['common']
          # make list of (prop, val) tupples
          tpltcc = list(tccommon.items())
          pdgmvals = []
          pdgmkeys = []
          # for key put tup[1] in list
          # for val put tuples in list in format tup[0]:tup[1] ('prop:val')
          # [Want to get pos value at head of list for ease of reading.]
          for tup in tpltcc:
               if tup[0] == 'pos':
                    pdgmkeys.insert(0,str(tup[1]))
                    pdgmvals.insert(0,str(tup[0] + ":" + tup[1]))
               else:
                    pdgmkeys.append(str(tup[1]))
                    pdgmvals.append(str(tup[0] + ":" + tup[1]))
                    # read sel from row-1 of 'terms'
          sel =  jdata['termclusters'][i]['terms'][0]
          # if not default (num,pers,gen,token), add to pval list
          selprops = ''
          if sel != ['number', 'person', 'gender', 'token']:
               selprops = str("%" + ",".join(sel))
          # make c and selprops into string and add to list
          pvalstring = ','.join(pdgmvals)
          pvalstring = str(pvalstring + selprops + "\n")
          #pvalstring = str(pvalstring + selprops)
          pkeystring = ','.join(pdgmkeys)
          #pkeystring2 = str(pkeystring1)
          pdgmdict[pkeystring] = pvalstring
          # pkeys.append(pkeystring2)
          pkeys += str(' ' + pkeystring)
          shelffile[pkeystring] = pvalstring
     shelffile.close()
     file = open(outfile1, "w")
     file.write(str(pdgmdict))
     file.close()
     file = open(outfile2, "w")
     file.write(str(pkeys))
     file.close()
     # SEE HOW 'SHELVE' WORKS WITH DICT


          

