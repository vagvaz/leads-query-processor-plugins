'''
Created on Jul 1, 2014

@author: nonlinear
'''

from sklearn.cluster import KMeans
from sklearn import preprocessing, metrics
from sklearn.decomposition.pca import PCA
import numpy
import pylab


class PagesDataset():
    
    def __init__(self,page_dicts_list):
        urls = []
        fl = []
        for pd in page_dicts_list:
            urls.append(pd.get("url"))
            fl.append(pd.get("ecom_features"))
        self.urls = urls
        self.features_lists, self.mean, self.std = self.__prepare(fl)
        
    
    def __prepare(self,features_lists):
        new_features_lists = []
        for fl in features_lists:
            nfl = [float(fl[0]), float(fl[1]),
                   float(fl[2]), float(fl[3]),
                   float(fl[8]), float(fl[9]),
                   float(fl[10]),float(fl[11])]
            new_features_lists.append(nfl)
            
        scaler = preprocessing.Scaler().fit(new_features_lists)
        print "mean", scaler.mean_                                      
        print "std", scaler.std_ 
        
        self.scaler = scaler             
        
        return scaler.transform(new_features_lists), scaler.mean_, scaler.std_
            
        #return preprocessing.scale(new_features_lists)


class ClusterProductCategoryPages(object):
    '''
    classdocs
    '''


    def __init__(self):
        self._K = 2
        
        
    def __label_cluster0(self,cluster_centers):
        transformed_cluster0_center = cluster_centers[0]
        transformed_cluster1_center = cluster_centers[1]
        
        cluster0_center = self.pd.scaler.inverse_transform(transformed_cluster0_center).tolist()
        cluster1_center = self.pd.scaler.inverse_transform(transformed_cluster1_center).tolist()
        
        for i,(feat0,feat1) in enumerate(zip(cluster0_center,cluster1_center)):
            cluster0_center[i] = int(100*feat0)
            cluster1_center[i] = int(100*feat1)
        
        print "CLUSTER 0 CENTER ::::::::::::::::::::: " + str(cluster0_center)
        print "CLUSTER 1 CENTER ::::::::::::::::::::: " + str(cluster1_center)
        
        # So now we got average values multiplied by 100 represented by integers
        # Let's make some magic...
        
        # CHECK 1: AtBButtons-1:
        if cluster0_center[7] < 50 and cluster1_center[0] > 50 and cluster1_center[7] >50:
            print "Labelling with check 1"
            return "cat"
        # ...reversed
        if cluster1_center[7] < 50 and cluster0_center[0] > 50 and cluster0_center[7] >50:
            print "Labelling with check 1 rev"
            return "prod"
        
        # CHECK 2: AtBButtons-2 (AtBButtons on category pages):
        if cluster0_center[7] > 50 and cluster0_center[7] < 150 and cluster1_center[7] > 150:
            print "Labelling with check 2"
            return "prod"
        # ...reversed
        if cluster1_center[7] > 50 and cluster1_center[7] < 150 and cluster0_center[7] > 150:
            print "Labelling with check 2 rev"
            return "cat"
        
        # CHECK 3: images:
        if cluster0_center[6] > 2 * cluster1_center[6]:
            print "Labelling with check 3"
            return "cat"
        # ...reversed
        if cluster1_center[6] > 2 * cluster0_center[6]:
            print "Labelling with check 3 rev"
            return "prod"
        
        # CHECK 4: price mentions etc. (would be wrong for Amazon - should work for others however):
        numbers = [1,2,3,4,5,6]
        cluster0_over_cluster1 = 0
        
        for i in range(len(cluster0_center)):
            for no in numbers:
                if cluster0_center[no] > cluster1_center[no]:
                    cluster0_over_cluster1 += 1
                elif cluster0_center[no] < cluster1_center[no]:
                    cluster0_over_cluster1 -= 1
         
        if cluster0_over_cluster1 > 1:
            print "Labelling with check 4"
            return "cat"
        elif cluster0_over_cluster1 < -1:
            print "Labelling with check 4"
            return "prod"
        else:
            return None
        
        
    def __get_50pc_80pc_distance(self,page_dict_list):
        list_len = len(page_dict_list)
        el_50pc_no = int(list_len/2)
        el_80pc_no = int(list_len/1.25)
        
        return page_dict_list[el_50pc_no].get('ecom_kmeans_dist'), page_dict_list[el_80pc_no].get('ecom_kmeans_dist') 
    
    
    def setK(self, K):
        self._K = K
        
        
    def cluster_test(self,page_dict_list):
        
        pd = PagesDataset(page_dict_list)
        self.pd = pd

        X_scaled = pd.features_lists
        
        print X_scaled
        
        kmeans = KMeans(k=self._K, init='k-means++', n_init=10, max_iter=300, tol=0.0001, precompute_distances=True, verbose=0, random_state=None, copy_x=True, n_jobs=1)
        kmeans.fit(X=X_scaled, y=None)
        
        Y = kmeans.predict(X_scaled)
        
        for i,(page_dict,cluster_no) in enumerate((zip(page_dict_list,Y))):
            print page_dict["url"],':',cluster_no
    
        
    def cluster(self, page_dict_list):
        
        pd = PagesDataset(page_dict_list)
        self.pd = pd

        X_scaled = pd.features_lists
        
        print X_scaled
        
