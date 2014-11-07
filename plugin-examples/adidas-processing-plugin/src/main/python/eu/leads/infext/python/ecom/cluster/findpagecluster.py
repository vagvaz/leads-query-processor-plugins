'''
Created on Jul 3, 2014

@author: nonlinear
'''
from sklearn import preprocessing

class FindPageCluster(object):
    '''
    classdocs
    '''


    def __init__(self, product_cluster_center, category_cluster_center, 
                 product_cluster_50pc_dist, product_cluster_80pc_dist, 
                 category_cluster_50pc_dist, category_cluster_80pc_dist,
                 scaler_mean, scaler_std):
        '''
        Constructor
        '''
        self.clusters_properties = {"prod": [product_cluster_center, product_cluster_50pc_dist, product_cluster_80pc_dist],
                                   "cat":  [category_cluster_center, category_cluster_50pc_dist, category_cluster_80pc_dist]}
        
        scaler = preprocessing.Scaler()
        scaler.mean_ = scaler_mean                                 
        scaler.std_ = scaler_std
        self.scaler = scaler
        
    
    def __position_inside_cluster(self, dist_cluster_center, cluster_properties):
        '''
        returns a group to which the page belongs
        0 - closer than a page with an medium distance
        1 - closer than 20% most further pages
        2 - in the last 20%
        '''
        if dist_cluster_center <= cluster_properties[1]:
            return 0
        elif dist_cluster_center <= cluster_properties[2]:
            return 1
        else:
            return 2
    
    
    def find(self,page_dict):
        
        fl = page_dict.get("ecom_features")
        page_features = [float(fl[0]), float(fl[1]),
               float(fl[2]), float(fl[3]),
               float(fl[8]), float(fl[9]),
               float(fl[10]),float(fl[11])]
        
        page_coordinates = self.scaler.transform(page_features)
        print page_coordinates
        
        from scipy.spatial.distance import euclidean
        dist_product_cluster_center = euclidean(self.clusters_properties.get("prod")[0], page_coordinates)
        dist_category_cluster_center = euclidean(self.clusters_properties.get("cat")[0], page_coordinates)
        
        if dist_product_cluster_center < dist_category_cluster_center:
            cluster = "prod"
            group = self.__position_inside_cluster(dist_product_cluster_center, self.clusters_properties.get("prod"))
            page_dict["ecom_kmeans_dist"] = dist_product_cluster_center
        else:
            cluster = "cat"
            group = self.__position_inside_cluster(dist_category_cluster_center, self.clusters_properties.get("cat"))
            page_dict["ecom_kmeans_dist"] = dist_category_cluster_center
            
        if cluster == "prod": # and group ...
            page_dict["category"] = 'ecom_product'
        elif cluster == "cat": # and group ...
            page_dict["category"] = 'ecom_category'
        
            
            
            
            
            
            
            
            
        
        
        
        
        