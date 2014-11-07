'''
Created on Feb 6, 2014

@author: nonlinear
'''

from lxml import html

def print_space():
    print('\n\n---\n\n\n')
    
def print_html(element):
    print(html.tostring(element,pretty_print=True))