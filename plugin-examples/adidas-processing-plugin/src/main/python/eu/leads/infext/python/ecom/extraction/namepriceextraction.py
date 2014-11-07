'''
Created on Dec 11, 2013

@author: nonlinear

Works well except two cases:
    - when price is received through AJAX request (how to make crawler get it?)
    - when there is original and discount price, take the smaller one
'''
from lxml import html
import re
from eu.leads.infext.python.ecom.extraction.maxsumseq import maxsumseq, max_sum_subsequence, max_subarray
from eu.leads.infext.python.ecom.dictionaries.currency import price_regex, __currency_regex_symbols,\
    currprice_regex, curr_regex, pricecurr_regex, price_any_regex,\
    curr_findall_regex
from eu.leads.infext.python.ecom.dictionaries.number import number_regex, numberr_regex, numberf_regex
from eu.leads.infext.python.ecom.extraction.isfunction import is_number
from eu.leads.infext.python.ecom.extraction.htmlanalysis import is_link, fix_xpath_errors
from eu.leads.infext.python.ecom.properties import MAX_NAME_PRICE_DISTANCE, MAX_PRICE_PRICE_DISTANCE,\
    MAX_PRICE_NAME_DISTANCE
import io
from eu.leads.infext.python.ops.xpathops import tree2textnodeslist
from eu.leads.infext.python.ops import xpathops
#import nltk


class StandardNamePriceExtraction(object):
    '''
    classdocs
    '''


    def __init__(self, url, code):
        '''
        Constructor
        '''
        self.__code = code
        self.__tree = xpathops.content2tree(code)
                        
                        
# --------------------------------------------------------------------------------- #
        
        
    def __find_cheapest_price(self,pricenodes):
        '''
        Returns paths of those nodes that are about the cheapest price.
        '''
        tree = self.__tree
        prices = []
        curr_pricenodes = []
        nodestrings = []
        for i in range(0,len(pricenodes)):
            nodepath = pricenodes[i]
            nodestring = tree.xpath(nodepath+'/text()')[0].strip()
            nodestrings.append(nodestring)
            matched_price = re.search(price_any_regex,nodestring)
            if matched_price:
                new_price = matched_price.group(0)#str(matched_price)
                new_price = new_price.replace(',','.')
                new_price = new_price.replace('-','')
                print(new_price)
                price_val = float(new_price)
                prices.append(price_val)
            else:
                prices.append(float("inf")) # some nodes will not contain the number
        
        min_price = min(prices)
        correct_price_pos = prices.index(min_price)
        
        curr_pricenodes.append(pricenodes[correct_price_pos])
        
        # Check whether the node of the lowest price contains a currency sign.
        if any(x.replace("\\","") in nodestrings[correct_price_pos] for x in __currency_regex_symbols):
            pass    # Everything's fine.
        else:
            if len(prices)==correct_price_pos+1:
                if prices[correct_price_pos-1]==float("inf"):
                    curr_pricenodes.append(pricenodes[correct_price_pos-1])
            elif 0==correct_price_pos:
                if prices[correct_price_pos+1]==float("inf"):
                    curr_pricenodes.append(pricenodes[correct_price_pos-1])
            else:
                if correct_price_pos%2==0:
                    if prices[correct_price_pos+1]==float("inf"):
                        curr_pricenodes.append(pricenodes[correct_price_pos+1])
                else:
                    if prices[correct_price_pos-1]==float("inf"):
                        curr_pricenodes.append(pricenodes[correct_price_pos-1])  
                        
        return curr_pricenodes
                        
                        
