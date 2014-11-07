'''
Created on Jul 28, 2014

@author: nonlinear
'''
from eu.leads.infext.python.CLAPI.conversionmethods import translateInputParameters
import sys

if __name__ == '__main__':
    params = translateInputParameters(sys.argv)
    print params[0]