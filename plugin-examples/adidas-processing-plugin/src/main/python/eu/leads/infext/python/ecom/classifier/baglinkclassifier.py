'''
Created on Feb 5, 2014

@author: nonlinear
'''
from eu.leads.infext.python.ecom.classifier.virtualclassifier import VirtualClassifier
from eu.leads.infext.python.lang.dicts import lang_dicts
import requests
from lxml import html
from eu.leads.infext.python.myprint import prettyprint
from eu.leads.infext.python.ecom.test import shopslist
from eu.leads.infext.python.ops import xpathops
from eu.leads.infext.python.ecom.dictionaries.regexfunc import getwordregex, regexofdictentries
import re
from eu.leads.infext.python.ecom.classifier.addtobagbuttonclassifier import AddToBasketButtonClassifier

namespaces = {"re": "http://exslt.org/regular-expressions"}

points = [3,1,1,1,1,1,1,1]


class BagLinkCandidate(object):
    __slots__= "node", "nodepath", "points",

    def items(self):
        "dict style items"
        return [
            (field_name, getattr(self, field_name))
            for field_name in self.__slots__]

    def __iter__(self):
        "iterate over fields tuple/list style"
        for field_name in self.__slots__:
            yield getattr(self, field_name)

    def __getitem__(self, index):
        "tuple/list style getitem"
        return getattr(self, self.__slots__[index])
    
    

class BagLinkClassifier(VirtualClassifier):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
    
    
    def find(self,params):
        
        tree = self.page_dict.get("tree")
        lang = self.page_dict.get("lang")
    
        # 1. Find all with attributes
        nodes = self.__nodes_containing_attrs(elem='*', lang=lang, dict_entries=["ECOM_CART","ECOM_CHECKOUT"])     
        #print("---")
        
        baglink_candidates_list = []
        for node in nodes:
            cand = BagLinkCandidate()
            cand.node = node
            cand.nodepath = tree.getpath(node)
            cand.points = 0
            baglink_candidates_list.append(cand)
        
        #print(len(baglink_candidates_list))
        # 2a. Remove ones that have more than 3 levels of children
        baglink_candidates_list = [cand for cand in baglink_candidates_list if not tree.xpath(cand.nodepath+"/*/*/*/*/*")]         
        #print(len(baglink_candidates_list))   
        # 2b. Remove ones that have more than 20 descendants
        baglink_candidates_list = [cand for cand in baglink_candidates_list if tree.xpath("count("+cand.nodepath+"//*)")<20]         
        #print(len(baglink_candidates_list))
        # 2c. Remove the ones that look like add-to-cart buttons
        addclassif = AddToBasketButtonClassifier()
        baglink_candidates_list = [cand for cand in baglink_candidates_list if not addclassif.classifygivennode(self.page_dict, cand.nodepath)] 
        # 2d. Remove the ones that have more than 20 words in text nodes inside
        baglink_candidates_list = [cand for cand in baglink_candidates_list if sum(len(text.split()) for text in tree.xpath(cand.nodepath+"//text()"))<10]         
        #print(len(baglink_candidates_list))  
        # 2e. Remove ones that are children of others found
        baglink_candidates_list = [cand for cand in baglink_candidates_list if self.__isbagroot(cand,baglink_candidates_list)] 
        #print(len(baglink_candidates_list))  
        #print(' '.join(x.node.tag for x in baglink_candidates_list))
        
        for cand in baglink_candidates_list:
            
            # 3. If element is anchor, add points
            if cand.node.tag == 'a':
                cand.points += points[0]
            # 4. If element has anchor descendants, add points
            xpath = cand.nodepath+"//"+"a"
            if tree.xpath(xpath,namespaces=namespaces):
                cand.points += points[1]
            # 5. If element is image descendants, add points
            xpath = cand.nodepath+"//"+"img"
            if tree.xpath(xpath,namespaces=namespaces):
                cand.points += points[2]
            # 6. If element has descendants with attrs, add points
            if self.__nodes_containing_attrs(root=cand.nodepath, elem="*", lang=lang, dict_entries=["ECOM_CART","ECOM_CHECKOUT"]):
                cand.points += points[3]
            # 7. If element has anchor descendants with attrs, add points
            if self.__nodes_containing_attrs(root=cand.nodepath, elem="a", lang=lang, dict_entries=["ECOM_CART","ECOM_CHECKOUT"]):
                cand.points += points[4]
            
