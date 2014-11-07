'''
Created on Jul 21, 2014

@author: nonlinear
'''
from eu.leads.infext.python.ecom.extract.mine.abstractminer import AbstractMiner

class DefaultProductNameMiner(AbstractMiner):

    def __init__(self):
        self.name = None
    
    def extractWhatMattersAndNameIt(self,textnodes):
        self.__prepareProductNameStringTuple(textnodes)
        return [('ecom_prod_name',self.name)]
        
    def __prepareProductNameStringTuple(self,textnodes):
        self.name = ' '.join(x[0] for x in textnodes)



class DefaultStringMiner(AbstractMiner):

    def __init__(self):
        self.text = None
    
    def extractWhatMattersAndNameIt(self,textnodes):
        self.__prepareProductNameStringTuple(textnodes)
        return [('text',self.text)]
        
    def __prepareProductNameStringTuple(self,textnodes):
        self.text = ' '.join(x[0] for x in textnodes)        