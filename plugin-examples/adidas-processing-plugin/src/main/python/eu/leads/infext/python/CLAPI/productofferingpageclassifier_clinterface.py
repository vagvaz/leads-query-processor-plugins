'''
Created on May 26, 2014

@author: root
'''

import sys
from eu.leads.infext.python.CLAPI.conversionmethods import translateInputParameters,\
    translateReturnValues
from eu.leads.infext.python.ecom.classifier.productofferingpageclassifier import ProductOfferingPageClassifier



if __name__ == '__main__':
    #sys.path.insert(0,'/home/lequocdo/workspace/leadsdm')
    #print sys.path
    
    params = translateInputParameters(sys.argv)
    
    page_dict = {"content":params[0],"lang":params[1]}
     
    classifier = ProductOfferingPageClassifier()
    classifier.classify(page_dict, None)
    
    features = classifier.getFeaturesVals()
    certainty = classifier.getCertainty()
    returnlist = []
    returnlist.append("true" if certainty==1 else "false")
    returnlist.append(features)
    button = classifier.addToBasketButtonClassifier.getNodePath()
    if button is not None:
        returnlist.append(button)
    else:
        returnlist.append(" ")
    bag = classifier.bagLinkClassifier.getNodePath()
    if bag is not None:
        returnlist.append(bag)
    else:
        returnlist.append(" ")
    #returnlist.extend([str(f) for f in features])
    print translateReturnValues(returnlist)