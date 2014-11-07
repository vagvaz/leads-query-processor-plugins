'''
Created on Jan 9, 2014

@author: nonlinear
'''

from eu.leads.infext.python.sellenium.dynamiccoderetriever import DynamicCodeRetriever
import requests
import io
from lxml import html
import re
from eu.leads.infext.python.ecom.dictionaries.currency import price_any_regex
from eu.leads.infext.python.ecom.classifier.namenodeclassifier import NameNodeClassifier
from eu.leads.infext.python.ops.stringops import find_common_patterns_of_all
from eu.leads.infext.python.ops import xpathops
from eu.leads.infext.python.ecom.extrschema.pricefinder import PriceFinder
from eu.leads.infext.python.ecom.extrschema.hiddenpriceschemaextractor import HiddenPriceSchemaExtractor
import string

class LeadsEcomSiteSchemaExtractor(object):
    '''
    classdocs
    '''


#     def __init__(self, url, sampleset_size=10):
#         '''
#         Constructor
#         '''
#         self.__base_url = url_base(url)
#         self.__url = url
#         self.__temp = 0
#         self.__sampleset_size = sampleset_size
#         
#         self.__draw_baseurl_sampleset()
        
        
    def __init__(self, page_dict_list):
        '''
        Constructor
        '''
        self.__page_dict_list = page_dict_list
        
        
    def getNameExtractionTuples(self):
        return self.__nameextractiontuples
        
        
    def getPriceExtractionTuples(self):
        return self.__priceextractiontuples
    
    
    def find_schema_v2(self):
        return self.find_schema()
    
    
    def find_schema(self):        
        '''
        Find schema for name,price,...  retrieval from pages of the site.        
        return list(string), list(PriceNode), boolean, [string, string], string, string
        '''
        #url = self.__get_product_offering_page_url()
        #contents_list = [ self.__get_page_static_content(self.__get_product_offering_page_url()) for i in range(0,self.__sampleset_size) ]
        page_dict_list = self.__page_dict_list
        contents_list = self.__get_pages_static_contents()
        dataset_pages_number = len(page_dict_list)
        
        # non-repeating title words picking
        title_words_perpage = self.__get_nonrepeating_title_words(contents_list)
        
        pricenodes = price_surrounding_str = None
        buttonnode_list, basketnode_list = self.__get_page_nodes_paths()
        
        price_dyn_needed = False
        
        namenode_list = []
        # classify name node
        for i,page_dict in enumerate(page_dict_list):
            namenode_classifier = NameNodeClassifier() 
            if namenode_classifier.classify(page_dict, params=title_words_perpage[i]):
                namenode = namenode_classifier.getNodePath()
                namenode_list.append(namenode)
            else:
                namenode_list.append(None)
        # see if nodes surrounding the first name node have also some name parts of the name
        #namenodes = [namenode]
        
        name_solvers = self.__list_name_solvers(namenode_list)
        
        print"--- namesolvers:"
        for solver in name_solvers:
            print [path for path in solver[0]], solver[1]         
        self.__nameextractiontuples = [(n[0],0,n[1]) for n in name_solvers]
        
#         ''' TODO translate to tuples '''
#             
#         print"--- namenodes:"
#         for i,namenodes in enumerate(namenode_list):
#             for namenode in namenodes:
#                 print namenode
#                 tree = xpathops.content2tree(page_dict_list[i].get("content"))
#                 print tree.xpath(namenode+'/text()')[0]
#             print "---"
#         print "\n"
        
        # classify button node
#         buttonnode_classifier = AddToBasketButtonClassifier()
#         if buttonnode_classifier.classify(primary_content, None):
#             buttonnode = buttonnode_classifier.getNodePath()
#             
        print"--- buttonnodes:"
        for buttonnode in buttonnode_list:
            print buttonnode
        print "\n"
        
        # classify shopping basket node
#         basketnode_classifier = BagLinkClassifier()
#         if basketnode_classifier.classify(primary_content, lang="en", params=None):
#             basketnode = basketnode_classifier.getNodePath()
        
        
        # basket link is a part of the navigation panel, so let's assume that all of the basket nodes should be extracted in the same place
        basketnode_list = self.__eval_basketnodes(basketnode_list)
        print"--- basketnode:"
        for basketnode in basketnode_list:
            print basketnode
        print "\n"
        
        # find price node:
        #   -> standard way
        price_finder = PriceFinder(contents_list, namenode_list, buttonnode_list, basketnode_list)
        pricenodes, is_extraction_sufficient = price_finder.find_price()
        
        self.__priceextractiontuples = []        
        #   -> if no fine candidate found, try other ways
        if pricenodes:            
            self.__priceextractiontuples = [(n[0],0,n[1]) for n in pricenodes]
        elif is_extraction_sufficient != True:
            price_dyn_needed = True
       
        if price_dyn_needed == True:
            pass
