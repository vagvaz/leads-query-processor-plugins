'''
Created on Jan 9, 2014

@author: nonlinear
'''

from selenium import webdriver
from selenium.webdriver.common.keys import Keys
import os

class DynamicCodeRetriever(object):
    '''
    classdocs
    '''

    def __enter__(self):
        path = os.path.dirname(os.path.realpath(__file__)) + '/bin/phantomjs'
        print path
        self.driver = webdriver.PhantomJS(path)
        #self.driver = webdriver.Firefox()
        return self
        
    def get_code(self, url):
        self.driver.get(url)
        page_source = self.driver.page_source
        return page_source
        
    def __exit__(self, type, value, traceback):
        self.driver.close()
      
      
      
      
      
if __name__ == '__main__':          
    with DynamicCodeRetriever() as dcr:
        dcr.get_code("http://www.adidas.com/us/product/mens-running-supernova-glide-6-shoes/IET69")