'''
Created on May 22, 2014

@author: root
'''
import requests

def downloadstaticcontent(url):
    page = requests.get(url)
    if page.status_code == 200:
        text = unicode(page.text.encode("utf-8"), errors="ignore")
        return text
    else:
        return None