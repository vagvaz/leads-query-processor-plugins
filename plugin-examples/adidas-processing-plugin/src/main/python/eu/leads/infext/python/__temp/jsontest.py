'''
Created on Jun 11, 2014

@author: lequocdo
'''
import json



if __name__ == '__main__':
    jsonstring = "[ \"value1\", \"value2\", \"value3\" ]"
    jsonarray = json.loads(jsonstring)
    for elem in jsonarray:
        print elem
    