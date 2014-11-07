'''
Created on Feb 13, 2014

@author: nonlinear
'''
from eu.leads.infext.python.lang.dicts import lang_dicts
import re

def getwordregex(word):
    word = word.lower()
    word2 = word.title()
    word3 = word.upper()
    return "((\W|\\b)"+word+"|"+word2+")(\W|\\b|[A-Z0-9])|((\W|\\b)"+word3+")(\W|\\b)"

def regexofdictentries(entries,lang='en'):
    regex = ""
    lang_dict = lang_dicts.get(lang)
    en_dict = lang_dicts.get('en')
    words_set = set()
    for entry in entries:
        for word in lang_dict.get(entry):
            words_set.add(word)
        if lang != 'en':
            for word in en_dict.get(entry):
                words_set.add(word)
    for word in words_set:   
        regex += getwordregex(word)
        regex += "|"
    return regex[:-1]

def countnonoverlappingmatches(pattern,text):
    text = unicode(text)
    return re.subn(pattern, '', text)[1]

#print regexofdictentries(["ECOM_CART","ECOM_CHECKOUT"], 'en')