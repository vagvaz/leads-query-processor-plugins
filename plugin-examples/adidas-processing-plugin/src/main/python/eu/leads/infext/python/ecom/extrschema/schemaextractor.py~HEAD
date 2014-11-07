'''
Created on Jan 9, 2014

@author: nonlinear
'''

from org.leads.root.sellenium.dynamiccoderetriever import DynamicCodeRetriever
from org.leads.root.ecom.extraction.namepriceextraction import StandardNamePriceExtraction
import requests
import io
from org.leads.root.ecom.properties import PAGES_FOR_SCHEMA_RETRIEVAL_NO,\
    PAGES_TO_FIND_REPEATING_TITLE_PART
from lxml import html
import re
from org.leads.root.ecom.dictionaries.number import numberr_any_regex
from org.leads.root.ecom.dictionaries.currency import price_any_regex
from org.leads.root.ecom.extrschema.hiddenpriceschemaextractor import HiddenPriceSchemaExtractor
from org.leads.root.ecom.extrschema.structures.structures import SiteRetrievingSchema
from org.leads.root.ecom.classifier.namenodeclassifier import NameNodeClassifier
from org.leads.root.ecom.classifier.addtobagbuttonclassifier import AddToBasketButtonClassifier
from org.leads.root.ecom.extrschema.pricefinder import PriceFinder
from org.leads.root.ecom.extrschema.ops.stringops import find_common_patterns_of_all
from org.leads.root.ecom.extrschema.ops import xpathops
from org.leads.root.comm.urltranslate import url_base
from org.leads.root.ecom.classifier.baglinkclassifier import BagLinkCandidate,\
    BagLinkClassifier
from org.leads.root.comm.connection import getpages
import random
from org.leads.root.comm.hbasebigtable import HBASE_URL_CONTENT_COLUMN

class LeadsEcomSiteSchemaExtractor(object):
    '''
    classdocs
    '''


    def __init__(self, url, sampleset_size=10):
        '''
        Constructor
        '''
        self.__base_url = url_base(url)
        self.__url = url
        self.__temp = 0
        self.__sampleset_size = sampleset_size
        
        self.__draw_baseurl_sampleset()
    
    
    def find_schema_v2(self):
        return self.find_schema()
    
    
    def find_schema(self):        
        '''
        Find schema for name,price,...  retrieval from pages of the site.        
        return list(string), list(PriceNode), boolean, [string, string], string, string
        '''
        #url = self.__get_product_offering_page_url()
        #contents_list = [ self.__get_page_static_content(self.__get_product_offering_page_url()) for i in range(0,self.__sampleset_size) ]
        contents_list = self.__get_sampleset_static_contents()
        primary_content = contents_list[0]
        
        # non-repeating title words picking
        title_words = self.__get_nonrepeating_title_words(contents_list)
        
        namenode = buttonnode = basketnode = pricenodes = price_surrounding_str = None
        price_dyn_needed = False
        
        # classify name node
        namenode_classifier = NameNodeClassifier() 
        if namenode_classifier.classify(primary_content, params=title_words):
            namenode = namenode_classifier.getNodePath()
        # see if nodes surrounding the first name node have also some name parts of the name
        namenodes = [namenode]
            
        print("--- namenode:",namenode)
        
        # classify button node
        buttonnode_classifier = AddToBasketButtonClassifier()
        if buttonnode_classifier.classify(primary_content, None):
            buttonnode = buttonnode_classifier.getNodePath()
            
        print("--- buttonnode:",buttonnode)
        
        # classify shopping basket node
        basketnode_classifier = BagLinkClassifier()
        if basketnode_classifier.classify(primary_content, lang="en", params=None):
            basketnode = basketnode_classifier.getNodePath()
            
        print("--- basketnode:",basketnode)
        
        # find price node:
        #   -> standard way
        price_finder = PriceFinder(contents_list, namenode, buttonnode, basketnode)
        pricenodes = price_finder.find_price()
        #   -> if no fine candidate found, try other ways
        if not pricenodes:
            price_dyn_needed = True
            
            #dyn_contents_list = [ self.__get_page_dynamic_content(self.__get_product_offering_page_url()) for i in range(0,self.__sampleset_size) ]
            dyn_contents_list = self.__get_sampleset_dynamic_contents()
            price_finder = PriceFinder(dyn_contents_list, namenode, buttonnode, basketnode)
            
            pricenodes = price_finder.find_price()
            prices_of_contents_list = [ self.__get_price_str_from_nodes(dyn_contents_list[i], pricenodes) for i in range(0,self.__sampleset_size) ]
                
            hidden_price_schema_ext = HiddenPriceSchemaExtractor(dyn_contents_list,prices_of_contents_list)
            price_surrounding_str = hidden_price_schema_ext.get_strings_surrounding_price()
        
        print("--- price nodes:",pricenodes)
        #print("--- priceval:",xpathops.parentpath2nodetext(xpathops.code2tree(primary_content), pricenode))
  
        # return list(string), list(PriceNode), boolean, [string, string], string, string
        return namenodes, pricenodes, price_dyn_needed, price_surrounding_str, buttonnode, basketnode
    
    
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
#             dyn_code = self.__get_page_dynamic_code(url)
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
#                 new_dyn_code = self.__get_page_dynamic_code(new_url)
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
    
    
    def __get_nonrepeating_title_words(self, contents_list):
        '''
        In the future: store title common part as base_url metadata! :)
        '''
        titles = []
        title = ''
        orig_title = ''
        for i in range(0,PAGES_TO_FIND_REPEATING_TITLE_PART):
            content = contents_list[i]
            title = self.__get_title(content)
            if i==0:    
                orig_title = title
            titles.append(title)
        common_parts = find_common_patterns_of_all(titles)
        for part in common_parts:
            part = re.sub('[\-\|]+', '', part)
            orig_title = orig_title.replace(part.strip(), '')
        print(orig_title)
        orig_title = orig_title.lower()
        orig_title = re.sub('[\-\|]+', '', orig_title)
        orig_title=list(set(orig_title.split()))
        orig_title = self.__make_unique_list(orig_title)
        print(orig_title)
        return orig_title
    
    
    def __make_unique_list(self,seq):
        seen = set()
        seen_add = seen.add
        return [ x for x in seq if x not in seen and not seen_add(x)]
            
      
    def __get_title(self, code):
        #f = io.StringIO(unicode(code, 'utf-8').encode('utf-8'))
        self.tree = xpathops.code2tree(code)
        return self.tree.xpath('/html/head/title/text()')[0].strip()
    
    
    def __draw_baseurl_sampleset(self):
        '''
        get pages, semi-random
        '''
        sampleset = []
        pages_generator = getpages(self.__url+"*",isnorm=False)
        
        sampleset_currsize = 0
        
        for page in pages_generator:
            drawn_number = random.randint(0,1)
            if drawn_number == 1:
                sampleset_currsize += 1
                sampleset.append(page)
            if sampleset_currsize == 20:
                break
                
        for sample in sampleset:
            print(sample[0])
            
        self.__sampleset = sampleset
        
        
    def __get_sampleset_static_contents(self):
        contents = []
        for url, data in self.__sampleset:
            contents.append(data[HBASE_URL_CONTENT_COLUMN])
        return contents
    
    
    def __get_sampleset_dynamic_contents(self):
        dyn_contents = []
        for url, data in self.__sampleset:
            with DynamicCodeRetriever() as dcr:
                content = dcr.get_code(url)
                dyn_contents.append(content)
        return dyn_contents
    
    
