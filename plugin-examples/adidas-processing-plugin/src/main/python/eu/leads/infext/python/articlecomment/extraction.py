__author__ = 'freak'
from goose import Goose
import happybase
from BeautifulSoup import BeautifulSoup
import urllib2
from lxml import etree
from StringIO import StringIO

class ExtractContent:
    def __init__(self):
        self.goose = Goose()
        self.db = DataBase()

    def extract_article(self, extract_url):
        goose_extract = self.goose.extract(extract_url)

        title = goose_extract.title
        article_text = goose_extract.cleaned_text
        article_text = article_text.encode('utf-8', 'ignore')
        pub_date = goose_extract.publish_date

        self.db.database_connect('content')
        self.db.export_article_details(extract_url, article_text, title, pub_date)

    def extract_comment(self, comments, extract_url):
        comments_class = []
        comments_ids = []
        cmt_tags = {} #Key vals of first and last tags. The first tags are never html/body
        cmt_paths = []

        #This is for comments section
        for c in comments:
            comments_class.append(c.class_name)
            comments_ids.append(c.id)
            cmt_paths.append(str(c.contents[0]))
            tmp_tag_key = ''
            tags = c.contents[0].split('/')#vals[1].contents[0].split('/')
            for t in tags:
                if t != 'html' and t != 'body' and t != '': ##TODO: This should be the part of best extraction schema
                    tmp_tag_key = t
                    break

            while tags[len(tags)-1] == '':
                tags.pop()
            tmp_tag_val = tags[len(tags)-1]

            if '[' in tmp_tag_key:
                tmp_tag_key = tmp_tag_key[:tmp_tag_key.index('[')]
            if '[' in tmp_tag_val:
                tmp_tag_val = tmp_tag_val[:tmp_tag_val.index('[')]

            if tmp_tag_key in cmt_tags.keys():
                cmt_tags[tmp_tag_key].append(tmp_tag_val)
            else:
                cmt_tags[tmp_tag_key] = []
                cmt_tags[tmp_tag_key].append(tmp_tag_val)

        extracted_data = []

        if not comments_class:
            extracted_data = self.extract_content_tag_structure(extract_url, cmt_paths)
        else:
            comments_class_set = set(comments_class) #Eleminates duplicate classes
            comments_class = list(comments_class_set)
            extracted_data = self.extract_content_by_classname(extract_url, cmt_tags, comments_class, comments_ids)

        self.db.database_connect('content')
        self.db.export_article_comments(self.url, extracted_data)


    def extract_multiple_article(self, multiple, extract_url):
        multiple_class = []
        multiple_ids = []
        mul_tags = {} #Key vals of first and last tags. The first tags are never html/body
        mul_paths = []
        extracted_data = []

        for m in multiple:
            multiple_class.append(m.class_name) #TODO:Check if all class have same value
            multiple_ids.append(m.id)
            mul_paths.append(str(m.contents[0]))
            tmp_tag_key = ''
            tags = m.contents[0].split('/')

            for t in tags:
                if t != 'html' and t != 'body' and t != '': ##TODO: This should be the part of best extraction schema
                    tmp_tag_key = t
                    break

            while tags[len(tags)-1] == '':
                tags.pop()
            tmp_tag_val = tags[len(tags)-1]
            mul_tags[tmp_tag_key] = tmp_tag_val

        if not multiple_class:
            extracted_data = self.extract_content_tag_structure(extract_url, mul_paths)
        else:
            multiple_class_set = set(multiple_class) #Eleminates duplicate classes
            multiple_class = list(multiple_class_set)
            extracted_data = self.extract_content_by_classname(extract_url, mul_tags, multiple_class, multiple_ids)

        self.db.database_connect('content')
        self.db.export_multiple_article(extract_url, extracted_data)

    def extract_content_by_classname(self, url, tags_pairs, classes, ids):
        page = urllib2.urlopen(url)
        text = page.read()
        sp = BeautifulSoup(text)
        text_extracted = []
        for key in tags_pairs:
            prs = sp.findAll(key, attrs={'class': classes})
            last_tags = set(tags_pairs[key]) #eleminates duplicates
            last_tags = list(last_tags)

            for p in prs:
                for tag in last_tags:
                    for s in p.findAll(tag):
                        textcontent = s.findAll(text=True)
                        if len(textcontent):
                            text_extracted.append(textcontent.pop().encode('utf-8', 'ignore'))

        return text_extracted

    def extract_content_tag_structure(self, url, paths_list):
        text_extracted = []
        page = urllib2.urlopen(url)
        text = page.read()
        text = text.decode('utf-8', 'ignore')
        doc = StringIO(text)
        htmlparser = etree.HTMLParser()
        tree = etree.parse(doc, parser=htmlparser)
        tag_pairs = self.extract_first_and_last_tags(paths_list)

        for keys in tag_pairs:
            st_tg = keys
            end_tg = tag_pairs[keys]

            for tag in end_tg:
                xpathtouse = '/html/body' + st_tg + '//' + tag
                elementlist = tree.xpath(xpathtouse)

                for ele in elementlist:
                    treepath = tree.getpath(ele)
                    path = treepath + '//text()'
                    text = tree.xpath(path)
                    print(text)
                    text_extracted.append(text).encode('utf-8', 'ignore')
        return text_extracted

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

    def extract_with_best_schema(self, urlstr, pagetype):
        extr = ExtractionSchema()
        db = DataBase()
        text_extracted = []
        url_split = urlstr.split('/')

        while url_split[len(url_split)-1] == '':
            url_split.pop()
        url_split.pop()

        domain = '/'.join(url_split)
        print(domain)
        start_and_end_tag_dict = extr.get_best_extraction_schema(domain)

        page = urllib2.urlopen(urlstr)
        text = page.read()
        text = text.decode('utf-8', 'ignore')
        doc = StringIO(text)
        htmlparser = etree.HTMLParser()
        tree = etree.parse(doc, parser=htmlparser)

        for keys in start_and_end_tag_dict:
            st_tg = keys
            end_tg = start_and_end_tag_dict[keys]

            for tag in end_tg:
                xpathtouse = '/html/body' + st_tg + '//' + tag
                elementlist = tree.xpath(xpathtouse)

                for ele in elementlist:
                    treepath = tree.getpath(ele)
                    path = treepath + '//text()'
                    text = tree.xpath(path)
                    #print(text)
                    text_extracted.append(text.encode('utf-8', 'ignore'))

        self.db.database_connect('content')

        if pagetype == 'article':
            self.db.export_article_details(urlstr, text_extracted)
        elif pagetype == 'comments':
            self.db.export_article_comments(urlstr, text_extracted)
        elif pagetype == 'multiple':
            self.db.export_multiple_article(urlstr, text_extracted)


