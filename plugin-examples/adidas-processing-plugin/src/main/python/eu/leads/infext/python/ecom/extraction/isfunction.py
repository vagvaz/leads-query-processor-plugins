'''
Created on Dec 13, 2013

@author: nonlinear
'''

def is_number(s):
    ''' could be more complex for various locale '''
    if ',' in s:
        s = s.replace(',','.')
    try:
        float(s)
        return True
    except ValueError:
        return False