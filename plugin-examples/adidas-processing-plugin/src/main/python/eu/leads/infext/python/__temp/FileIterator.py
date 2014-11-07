'''
Created on May 8, 2014

@author: root
'''

class FileIterator():
    '''
    classdocs
    '''


    def __init__(self, filename):
        self.f = open(filename,'rU')
        
    def getLine(self):
        for line in self.f:
            yield line
        
    