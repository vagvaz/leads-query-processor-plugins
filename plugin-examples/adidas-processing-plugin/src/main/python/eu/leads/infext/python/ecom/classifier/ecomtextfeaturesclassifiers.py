'''
Created on Apr 30, 2014

@author: root
'''
from eu.leads.infext.python.ecom.classifier.virtualclassifier import VirtualClassifier
from eu.leads.infext.python.ops.xpathops import tree2textnodeslist, content2tree,\
    tree2attributevalslist
from eu.leads.infext.python.lang.dicts import lang_dicts
from eu.leads.infext.python.ecom.dictionaries.regexfunc import regexofdictentries,\
    countnonoverlappingmatches
import re
from eu.leads.infext.python.ecom.dictionaries.currency import price_any_regex,\
    pricecurr_any_regex, currprice_any_regex

############################################################################

class PriceMentionsClassifier(VirtualClassifier):
    '''
    classdocs
    '''


    def find(self,params):
        '''
        Look for the word "price" in the text and attributes values
        ''' 
        result = None
        
        tree = self.page_dict.get("tree")
        lang = self.page_dict.get("lang")
        text_nodes = []
        attr_vals = []    
        
        # get a list of all nonempty strings from body (except scripts)
        if "text_nodes" in self.page_dict:
            text_nodes = self.page_dict["text_nodes"]
            text_nodes_paths = self.page_dict["text_nodes_paths"]
        else:  
            text_nodes, text_nodes_paths = tree2textnodeslist(tree)
            self.page_dict["text_nodes"] = text_nodes
            self.page_dict["text_nodes_paths"] = text_nodes_paths
            
        # get a list of all attribute values from the body (except scripts)
        if "attr_vals" in self.page_dict:
            attr_vals = self.page_dict["attr_vals"]
        else:  
            attr_vals = tree2attributevalslist(tree)
            self.page_dict["attr_vals"] = attr_vals
        
        dict_entries = ["PRICE"]
        
        priceregex = regexofdictentries(dict_entries, lang)
            
        '''
        CORE
        '''          
        counter1 = counter2 = 0
        for text in text_nodes:
            #text = str(text)
            if re.search(priceregex,text):
                counter1 += 1
        for val in attr_vals:
            #val = str(val)
            if re.search(priceregex,val):
                counter2 += 1   
        '''
        '''  
                
        self.features = [counter1,counter2]
        
        if counter1 + counter2 > 10:
            result = self.certainty = 1
        else:
            result = self.certainty = 0
        
        return result
    
    
############################################################################

class WishlistMentionsClassifier(VirtualClassifier):
    '''
    classdocs
    '''


    def find(self,params):
        '''
        Look for the word "wishlist" in the anchors
        ''' 
        result = None
        
        tree = self.page_dict.get("tree")
        lang = self.page_dict.get("lang")
        a_text_nodes = []
        
        # get a list of all nonempty strings from body (only children of anchors)
        if "a_text_nodes" in self.page_dict:
            a_text_nodes = self.page_dict["a_text_nodes"]
            text_nodes_paths = self.page_dict["a_text_nodes_paths"]
        else:  
            a_text_nodes, text_nodes_paths = tree2textnodeslist(tree,element="a")
            self.page_dict["a_text_nodes"] = a_text_nodes
            self.page_dict["a_text_nodes_paths"] = text_nodes_paths
        
        dict_entries = ["WISHLIST"]
        
        wishregex = regexofdictentries(dict_entries, lang)
            
        '''
        CORE
        '''          
        counter1 = 0
        for text in a_text_nodes:
            #text = str(text)
            if re.search(wishregex,text):
                counter1 += 1
        '''
        '''  
                
        self.features = [counter1]
        
        if counter1 > 1:
            result = self.certainty = 1
        elif counter1 == 1:
            result = self.certainty = 0.5
        else:
            result = self.certainty = 0
        
        return result
    
    
############################################################################

class WarrantyTermsMentionsClassifier(VirtualClassifier):
    '''
    classdocs
    '''


    def find(self,params):
        '''
        Look for the warranty terms link 
        ''' 
        result = None
        
        tree = self.page_dict.get("tree")
        lang = self.page_dict.get("lang")
        a_text_nodes = []
        
        # get a list of all nonempty strings from body (only children of anchors)
        if "a_text_nodes" in self.page_dict:
            a_text_nodes = self.page_dict["a_text_nodes"]
            text_nodes_paths = self.page_dict["a_text_nodes_paths"]
        else:  
            a_text_nodes, text_nodes_paths = tree2textnodeslist(tree,element="a")
            self.page_dict["a_text_nodes"] = a_text_nodes
            self.page_dict["a_text_nodes_paths"] = text_nodes_paths
        
        dict_entries = ["WARRANTY"]
        
        wishregex = regexofdictentries(dict_entries, lang)
            
        '''
        CORE
        '''          
        counter1 = 0
        for text in a_text_nodes:
            #text = str(text)
            if re.search(wishregex,text):
                counter1 += 1
        '''
        '''  
                
        self.features = [counter1]
        
        if counter1 >= 1:
            result = self.certainty = 0.5
        else:
            result = self.certainty = 0
        
        return result
    
    
