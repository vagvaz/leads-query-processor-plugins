'''
Created on May 22, 2014

@author: root
'''
from eu.leads.infext.python.comm.curl.pagecontentdownloader import downloadstaticcontent
from lxml import etree
from eu.leads.infext.python.comm import urltranslate

class GoogleNewsFeedsChecker(object):
    '''
    classdocs
    '''


    def __init__(self):
        self.__urltempl = "https://news.google.com/news/feeds?output=rss&q=%(domain)s"
        self.__nodespath = "/rss/channel/item/link/text()"
        
        self.__beforenewslink = "url="
        self.__afternewslink = "&"
    
    
    def isnewsdomain(self, domainname):
        
        url = self.__urltempl % {"domain" : domainname}
        
        xmlstr = downloadstaticcontent(url)
        if xmlstr is None:
            # TODO log that sth changed in Google News and needs to be changed
            pass
            return None
        print xmlstr
        xmltree = etree.fromstring(xmlstr)
        
        gnewslinks = xmltree.xpath(self.__nodespath)
        
        for link in gnewslinks:
            newssitelink = self.cutnewssitelink(link)
            if urltranslate.net_loc(newssitelink) == domainname:
                return True
            
        return False
            
        
    
    def cutnewssitelink(self,link):
        
        begin_min4 = link.find(self.__beforenewslink)
        if begin_min4 == None:
            return None
        begin = begin_min4 + 4
        
        end = link.find(self.__afternewslink,begin)
        if end == -1:
            return link[begin:]
        else:
            return link[begin:end]
        
        
if __name__ == "__main__":        
    o = GoogleNewsFeedsChecker()
    domainname = urltranslate.net_loc("http://www.cnn.com")
    print domainname
    print o.isnewsdomain(domainname)
        
        
        
        
        
        
        
        