#         X_reduced = PCA(n_components=2).fit_transform(X_scaled)
#         print X_reduced
        
        kmeans = KMeans(k=self._K, init='k-means++', n_init=10, max_iter=300, tol=0.0001, precompute_distances=True, verbose=0, random_state=None, copy_x=True, n_jobs=1)
        kmeans.fit(X=X_scaled, y=None)
        
#         print( '% 9s   %i   %.3f   %.3f   %.3f   %.3f   %.3f    %.3f'
#           % ("Example", kmeans.inertia_,
#              metrics.homogeneity_score(labels, kmeans.labels_),
#              metrics.completeness_score(labels, kmeans.labels_),
#              metrics.v_measure_score(labels, kmeans.labels_),
#              metrics.adjusted_rand_score(labels, kmeans.labels_),
#              metrics.adjusted_mutual_info_score(labels,  kmeans.labels_),
#              metrics.silhouette_score(X_scaled, kmeans.labels_,
#                                       metric='euclidean',
#                                       sample_size=3)))
        
        Y = kmeans.predict(X_scaled)
        X_new = kmeans.transform(X_scaled, y=None)
        print Y
        print X_new
        print kmeans.cluster_centers_
        
        pages_dict_list_clusters = [[],[]]
        
        for i,(page_dict,cluster_no,distance_pair) in enumerate((zip(page_dict_list,Y,X_new))):
            page_dict['ecom_kmeans_dist'] = distance_pair[cluster_no]
            pages_dict_list_clusters[cluster_no].append(page_dict)
            
        for i,pages_dict_list_cluster in enumerate(pages_dict_list_clusters):
            pages_dict_list_clusters[i] = sorted(pages_dict_list_cluster,key=lambda dict: dict['ecom_kmeans_dist'])
        
        product_pages_indices_list = []
        category_pages_indices_list = []
        
        cluster0_label = self.__label_cluster0(kmeans.cluster_centers_)
        if cluster0_label == "cat":
            cat_no = 0
            prod_no = 1
            retval = True
        elif cluster0_label == "prod":
            cat_no = 1
            prod_no = 0
            retval = True
        else:
            retval = False
        
        if retval == True:
            self.product_pages_dict_list = pages_dict_list_clusters[prod_no]
            self.category_pages_dict_list = pages_dict_list_clusters[cat_no]
            
            for i,cluster_no in enumerate(Y):
                if cluster_no == prod_no:
                    product_pages_indices_list.append(i)
                elif cluster_no == cat_no:
                    category_pages_indices_list.append(i)
            
            for dic in self.product_pages_dict_list:
                dic['category'] = 'ecom_product'
            for dic in self.category_pages_dict_list:
                dic['category'] = 'ecom_category'
            
            self.product_cluster_center = kmeans.cluster_centers_[prod_no]
            self.category_cluster_center = kmeans.cluster_centers_[cat_no]
            self.product_cluster_50pc_dist, self.product_cluster_80pc_dist = self.__get_50pc_80pc_distance(self.product_pages_dict_list)
            self.category_cluster_50pc_dist, self.category_cluster_80pc_dist = self.__get_50pc_80pc_distance(self.category_pages_dict_list)
            self.product_pages_indices_list = product_pages_indices_list
            self.category_pages_indices_list = category_pages_indices_list
        
        return retval
        
#         # Step size of the mesh. Decrease to increase the quality of the VQ.
#         h = .02     # point in the mesh [x_min, m_max]x[y_min, y_max].
#         
#         # Plot the decision boundary. For that, we will assign a color to each
#         x_min, x_max = X_reduced[:, 0].min() + 1, X_reduced[:, 0].max() - 1
#         y_min, y_max = X_reduced[:, 1].min() + 1, X_reduced[:, 1].max() - 1
#         xx, yy = numpy.meshgrid(numpy.arange(x_min, x_max, h), numpy.arange(y_min, y_max, h))
#         
#         # Obtain labels for each point in mesh. Use last trained model.
#         Z = kmeans.predict(numpy.c_[xx.ravel(), yy.ravel()])
#         
#         # Put the result into a color plot
#         Z = Z.reshape(xx.shape)
#         pylab.figure(1)
#         pylab.clf()
#         pylab.imshow(Z, interpolation='nearest',
#                   extent=(xx.min(), xx.max(), yy.min(), yy.max()),
#                   cmap=None,
#                   aspect='auto', origin='lower')
#         
#         pylab.plot(X_reduced[:, 0], X_reduced[:, 1], 'k.', markersize=2)
#         # Plot the centroids as a white X
#         centroids = kmeans.cluster_centers_
#         pylab.scatter(centroids[:, 0], centroids[:, 1],
#                    marker='x', s=169, linewidths=3,
#                    color='w', zorder=10)
#         pylab.title('K-means clustering on the digits dataset (PCA-reduced data)\n'
#                  'Centroids are marked with white cross')
#         pylab.xlim(x_min, x_max)
#         pylab.ylim(y_min, y_max)
#         pylab.xticks(())
#         pylab.yticks(())
#         pylab.show()
        

        
        
if __name__ == "__main__":
    obj = ClusterProductCategoryPages()
    obj.cluster(None)
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    