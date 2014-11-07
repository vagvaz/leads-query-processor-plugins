'''
Created on Mar 24, 2014

@author: nonlinear
'''
from eu.leads.infext.python.ecom.extrschema.schemaextractor import LeadsEcomSiteSchemaExtractor
from eu.leads.infext.python.comm.connection import getpages, getrow


base_urls_list = [
                  "http://www.adidas.com/us/product/",
                  "http://store.nike.com/us/en_us/pd/",
                  "http://www.roadrunnersports.com/rrs/products/",
                  "http://www.footlocker.com/product/",
                  "http://www.dickssportinggoods.com/product/",
                  "http://www.sportsshoes.com/product/",
                  #"http://www.zappos.com/",
                  #"http://www.next.co.uk/",
                  #"http://www.zalando.co.uk/",
                  #"http://www.kickz.com/uk/",
                  #"http://www.holabirdsports.com/",
                  #"http://www.runningwarehouse.com/",
                  #"http://www.amazon.com"
                  ]

check_urls_list = [
                   "http://www.adidas.com/us/",
                   "http://www.adidas.com/us/product/",
                  "http://store.nike.com/us/en_us/pd/",
                  "http://store.nike.com/us/en_us/",
                  "http://www.roadrunnersports.com/rrs/products/",
                  "http://www.roadrunnersports.com/",
                  "http://www.footlocker.com/product/",
                  "http://www.footlocker.com/",
                  "http://www.dickssportinggoods.com/product/",
                  "http://www.dickssportinggoods.com/",
                  "http://www.sportsshoes.com/product/",
                  "http://www.sportsshoes.com/",
                    "http://www.zappos.com/",
                    "http://www.next.co.uk/",
                    "http://www.zalando.co.uk/",
                    "http://www.kickz.com/uk/",
                    "http://www.holabirdsports.com/",
                    "http://www.runningwarehouse.com/",
                    "http://www.amazon.com"
                   ]

class ProofOfConcept1(object):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        
    def runProof(self):
        
        for url in base_urls_list:
            print("!!! RUN "+url+" !!!")
            schemaExtractor = LeadsEcomSiteSchemaExtractor(url)
            schema = schemaExtractor.find_schema()
            print(schema)
            print("\n---\n")
            
    def checkpages(self):
        for string in check_urls_list:
            print "FOR "+"\""+string+"\":"
            pages = getpages(string+"*", isnorm=False)
            i = 0
            for page in pages:
                print(page[0])
                i+=1
                if i == 20:
                    break
            print ""
        
        
    
poc = ProofOfConcept1()
#poc.runProof()
#poc.checkpages()
# pages = getpages("com.roadrunnersports.www:http/rrs/products/ASC1545/womens-asics-gelfujiracer/*", isnorm=True)
# for page in pages:
#     print("A"+page[0])

row = getpages('uk.co.zalando.www:http/*',isnorm=True)
count = 0
for col in row:
    count += 1
print count
