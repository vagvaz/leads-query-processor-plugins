'''
Created on Jan 30, 2014

@author: nonlinear
'''

class AbstractBigTable(object):
    '''
    classdocs
    '''

    def __init__(self, params):
        '''
        Constructor
        '''
        
    def connect(self,hostname,tablename):
        pass
    
    def put(self,rowkey,keyvals):
        pass
       
    def get(self,rowkey,columns=None,timestamp=None):
        pass
        
    def delete(self,rowkey,columns=None,timestamp=None):
        pass
    
    def scan(self,rowprefix,columns,timestamp=None):
        pass