'''
Created on Apr 9, 2014

@author: nonlinear
'''

import requests
import StringIO


from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from comm.urltranslate import url_base, url_norm, url_base_full
import ThriftHive


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
        
        query = "INSERT INTO TABLE google_news_sites VALUES "
        line = buf.readline()
        
        while line:
            words = line.split()
            if len(words) == 2:
                lang = words[0]
                url_read = words[1]
                
                # normalize URL
                url_ful = url_base_full(url_read)
                # change URL form
                url = url_norm(url_ful)
                
                print(url_read,url_ful,url)
                
                row = "('"+url+"','"+lang+"'),"
                query += row
                
            line = buf.readline()
                
        query = query[:-1]
        print(query)
        
        try:
            transport = TSocket.TSocket('141.76.44.143', 2201)
            transport = TTransport.TBufferedTransport(transport)
            protocol = TBinaryProtocol.TBinaryProtocol(transport)
            client = ThriftHive.Client(protocol)
            transport.open()
            client.execute(query)
            transport.close()
        except Thrift.TException, tx:
            print '%s' % (tx.message)
    
    
o = InsertNewsPages()
o.insert()