'''
Created on Jul 14, 2014

@author: nonlinear
'''
import re
from eu.leads.infext.python.ecom.dictionaries import currency
from eu.leads.infext.python.ecom.extract.mine.abstractminer import AbstractMiner

class DefaultPriceMiner(AbstractMiner):
    '''
    The only good way to do this mining is during the batch process - get the whole text of the parent node combined,
    look for hyphens, words, and CSS STYLE of words (size, strikethrough, bold)
    '''


    def __init__(self):
        self.currency = None
        self.lowest_price = None
        self.highest_price = None
        
        
    def extractWhatMattersAndNameIt(self,textnodes):
        self.__extractPricesFromText(textnodes)
        
        return [("ecom_prod_price_low",self.lowest_price),
                ("ecom_prod_price_high",self.highest_price),
                ("ecom_prod_currency",self.currency)]
    
    
    def __extractPricesFromText(self,textnodes):
        '''
        textnodes - [(text,attributes)]
        '''
        
        # Convert to list [(word,attributes)]
        wordnodes = self.__converttowordattrslist(textnodes)
        
        # LXML how to get node style? (przekreslenie...)
        # Use CSS Utils! -> TODO in my next life
        
        allclear = False
        
        # get list of words with price
        words_with_price = self.__createListOfWordsWithPrice(wordnodes)
        
        # get list of words with currency symbol
        words_with_currency =  self.__createListOfWordsWithCurrency(wordnodes)
        
        # check if the same length of lists
        if len(words_with_price) == len(words_with_currency):
            allclear = True
        else:
            # throw away numbers that have different structure that others
            words_with_price = self.__removeWordsWithNoPrice(words_with_price)
            allclear = True
        
        number_of_prices = len(words_with_price)
        
        if number_of_prices > 0:
            prices_values = self.__extractpricesvalues(words_with_price)
            sorted_prices_values = sorted(prices_values)
    
            # One exception: Three prices
            if number_of_prices == 3 and abs(sorted_prices_values[0]+sorted_prices_values[1]-sorted_prices_values[-1]) < 0.1:
                self.lowest_price = sorted_prices_values[1]
                self.highest_price = sorted_prices_values[-1]
            else:
                self.lowest_price = sorted_prices_values[0]
                self.highest_price = sorted_prices_values[-1]
            
        currency_values = self.__extractcurrencyvalues(words_with_currency)
        # just keep it simple
        self.currency = currency_values[0] if len(currency_values)>0 else None
            
    
    def __converttowordattrslist(self,textnodes):
        wordnodes = []
        for textnode in textnodes:
            splittedtext = textnode[0].split()
            for word in splittedtext:
                wordnodes.append((word,textnode[1]))
        return wordnodes
      
      
    def __createListOfWordsWith(self,wordnodes,regex):
        words_with = []
        for i,word in enumerate(wordnodes):
            matched = re.search(regex, word[0])
            if matched:
                words_with.append(word)
        return words_with         
    
    def __createListOfWordsWithPrice(self,wordnodes):
        return self.__createListOfWordsWith(wordnodes, currency.price_any_regex)
    
    def __createListOfWordsWithCurrency(self,wordnodes):
        return self.__createListOfWordsWith(wordnodes, currency.curr_any_regex)
        
    
    def __removeWordsWithNoPrice(self,words_with_price):
        features_per_word = []
        indices_for_removal = []
        for i,word in enumerate(words_with_price):
            matched = re.search(currency.price_any_regex, word[0])     
            if matched:
                new_price = matched.group(0)
                feature1 = None
                feature2 = 0
                index = None
                if '.' in new_price:
                    feature1 = '.'
                    index = new_price.index('.')
                elif ',' in new_price:
                    feature1 = ','
                    index = new_price.index(',')
                else:
                    feature1 = ''
                if index is not None:
                    feature2 = len(new_price)-index
                features_per_word.append((feature1,feature2))
        from collections import Counter
        counter = Counter(features_per_word).most_common()
        if len(set(x[1] for x in counter)) == 1: # if of each there is the same number
            # sort features per word
            max_feature2 = max(x[1] for x in features_per_word)
            new_words_with_price = []
            for i,word_feat in enumerate(features_per_word):
                if word_feat[1] == max_feature2:
                    new_words_with_price.append(words_with_price[i])
            words_with_price = new_words_with_price            
        else:
            for i,elem in enumerate(counter):
                if i>0:
                    indices = [i for i, x in enumerate(features_per_word) if x == elem[0]]
                    indices_for_removal.extend(indices)
            indices_for_removal = sorted(indices_for_removal,reverse=True)
        for i in indices_for_removal:
            words_with_price.pop(i)
        return words_with_price
        
        
    def __extractpricesvalues(self, words_with_price):
        values = []
        for word in words_with_price:
            val = re.search(currency.price_any_regex,word[0]).group(0)
            values.append(float(val))
        return values
        
        
    def __extractcurrencyvalues(self, words_with_currency):
        values = []
        if words_with_currency is not None:
            for word in words_with_currency:
                values.append(re.search(currency.curr_any_regex,word[0]).group(0))
            for i,val in enumerate(values):
                values[i] = currency.currency_symbols_map.get(val)
        return values
        
        
if __name__ == '__main__':
    params = [(u'New: '+unichr(163)+'14.99',None),(u'Before: '+unichr(163)+'18.99',None),(u'you save '+unichr(163)+'5.00! (30%)',None)]    
    miner = DefaultPriceMiner()
    miner.extractWhatMattersAndNameIt(params)  
    print miner.currency, miner.lowest_price, miner.highest_price  
        
    
        
        
        
        
        