#             dyn_contents_list = self.__get_sampleset_dynamic_contents()
#             price_finder = PriceFinder(dyn_contents_list, namenode_list, buttonnode_list, basketnode_list)
#              
#             pricenodes = price_finder.find_price()
#             prices_of_contents_list = [ self.__get_price_str_from_nodes(dyn_contents_list[i], pricenodes) for i in range(0,dataset_pages_number) ]
#                  
#             hidden_price_schema_ext = HiddenPriceSchemaExtractor(dyn_contents_list,prices_of_contents_list)
#             price_surrounding_str = hidden_price_schema_ext.get_strings_surrounding_price()
#              
#             self.__priceextractiontuples.extend([(n[0],1,n[1]) for n in price_surrounding_str])
#             self.__priceextractiontuples.extend([(n[0],2,n[1]) for n in pricenodes])

        return True
    
    
#     def find_schema_old(self):
#         '''
#         Find schema for name,price,...  retrieval from pages of the site
#         '''
#         url = self.__get_product_offering_page_url()
#         code = self.__get_page_static_content(url)
#         
#         
#         
#         standard_ext = StandardNamePriceExtraction(url,code)
#         standard_ext.run()
#         
#         print('static namenodes:',standard_ext.namenodes)
#         print('static pricenodes:',standard_ext.pricenodes)
#         
#         name_nodes = standard_ext.namenodes
#         is_price_nodes = True
#         price_nodes = standard_ext.pricenodes
#         price_surrounding_str = None
#         
#         # if pricenode list empty, it is not filled up in the static HTML
#         if not standard_ext.pricenodes:
#             is_price_nodes = False
#             
#             # get dynamic code in order to find it as it is (close to namenode!)
#             dyn_code = self.__get_page_dynamic_content(url)
#         
#             standard_ext = StandardNamePriceExtraction(url,dyn_code)
#             standard_ext.run()
#             
#             print('dynamic namenodes:',standard_ext.namenodes)
#             print('dynamic pricenodes:',standard_ext.pricenodes)
#             
#             # price_value
#             price_value = self.__get_price_str_from_nodes(dyn_code,standard_ext.pricenodes)
#             
#             pages_code = []
#             pages_price = []
#             
#             # get a few product offering pages of the site
#             pages_code.append(code)
#             pages_price.append(price_value)
#             for i in range(PAGES_FOR_SCHEMA_RETRIEVAL_NO-1):
#                 new_url = self.__get_product_offering_page_url()
#                 new_code = self.__get_page_static_content(new_url)
#                 pages_code.append(new_code)
#                 
#                 new_dyn_code = self.__get_page_dynamic_content(new_url)
#                 
#                 new_price_value = self.__get_price_str_from_nodes(new_dyn_code, standard_ext.pricenodes)
#                 pages_price.append(new_price_value)
#                 
#             hidden_price_schema_ext = HiddenPriceSchemaExtractor(pages_code,pages_price)
#             price_surrounding_str = hidden_price_schema_ext.get_strings_surrounding_price()
#             
#         retr_schema = SiteRetrievingSchema(name_nodes,is_price_nodes,price_nodes,price_surrounding_str)
#         
#         return retr_schema
            
            
    def get_product_name_retrieval_pattern(self):
        return None
    
    
    def get_product_price_retrieval_pattern(self):
        return None
      
        
# private methods


    def __list_name_solvers(self,namenode_list):
        from itertools import groupby
        namenode_list_groups = groupby(sorted(namenode_list))
        namenode_list_solvers = [(n[0], len(list(n[1]))) for n in namenode_list_groups]
        namenode_list_solvers = sorted(namenode_list_solvers, key=lambda n: n[1])
        
        namenode_list_solvers = [n for n in namenode_list_solvers if n[1] > 0.1*len(namenode_list) and n[0] is not None]
        namenode_list_solvers = reversed(namenode_list_solvers)
        return list(namenode_list_solvers)
        #return [(n.key, len(n.groups)) for n in namenode_list_groups]
    
    
    def __get_nonrepeating_title_words(self, contents_list):
        '''
        In the future: store title common part as base_url metadata! :)
        '''
        titles = []
        title = ''
        orig_titles = []
        orig_titles_final = []
        for i in range(0,len(contents_list)):
            content = contents_list[i]
            title = self.__get_title(content)
            #if i==0:    
            #    orig_title = title
            if title is not None:
                titles.append(title)
            orig_titles.append(title)
        common_parts = find_common_patterns_of_all(titles)
        common_parts = sorted(common_parts,key=lambda part: -len(part.split()))
        
        for orig_title in orig_titles:
            if orig_title is not None:
                for part in common_parts:
                    part = re.sub('[\-\|]+', '', part)
                    orig_title = orig_title.replace(part.strip(), '')
                print(orig_title)
                orig_title = orig_title.lower()
                orig_title = re.sub('[\-\|]+', '', orig_title)
                orig_title=list(set(orig_title.split()))
                orig_title = self.__make_unique_list(orig_title)
                print(orig_title)
                orig_titles_final.append(orig_title)
            else:
                orig_titles_final.append('')
        return orig_titles_final
    
    
    def __make_unique_list(self,seq):
        seen = set()
        seen_add = seen.add
        return [ x for x in seq if x not in seen and not seen_add(x)]
            
      
    def __get_title(self, code):
        #f = io.StringIO(unicode(code, 'utf-8').encode('utf-8'))
        self.tree = xpathops.content2tree(code)
        titleElements = self.tree.xpath('/html/head//title/text()')
        if len(titleElements) > 0:
            titleText = titleElements[0].strip()
            titleText = filter(lambda x: x in string.printable, titleText)
            print titleText
        else:
            titleText = None
        return titleText
    
    
