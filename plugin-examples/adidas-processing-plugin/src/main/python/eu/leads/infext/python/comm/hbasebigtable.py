'''
Created on Jan 30, 2014

@author: nonlinear
'''
import happybase
from eu.leads.infext.python.comm.abstractbigtable import AbstractBigTable

HBASE_URL_CONTENT_COLUMN = 'f:cnt'

class HBaseBigTable(AbstractBigTable):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        
        
    def connect(self,hostname,port,tablename,compat='0.90'):
        self.connection = happybase.Connection(hostname, port, compat=compat)
        self.table = self.connection.table(tablename, True)
        
        
    def put(self,rowkey,keyvals):
        '''
        keyvals: {'family:qual1': 'value1',
                  'family:qual2': 'value2'}
        '''
        self.table.put(rowkey,keyvals)
        
        
    def get(self,rowkey,columns=None,timestamp=None):
        if type(rowkey) == str:
            return self.table.row(rowkey, columns, timestamp)
        elif type(rowkey) == list:
            return self.table.rows(rowkey, columns, timestamp)
        
        
    def scan(self,rowprefix,columns,timestamp=None):
        return self.table.scan(row_prefix=rowprefix,columns=columns,timestamp=timestamp)    
        
        
    def delete(self,rowkey,columns=None,timestamp=None):
        self.table.delete(rowkey, columns, timestamp)
        
        
    
    
        
        
        