#             if not attention_flag:  
#                 # 8. If element has  descendants with text type 1, add points
#                 xpath = self.__xpath_contains_attrs(root=cand.nodepath, elem='*', attr="text()", 
#                                                      lang=lang, dict_entries=["ECOM_CART","ECOM_CHECKOUT"]) 
#                 if tree.xpath(xpath,namespaces=namespaces):
#                     cand.points += points[5]                
#                 # 9. If element has  descendants with text type 2, add points
#                 xpath = self.__xpath_contains_attrs(root=cand.nodepath, elem='*', attr="text()", 
#                                                      lang=lang, dict_entries=["ECOM_ITEM"]) 
#                 if tree.xpath(xpath,namespaces=namespaces):
#                     cand.points += points[6]
#                 # 9. If element has  descendants with text type 3, add points
#                 xpath = self.__xpath_contains_attrs(root=cand.nodepath, elem='*', attr="text()", 
#                                                      lang=lang, regexs=["\d+"]) 
#                 if tree.xpath(xpath,namespaces=namespaces):
#                     cand.points += points[7]
#             else:
            text_nodes = tree.xpath(cand.nodepath+"//text()")
            text_nodes = [text.strip() for text in text_nodes if text.strip()]
            
            match1 = match2 = match3 = 0
            regex1 = regexofdictentries(entries=["ECOM_CART","ECOM_CHECKOUT"],lang=lang)
            regex2 = regexofdictentries(entries=["ECOM_ITEM"],lang=lang)
            regex3 = "\d+"
            for text in text_nodes:
                #text = str(text)
                # 8. If element has  descendants with text type 1, add points
                if match1==0 and re.search(regex1,text):
                    match1 = 1
                # 9. If element has  descendants with text type 2, add points
                if match2==0 and re.search(regex2,text):
                    match2 = 1
                # 10. If element has  descendants with text type 3, add points
                if match3==0 and re.search(regex3,text):
                    match3 = 1
            cand.points += match1 + match2 + match3
                
        if len(baglink_candidates_list) > 0:
            baglink_candidates_list = sorted(baglink_candidates_list, key=lambda x: -x.points)
            
            cand = baglink_candidates_list[0]
    #         print("\n--\n\n")
    #         print(i,cand.points,":::")
    #         prettyprint.print_html(cand.node)
            
            self.features = [cand.points]
            self.nodepath = cand.nodepath if cand.points>=2 else None
            self.certainty = 1.0 if cand.points>5 else cand.points/5.0  
            
        else:
            self.features = [0]   
        
        return self.nodepath
    
    
    def __xpath_contains_attrs(self,root="",dict_entries=None,regexs=None,elem="",attr=None,lang='en'):
        
        if not dict_entries and not regexs:
            return None
        
        if attr == None: attr = "@*"
        if elem == None: elem = "*"
        
        lang_dict = lang_dicts.get(lang)
        en_dict = lang_dicts.get('en')
        
        xpath = ""
        xpath += root
        xpath += "//"
        xpath += elem
        xpath += "["
        
        if dict_entries:
            words_set = set()
            for entry in dict_entries:
                for word in lang_dict.get(entry):
                    words_set.add(word)
                if lang != 'en':
                    for word in en_dict.get(entry):
                        words_set.add(word)
                
            for word in words_set:
                xpath += "re:match("+attr+",'"+getwordregex(word)+"')"
                xpath += " or "
        
        if regexs:
            for regex in regexs:
                xpath += "re:match("+attr+",'"+regex+"','i')"
                xpath += " or "
        
        xpath = xpath[:-4]
            
        xpath += "]"
        
        return xpath
    
    
    def __nodes_containing_attrs(self,root="",dict_entries=None,elem="",attr=None,lang='en'):
        
        if not dict_entries:
            return None
        
        tree = self.page_dict.get("tree")
        
        if attr == None: attr = "@*"
        if elem == None: elem = "*"
        
        lang_dict = lang_dicts.get(lang)
        en_dict = lang_dicts.get('en')
        
        xpath = ""
        xpath += root
        xpath += "//"
        xpath += elem
        xpath += "["
        
        words_set = set()
        for entry in dict_entries:
            for word in lang_dict.get(entry):
                words_set.add(word)
            if lang != 'en':
                for word in en_dict.get(entry):
                    words_set.add(word)
                    
        xpath += "@*["
            
        for word in words_set:
            xpath += "contains(.,'"+word.lower()+"')"
            xpath += " or "
            xpath += "contains(.,'"+word.title()+"')"
            xpath += " or "
    
        xpath = xpath[:-4]
        xpath += "]]"
        
        nodes = tree.xpath(xpath,namespaces=namespaces)
        if nodes:
            eval_nodes = []
            regexdictentries = regexofdictentries(dict_entries, lang=lang)
            for node in nodes:
                attrvals = node.values()
                attrvals = [attr for attr in attrvals if re.search(regexdictentries,attr)]
                if attrvals:
                    eval_nodes.append(node)
            return eval_nodes
        else:
            return nodes
    
    
    def __isbagroot(self,cand,baglink_candidates_list):
        for potancestor in baglink_candidates_list:
            if cand == potancestor:
                break
            if xpathops.isancestor(cand.nodepath, potancestor.nodepath):
                return False                
            
        return True
        
        

# classif = BagLinkClassifier()  
# for url in shopslist.page_per_shop_list+shopslist.page_per_shop_test_list:
#     print(url)
#     if classif.classify(requests.get(url).text, 'en'):
#         print(classif.getCertainty(),classif.getNodePath())
#     else:
#         print "Nope"
#     prettyprint.print_space()
      