#     def __draw_baseurl_sampleset(self):
#         '''
#         get pages, semi-random
#         '''
#         sampleset = []
#         pages_generator = getpages(self.__url+"*",isnorm=False)
#         
#         sampleset_currsize = 0
#         
#         for page in pages_generator:
#             drawn_number = random.randint(0,1)
#             if drawn_number == 1:
#                 sampleset_currsize += 1
#                 sampleset.append(page)
#             if sampleset_currsize == 20:
#                 break
#                 
#         for sample in sampleset:
#             print(sample[0])
#             
#         self.__sampleset = sampleset
        
    
    def __get_page_nodes_paths(self):
        '''
        TODO
        Fresh code, might be wrong
        '''
        buttonnode_list = [] 
        basketnode_list = []
        for page_dict in self.__page_dict_list:
            buttonnode = page_dict["buttonnode"]
            basketnode = page_dict["basketnode"]
            if isinstance(buttonnode, list):
                buttonnode = buttonnode[0]
            if isinstance(basketnode, list):
                basketnode = basketnode[0] 
            buttonnode_list.append(buttonnode)
            basketnode_list.append(basketnode)   
        return buttonnode_list, basketnode_list
    
        
    def __get_pages_static_contents(self):
        contents = []
        for page_dict in self.__page_dict_list:
            contents.append(page_dict.get("content"))
#         for url, data in self.__sampleset:
#             contents.append(data[HBASE_URL_CONTENT_COLUMN])
        return contents
    
    
    def __get_pages_trees(self):
        trees = []
        for page_dict in self.__page_dict_list:
            tree = page_dict.get("tree")
            if tree is None:
                tree = xpathops.content2tree(page_dict.get("content"))
                page_dict["tree"] = tree
            trees.append(tree)
        return trees
    
    
    def __get_sampleset_dynamic_contents(self):
        dyn_contents = []
        for page_dict in self.__page_dict_list:
            url = page_dict.get("url")
            with DynamicCodeRetriever() as dcr:
                content = dcr.get_code(url)
                dyn_contents.append(content)
#         for url, data in self.__sampleset:
#             with DynamicCodeRetriever() as dcr:
#                 content = dcr.get_code(url)
#                 dyn_contents.append(content)
        return dyn_contents
        
    
    def __get_page_dynamic_content(self, url):
        '''
        Of course TODO in a way that Firefox is started only once per execution
        '''
        with DynamicCodeRetriever() as dcr:
            code = dcr.get_code(url)
            return code
        
        
    def __get_price_str_from_nodes(self,code,pricenodes):
        if not pricenodes:
            return None
        
        f = io.StringIO(code)
        tree = html.parse(f)
        
        pricestring = ''
        partstring = ''
        for pricenode in pricenodes:
            pricestringlist = tree.xpath(pricenode.nodepath+'/text()')
            if pricestringlist: partstring = pricestringlist[0]
            if partstring: pricestring += partstring + ' '
    
        return self.__get_price_val_string(pricestring)
        
        
    def __get_price_val_string(self,pricestring):
        matched_price = re.search(price_any_regex,pricestring)
        if matched_price:
            print(matched_price.group())
            return matched_price.group()


    def __eval_basketnodes(self,basketnode_list):
        trees_list = self.__get_pages_trees()
        if len(set(basketnode_list)) <= 1:
            pass # it's ok - all the basket paths the same
        else:
            from collections import Counter
            c = Counter(basketnode_list)
            mostcommon = c.most_common(1)[0]
            if mostcommon[0] is None:
                basketnode_list = [None]*len(basketnode_list)
            else:
                if mostcommon[1] >= 0.5*len(basketnode_list):
                    # change if that basket's node works for other pages or set None there
                    # --- comment: could be changed and set to None in all anyway
                    for i,(tree,path) in enumerate(zip(trees_list,basketnode_list)):
                        if path == mostcommon[0]:
                            continue
                        else:
                            node = tree.xpath(mostcommon[0])
                            if node is not None:
                                # probably basket node, right? :)
                                basketnode_list[i] = mostcommon[0]
                            else:
                                basketnode_list[i] = None
                else:
                    # assumption: random results
                    basketnode_list = [None]*len(basketnode_list)
        
        return basketnode_list

''' --------------------------------- '''

# 
# schemaExtractor = LeadsEcomSiteSchemaExtractor("http://www.zalando.co.uk/")
# schema = schemaExtractor.find_schema()
# print(schema)
# print("\n---\n")




        