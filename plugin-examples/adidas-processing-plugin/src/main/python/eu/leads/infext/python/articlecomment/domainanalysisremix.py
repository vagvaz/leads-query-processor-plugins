from eu.leads.infext.python.ops.xpathops import element2path
import requests
from eu.leads.infext.python.ops import xpathops
__author__ = 'freak'

from sd_algorithm import SDAlgorithm
from BeautifulSoup import BeautifulSoup
import happybase
import urllib2
import re
from urlparse import urlparse
from verification import Verify


class DomainAnalysis:
    '''
    This class would be called from my code, given a list of dictionaries per every url containing:
    url, content and tree
    If you want to do some simulation of my class that retrieves data with happybase, it needs to be separate from this class
    '''
    
    def __init__(self):
        self.sd = SDAlgorithm()
        self.current_domain = ''

    def save_domain_verification_result(self, verification_result):
        self.database_connect('verifieddomains')
        rows = self.domains_table.scan()
        row_count = sum(1 for _ in rows)

        for key in verification_result:
            urlstr = key
            articletype = verification_result[key][0]
            path_dict = verification_result[key][1]
            row_count = row_count + 1
            print {'domain:domaintested': self.current_domain, 'domain:urltested': urlstr, 'article:type':articletype, 'article:schema': str(path_dict)}

    def process_article_details(self, article):
        content_list = []
        density_list = []
        distance_list = []
        content_list.append(article.contents)
        density_list.append(article.density)
        distance_list.append(article.distance_from_root)

        return content_list, density_list, distance_list

    def process_comment_details(self, comments):
        #content_list = []
        comment_root_paths = []
        density_list = []
        distance_list = []

        for comment in comments:
            #content_list.append(comment.contents)
            comment_root_paths.append(element2path(self.sd.tree, comment.root_node))
            density_list.append(comment.density)
            distance_list.append(comment.disance_from_root)

        return comment_root_paths, density_list, distance_list

    def process_multiple_details(self, multiple):
        #content_list = []
        density_list = []
        distance_list = []
        multiple_article_root_paths = []

        for article in multiple:
            #content_list.append(article.contents)
            multiple_article_root_paths.append(element2path(self.sd.tree, article.root_node))
            density_list.append(article.density)
            distance_list.append(article.distance_from_root)

        return multiple_article_root_paths, density_list, distance_list


    #def create_blog_domain_url(self, url): #TODO:Test to get the exact domain
    #    url_split = url.split('/')
    #    #while url_split[0] in ['', 'http:', 'https:']:
    #    #    del url_split[0]
    #    print(url_split)
    #    while url_split[len(url_split)-1] == '':
    #        url_split.pop()
    #    page_in_url = url_split.pop()
    #    if len(url_split) <= 3: #This check means, the url and domain are same and there is no sub domain to this url
    #        url_split.append(page_in_url)

    #    domain_to_check = '/'.join(url_split)
    #    self.current_domain = domain_to_check
    #    print('current domain to check' + domain_to_check)
    #    return domain_to_check


    def create_blog_domain_url(self, url):
        domain = ''
        parsed = urlparse(url)
        if parsed.path == '' or parsed.path == '/':
            domain = parsed.netloc
        elif len(parsed.path) > 1:
            path_split = parsed.path.split('/')
            while path_split[len(path_split)-1] == '':
                path_split.pop()
            while path_split[0] == '':
                del(path_split[0])

            if len(path_split) == 1 and 'blog' in str(path_split[0]):
                domain = parsed.netloc + '/' + path_split[0]
            elif len(path_split) >= 1:
                path_split.pop()
            domain = parsed.scheme + '://' + parsed.netloc + '/' + '/'.join(path_split)

        return domain

    def extract_first_and_last_tags(self, path_list):
        tag_pairs = {}

        for path in path_list:
            path_split = path.split('/')
            first_tags = ''
            last_tags = ''
            tmp = []
            for tag in path_split:
                if '[' in tag:
                    t = tag[:tag.index('[')]
                    tmp.append(t)
                else:
                    tmp.append(tag)
            path_split = tmp

            if len(path_split) > 4:
                first_tags = path_split[0] + '/' + path_split[1]
                last_tags = path_split[len(path_split) - 2] + '/' + path_split[len(path_split) - 1]

            #creating a dictionary with keys as first_tags and values as list of last tags for corresponding first tag
            # the list of last tags contains unique list of tags
            if first_tags in tag_pairs.keys():
                if last_tags not in tag_pairs[first_tags]:
                    tag_pairs[first_tags].append(last_tags) #TODO:eleminated duplicates
            else:
                tag_pairs[first_tags] = []
                tag_pairs[first_tags].append(last_tags)

        return tag_pairs


    def verify_domain(self, details):
        positive_url_details = {}
        verf = Verify()
        verified_result = {}

        for key in details:
            artcile_type = details[key][0]

            if artcile_type == "article":
                verified = verf.verify_article(key, details[key][1])
                if verified:
                    tag_pairs = self.extract_first_and_last_tags(details[key][1])
                    verified_result[key] = [artcile_type, tag_pairs]
                    #self.save_best_schema_for_domain(key, artcile_type, tag_pairs)
            elif artcile_type == "comments":
                verified = verf.verify_comments(details[key][1], self.sd.tree)

                if verified:
                    tag_pairs = self.extract_first_and_last_tags(details[key][1])
                    verified_result[key] = [artcile_type, tag_pairs]
                    #self.save_best_schema_for_domain(key, artcile_type, tag_pairs)

            elif artcile_type == "multiple":
                verified = verf.verify_multiple_articles_pagetags_structure(details[key][1])
                if verified:
                    #positive_url_details[key] = [artcile_type, details[key][1]]
                    tag_pairs = self.extract_first_and_last_tags(details[key][1])
                    verified_result[key] = [artcile_type, tag_pairs]
                    #self.save_best_schema_for_domain(key, artcile_type, tag_pairs)

        return verified_result


    def process_domain(self, pages_dict_list):
        details = {}
        
        for page_dict in pages_dict_list[0:10]:
            url = page_dict.get("url")
            self.sd = SDAlgorithm()
            self.sd.tree = page_dict.get("tree")
            vals = self.sd.analyze_page()

            if vals[0] == "article":
                root_path, density, distance = self.process_article_details(vals[1])
                details[url] = ["article", root_path, density, distance]
            elif vals[0] == "comments":
                root_path, density, distance = self.process_article_details(vals[1])
                details[url] = ["article", root_path, density, distance]
                root_path, density, distance = self.process_comment_details(vals[2])
                details[url] = ["comments", root_path, density, distance]
            elif vals[0] == "multiple":
                
                root_path, density, distance = self.process_multiple_details(vals[3])
                details[url] = ["multiple", root_path, density, distance]

        result = self.verify_domain(details)
        return result


