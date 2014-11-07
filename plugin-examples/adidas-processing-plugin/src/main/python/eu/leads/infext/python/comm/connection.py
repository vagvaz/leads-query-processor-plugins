'''
Created on Jan 30, 2014

@author: nonlinear
'''
from eu.leads.infext.python.comm.hbasebigtable import HBaseBigTable
from eu.leads.infext.python.comm.urltranslate import url_norm

hostname = '141.76.44.141'
tablename = 'webpage'
columns = ['f:cnt']

bigtable = None

def connect():
    global bigtable
    bigtable = HBaseBigTable()
    bigtable.connect(hostname, 9090, tablename)
    
    
def getpages(url,isnorm=True,filter=None): # filter could be a dictionary
    if bigtable is None:
        connect()
    
    url = url_norm(url) if not isnorm else url
    scan = False
    if url[-1] == '*':
        url = url[:-1]
        scan = True
    
    if scan:
        return bigtable.scan(url,columns)
    else:
        return bigtable.get(url,columns)
    
def getrow(url,isnorm=True):
    if bigtable is None:
        connect()
        
    url = url_norm(url) if not isnorm else url
    scan = False
    
    return bigtable.get(url)

def addmetadata(url,metaname,value):
    bigtable.put(url, {metaname,value})
    
    
#getpages("http://www.adidas.com/*",isnorm=False)


