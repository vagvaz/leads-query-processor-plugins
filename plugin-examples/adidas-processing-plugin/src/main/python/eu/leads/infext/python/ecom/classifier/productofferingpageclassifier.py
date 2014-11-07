'''
Created on Jan 24, 2014

@author: nonlinear
'''
from eu.leads.infext.python.ecom.classifier.virtualclassifier import VirtualClassifier
from eu.leads.infext.python.ecom.classifier.addtobagbuttonclassifier import AddToBasketButtonClassifier
from eu.leads.infext.python.ecom.classifier.baglinkclassifier import BagLinkClassifier
from eu.leads.infext.python.__temp.FileIterator import FileIterator
import requests
from eu.leads.infext.python.ecom.classifier import ecomtextfeaturesclassifiers
from eu.leads.infext.python.ecom.classifier import featuresclassifiers


class ProductOfferingPageClassifier(VirtualClassifier):
    '''
    classdocs
    '''

    def __init__(self):
        self.addToBasketButtonClassifier = AddToBasketButtonClassifier()
        self.bagLinkClassifier = BagLinkClassifier()
        self.priceMentionsClassifier = ecomtextfeaturesclassifiers.PriceMentionsClassifier()
        self.wishlistMentionsClassifier = ecomtextfeaturesclassifiers.WishlistMentionsClassifier()
        self.warrantyTermsMentionsClassifier = ecomtextfeaturesclassifiers.WarrantyTermsMentionsClassifier()
        self.shippingReturnsMentionsClassifier = ecomtextfeaturesclassifiers.ShippingReturnsMentionsClassifier()
        self.taxesMentionsClassifier = ecomtextfeaturesclassifiers.TaxesMentionsClassifier()
        
        self.priceValuesClassifier = ecomtextfeaturesclassifiers.PriceValuesClassifier()
        self.imagesNumberClassifier = featuresclassifiers.ImageFeatureClassifier()
        
        
    def find(self,params):
        bresult = False
        
        page_dict = self.page_dict
        
        '''
        Features only on the product offering pages
        '''
        
        # add to bag functionality existence
        self.addToBasketButtonClassifier.classify(page_dict, params)
        featureCert1 = self.addToBasketButtonClassifier.getCertainty() 
        # FEATURE 0
        addToBasketFeature = self.addToBasketButtonClassifier.getFeaturesVals()[0] 
        
        '''
        Features on the product pages as well as product lists pages
        '''
        
        # price nodes existence
        self.priceMentionsClassifier.classify(page_dict, params)
        featureCert3 = self.priceMentionsClassifier.getCertainty()
        # FEATURE 1,2
        priceMentionsTextFeature = self.priceMentionsClassifier.getFeaturesVals()[0] 
        priceMentionsAttrFeature = self.priceMentionsClassifier.getFeaturesVals()[1] 
        
        # taxes info
        self.taxesMentionsClassifier.classify(page_dict, params)
        featureCert7 = self.taxesMentionsClassifier.getCertainty()
        # FEATURE 3
        taxesMentionsFeature = self.taxesMentionsClassifier.getFeaturesVals()[0] 
        
        '''
        Features on every page in Ecommerce
        ''' 
        
        # bag functionality existence
        self.bagLinkClassifier.classify(page_dict, params)
        featureCert2 = self.bagLinkClassifier.getCertainty()
        # FEATURE 4
        bagLinkFeature = self.bagLinkClassifier.getFeaturesVals()[0] 
        
        # wishlist existence
        self.wishlistMentionsClassifier.classify(page_dict, params)
        featureCert4 = self.wishlistMentionsClassifier.getCertainty()
        # FEATURE 5
        wishlistMentionsFeature = self.wishlistMentionsClassifier.getFeaturesVals()[0] 
        
        # warranty terms info
        self.warrantyTermsMentionsClassifier.classify(page_dict, params)
        featureCert5 = self.warrantyTermsMentionsClassifier.getCertainty()
        # FEATURE 6
        warrantyTermsMentionsFeature = self.warrantyTermsMentionsClassifier.getFeaturesVals()[0] 
        
        # shipping (delivery) and returns info
        self.shippingReturnsMentionsClassifier.classify(page_dict, params)
        featureCert6 = self.shippingReturnsMentionsClassifier.getCertainty()
        # FEATURE 7
        shippingReturnsMentionsFeature = self.shippingReturnsMentionsClassifier.getFeaturesVals()[0] 
   
        # mentions of price values
        self.priceValuesClassifier.classify(page_dict, params)
        # FEATURE 8,9
        currPriceMentionsFeature = self.priceValuesClassifier.getFeaturesVals()[0]
        priceValueMentionsFeature = self.priceValuesClassifier.getFeaturesVals()[1] 
        
        # number of images
        self.imagesNumberClassifier.classify(page_dict, params)
        # FEATURE 10
        imagesFeature = self.imagesNumberClassifier.getFeaturesVals()[0] 
        
        # add to basket button count
        # FEATURE 11
        atbbNodePaths = self.addToBasketButtonClassifier.getNodePath()
        addToBasketButtonsNumberFeature = len(atbbNodePaths) if atbbNodePaths != None else 0
        
        # analyze features
        self.features = [addToBasketFeature,priceMentionsTextFeature,priceMentionsAttrFeature,taxesMentionsFeature,
                         bagLinkFeature,wishlistMentionsFeature,warrantyTermsMentionsFeature,shippingReturnsMentionsFeature,
                         currPriceMentionsFeature,priceValueMentionsFeature,imagesFeature,addToBasketButtonsNumberFeature]
        
        page_dict['ecom_features'] = self.features
        
        self.certainty = self.countCertainty(self.features)
        
        return self.features
    
    
    def getExtractedNodes(self):
        buttonnode = self.addToBasketButtonClassifier.getNodePath()
        basketnode = self.bagLinkClassifier.getNodePath()
        return buttonnode, basketnode            
    
    
    def countCertainty(self,features):
        
        condition1 = 1 if (features[0]>0 or features[4]>0) else 0
        
        condition2a= 1 if (features[1]>0) else 0
        condition2b= 1 if (features[2]>0) else 0
        condition2c= 1 if (features[3]>0) else 0
        condition2d= 1 if (features[5]>0) else 0
        condition2e= 1 if (features[6]>0) else 0
        condition2f= 1 if (features[7]>0) else 0
        condition2g= 1 if (features[8]>0) else 0
        condition2h= 1 if (features[9]>0) else 0
        condition2 = condition2a+condition2b+condition2c+condition2d+condition2e+condition2f+condition2g+condition2h
        
        if condition1 == 1 and condition2 >= 3:
            return 1
        elif condition2 >= 5:
            return 1
        else:
            return 0


if __name__ == '__main__':
    classifier = ProductOfferingPageClassifier()
#     fileIt = FileIterator("/home/lequocdo/workspace/leadsdm/org/leads/root/__temp/micro_dataset_ecomclassif.txt")
#     
#     for line in fileIt.getLine():
#         if line.startswith("#"):
#             print(line)
#         else:
#             line = line.strip()
#             
#             # get content
#             page = requests.get(line)
#             content = page.content
#             
#             # prepare page_dict   
#             page_dict = { "content": content, "lang": "en" }
#             
#             classifier.classify(page_dict, None)
#             features = classifier.getFeaturesVals()
#             print line,
#             print ', '.join([str(f) for f in features])

    # get content
    line = 'http://www.next.co.uk/g814098s2'
    page = requests.get(line)
    content = page.content
     
    # prepare page_dict   
    page_dict = { "content": content, "lang": "en" }
     
    classifier.classify(page_dict, None)
    features = classifier.getFeaturesVals()
    print line,
    print ', '.join([str(f) for f in features])








































    
    