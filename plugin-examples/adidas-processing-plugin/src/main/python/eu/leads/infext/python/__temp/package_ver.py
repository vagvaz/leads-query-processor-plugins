'''
Created on Jul 4, 2014

@author: nonlinear
'''

if __name__ == '__main__':
    from pkg_resources import require
    match = require("lxml")
    print match
    lxml = match[0]
    print lxml.version