#     def __get_product_offering_page_url(self):
#         '''
#         get some page with a base URL with product offering (?),
#         store on a list so that the next time, another is taken (or sth like that)
#         '''
#         
#         # first: find pages of the url_base classified as offering product
#         
#         
#         self.__temp += 1
#         # return "http://www.zalando.co.uk/nike-performance-flex-2013-run-lightweight-running-shoes-black-n1241a08r-q13.html"
#         if self.__temp == 1:
#             #return "http://www.next.co.uk/x535656s6"
#             return "http://www.zalando.co.uk/adidas-performance-studio-pure-seamless-tights-grey-ad541b1hr-102.html"
#             return "http://www.zappos.com/adidas-running-marathon-10-ng~2"
#             return "http://www.adidas.com/us/product/mens-running-supernova-glide-6-shoes/IET69"
#             return "http://www.footlocker.com/product/model:157341/sku:60883011/"
#         elif self.__temp == 2:
#             #return "http://www.next.co.uk/x532512s3"
#             return "http://www.zalando.co.uk/odlo-vest-pink-od141b01c-402.html"
#             return "http://www.zappos.com/under-armour-tech-s-s-tee-asphalt-heather-white"
#             return "http://www.adidas.com/us/product/mens-soccer-adizero-f50-trx-fg-messi-cleats/AU179"
#             return "http://www.footlocker.com/product/model:200716/sku:66519/"
#         elif self.__temp == 3:
#             return "http://www.zalando.co.uk/casall-essentials-basic-t-shirt-black-c4441b05i-q11.html"
#         elif self.__temp == 4:
#             return "http://www.zalando.co.uk/nike-performance-new-nike-pro-bra-sports-bra-yellow-ni141b01n-212.html"
#         elif self.__temp == 5:
#             return "http://www.zalando.co.uk/odlo-logo-line-vest-white-od141b0cv-002.html"
#         elif self.__temp == 6:
#             return "http://www.zalando.co.uk/asics-gel-zaraca-2-trainers-green-as141a06e-m11.html"
#         elif self.__temp == 7:
#             return "http://www.zalando.co.uk/adidas-performance-sq-sports-shirt-purple-ad541b1me-404.html"
#         elif self.__temp == 8:
#             return "http://www.zalando.co.uk/asics-skapri-tights-black-as141e00k-q11.html"
#         elif self.__temp == 9:
#             return "http://www.zalando.co.uk/asics-gel-kinsei-5-cushioned-running-shoes-pink-as141a05u-j11.html"
#         else:
#             #return "http://www.next.co.uk/x535332s1"
#             return "http://www.zalando.co.uk/reebok-carthage-3-0-cushioned-running-shoes-white-re541a04i-a11.html"
#             return "http://www.zappos.com/product/8075845/color/391080"
#             return "http://www.adidas.com/us/product/womens-originals-graphic-tee/AMT72?cid=F79347"
#             return "http://www.footlocker.com/product/model:179288/sku:1390559/"
            
    
    def __get_page_static_content(self, url):
        code = requests.get(url).text
        #code = io.StringIO('../extraction/webpage_ade.html').read()
        #with open ('../extraction/webpage_ade.html', "r") as myfile:
        #    code=myfile.read()
            #print(code)
            #return code
        return code
    
    
    def __get_page_dynamic_code(self, url):
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


''' --------------------------------- '''

# 
# schemaExtractor = LeadsEcomSiteSchemaExtractor("http://www.zalando.co.uk/")
# schema = schemaExtractor.find_schema()
# print(schema)
# print("\n---\n")




        