# --------------------------------------------------------------------------------- #
                        
                        
    def __find_prices(self,runno,lastnodepos,nonnumbernodes_count,forwards=True):   
        print(self.__find_prices.__name__)
        
        if runno not in range(1,3):
            return None
        
        text_nodes = self.__text_nodes
        text_nodes_paths = self.__text_nodes_paths
        pos = lastnodepos
        
        pricecurrnodes = []
                        
        howtocontinue = None
        foundprice = None
        maybeprice = None
        
        maybepricecurrnode = None
        
        foundprices = 0
        
        start_pos = None
        end_pos = None
        
        if forwards:
            start_pos = pos+1
            end_pos = len(text_nodes)-1
        else:
            start_pos = pos-1
            end_pos = 0
            
        i = start_pos-1
        
        while i != end_pos:
        #for i in range(pos+1,len(text_nodes)-1):
            if forwards: i += 1
            else: i -= 1
        
            node_string = text_nodes[i].strip()
            node_path = text_nodes_paths[i]
            
            print(nonnumbernodes_count,":",node_path,":","<<"+node_string+">>")
            
            if node_string:
                if runno==1:
                    if not re.match(numberr_regex, node_string) and not re.match(numberf_regex, node_string):
                        nonnumbernodes_count -= 1
                elif runno==2:
                    nonnumbernodes_count -= 1 
            
            if howtocontinue:
                if howtocontinue == 'find_curr' and re.match(curr_regex, node_string):
                    foundprice = maybeprice
                    pricecurrnodes.append(node_path)
                    pricecurrnodes.append(maybepricecurrnode)
                elif howtocontinue == 'find_price' and re.match(price_regex, node_string):
                    foundprice = node_string
                    pricecurrnodes.append(node_path)
                    pricecurrnodes.append(maybepricecurrnode)
                elif not node_string:
                    # if empty, try the same with the next node
                    continue
                else:
                    # if sth else, continue normally
                    howtocontinue = None
                    continue
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
                    pricecurrnodes.append(node_path)
                    foundprice = node_string   
                    
            if foundprice:   
                if runno==1:
                    other_pricecurrnodes = self.__find_prices(2, i, MAX_PRICE_PRICE_DISTANCE, forwards)
                    if other_pricecurrnodes[0]:
                        pricecurrnodes.extend(other_pricecurrnodes[0])
                    foundprices = 1 + other_pricecurrnodes[1]
                if runno==2:
                    foundprices = 1
                
                return [pricecurrnodes,foundprices]   
            
            if nonnumbernodes_count <= 0:
                if forwards:
                    return self.__find_prices(runno, lastnodepos, MAX_PRICE_NAME_DISTANCE if runno==1 else MAX_PRICE_PRICE_DISTANCE, False)
                else:
                    return[None,0]
                        
                        
# --------------------------------------------------------------------------------- #
      
      
    def __find_price_further(self,namenode):
        ''' 
        Iterate text nodes behind the products name node (almost) as long as you find the string of a price.
        Return: list of price nodes.
        '''
        print(self.__find_price_further.__name__)
        
        pos = self.__text_nodes_paths.index(namenode)
            
        found_nodes = self.__find_prices(1, pos, MAX_NAME_PRICE_DISTANCE)
        pricecurrnodes = found_nodes[0]
        is_more_prices = found_nodes[1]
        
        # if there are a few price nodes, choose the one with the lowest price as the correct one!      
        if is_more_prices:
            self.__find_cheapest_price(pricecurrnodes)
        
        return pricecurrnodes
                        
                        
# --------------------------------------------------------------------------------- #


    def __find_name_price_sequence(self,word_nodes,title_words,word_nodes_parent_type):
        word_values = []
        for i in range(0,len(word_nodes)):
            x = word_nodes[i]
            if any(x.lower() == val.lower() for val in title_words):
                word_title_pos = title_words.index(x.lower()) # to be done case insensitively
                appendval = 0
                if word_title_pos != 0 and word_nodes[i-1].lower() == title_words[word_title_pos-1]:
                    if word_nodes_parent_type[i] == True:
                        appendval = -1
                    else:
                        appendval = word_values[-1]+1
                else:
                    if word_nodes_parent_type[i] == True:
                        appendval = -1
                    else:
                        appendval = 4
                '''if word_title_pos != len(title_words)-1 and word_nodes[i+1].lower() == title_words[word_title_pos+1]:
                    appendval += 1'''
                word_values.append(appendval)
            elif re.match(price_regex,x):
                if word_nodes[i-1] in __currency_regex_symbols or word_nodes[i+1] in __currency_regex_symbols:
                    if word_nodes_parent_type[i] == True:
                        word_values.append(-1)
                    else:
                        word_values.append(12)
                else:
                    word_values.append(0)
            elif x in __currency_regex_symbols:
                if word_nodes_parent_type[i] == True:
                    word_values.append(-1)
                else:
                    word_values.append(1)
            elif re.match(currprice_regex,x) or re.match(pricecurr_regex,x):
                if word_nodes_parent_type[i] == True:
                    word_values.append(-1)
                else:
                    word_values.append(13)
            elif self.__is_price_word(x):
                word_values.append(4)
            else:
                word_values.append(-3)
        return maxsumseq(word_values)


