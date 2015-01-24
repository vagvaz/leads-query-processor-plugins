'''
Created on Jun 11, 2014

@author: lequocdo
'''
import sys
from eu.leads.infext.python.CLAPI.conversionmethods import translateInputParameters,\
    translateReturnValues
import json
from eu.leads.infext.python.ecom.classifier.productofferingpageclassifier import ProductOfferingPageClassifier
from eu.leads.infext.python.ecom.cluster import findpagecluster
import ast

#if __name__ == '__main__':
class AccessPoint:
    def execute(self,params):
        '''
        Parameters:
        1 new page content
        2 new page language
        3 new page's site -> is button extracted?
        4 
        '''
        #sys.path.insert(0,'/home/lequocdo/workspace/leadsdm')
        #print sys.path
        
        #params = translateInputParameters(sys.argv)
         
        page_dict = {"content":params[0],"lang":params[1]}
        site_bag_button_extracted = True if params[2] == "true" else False
        product_cluster_center = params[3]
        category_cluster_center = params[4]
        product_cluster_50pc_dist = params[5]
        product_cluster_80pc_dist = params[6]
        category_cluster_50pc_dist = params[7]
        category_cluster_80pc_dist = params[8]
        scaler_mean = params[9]
        scaler_std = params[10]
         
        classifier = ProductOfferingPageClassifier()
        classifier.classify(page_dict, None)
         
        features = classifier.getFeaturesVals()
        
        is_bag_product_page_assumption = None
        if site_bag_button_extracted == True:
            if features[-1] == 1:
                is_bag_product_page_assumption = True
            else:
                is_bag_product_page_assumption = False
        
        pageclusterfinder = findpagecluster.FindPageCluster(product_cluster_center, category_cluster_center, product_cluster_50pc_dist, product_cluster_80pc_dist, 
                                            category_cluster_50pc_dist, category_cluster_80pc_dist, scaler_mean, scaler_std)
        pageclusterfinder.find(page_dict)
        
        if page_dict.get("category") == "ecom_product":
            # if product, 3 possibilities:
            # either we are sure
            if is_bag_product_page_assumption == True:
                pass
            # or we assume it's a product page
            elif is_bag_product_page_assumption == None:
                pass
            # or we are sure it's not - only looks like
            else:
                page_dict["category"] = "ecom_other"
        
        
        returnlist = []
        returnlist.append(page_dict["category"])
        returnlist.append(str(features))
        # processing_off(processing)
        # print translateReturnValues(returnlist)
        return translateReturnValues(returnlist)
        
    #     returnlist.append("true" if certainty==1 else "false")
    #     returnlist.extend([str(f) for f in features])
        
        
        
        
        
        