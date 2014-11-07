'''
Created on Jul 19, 2014

@author: nonlinear
'''
from sd_algorithm import SDAlgorithm
from domainanalysis import DomainAnalysis
from eu.leads.infext.python.ops import xpathops
from verification import Verify
from extraction import ExtractContent
import requests

class Classify:
    
    def __init__(self):
        self.verify = Verify()
        self.extr = ExtractContent()

    def classify_page(self,page_dict,is_domain_verified):
        
        export = ExportAlt()
        
        url = page_dict.get("url")
        content = page_dict.get("content")
        tree = xpathops.content2tree(content)
        self.pagetype = ''

        sd = SDAlgorithm()
        sd.tree = tree
        analysis_result = sd.analyze_page()
        
        for x in analysis_result[3]:
            print "FULL TEXT:"
            print x.full_text

        if analysis_result[0] == 'article':
            isarticle = self.verify.verify_article(url, content, analysis_result[1])

            if isarticle:
                self.extr.extract_article(url)
                export.export_urls(url, 'proper', 'article', analysis_result[1])
            elif not isarticle:
                self.pagetype = self.reclassify()
                export.export_urls(url, 'improper', 'article', analysis_result[1])

                if is_domain_verified is not None:
                    if is_domain_verified:
                        self.extr.extract_with_best_schema(url, self.pagetype)
                    elif not is_domain_verified:
                        analysis = DomainAnalysis()
                        analysis.process_domain(url)
                        self.extr.extract_with_best_schema(url, self.pagetype)


        elif analysis_result[0] == 'comment':
            isarticle = self.verify.verify_article(analysis_result[1])
            iscomments = self.verify.verify_comments(analysis_result[2], tree)

            if isarticle and iscomments:
                self.extr.extract_article(url)
                self.extr.extract_comment(analysis_result[2], url)
                export.export_urls(url, 'proper', 'comment', analysis_result[1], analysis_result[2])
            else:
                self.pagetype = self.reclassify('comment')
                export.export_urls(url, 'improper', 'comment', analysis_result[1], analysis_result[2])

                if is_domain_verified is not None:
                    if not is_domain_verified:
                        analysis = DomainAnalysis()
                        analysis.process_domain(url)
                    self.extr.extract_with_best_schema(url, self.pagetype)

        elif analysis_result[0] == 'multiple':
            ismultiple = self.verify.verify_multiple_articles(analysis_result[3], url, tree, content)

            if ismultiple:
                self.extr.extract_multiple_article(analysis_result[3], url)
                export.export_urls(url, 'proper', 'multiple', None, analysis_result[3])
            else:
                self.pagetype = self.reclassify('multiple')
                export.export_urls(url, 'improper', 'multiple', None, analysis_result[3])

                if is_domain_verified is not None:
                    if not is_domain_verified:
                        analysis = DomainAnalysis()
                        analysis.process_domain(url)
                    self.extr.extract_with_best_schema(url, self.pagetype)

    def reclassify(self, currentType):
        classified_type = ''
        if currentType == 'article':
            classified_type = 'article' #TODO: find the right alternative
        if currentType == 'comments':
            classified_type = 'multiple'
        elif currentType == 'multiple':
            classified_type = 'article'

        return classified_type
    
    
    
class ExportAlt:
    def __init__(self):
        self.table = None

    def get_next_rowid(self):
        rows = self.table.scan()
        row_count = sum(1 for _ in rows)
        rowkey = 'r' + str(row_count + 1)
        return rowkey

    def export_urls(self, urlstr, verf_type, article_type,  article_tag_struct, other_tag_struct = None):
        #calculate the next row key
        if article_tag_struct is not None:
            print {'url:urlttested': urlstr, 'article:type': article_type, 'article:paths': str(article_tag_struct.contents), 'verifiedas:v':verf_type}

        if other_tag_struct is not None:
            paths = []
            for o in other_tag_struct:
                paths.append(o.contents)

            print {'url:urlttested': urlstr, 'article:type': article_type, 'article:paths': str(paths), 'verifiedas:v':verf_type}



if __name__ == "__main__":
    url = "http://magellan88.blogspot.de/2007/12/jak-mwi-ludowe-przysowie.html"
    page = requests.get(url)
    content = page.text
    page_dict = {"url":url,"content":content}
    
    classify = Classify()
    classify.classify_page(page_dict,False)
    
    
    
    
