'''
Created on Jul 28, 2014

@author: nonlinear
'''
import requests
from eu.leads.infext.python.ops import xpathops

if __name__ == '__main__':
    with open('/home/nonlinear/workspace/leadsdm/eu/leads/infext/python/__temp/files/alldomainssampleset.txt') as f:
        while True:
            tree = None
            line = f.readline()
            if line is None:
                break
            else:
                line = line[:-1]
                content = requests.get(line).text
                try:
                    tree = xpathops.code2tree_old(content)
                except:
                    print 'NOPE'
                    tree = None
                print line
                print tree