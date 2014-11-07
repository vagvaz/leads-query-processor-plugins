'''
Created on Jun 27, 2014

@author: nonlinear
'''
import re
from eu.leads.infext.python.ecom.dictionaries import currency

class DefaultVerifier(object):
    '''
    classdocs
    '''
    
    def verify(self,text_list):
        return True
    
#################################################    
    
class DefaultEcomPriceVerifier(object):
    '''
    classdocs
    '''
    
    def verify(self,text_list):
        name = ' '.join(x[0] for x in text_list)
        if re.search(currency.price_any_regex, name):
            print "price verified"
            return True
        else:
            print "price NOT verified"
            return False
    
#################################################    
    
class DefaultEcomNameVerifier(object):
    '''
    classdocs
    '''    
    
    def verify(self,text_list):
        name = ' '.join(x[0] for x in text_list)
        words = name.split()
        #####
        chars_num = len(text_list)
        words_num = len(words)
        capitalletter_words_num = len([word for word in words if word[0].isupper()])
        number_words_num = len([word for word in words if word[0].isdigit()])
        # if less than 100 characters, less than 15 words, and at least 50% of alphanumeric words start with capital letter
        if chars_num < 100 and words_num < 15 and capitalletter_words_num*2 > words_num - number_words_num:
            print "name verified"
            return True
        else:
            print "name NOT verified"
            return False
    
    
    
    
    
