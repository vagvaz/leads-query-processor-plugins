'''
Created on Jan 22, 2014

@author: nonlinear
'''
from eu.leads.infext.python.ecom.classifier.virtualclassifier import VirtualClassifier
from eu.leads.infext.python.ops.xpathops import tree2textnodeslist

from  collections import namedtuple
import requests
import re
import io
from eu.leads.infext.python.ecom.dictionaries.pagelement import productname_divattr_regex,\
    productname_divattrval
from lxml import html
from eu.leads.infext.python.ops import xpathops

NameClassifCandTuple = namedtuple("NameClassifCandTuple","h1_child_coef div_child_coef title_words_coef node_path node_string")

class NameNodeClassifier(VirtualClassifier):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        
        
    def find(self,params):
        ''' Should get a list of unique words contained in a part of the title that is non-repeating '''
        self.__title_words = []
        for word in params:
            word = ''.join([c for c in word if (c.isalnum() or c==' ')])
            self.__title_words.append(word)
        
        tree = self.page_dict.get("tree")
        # get a list of all nonempty strings from body (except scripts)
        if self.page_dict.get("text_nodes"):
            text_nodes = self.page_dict["text_nodes"]
            text_nodes_paths = self.page_dict["text_nodes_paths"]
        else:  
            text_nodes, text_nodes_paths = tree2textnodeslist(tree)
            self.page_dict["text_nodes"] = text_nodes
            self.page_dict["text_nodes_paths"] = text_nodes_paths
        
        title_words = []
        for word in params:
            word = ''.join([c for c in word if (c.isalnum())])
            title_words.append(word)
        
        candidates = []
        
        for i in range(0,len(text_nodes)):
            node_path = text_nodes_paths[i]
            if not self.__is_anchor_descendant(node_path):
                node = text_nodes[i]
                node = node.lower()
                node = ''.join([c for c in node if (c.isalnum() or c==' ')])
                node_words = node.split()
                if any(word in node_words for word in title_words):
                    #print(node,node_path)
                    # count the number of present words
                    coef3 = self.__count_title_words_coef(node_words)
                    # check if one of ancestors is <h1> node
                    coef1 = self.__get_h1_ancestor_coef(node_path)
                    # check if one of ancestors is <div> node with  defined attribute values
                    coef2 = self.__get_div_ancestor_attrs_coef(node_path)
                    
                    candidate_tuple = NameClassifCandTuple(coef1, coef2, coef3, node_path, node)
                    candidates.append(candidate_tuple)
                
                
        if candidates:
            candidates = sorted(candidates,key=lambda x: -(x.h1_child_coef+x.div_child_coef+x.title_words_coef))
            print(candidates)
            
            solver = candidates[0]
            
            self.nodepath = self.__check_solver_surroundings(solver,candidates,title_words)
            self.nodepath = sorted(self.nodepath)
            
            return True
        else:
            self.nodepath = None
            return False
        
        
    def __are_new_words(self,string,newstring,titlewords):
        words1 = string.split()
        words2 = newstring.split()
        newwords = set(words2).difference(set(words1))
        if any(titleword in newwords for titleword in titlewords):
            return True
        else:
            return False
    
    
    def __check_solver_surroundings(self,solver,candidates,title_words):
        nodepaths = [solver.node_path]
        
        text_nodes = self.page_dict["text_nodes"]
        text_nodes_paths = self.page_dict["text_nodes_paths"]
        
        index = text_nodes_paths.index(solver.node_path)
        
        nodes_checked = 0
        for text,text_node in zip(text_nodes[index+1:],text_nodes_paths[index+1:]):
            if nodes_checked > 2: 
                break
            if len(text)>0 and not text.isspace():
                nodes_checked += 1
                for cand in candidates[1:]:
                    if cand.node_path == text_node:
                        if self.__are_new_words(solver.node_string,cand.node_string,title_words):
                            nodepaths.append(cand.node_path)
        
        nodes_checked = 0                
        for text,text_node in zip(reversed(text_nodes[:index]),reversed(text_nodes_paths[:index])):
            if nodes_checked > 2: 
                break
            if len(text)>0 and not text.isspace():
                nodes_checked += 1
                for cand in candidates[1:]:
                    if cand.node_path == text_node:
                        if self.__are_new_words(solver.node_string,cand.node_string,title_words):
                            nodepaths.append(cand.node_path)
                        
        return nodepaths
        
    
    def __count_title_words_coef(self,text):
        title_words = self.__title_words
        counter = 0
        for word in title_words:
            if word in text:  
                counter += 1
        return 100*counter/(len(title_words)*len(text)) - (len(title_words)-counter)
    
    
    def __is_anchor_descendant(self,nodepath):
        return xpathops.isdescendantorelemtype(nodepath, 'a')
            
    
    def __get_h1_ancestor_coef(self,node):
        last = len(node)
        elem = 'h1'
        found = 0
        while True:
            index = node.rfind(elem,0,last)
            if index > -1:
                #print(node,"found h1")
                found = 1
            break            
        return 12*found
    
    
    def __get_div_ancestor_attrs_coef(self, node):
        tree = self.page_dict["tree"]
        element = tree.xpath(node)[0]
        divelems = element.xpath("ancestor-or-self::div")
        bestfound = 0
        if divelems:
            divelems.reverse()
            for divelem in divelems:   
                #print(divelem)
                for attr in divelem.items():
                    curr = 0
                    if attr[0] and re.match(productname_divattr_regex,attr[0]):
                        for words in productname_divattrval:
                            if any(word in attr[1].lower() for word in words):
                                curr += 1
                        #if curr > 0: print(attr[0],"=",attr[1])
                        bestfound = max(bestfound,curr)
                if bestfound == len(productname_divattrval):
                        break
        return 4*bestfound
   
   

# test_urls = ['http://www.amazon.com/gp/product/B00CSHZK76/',
#              'http://www.dickssportinggoods.com/product/index.jsp?productId=19283636',
#              'http://www.zappos.com/adidas-running-marathon-10-ng~2',
#              'http://www.zalando.co.uk/nike-performance-flex-2013-run-lightweight-running-shoes-black-n1241a08r-q13.html',
#              'http://www.kickz.com/uk/nike/shoes/sneakers-high/blazer-hi-suede-vintage-royalblue_white',
#              'http://www.footlocker.com/product/model:157341/sku:60883011/',
#              'http://store.nike.com/us/en_us/pd/graphic-2-usoc-t-shirt/pid-859585/',
#              'http://www.adidas.com/us/product/mens-basketball-rose-4-shoes/HH460',
#              'http://www.next.co.uk/x535656s6']
# for url in test_urls:
#     content = requests.get(url).text
#     f = io.StringIO(content)
#     tree = html.parse(f)
#     title = tree.xpath('/html/head/title/text()')[0]
#     title = re.sub('[\-\|]+', '', title)
#     title=list(set(title.split()))
#     title = list(set(title))
#     classif = NameNodeClassifier() 
#     print(url,classif.classify(content,title),title)
#     #print(classif.getNodePath())
#     print('---')
    
#     def getNodePath(self):
#         if self.nodepath is not None:
#             tree = self.page_dict['tree']
#             print self.nodepath
#             print tree.xpath(self.nodepath+'/text()')[0]
#         return self.nodepath
    
    
    
    
    
    
         