class DomainAnalysisDatasetPrepare():
    
    def crawl_multiple_article_of_domain(self, fqdn):
        url_list = []

        page = urllib2.urlopen(fqdn)
        page_html = page.read()
        bs = BeautifulSoup(page_html)
        urls = bs.findAll('a')  #TODO: Change the regex to extract blog urls in general
        '''
        Comment to your to do:
        You know you won't find it - the challenge is that your component would get a dataset consisting of 
        various pages (not only blog pages) and will be able to mine which of them are blog pages.
        '''
        cnt = 0
        for u in urls:
            if cnt < 40 and u['href'].startswith(fqdn):
                url_list.append(u['href'])
                cnt += 1
            elif cnt == 10:
                break

        return url_list
    
    
    def prepareSitePagesContent(self,fqdn):
        '''
        fqdn = fully qualified domain name https://kb.iu.edu/d/aiuv
        '''
        pages_dict_list = []
        
        url_list = self.crawl_multiple_article_of_domain(fqdn)
        
        for url in url_list:
            try:
                content = requests.get(url).text
                tree = xpathops.content2tree(content)
                page_dict = {"url":url,"content":content,"tree":tree}
                pages_dict_list.append(page_dict)
            except:
                pass
        
        return pages_dict_list
    
    

if __name__ == "__main__":
    datasetCrawl = DomainAnalysisDatasetPrepare()
    pages_dict_list = datasetCrawl.prepareSitePagesContent('http://blogs.economictimes.indiatimes.com/')
    
    analysis = DomainAnalysis()
    result = analysis.process_domain(pages_dict_list)
    print '----------------------------'
    print '---------RESULT-------------'
    print '----------------------------'
    print result
    
    
    

    
    
    