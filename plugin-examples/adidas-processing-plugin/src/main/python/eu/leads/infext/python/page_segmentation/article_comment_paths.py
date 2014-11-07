'''
Created on May 9, 2014

@author: root
'''
from eu.leads.infext.python.page_segmentation.sd_algorithm import SDAlgorithm
from eu.leads.infext.python.ops.xpathops import element2path, parentpath2nodetext,\
    findcommonstandardxpath
from eu.leads.infext.python.ops.xpathops import standardizexpath
import requests
from eu.leads.infext.python.ops import stringops, xpathops

class ArticleCommentPaths():
    '''
    classdocs
    '''


    def __init__(self):
        pass
    
    
    def run(self, page_dict):
        
        article_xpath = comment_xpaths = post_xpaths = None
        
        content = page_dict.get("content")
        
        sd = SDAlgorithm()
        sd.content = content
        type,article,comments,multiple = sd.analyze_page()
        
        if type in ['article','comment']:
            article_xpath = element2path(sd.tree,article.root_node)
            # create a few variants of the xpath
            article_xpaths = standardizexpath(sd.tree,article_xpath)
            #print '\n'.join(str(path) for path in article_xpaths)
        
        if type in ['comment']:
            comment_root_paths = []
            for comment in comments:
                comment_root_paths.append(element2path(sd.tree,comment.root_node))
            # look for the regularity in the comments' paths
            comment_root_paths, common_beg, diff_middle, common_end = stringops.find_difference_inside(comment_root_paths)
            if diff_middle.isdigit():
                #print 'regularity found'
                commenttemplatepath = common_beg+xpathops.STANDARD_REPLACEMENT_STRING+common_end
                # create a few variants of the xpath
                comment_xpaths = findcommonstandardxpath(sd.tree,commenttemplatepath,comment_root_paths)
                #print '\n'.join(str(path) for path in comment_xpaths)
        
        if type in ['multiple']:
            posts_root_paths = []
            for post in multiple:
                posts_root_paths.append(element2path(sd.tree,post.root_node))
            # look for the regularity in the comments' paths
            posts_root_paths, common_beg, diff_middle, common_end = stringops.find_difference_inside(posts_root_paths)
            if diff_middle.isdigit():
                #print 'regularity found'
                posttemplatepath = common_beg+xpathops.STANDARD_REPLACEMENT_STRING+common_end
                # create a few variants of the xpath
                post_xpaths = findcommonstandardxpath(sd.tree,posttemplatepath,posts_root_paths)
                #print '\n'.join(str(path) for path in post_xpaths)
                
        return article_xpath, comment_xpaths, post_xpaths
            
  
  
        
        
url = "http://blog.trove.com/post/87305627825/troves-we-love-applethings"#
page = requests.get(url)

page_dict = {"content": page.content}

o = ArticleCommentPaths()
result = o.run(page_dict)

print "article:"
print result[0]
print "comments:"
print result[1]
print "posts:"
print result[2]

