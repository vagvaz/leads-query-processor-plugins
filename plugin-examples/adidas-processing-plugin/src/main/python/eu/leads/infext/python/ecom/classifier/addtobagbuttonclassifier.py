'''
Created on Jan 21, 2014

@author: nonlinear
'''

from eu.leads.infext.python.ecom.dictionaries.pagelement import addtocartbutton_regex
import re
from eu.leads.infext.python.ecom.classifier.virtualclassifier import VirtualClassifier
from eu.leads.infext.python.ops import xpathops
from lxml import html

class AddToBasketButtonClassifier(VirtualClassifier):
    '''
    Counts a feature that means a number of attributes/text descendants of the most promising node
     that contain a string that is characteristic for the Add to Basket button
    '''


    def __init__(self):
        '''
        Constructor
        '''
    
    
    def find(self,params):
        
        tree = self.page_dict.get("tree")
        
        candidates = []
        candidates.extend(self.__searchInputNodes())
        candidates.extend(self.__searchAnchorNodes())
        candidates.extend(self.__searchButtonNodes())
        candidates.extend(self.__searchImageNodes())
        if candidates:
            sorted_candidates = sorted(candidates,key=lambda x: -x[1])
            
#             for elem in sorted_candidates:
#                 print elem[0],'->',elem[1]
#                 print html.tostring(elem[0])
#                 print '#######################################################\n'
            
            element = sorted_candidates[0]
            elements_list = [element]
            for i in range(1,len(candidates)):
                if element[1] == candidates[i][1]:
                    elements_list.append(candidates[i])
                else:
                    break
            self.nodepath = []
            for elem in elements_list:
                self.nodepath.append(xpathops.element2path(tree, elem[0]))
            self.features = [element[1]]
            self.certainty = 1 if element[1] >= 1 else 0
            return self.nodepath
        else:
            self.features = [0]
            self.certainty = 0
            self.nodepath = None
            return self.nodepath
        
        
    def classifygivennode(self,page_dict,xpath):
        self.page_dict = page_dict
        if self.__searchNodes(xpath, ['id','title','value','name','class'], addtocartbutton_regex, True):
            return True
        else:
            return False
        
        
    def __searchNodes(self,xpath,attrs,regex,text_desc=False):
        tree = self.page_dict.get("tree")
        nodes = tree.xpath(xpath)
        
        node_candidates = []
        
        i=0
        for node in nodes:
            found = 0
            if node.get("type")!="hidden":
                for attr in attrs:
                    value = node.get(attr)
                    if value and re.match(regex,value,re.IGNORECASE):
                        #print(i,":",attr,"=",value)
                        found += 1
                if text_desc:
                    for text in node.itertext():
                        text = text.strip()
                        if text and re.match(regex,text,re.IGNORECASE):
                            #print(i,":","text","=",text)
                            found += 1
            if found > 0:
                node_candidates.append([node,found])
            i+=1
        
        return node_candidates
        
        
    def __searchInputNodes(self):
        xpath = '//body//input'
        attr = ['id','title','value','name']
        regex = addtocartbutton_regex
        return self.__searchNodes(xpath, attr, regex)
    
    
    def __searchAnchorNodes(self):
        xpath = '//body//a'
        #attr = ['id']
        attr = ['id','title','value','name']
        regex = addtocartbutton_regex
        return self.__searchNodes(xpath, attr, regex, True)
    
    
    def __searchButtonNodes(self):
        xpath = '//body//button'
        #attr = ['id','name']
        attr = ['id','title','value','name']
        regex = addtocartbutton_regex
        return self.__searchNodes(xpath, attr, regex, True)
    
    
    def __searchImageNodes(self):
        xpath = '//body//img'
        attr = ['alt']
        regex = addtocartbutton_regex
        return self.__searchNodes(xpath, attr, regex, False)
    
    
    
    
# classif = AddToBasketButtonClassifier()
# print("\namazon")
# print(classif.classify(requests.get('http://www.amazon.com/gp/product/B00CSHZK76/').text))
# print("\ndicks")
# print(classif.classify(requests.get('http://www.dickssportinggoods.com/product/index.jsp?productId=19283636').text))
# print("\nzappos")
# print(classif.classify(requests.get('http://www.zappos.com/adidas-running-marathon-10-ng~2').text))
# print("\nzalando")
# print(classif.classify(requests.get('http://www.zalando.co.uk/nike-performance-flex-2013-run-lightweight-running-shoes-black-n1241a08r-q13.html').text))
# print("\nkickz")
# print(classif.classify(requests.get('http://www.kickz.com/uk/nike/shoes/sneakers-high/blazer-hi-suede-vintage-royalblue_white').text))
# print("\nfootlocker")
# print(classif.classify(requests.get('http://www.footlocker.com/product/model:157341/sku:60883011/').text))
# print("\nnike")
# print(classif.classify(requests.get('http://store.nike.com/us/en_us/pd/graphic-2-usoc-t-shirt/pid-859585/').text))
# print("\nadidas")
# print(classif.classify(requests.get('http://www.adidas.com/us/product/mens-basketball-rose-4-shoes/HH460').text))
# print("\nnext")
# print(classif.classify(requests.get('http://www.next.co.uk/x535656s6').text))
     
     
     
    
    
    
    
    
    
    
        