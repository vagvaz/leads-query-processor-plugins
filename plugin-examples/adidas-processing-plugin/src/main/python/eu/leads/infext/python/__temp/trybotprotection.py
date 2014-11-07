'''
Created on Apr 9, 2014

@author: nonlinear
'''

import requests
from random import randint
from eu.leads.infext.python.ops.xpathops import code2tree
import time

class TryBotProtection(object):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        
    def tryit(self):
        sleepevery = 0.1
        sleepwhenstopped = 1.1
        listtocheck = ["adidas.de","bbc.co.uk","cnn.com","nike.com","onet.pl"]
        
        firststop = False
        
        while True:
            suffix = listtocheck[randint(0,4)]
            url = 'http://all-whois.com/'+suffix
            print("URL:"+url)
            page2 = requests.get(url)
            text = page2.text
            tree = code2tree(text)
            node = tree.xpath("/html/body/div[2]/div[4]/div[3]/div[1]/table[3]/tbody/tr[1]/td[2]/text()")
            if node is not None and node[0] is not None:
                firststop = False
                print (node[0])
                time.sleep(sleepevery)
                print "sleeping "+str(sleepevery)
            else:
                #if firststop:
                #    break
                #else:
                firststop = True
                print("nie")
                time.sleep(sleepwhenstopped)
                print "sleeping "+str(sleepwhenstopped)
 
tryme = TryBotProtection()
tryme.tryit() 
 
                