############################################################################

class ShippingReturnsMentionsClassifier(VirtualClassifier):
    '''
    classdocs
    '''


    def find(self,params):
        '''
        Look for the shipping and returns information
        ''' 
        result = None
        
        tree = self.page_dict.get("tree")
        lang = self.page_dict.get("lang")
        text_nodes = []
        
        # get a list of all nonempty strings from body (only children of anchors)
        if "text_nodes" in self.page_dict:
            text_nodes = self.page_dict["text_nodes"]
            text_nodes_paths = self.page_dict["text_nodes_paths"]
        else:  
            text_nodes, text_nodes_paths = tree2textnodeslist(tree,element="a")
            self.page_dict["text_nodes"] = text_nodes
            self.page_dict["text_nodes_paths"] = text_nodes_paths
        
        dict_entries = ["SHIPPING","RETURNS"]
        
        shipregex = regexofdictentries(dict_entries, lang)
            
        '''
        CORE
        '''          
        counter1 = 0
        for text in text_nodes:
            #text = str(text)
            if re.search(shipregex,text):
                counter1 += 1
        '''
        '''  
                
        self.features = [counter1]
        
        if counter1 > 3:
            result = self.certainty = 1
        elif counter1 > 1:
            result = self.certainty = 0.5
        else:
            result = self.certainty = 0
        
        return result
    
    
############################################################################

class TaxesMentionsClassifier(VirtualClassifier):
    '''
    classdocs
    '''


    def find(self,params):
        '''
        Look for the information on taxes
        ''' 
        result = None
        
        tree = self.page_dict.get("tree")
        lang = self.page_dict.get("lang")
        text_nodes = []
        
        # get a list of all nonempty strings from body (only children of anchors)
        if "text_nodes" in self.page_dict:
            text_nodes = self.page_dict["text_nodes"]
            text_nodes_paths = self.page_dict["text_nodes_paths"]
        else:  
            text_nodes, text_nodes_paths = tree2textnodeslist(tree,element="a")
            self.page_dict["text_nodes"] = text_nodes
            self.page_dict["text_nodes_paths"] = text_nodes_paths
        
        dict_entries = ["ECOM_TAXES"]
        
        taxesregex = regexofdictentries(dict_entries, lang)
            
        '''
        CORE
        '''          
        counter1 = 0
        for text in text_nodes:
            #text = str(text)
            if re.search(taxesregex,text):
                counter1 += 1
        '''
        '''  
                
        self.features = [counter1]
        
        if counter1 > 0:
            result = self.certainty = 0.5
        else:
            result = self.certainty = 0
        
        return result
    
    
############################################################################

class PriceValuesClassifier(VirtualClassifier):
    '''
    classdocs
    '''


    def find(self,params):
        '''
        Look for the information on taxes
        ''' 
        result = None
        
        tree = self.page_dict.get("tree")
        lang = self.page_dict.get("lang")
        text_nodes = []
        
        # get a list of all nonempty strings from body (only children of anchors)
        if "text_nodes" in self.page_dict:
            text_nodes = self.page_dict["text_nodes"]
            text_nodes_paths = self.page_dict["text_nodes_paths"]
        else:  
            text_nodes, text_nodes_paths = tree2textnodeslist(tree)
            self.page_dict["text_nodes"] = text_nodes
            self.page_dict["text_nodes_paths"] = text_nodes_paths
            
        price_any_regex
        pricecurr_any_regex
        currprice_any_regex
            
        '''
        CORE
        '''   
        page_text = ' '.join(text for text in text_nodes)
        pricecurrcount = countnonoverlappingmatches(pricecurr_any_regex, page_text)
        currpricecount = countnonoverlappingmatches(currprice_any_regex, page_text)
        pricecount     = countnonoverlappingmatches(price_any_regex, page_text)
        
        '''
        '''  
                
        result = self.features = [pricecurrcount+currpricecount,pricecount]
        
        return result    
    
##################################
############# tescik #############
##################################

# import requests
# 
# page2 = requests.get('http://www.adidas.com/us/product/mens-running-cc-rocket-boost-shoes/IEU81')
# content = page2.text
# tree = code2tree(content)
# attrs = tree2attributevalslist(tree)
# for attr in attrs:
#     print attr





