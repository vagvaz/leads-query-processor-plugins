from eu.leads.infext.python.ops.xpathops import element2path
from eu.leads.infext.python.ops import stringops, xpathops
import string
__author__ = 'freak'
from goose import Goose
import urllib2
import re
from BeautifulSoup import BeautifulSoup

class Verify():
    def __init__(self):
        self.goose = Goose()

    def verify_article(self, url, content, val):
        ext = self.goose.extract(raw_html=content)
        istitleverified = self.verify_article_title(ext.title, url)
        return istitleverified

    def verify_article_title(self, article_title, url):
        #Using goose extract title. Then go to the blog home page and extract the text and
        #check for String with same as title
        url_to_check = 'http:/'
        checked = False
        titlepresent = False
        i = 0
        url_split = url.split('/')

        while url_split[0] in ['', 'http:', 'https:']:
            del url_split[0]

        while url_split[len(url_split)-1] == '':
            url_split.pop()
        url_split.pop()

        while not checked:
            url_to_check = url_to_check + '/' + url_split[i]
            print(url_to_check)
            i += 1
            if i == len(url_split)-1:
                checked = True

            ext1 = self.goose.extract(url_to_check)
            raw_html = ext1.raw_html.decode('utf-8', 'ignore')

            if article_title in raw_html: #TODO: Handel &nbsps and other utf-8 characters
                checked = True
                titlepresent = True
            else:
                titlepresent = False

        return titlepresent


    def verify_comments(self, comments, tree):
        comment_root_paths = []

        for com in comments:
            comment_root_paths.append(element2path(tree, com.root_node))

        comment_root_paths, common_beg, diff_middle, common_end = stringops.find_difference_inside(comment_root_paths)

        if diff_middle.isdigit():
            print 'Comments: regularity found'
            return True
        else:
            return False


    def verify_multiple_articles(self, mulart, url, tree, content):
        mul_art_root_paths = []
        multiple_article_text = []

        for mul in mulart:
            multiple_article_text.append(mul.full_text.encode('utf-8', 'ignore'))
            mul_art_root_paths.append(element2path(tree, mul.root_node))

        mul_art_root_paths, multiple_article_text = self.leave_roots_only(mul_art_root_paths, multiple_article_text)
        if(len(mul_art_root_paths)>1):
            struct_verified = self.verify_multiple_articles_pagetags_structure(mul_art_root_paths)
            href_verified = self.verify_multiple_article_hrefs(url, content)
            sim_text_verified = self.verify_similar_text(multiple_article_text, url)
            if struct_verified and href_verified and sim_text_verified:
                return True
            else:
                return False
        else:
            return False # but actually it means : that's an article
        
    
    def leave_roots_only(self,mul_art_root_paths, multiple_article_text):
        '''
        Check this out...
        One of the errors of the SD-algorithm is that it does not check whether the content is the same for various multiple nodes
        (check out for http://magellan88.blogspot.de/2007/12/jak-mwi-ludowe-przysowie.html)
        '''
        r_length = len(mul_art_root_paths)
        to_be_removed = []
        
        for i in range(r_length):
            for j in range(r_length):
                if i != j:
                    if xpathops.isancestororself(mul_art_root_paths[i], mul_art_root_paths[j]):
                        pattern = re.compile('[\W_]+')
                        str1 = pattern.sub('', multiple_article_text[i])
                        str2 = pattern.sub('', multiple_article_text[j])
                        if str1 in str2:
                            to_be_removed.append(i)
                            break
                    
        for index in reversed(to_be_removed):
            mul_art_root_paths.pop(index)
            multiple_article_text.pop(index)
            
        return mul_art_root_paths, multiple_article_text
                    
        
    def verify_multiple_articles_pagetags_structure(self, multiple_articles):
        comment_root_paths, common_beg, diff_middle, common_end = stringops.find_difference_inside(multiple_articles)

        if diff_middle.isdigit():
            print 'Multiple article page: regularity found'
            return True
        else:
            return False

    def verify_multiple_article_hrefs(self, url, content):
        '''
        QUESTION: what is that method supposed to do? I see nothing here that could help with a random page
        '''
        bs = BeautifulSoup(content)
        a_tag = bs.findAll('a', href=re.compile('blog/'))#TODO:Write the right regex for all blogs
        cnt = 0
        text_vals = []

        for i in a_tag:
            text_vals.append(i.findAll(text=True)) #TODO: Filter the number of links and verify
            cnt += 1

        if cnt == 0:
            return False
        else:
            return True

    def verify_url_domain(self, url, ver_dom):
        url_split = url.split('/')

        while url_split[len(url_split)-1] == '':
            url_split.pop()
        url_split.pop()

        domain_to_check = '/'.join(url_split)

        if domain_to_check in ver_dom:
            return True
        else:
            return False


    def verify_similar_text(self, multiple_article_text, url):
        '''
        QUESTION: again, I don't see how this could help with a random page
        when article_text is empty, this returns True - why?
        '''
        url_split = url.split('/')
        while url_split[len(url_split)-1] == '':
            url_split.pop()
        url_split.pop()

        domain = '/'.join(url_split)

        article = self.goose.extract(domain)
        article_text = article.cleaned_text
        for s in multiple_article_text:
            if s in article_text.encode('utf-8', 'ignore'):
                return False
            else:
                return True
