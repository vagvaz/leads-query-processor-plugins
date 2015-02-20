'''
Created on Jan 28, 2015

@author: nonlinear
'''
from eu.leads.infext.python.ops import xpathops
import dateutil.parser as dparser
from dateutil.parser import _timelex, parser
import datetime
import requests
import re
from eu.leads.infext.python.CLAPI.conversionmethods import translateReturnValues
import logging


class AccessPoint:
    
    def __init__(self):
        self.logger = logging.getLogger("leads")

    def execute(self,params):
        html = params[0]
        url = params[1]
        tree = xpathops.content2tree(html)
        metacontentvals = tree.xpath("/html/head//meta/@content")
        metacontentvals.extend(tree.xpath("/html/body//time/@datetime"))
        search = DateSearch()
        publishdate = search.getdate(metacontentvals,url)
        returnlist = []
        if publishdate is not None:
            returnlist.append(publishdate.year)
            returnlist.append(publishdate.month)
            returnlist.append(publishdate.day)
        self.logger.debug(returnlist)
        return returnlist
        

class DateSearch:
    
    p = parser()
    info = p.info
    
    pattern = "/(\d{4}-[01]\d-[0-3]\d[\s|T][0-2]\d:[0-5]\d:[0-5]\d\.\d+)|(\d{4}-[01]\d-[0-3]\d[\s|T][0-2]\d:[0-5]\d:[0-5]\d)|(\d{4}-[01]\d-[0-3]\d[\s|T][0-2]\d:[0-5]\d)|(\d{4}-[01]\d-[0-3]\d)/"
    dateregex = re.compile(pattern)
    
    def getdate(self,strings, url):
        publishdate = None
        publishdates = self.getpublishdate(strings)
        if publishdates is None or not publishdates:
            publishdates = self.getdatefromurl(url)
        if publishdates is not None:
            publishdate = min(publishdates)
        return publishdate   
    
    def getpublishdate(self,strings):
        datelist = []
        
        for string in strings:
            #print string,"|||",
            result = self.dateregex.match(string)
            if result is not None:
                #print string
                datelist.append(self.p.parse(string))
            #else:
                #print ""
        return datelist
        
        
    def getdatefromurl(self,url):
        urlparts = url.split("/")
        year = None
        month = None
        day = None
        
        for string in urlparts:
            if self.__isnumber(string):
                if year is None:
                    potyear = int(string)
                    if potyear > 1979 and potyear < 2050:
                        year = potyear
                elif month is None:
                    potm = int(string)
                    if potm > 0 and potm < 13:
                        month = potm
                elif day is None:
                    potd = int(string)
                    if potd > 0 and potd < 32:
                        day = potd
            else:
                if year is not None:
                    break
        
        if year is not None and month is not None:
            return [datetime.date(year,month,day if day is not None else 1)]
                    
            
    def __isnumber(self,s):
        try:
            int(s)
            return True
        except ValueError:
            return False  
        

    
#     def filter(self,string):
#         retstring = ""
#         parts = string.split()
#         for part in parts:
#             if re.match("\d.*",part):
#                 retstring = retstring,part
#         return retstring if retstring != "" else None      
#             
#             
#             for item in self.timesplit(string):
#                 print item+" ||| ",
#                 try:
#                     date = self.p.parse(item)
#                 except ValueError:
#                     print ""
#                     continue
#                 print date
#                 datelist.append(date) 
                
                               
                
#           datefound = None
#           datefound = self.filter(string)
#             if datefound is None:
#                 continue
#             print "FILTERED!"
#             try:
#                 datefound = dparser.parse(string, fuzzy=True, default=self.__unknowndatetime)
#             except ValueError:
#                 continue
#             print "found ", datefound
#             datefound = datefound.replace(tzinfo.None)
#             if datefound != self.__unknowndatetime:
#                 datelist.append(datefound)
#                 print "ACCEPTED!"
                
#         print datelist 
# 
#     def timetoken(self,token):
#         try:
#             float(token)
#             return True
#         except ValueError:
#             pass
#         return any(f(token) for f in (self.info.jump,self.info.weekday,self.info.month,self.info.hms,self.info.ampm,self.info.pertain,self.info.utczone,self.info.tzoffset))
#     
#     def timesplit(self,input_string):
#         batch = []
#         for token in _timelex(input_string):
#             if self.timetoken(token):
#                 if self.info.jump(token):
#                     continue
#                 batch.append(token)
#             else:
#                 break
#                 if batch:
#                     yield " ".join(batch)
#                     batch = []
#         if batch:
#             yield " ".join(batch)

if __name__=='__main__':
    pages = [
            "http://www.bbc.com/news/blogs-trending-31019565",
            "http://wiadomosci.onet.pl/kraj/siemoniak-ostrzega-czekaja-nas-trudne-lata-polityka-rosji-stwarza-zagrozenie/nfvxq",
            "http://www.spiegel.de/wirtschaft/unternehmen/deutsche-bank-bilanz-von-anshu-jain-und-juergen-fitschen-a-1015665.html",
            "http://edition.cnn.com/2015/01/28/africa/boko-haram-raids/index.html",
            "http://www.runnersworld.com/other-gear/6-gym-bags-for-runners",
            "http://thebullrunner.com/2015/01/dream-chasers-2015/#.VMpKBGjF-So",
            "http://www.sbnation.com/lookit/2015/1/29/7938937/new-england-patriots-hotel-fire-alarm-prank",
            "http://www.thystride.com/nnautica-malibu-international-triathlon.html",
            "http://strengthrunning.com/2015/01/running-for-weight-loss/"
             ]
    
    for page in pages:
        pageobj = requests.get(page)
        print(page)
        content = pageobj.content
        ap = AccessPoint()
        print(ap.execute([content,page]))
        print("---")