# --------------------------------------------------------------------------------- #


    def __is_price_word(self,word):
        ''' the goal: check language. if English, look for the word: price '''
        if 'price' in word.lower():
            return True
        else:
            return False


# --------------------------------------------------------------------------------- #
        
        
    def run(self):
        tree = self.__tree
        
        # take the title
        title = tree.xpath('/html/head/title/text()')[0].strip()
        print(title)
        
        # split the title into words
        title_words_orig = title.split()
        title_words = []
        title_words.extend(x.lower() for x in title_words_orig[:])
        print(title_words)
        
        # TODO: remove the common part of a title for the domain
        pass
        
        # TODO maybe: boilerplate recognition ;)
        pass
                
        # get a list of all nonempty strings from body (except scripts)
        text_nodes, text_nodes_paths = tree2textnodeslist(tree)
        
        word_nodes = []
        word_nodes_parent_type = []
        word_nodes_paths = []
        for i in range(0,len(text_nodes)-1):
            x = text_nodes[i].strip()
            #y = nltk.word_tokenize(x)
            y = x.split()
            word_nodes.extend(y)
            text_nodes_paths[i] = fix_xpath_errors(text_nodes_paths[i])
            isparentlink = is_link(text_nodes_paths[i])
            for j in range(0,len(y)):
                word_nodes_paths.append(text_nodes_paths[i])
                word_nodes_parent_type.append(isparentlink)
                
        self.__text_nodes = text_nodes
        self.__text_nodes_paths = text_nodes_paths
          
        # find the sequence that at most probably is the name and the price
        firstlastword = self.__find_name_price_sequence(word_nodes, title_words, word_nodes_parent_type)        
        
        # find nodes that contain the found sequence
        namepricenodes = []
        namepricenodesstrings = []
        for i in range(firstlastword[0],firstlastword[1]):
            namepricenodes.append(word_nodes_paths[i])
            #print(word_nodes_paths[i], word_nodes[i])
            
#         for i in range(firstlastword[0]-10,firstlastword[1]+10):
#             print(word_nodes[i],word_values[i])
            
        namepricenodes = list(set(namepricenodes))
        for x in namepricenodes:
            print(x)
            namepricenodesstrings.append(tree.xpath(x+'/text()')[0].strip())
            print(x, namepricenodesstrings[-1])
        
        namenodes = []
        pricenodes = []
        
        pricesfound = 0
        i = 0
        # pair the information with the node
        for i in range(0,len(namepricenodes)):
            node = namepricenodesstrings[i]
            nodewords = node.split()
            if (set(nodewords) & set(title_words_orig)):
                namenodes.append(namepricenodes[i])
                print('product name (part):',node)
            else:
                if (set(nodewords) & set(__currency_regex_symbols)):
                    print('currency:',node)
                    pricenodes.append(namepricenodes[i])
                for x in nodewords:
                    if(is_number(x)):
                        print('price:',node)
                        pricenodes.append(namepricenodes[i])
                        pricesfound += 1
                        break
                    if re.match(pricecurr_regex,x) or re.match(currprice_regex,x):
                        print('currency & price:',node)
                        pricenodes.append(namepricenodes[i]) 
                        pricesfound += 1
                        
        # If the one price is found, take a look if it's not followed by some other prices
        if pricesfound==1:  
            other_pricecurrnodes = self.__find_prices(2, i, MAX_PRICE_PRICE_DISTANCE)
            if other_pricecurrnodes[0]:
                pricenodes.extend(other_pricecurrnodes[0])
            pricesfound += other_pricecurrnodes[1]
                     
        # If there are a few price nodes, choose the one with the lowest price as the correct one!      
        if pricesfound>1:
            pricenodes = self.__find_cheapest_price(pricenodes)
            
        print('product name:' + str(namenodes))
        print('product price:' + str(pricenodes))
                    
        # if pricenode list empty - look for it somewhere further
        if not pricenodes: # = is empty?
            pricenodes = self.__find_price_further(namenodes[0])
        
        self.namenodes = namenodes
        self.pricenodes = pricenodes
        
        return [namenodes,pricenodes]
    
 
        
        
        
        
