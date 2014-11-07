'''
Created on Dec 10, 2013

@author: nonlinear
'''

import requests
from BeautifulSoup import BeautifulSoup

page2 = requests.get('http://www.wiggle.com/adidas-adizero-adios-boost-shoes-ss14/')
with open('webpage_wiggle.html', 'w') as myFile:
    soup = BeautifulSoup(page2.text)
    code = unicode(soup.renderContents(), errors='ignore')
    myFile.write(code)
