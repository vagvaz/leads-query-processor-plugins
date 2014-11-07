'''
Created on Jun 12, 2014

@author: lequocdo
'''
from eu.leads.infext.python.ecom.classifier.virtualclassifier import VirtualClassifier
from eu.leads.infext.python.ops.xpathops import tree2imgnodeslist


class ImageFeatureClassifier(VirtualClassifier):
    '''
    classdocs
    '''


    def find(self,params):
        '''
        Look for the word "wishlist" in the anchors
        ''' 
        result = None
        
        tree = self.page_dict.get("tree")
        
        # get a list of all nonempty strings from body (only children of anchors)
        if "img_nodes" in self.page_dict:
            img_urls = self.page_dict["img_urls"]
            img_nodes_paths = self.page_dict["img_nodes_paths"]
        else:  
            img_urls, img_nodes_paths = tree2imgnodeslist(tree)
            self.page_dict["img_urls"] = img_urls
            self.page_dict["img_nodes_paths"] = img_nodes_paths 
                
        self.features = [len(img_urls)]
        
        return result
        