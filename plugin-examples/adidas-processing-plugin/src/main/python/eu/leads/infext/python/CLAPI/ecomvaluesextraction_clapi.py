'''
Created on Jul 14, 2014

@author: nonlinear
'''
from eu.leads.infext.python.CLAPI.conversionmethods import translateInputParameters
import sys
import ast
from eu.leads.infext.python.ecom.extract.extractvalues import ExtractValues
from eu.leads.infext.python.ops import xpathops
from eu.leads.infext.python.ecom.extract.verify import defaultverifier

#if __name__ == '__main__':
class AccessPoint:
    def execute(self,params):
        '''
        Parameters:
        1 page content
        2 nameextractiontuples
        3 pricextractiontuples
        
        Returns:
        name
        price_currency
        is_current_price is_lowest_price is_highest_price is_old_price is_vip_price
        (optional) current_price
        (optional) lowest_price
        (optional) highest_price
        (optional) old_price
        (optional) vip_price
        '''
        #sys.path.insert(0,'/home/lequocdo/workspace/leadsdm')
        #print sys.path
        
        params = translateInputParameters(sys.argv)
        
        page_content = params[0]
        page_tree = xpathops.content2tree(page_content)
        page_dict = {"tree":page_tree}
        
        name_extraction_tuples_list = ast.literal_eval(params[1])
        price_extraction_tuples_list = ast.literal_eval(params[2])
        
        values_extractor = ExtractValues(page_dict)
        default_verifier = defaultverifier()
        values_extractor.extract(name_extraction_tuples_list, default_verifier)
        
        
        
        
        
        
        
        
        
        