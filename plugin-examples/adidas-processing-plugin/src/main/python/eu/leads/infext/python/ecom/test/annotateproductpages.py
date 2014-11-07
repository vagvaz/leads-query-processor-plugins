'''
Created on Mar 26, 2014

@author: nonlinear
'''
from eu.leads.infext.python.comm.connection import getpages, addmetadata

base_urls_list = [
                  "com.adidas.www:http/us/product/",
                  "com.nike.store:http/us/en_us/pd/",
                  "com.roadrunnersports.www:http/rrs/products/",
                  "com.footlocker.www:http/product/",
                  "com.dickssportinggoods.www:http/product",
                  "com.sportsshoes.www:http/product/",
                    "com.zappos.www:http/",
                    "uk.co.next.www:http/",
                    "uk.co.zalando.www:http/",
                    "com.kickz.www:http/uk/",
                    "com.holabirdsports.www:http/",
                    "com.runningwarehouse.www:http/",
                    "com.amazon.www:http/"
                  ]

for url in base_urls_list:
    print("!!! RUN "+url+" !!!")
    pages = getpages(url+"*")
    i=0
    for page in pages:
        print(page[0])
        option = raw_input('Product offer? (N/y/q)')
        if option == 'y':
            addmetadata(url, 'ecom:pdpage', 'True')
            i+=1
            print i
        elif option == 'q':
            break
        
    print("\n---\n")