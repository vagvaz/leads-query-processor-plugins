'''
Created on Jul 10, 2014

@author: nonlinear
'''
import ast

if __name__ == '__main__':
    somelist = [[1.23651, 5.25124, 7.5125],
                [6.33651, 5.65124, 5.5125]]
    somedict = {'a':1,
                'b':2,
                'c':8}
    string = str(somedict)
    string = string.replace(" ", "")
    print string
    value = ast.literal_eval(string)
    print value