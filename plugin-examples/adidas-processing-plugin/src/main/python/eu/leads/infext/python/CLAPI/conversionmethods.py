#!/usr/bin/python
# -*- coding: latin-1 -*-

'''
Created on May 27, 2014

@author: root
'''

import os
import ast
import sys

class NullDevice():
    def write(self, s):
        pass
    
def processing_on():
    original_stdout = sys.stdout
    sys.stdout = NullDevice()   
    return original_stdout
    
def processing_off(original_stdout):
    sys.stdout = original_stdout


def translateInputParameters(argv):
    
    params = argv[1:]
    
    if params[0].startswith("paramsfile:"):
        fname = params[0][11:]
        f = open(fname,'r')
        fileargv = f.readlines()
        f.close()
        os.remove(fname)
        params = [x[:-1] for x in fileargv]
    
    for i in range(len(params)):
        if params[i].startswith("file:"):
            fname = params[i][5:]
            f = open(fname,'r')
            content = f.read()
            f.close()
            params[i] = content
            os.remove(fname)
        else:
            try:
                params[i] = ast.literal_eval(params[i].replace('\n',' '))
            except:
                #print sys.exc_info()
                params[i] = params[i]
#     # if DEBUG:
#     for par in params:
#         par = par[:40] if type(par) is str and len(par)>40 else par
#         print type(par),':',par 
            
    return params

        
def translateReturnValues(objlist):
    outputstring = ""
    for obj in objlist:
        if obj is not None:
            outputstring += "[leadsret:] "
            outputstring += unicode(obj)
            outputstring += "\n"
        else:
            outputstring += "[leadsret:] "
            outputstring += "\n"
    return outputstring[:-1].encode('utf8', 'replace')


if __name__ == '__main__':
    print translateReturnValues([u'$15.99',u'$12.99',u'Carhartt® Brushed-Fleece'])



    