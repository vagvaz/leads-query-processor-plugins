'''
Created on Jan 16, 2014

@author: nonlinear
'''

from eu.leads.infext.python.ecom.properties import PAGES_FOR_SCHEMA_RETRIEVAL_NO
from eu.leads.infext.python.ops import stringops

class HiddenPriceSchemaExtractor(object):
    '''
    Takes a source code of a few pages of Ecom site
    and tries to extract schema for the price retrieval
    by finding strings preceding and following the mention of a price
    that all of the pages have in common
    '''


    def __init__(self, pages_code, pages_price):
        '''
        Constructor
        '''
        self.__pages_code = pages_code
        self.__pages_price = pages_price
        
        
    def get_strings_surrounding_price(self):
        
        pages_code = self.__pages_code
        pages_price = self.__pages_price
        pages_str_preceding_price = []
        pages_str_following_price = []
        
        for i in range(len(pages_code)):
            index = pages_code[i].find(pages_price[i])
            price_len = len(pages_price[i])
            
            str_preceding_price = pages_code[i][index-30:index]
            str_following_price = pages_code[i][index+price_len:index+price_len+30]
            
            print(i,":",str_preceding_price,"/",str_following_price)
            
            pages_str_preceding_price.append(str_preceding_price)
            pages_str_following_price.append(str_following_price)
            
        long_prec = stringops.long_substr(pages_str_preceding_price)
        print(pages_str_preceding_price)
        long_foll = stringops.long_substr(pages_str_following_price)
        print(pages_str_following_price)
        
        return [((long_prec,long_foll),1)]
        
    