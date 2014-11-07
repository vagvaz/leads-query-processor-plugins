'''
Created on Apr 9, 2014

@author: nonlinear
'''

import requests
import StringIO

import happybase
from eu.leads.infext.python.comm.urltranslate import url_norm, url_fix


class InsertNewsPages(object):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        
    def insert(self):
        
        page2 = requests.get('http://img.labnol.org/files/Google-News.txt')
        text = page2.text
        buf = StringIO.StringIO(text)
        
        # TODO do it with happybase
        connection = happybase.Connection('141.76.44.143',compat='0.90')
        table = connection.table('google_news_sites')
        
#         for key, data in table.scan(""):
#             print key, data  # prints row key and data for each row
        
        line = buf.readline()
         
        while line:
            words = line.split()
            if len(words) == 2:
                lang = words[0]
                url_read = words[1]
                fix_url = url_fix(url_read)
                norm_url = url_norm(fix_url)
                table.put(norm_url, {'cf:lang': lang})                
            line = buf.readline()
    
o = InsertNewsPages()
o.insert()