class ExtractionSchema:
    def __init__(self):
        self.db = DataBase()

    def get_best_extraction_schema(self, domain):
        self.db.database_connect('verifieddomains')
        self.db.get_domain_extraction_schema(domain)
        #TODO:get only the dictionary type of the tag pairs and return it


class DataBase:
    def __init__(self):
        self.table = None
        self.connection = None

    def database_connect(self, tablename):
        self.connection = happybase.Connection(host='127.0.0.1', port=9090)
        self.table = self.connection.table(tablename)

    def getrowkey(self):
        rows = self.table.scan()
        row_count = sum(1 for _ in rows)
        rowkey = 'r' + str(row_count + 1)
        return rowkey

    def export_article_details(self, urlstr, article_text, title=None, pub_date=None):
        rowkey = self.getrowkey()
        self.table.put(rowkey, {'url:urlstr': urlstr, 'article:title': title, 'article:publishdate': pub_date, 'article:data': str(article_text)})

    def export_article_comments(self, urlstr, comment_list):
        rowkey = self.getrowkey()
        self.table.put(rowkey, {'url:urlstr': urlstr, 'comment:commentlist': str(comment_list)})

    def export_multiple_article(self, urlstr, mul_art_list):
        rowkey = self.getrowkey()
        self.table.put(rowkey, {'url:urlstr': urlstr, 'multiple:article_list': str(mul_art_list)})

    def insert_domain(self, domain, ver_type):
        rows = self.table.scan()
        row_count = sum(1 for _ in rows)
        rowkey = 'r' + str(row_count + 1)
        print(ver_type, rowkey, domain)
        self.table.put(rowkey, {ver_type + ':a': domain})

    def get_domain_extraction_schema(self, domain):
        rows = self.table.scan()
        for r in rows:
            if domain in r[1]['domain:domaintested']:
                pass
