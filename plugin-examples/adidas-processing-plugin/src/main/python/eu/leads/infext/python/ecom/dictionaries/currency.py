# coding: utf-8
'''
Created on Dec 12, 2013

@author: nonlinear
'''

from eu.leads.infext.python.ecom.dictionaries.generalreg import anyspace_regpart, B, E
import re

# __currency_regex_symbols = ('\&euro;', '\€', 'EUR', '\&#8364;',
#                     '\&dollar;', '\$', 'USD', '\&#036;',
#                     '\&pound;', '\£', 'GBP', '\&#163;', u'\xa3')

currency_symbols_map = {
                        '€':            'EUR', 
                        'EUR':          'EUR',
                        unichr(128):    'EUR',
                        #
                        '$':            'USD', 
                        'USD':          'USD',
                        #
                        '£':            'GBP',
                        'GBP':          'GBP',
                        unichr(163):    'GBP'
                        }

__currency_regex_symbols = ('\&euro;', '\\'+u"\u20AC", 'EUR', '\&#8364;',
                    '\&dollar;', '\$', 'USD', '\&#036;',
                    '\&pound;', '\\'+unichr(163), 'GBP', '\&#163;', u'\\xa3', '\\'+u'\u00A3')

priceval_regpart = "\d{1,10}((\.|\,)(\d{2}|\-))?"
curr_regpart = u"("
curr_findall_regex = ""
for x in __currency_regex_symbols:
    if __name__ == '__main__': print x
    curr_regpart += x + '|'
    curr_findall_regex += x
else:
    curr_regpart = curr_regpart[:-1] + ")"

price_any_regex = priceval_regpart
price_regex = B + priceval_regpart + E
currprice_any_regex = curr_regpart + anyspace_regpart + priceval_regpart
currprice_regex = B + curr_regpart + anyspace_regpart + priceval_regpart + E
pricecurr_any_regex = priceval_regpart + anyspace_regpart + curr_regpart
pricecurr_regex = B + priceval_regpart + anyspace_regpart + curr_regpart + E
curr_regex = B + curr_regpart + E
curr_any_regex = curr_regpart

if __name__ == '__main__':
#     if any(x.replace("\\","") in "$29.99" for x in __currency_regex_symbols): print(1)
#     else: print(2)
    print re.subn(currprice_any_regex,'',u"22 - £24")