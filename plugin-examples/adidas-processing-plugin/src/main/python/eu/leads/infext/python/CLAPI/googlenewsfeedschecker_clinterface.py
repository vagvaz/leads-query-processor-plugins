'''
Created on Jul 20, 2014

@author: nonlinear
'''
from eu.leads.infext.python.CLAPI.conversionmethods import translateInputParameters,\
    translateReturnValues, processing_on, processing_off
import sys
import ast
from eu.leads.infext.python.ecom.extract.extractvalues import ExtractValues
from eu.leads.infext.python.ops import xpathops
from eu.leads.infext.python.ecom.extract.verify import defaultverifier
from eu.leads.infext.python.ecom.extract import extracthelpersfactory
from eu.leads.infext.python.news.googlenewsfeedschecker import GoogleNewsFeedsChecker

#if __name__ == '__main__':
class AccessPoint:
    def execute(self,params):
        '''
        Parameters:
        1 fully qualified domain name ( my.example.com )
        
        Returns:
        isinthenewsfeed
        '''
        
        #params = translateInputParameters(sys.argv)
        #processing = processing_on()
        
        fqdn = params[0]
        
        gnfChecker = GoogleNewsFeedsChecker()
        isit = gnfChecker.isnewsdomain(fqdn)
        
        if isit == True:
            retval = "true"
        else:
            retval = "false"
        
        returnlist = []
        returnlist.append(retval)
        
        # processing_off(processing)
        # print translateReturnValues(returnlist)
        return translateReturnValues(returnlist)