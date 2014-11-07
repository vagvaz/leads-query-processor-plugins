'''
Created on Apr 2, 2014

@author: nonlinear
'''

import requests
import time
from eu.leads.infext.python.ops import xpathops
from random import randint

class SiteCategorization(object):
    '''
    classdocs
    '''


    def __init__(self):
        pass
    
        
    def categorize(self, domain_url):
        
        content = None
        page = requests.get('http://www.similarweb.com/website/'+domain_url)
        
        if page.status_code == requests.codes.ok:
            content = page.text
            
        if content:
            ''' scraping bitch '''
            tree = xpathops.content2tree(content)
            category_string = tree.xpath("//*[@id=\"overview\"]/div[2]/div[1]/div/div[1]/div[2]/ul/li[3]/div/h3/span/a/text()")
            country_string = tree.xpath("//*[@id=\"geo-countries-accordion\"]/div[1]/div[1]/span/h3/text()")
            return [category_string, country_string]
        else:
            return None

urls = ["nike.com","adidas.com", "adidas.de", "google.com", "facebook.com", "onet.pl", "wp.pl", "bbc.co.uk"]        
        
sc = SiteCategorization()
while True:
    [cat, cou] = sc.categorize(urls[randint(0,7)])
    if cat:
        print (cat,cou)
        time.sleep(randint(3000,4000)/100.0)
    else:
        break
# time.sleep(31)
# print (sc.categorize("onet.pl"))
# time.sleep(31)
# print (sc.categorize("nike.com"))
# time.sleep(31)
# print (sc.categorize("bbc.co.uk"))
# time.sleep(31)
# print (sc.categorize("onet.pl"))
# time.sleep(31)
# print (sc.categorize("nike.com"))
# time.sleep(randint(3000,4000)/100.0)