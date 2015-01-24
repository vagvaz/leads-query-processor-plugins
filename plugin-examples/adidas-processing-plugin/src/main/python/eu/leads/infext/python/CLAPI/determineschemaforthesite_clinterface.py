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
        product_cluster_center
        category_cluster_center
        product_cluster_50pc_dist
        product_cluster_80pc_dist
        category_cluster_50pc_dist
        category_cluster_80pc_dist
        pd.mean
        std.tolist
        product_pages_indices_list
        category_pages_indices_list
        '''
        
        returnlist = []
        
        #params = translateInputParameters(sys.argv)
        #processing = processing_on()
         
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
            #print params[url_index]
            page_dict["tree"] = xpathops.content2tree(page_dict.get("content"))
            page_dict_list.append(page_dict) 
        
        correct_clustering = True
    
        clusterPages = ClusterProductCategoryPages()
        if clusterPages.cluster(page_dict_list):
            product_page_dict_list = clusterPages.product_pages_dict_list
            #for dict in product_page_dict_list:
                #print dict.get("url"),":",dict.get("ecom_kmeans_dist")
        else:
            print 'incorrect clustering'
            correct_clustering = False
            
        if correct_clustering == True:
            schema_extractor = LeadsEcomSiteSchemaExtractor(product_page_dict_list)
            schema_extractor.find_schema()
        
            nameextractiontuples = schema_extractor.getNameExtractionTuples()
            priceextractiontuples= schema_extractor.getPriceExtractionTuples()
        
    #     print '1',str(nameextractiontuples),'2',str(priceextractiontuples),'3',str(clusterPages.product_cluster_center),'4',str(clusterPages.category_cluster_center),
    #     print '5',str(clusterPages.product_cluster_50pc_dist),'6',str(clusterPages.product_cluster_80pc_dist),'7',str(clusterPages.category_cluster_50pc_dist),
    #     print '8',str(clusterPages.category_cluster_80pc_dist),'9',str(clusterPages.pd.mean),'10',str(clusterPages.pd.std)
        
            returnlist.append(str(nameextractiontuples))
            returnlist.append(str(priceextractiontuples))
            returnlist.append(str(clusterPages.product_cluster_center.tolist()))
            returnlist.append(str(clusterPages.category_cluster_center.tolist()))
            returnlist.append(str(clusterPages.product_cluster_50pc_dist))
            returnlist.append(str(clusterPages.product_cluster_80pc_dist))
            returnlist.append(str(clusterPages.category_cluster_50pc_dist))
            returnlist.append(str(clusterPages.category_cluster_80pc_dist))
            returnlist.append(str(clusterPages.pd.mean.tolist()))
            returnlist.append(str(clusterPages.pd.std.tolist()))
            returnlist.append(str(clusterPages.product_pages_indices_list))
            returnlist.append(str(clusterPages.category_pages_indices_list))
        
        print '///'
        print type(clusterPages.product_cluster_center)
        print clusterPages.product_cluster_center
        print '///'
        
        #processing_off(processing)
        #print translateReturnValues(returnlist) 
        return translateReturnValues(returnlist)
        