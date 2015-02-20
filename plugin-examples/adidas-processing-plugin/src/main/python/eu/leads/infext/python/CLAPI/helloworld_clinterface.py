#!/usr/bin/env python
# -*- coding: utf-8 -*-
'''
Created on Jul 20, 2014

@author: nonlinear
'''
from eu.leads.infext.python.CLAPI.conversionmethods import translateReturnValues
import time
import logging

class AccessPoint:
    def execute(self,params):
        #time.sleep(1)
        logger = logging.getLogger("leads")
        logger.debug("params: %s" % str(params))
        return translateReturnValues([params[0]+" "+params[1]+" of Java, it's Python!"])        

# if __name__ == '__main__':
#     '''
#     No parameters: :)
#     
#     Returns:
#     "Hello Java, it's Python!"
#     ...
#     '''
#     time.sleep(0.1)
#     
#     print translateReturnValues(["Hello Java, it's Python!"])
    
    
    