'''
Created on Jan 24, 2014

@author: nonlinear
'''

from org.leads.root.ecom.dictionaries.currency import price_regex, currency_symbols,\
    currprice_regex, curr_regex, pricecurr_regex, price_any_regex,\
    curr_findall_regex
from org.leads.root.ecom.dictionaries.number import number_regex, numberr_regex, numberf_regex
import re
from org.leads.root.ecom.properties import MAX_PRICE_PRICE_DISTANCE,\
    MAX_NAME_PRICE_DISTANCE, MAX_PRICE_NAME_DISTANCE, MAX_BUTTON_PRICE_DISTANCE,\
    MAX_PRICE_BUTTON_DISTANCE
import org.leads.root.ecom.extrschema.ops.xpathops
from org.leads.root.ecom.extrschema.ops import xpathops

class PriceFinder(object):
    '''
    classdocs
    '''


    def __init__(self, code, namenode, buttonnode):
        '''
        Constructor
        '''
        self.__code = code
        self.__tree = tree = xpathops.code2tree(code)
        self.__text_nodes, self.__text_nodes_paths = xpathops.tree2textnodeslist(tree)
        
        self.__namenode = namenode
        self.__buttonnode = buttonnode
        
    
    def __find_best_looking_price(self,pricenodes):
        return self.__find_cheapest_price(pricenodes)
    
    
    def __find_cheapest_price(self,pricenodes):
        '''
        Returns paths of those nodes that are about the cheapest price.
        '''
        tree = self.__tree
        prices = []
        nodestrings = []
        
        if len(pricenodes) == 1:
            return pricenodes[0]
        
        for i in range(0,len(pricenodes)):
            nodepaths = pricenodes[i]
            if type(nodepaths)==str:
                nodepaths = [nodepaths]
            nodestring = ''
            for nodepath in nodepaths:
                nodestring += tree.xpath(nodepath+'/text()')[0].strip()
            nodestrings.append(nodestring)
            matched_price = re.search(price_any_regex,nodestring)
            if matched_price:
                new_price = matched_price.group(0)#str(matched_price)
                new_price = new_price.replace(',','.')
                new_price = new_price.replace('-','')
                print(new_price)
                price_val = float(new_price)
                if price_val > 0.0:
                    prices.append(price_val)
                else:
                    prices.append(float("inf"))
            else:
                prices.append(float("inf")) # some nodes will not contain the number
        
        min_price = min(prices)
        correct_price_pos = prices.index(min_price)
        
        cheapestprice_nodes = pricenodes[correct_price_pos]
        
        nodepaths = cheapestprice_nodes
        if type(nodepaths)==str:
            nodepaths = [nodepaths]
        nodestring = ''
        for nodepath in nodepaths:
            nodestring += tree.xpath(nodepath+'/text()')[0].strip()
        print(nodestring)
        
        # Check whether the node of the lowest price contains a currency sign.
#         if any(x.replace("\\","") in nodestrings[correct_price_pos] for x in currency_symbols):
#             pass    # Everything's fine.
#         else:
#             if len(prices)==correct_price_pos+1:
#                 if prices[correct_price_pos-1]==float("inf"):
#                     curr_pricenodes.append(pricenodes[correct_price_pos-1])
#             elif 0==correct_price_pos:
#                 if prices[correct_price_pos+1]==float("inf"):
#                     curr_pricenodes.append(pricenodes[correct_price_pos-1])
#             else:
#                 if correct_price_pos%2==0:
#                     if prices[correct_price_pos+1]==float("inf"):
#                         curr_pricenodes.append(pricenodes[correct_price_pos+1])
#                 else:
#                     if prices[correct_price_pos-1]==float("inf"):
#                         curr_pricenodes.append(pricenodes[correct_price_pos-1])  
                        
        return cheapestprice_nodes
    
    
    def __is_regular_text_node(self,node_path,node_string):
        if not node_string:
            return False
        if "/li" in node_path:
            return False
        if "/select" in node_path:
            return False
        else:
            return True
    
    
    def __find_around(self,node,distance,is_forward):
        
        text_nodes = self.__text_nodes
        text_nodes_paths = self.__text_nodes_paths
        pos = xpathops.getposintextnodeslist(self.__tree,text_nodes_paths, node)
        nonnumbernodes_count = distance
        
        pricecurrnodes = []
                        
        howtocontinue = None
        foundprice = None
        maybeprice = None
        
        maybepricecurrnode = None
        
        foundprices = 0
        
        start_pos = None
        end_pos = None
        
        if is_forward==True:
            start_pos = pos+1
            end_pos = len(text_nodes)-1
        else:
            start_pos = pos-1
            end_pos = 0
            
        i = start_pos-1
        
        while i != end_pos:
            if is_forward: i += 1
            else: i -= 1
        
            node_string = text_nodes[i].strip()
            node_path = text_nodes_paths[i]
            
            if self.__is_regular_text_node(node_path,node_string):
                nonnumbernodes_count -= 1 
            else:
                continue
            
            print(nonnumbernodes_count,":",node_path,":","<<"+node_string+">>")
            
            if howtocontinue:
                if howtocontinue == 'find_curr' and re.match(curr_regex, node_string):
                    foundprice = maybeprice
                    # pricecurrnodes.append(node_path)
                    # pricecurrnodes.append(maybepricecurrnode)
                    pricecurrnodes.append((node_path,maybepricecurrnode))
                    print(foundprice,node_string)
                elif howtocontinue == 'find_price' and re.match(price_regex, node_string):
                    foundprice = node_string
                    # pricecurrnodes.append(node_path)
                    # pricecurrnodes.append(maybepricecurrnode)
                    pricecurrnodes.append((node_path,maybepricecurrnode))
                    print(foundprice,node_string)
                else:
                    # if sth else, continue normally
                    howtocontinue = None
                    # continue
            else:
                if re.match(price_regex,node_string):
                    # if fits price, look for currency in the next nodes
                    howtocontinue = 'find_curr'
                    maybeprice = node_string
                    maybepricecurrnode = node_path
                elif re.match(curr_regex, node_string):
                    # if fits currency, look for price in the next nodes
                    howtocontinue = 'find_price'
                    maybepricecurrnode = node_path
                elif re.match(currprice_regex, node_string) or re.match(pricecurr_regex, node_string):
                    # if fits both, PROBABLY this is it
                    pricecurrnodes.append((node_path))
                    foundprice = node_string   
                    print(foundprice)
                    
            if foundprice:
                foundprices += 1  
                foundprice = None
                maybeprice = None
                howtocontinue = None
                nonnumbernodes_count = max(nonnumbernodes_count,MAX_PRICE_PRICE_DISTANCE)
                #print("found by now:",pricecurrnodes)
            
            if nonnumbernodes_count <= 0:
                print("FOUND HERE:",pricecurrnodes)
                return pricecurrnodes
            
        return []
    
    
    def find_price(self):
        price_candidates = []
        if self.__namenode:
            price_candidates.extend(self.__find_around(self.__namenode, MAX_NAME_PRICE_DISTANCE, True))
            price_candidates.extend(self.__find_around(self.__namenode, MAX_PRICE_NAME_DISTANCE, False))
        if self.__buttonnode:
            price_candidates.extend(self.__find_around(self.__buttonnode, MAX_BUTTON_PRICE_DISTANCE, True))
            price_candidates.extend(self.__find_around(self.__buttonnode, MAX_PRICE_BUTTON_DISTANCE, False))
        
        print(price_candidates)
        
        # evaluate price candidates
        if price_candidates:
            pricenode = self.__find_best_looking_price(price_candidates)
            return pricenode
        
        
        
        
        
        
        