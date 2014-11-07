'''
Created on Jul 13, 2014

@author: nonlinear
'''
import ast
import requests
from eu.leads.infext.python.ops import xpathops
from eu.leads.infext.python.__temp.FileIterator import FileIterator

if __name__ == '__main__':
    fi = FileIterator("/home/nonlinear/workspace/leadsdm/eu/leads/infext/python/__temp/files/www.amazon.com")
    for line in fi.getLine():
        if line.startswith("#") or line.startswith("//"):
            continue
        url = line[:-1]
        content = requests.get(url).text
        print url
        tree = xpathops.content2tree(content)
        print len(content)
        titleNodes = tree.xpath('//title/text()')
        if len(titleNodes)>0:
            titleText = titleNodes[0].strip()
            print titleText
            titlePath = xpathops.element2parentpath(tree, titleNodes[0])
            print titlePath
        else:
            print "NO TITLE"
        print "---"
#     table = [0.54433105, -0.81649658, -0.75842509, -0.75282172, -0.79788596, -0.58532848, 0.54433105]
#     print str(table)
#     string = '[0.54433105 -0.81649658 -0.75842509 -0.75282172 -0.79788596 -0.58532848 0.54433105]'
#     print ast.literal_eval(string)