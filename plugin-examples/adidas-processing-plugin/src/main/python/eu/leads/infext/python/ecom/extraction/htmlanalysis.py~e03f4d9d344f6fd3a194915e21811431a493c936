'''
Created on Dec 16, 2013

@author: nonlinear
'''

def is_link(path):
    returnval = False
    if "/a/" in path:
        returnval = True
    elif path.endswith("/a"):
        returnval = True
    return returnval

def fix_xpath_errors(path):
    returnval = path
    if path.endswith("/br"):
        returnval = path[0:-3]
    return returnval