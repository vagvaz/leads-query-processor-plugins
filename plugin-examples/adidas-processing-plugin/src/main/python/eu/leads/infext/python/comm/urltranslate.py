'''
Created on Jan 30, 2014

@author: nonlinear
'''
from urlparse import urlparse
import re
#import urlnorm

def url_fix(url):
    if '://' not in url:
        url = "http://"+url
    return url

def url_norm(url):
    '''
    Convert url to Nutch format url
    '''
    _url = urlparse(url)
    netloclist =  _url.netloc.split('.')
    netloclist.reverse()
    host = '.'.join(netloclist)
    #if len(netloclist)==3:
    #    host += ".www"
    
    url_norm = host + ":" + _url.scheme + _url.path
    print(url,url_norm)
    return url_norm


def url_base_full(url):
    parts = re.split('[\./]?',url)
    if len(parts) == 2:
        parts = ['www'] + parts
    if ':' not in parts[0]:
        parts = ['http'] + parts
    return parts[0]+'://'+parts[1]+'.'+parts[2]+'.'+parts[3]+'/'
    

def url_base(url):
    _url = urlparse(url)
    url_base = _url.scheme + "://" + _url.netloc + "/"
    return url_base


def net_loc(url):
    _url = urlparse(url)
    net_loc = _url.netloc
    return net_loc

if __name__ == "__main__":
    print url_norm('http://xo.stackoverflow.com/questions/9626535/get-domain-name-from-url')