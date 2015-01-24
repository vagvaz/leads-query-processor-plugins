'''
Created on Jun 11, 2014

@author: lequocdo
'''
import sys
from eu.leads.infext.python.CLAPI.conversionmethods import translateInputParameters,\
    translateReturnValues
from eu.leads.infext.python.ecom.classifier.addtobagbuttonclassifier import AddToBasketButtonClassifier
import json

#if __name__ == '__main__':
    #sys.path.insert(0,'/home/lequocdo/workspace/leadsdm')
    #print sys.path
class AccessPoint:
    def execute(self,params):
        
        #params = translateInputParameters(sys.argv)
         
        page_dict = {"content":params[0],"lang":params[1]}
        if(len(params)>2):
            xpaths_json = json.loads(params[2])
        else:
            xpaths_json = []
         
        classifier = AddToBasketButtonClassifier()
        
        classifyneeded = True
        returnlist = []
        
        # method for checking if a given node looks like a button
        for xpath in xpaths_json:
            check = classifier.classifygivennode(page_dict, xpath)
            if check == True:
                classifyneeded = False
                returnlist.append("true")
                break
            
        if classifyneeded == True:
            classifier.classify(page_dict, None)
            returnlist.append(classifier.getCertainty())
            returnlist.append(classifier.getNodePath())
        
        #print translateReturnValues(returnlist)
        return translateReturnValues(returnlist)
    
    
    
    
    