'''
Created on May 26, 2014

@author: root
'''

import sys
from eu.leads.infext.python.CLAPI.conversionmethods import translateInputParameters,\
    translateReturnValues, processing_on,\
    processing_off
from eu.leads.infext.python.ecom.classifier.productofferingpageclassifier import ProductOfferingPageClassifier
from eu.leads.infext.python.ecom.cluster.clusterproductcategorypages import ClusterProductCategoryPages
from eu.leads.infext.python.ecom.extrschema.siteschemaextractor import LeadsEcomSiteSchemaExtractor
from eu.leads.infext.python.ops import xpathops


#if __name__ == '__main__':
class AccessPoint:
    def execute(self,params):
        '''
        Parameters:
        [url content lang ecom_features buttonnode basketnode] [url content lang ecom_features buttonnode basketnode] ...
        
        Returns:
        nameextractiontuples
        priceextractiontuples
        '''
        
        returnlist = []
    
        #params = translateInputParameters(sys.argv)
         
        page_dict_list = []
        for i in range(len(params)/6):
            url_index      = i*6
            content_index  = url_index+1
            lang_index     = url_index+2
            features_index = url_index+3
            button_index   = url_index+4
            basket_index   = url_index+5
            page_dict = { "url": params[url_index],
                          "content": params[content_index],
                          "lang": params[lang_index],
                          "ecom_features": params[features_index],
                          "buttonnode": params[button_index],
                          "basketnode": params[basket_index] }
            page_dict["tree"] = xpathops.content2tree(page_dict.get("content"))
            page_dict_list.append(page_dict) 
        
        correct_clustering = True
        clusterPages = ClusterProductCategoryPages()
        for K in range(5,1,-1):
            clusterPages.setK(K)
            print "KMeans: "+str(K)
            clusterPages.cluster_test(page_dict_list)
            
        
    
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    
    
    
    
    
    