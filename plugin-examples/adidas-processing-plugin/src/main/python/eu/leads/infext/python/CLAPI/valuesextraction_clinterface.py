#!/usr/bin/env python
# -*- coding: utf-8 -*-
'''
Created on Jul 20, 2014

@author: nonlinear
'''
from eu.leads.infext.python.CLAPI.conversionmethods import translateInputParameters,\
    translateReturnValues, NullDevice, processing_on, processing_off
import sys
import ast
from eu.leads.infext.python.ecom.extract.extractvalues import ExtractValues
from eu.leads.infext.python.ops import xpathops
from eu.leads.infext.python.ecom.extract.verify import defaultverifier
from eu.leads.infext.python.ecom.extract import extracthelpersfactory
import requests
import lxml.html


# content = requests.get('http://www.next.co.uk/x54490s4#878840x54').text
# extobject = "ecom_product_price"
# exttuples = [(['/html/body/section[1]/section[1]/div[1]/div[3]/div[1]/section[2]/article[1]/section[2]/div[1]'],0)]
# params = [content,extobject,exttuples]

#if __name__ == '__main__':
class AccessPoint:
    def execute(self,params):
        '''
        Parameters:
        1 pagecontent
        2 objectofextraction ("ecom_product_name","ecom_product_price")
        3 extractiontuples
        
        Returns:
        successfulextractiontuple
        [extractedobjname
        extractedobjvalue]
        ...
        '''
        
        #params = translateInputParameters(sys.argv)
        #processing = processing_on()
        
        page_content = params[0]
        page_tree = xpathops.content2tree(page_content)
    #    page_content = page_content.replace(unichr(163),'$')
    #     print lxml.html.tostring(page_tree)
    #     print page_content
    #     print page_tree.getpath(page_tree.xpath(u"//*[contains(.,'25')]")[-1])
    #     print page_tree.xpath('/html/body/section[7]/div[1]//text()')
        page_dict = {"tree":page_tree}
        
        extraction_obj_name = params[1]
        extraction_tuples_list = params[2]
        if type(extraction_tuples_list) is tuple:
            extraction_tuples_list = [extraction_tuples_list]       
        
        returnlist = []
        
        if len(extraction_tuples_list) > 0:
        
            values_extractor = ExtractValues(page_dict)
            verifier = extracthelpersfactory.verifierFactoryMethod(extraction_obj_name)
            miner = extracthelpersfactory.minerFactoryMethod(extraction_obj_name)
            
            describedvalues_list, successful_extraction_tuple = values_extractor.extract(extraction_tuples_list, verifier, miner)
            
            returnlist.append(successful_extraction_tuple)
            for nameval in describedvalues_list:
                returnlist.append(nameval[0])
                returnlist.append(nameval[1])
            
        # processing_off(processing)
        # print translateReturnValues(returnlist)
        return translateReturnValues(returnlist)
        
        