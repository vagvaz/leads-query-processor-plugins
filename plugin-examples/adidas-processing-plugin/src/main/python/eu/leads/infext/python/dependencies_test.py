'''
Created on Jan 30, 2014

@author: amo remix
'''

from sklearn import linear_model
import requests
from lxml import etree

xml = '<a xmlns="test"><b xmlns="test"/></a>'
root = etree.fromstring(xml)
print etree.tostring(root)


clf = linear_model.Ridge (alpha = .5)
print clf.fit ([[0, 0], [0, 0], [1, 1]], [0, .1, 1])
print clf.coef_
print clf.intercept_ 

r = requests.get('https://api.github.com/events')
print r.url
