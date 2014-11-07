'''
Created on Dec 13, 2013

@author: nonlinear
'''

import re

class HTMLCodeLine:
    
    def __init__(self,line):
        self.line = line

    def tags_no(self):
        '''
        for now, consider as opening tags number
        TODO: check the paper with TTR
        '''
        etcount = 0
        otcount = 0
        ultimate_regexp = "(?i)<\/?\w+((\s+\w+(\s*=\s*(?:\".*?\"|'.*?'|[^'\">\s]+))?)+\s*|\s*)\/?>"
        for match in re.finditer(ultimate_regexp, self.line):
            if repr(match.group()).startswith("'</"):
                etcount += 1
            else:
                otcount += 1
        return otcount
    
    def non_tag_chars_no(self):
        '''
        for now, returns the number of signs outside of tags, without those listed in BAD_LETTERS
        TODO: remove special characters (&lt; is now 2)
        TODO: remove scripts!
        '''
        TAG_RE = re.compile(r'<[^>]+>')
        non_tag_text = TAG_RE.sub('', self.line)
        BAD_LETTERS = " \n\t.,:;"
        return len([letter for letter in non_tag_text if letter not in BAD_LETTERS])
    
    def has_table_begin_tag(self):
        table_begin_regexp = ".*<table?\w+((\s+\w+(\s*=\s*(?:\".*?\"|'.*?'|[^'\">\s]+))?)+\s*|\s*)>.*"
        #table_begin_regexp = "<table"
        if re.match(table_begin_regexp, self.line):
            return True
        else:
            return False
    
    def has_table_close_tag(self):
        '''
        assumption that there is nothing like <table/>
        '''
        table_begin_regexp = ".*<\/table?\w+((\s+\w+(\s*=\s*(?:\".*?\"|'.*?'|[^'\">\s]+))?)+\s*|\s*)>.*"
        if re.match(table_begin_regexp, self.line):
            return True
        else:
            return False
    
    def has_pbrbq_begin_tag(self):
        table_begin_regexp = ".*<(((p|blockquote)?\w+((\s+\w+(\s*=\s*(?:\".*?\"|'.*?'|[^'\">\s]+))?)+\s*|\s*))|(br\s*\/?))>.*"
        #table_begin_regexp = "<table"
        if re.match(table_begin_regexp, self.line):
            return True
        else:
            return False
    
    def has_pbrbq_end_tag(self):
        table_begin_regexp = ".*<((\/(p|blockquote)?\w+((\s+\w+(\s*=\s*(?:\".*?\"|'.*?'|[^'\">\s]+))?)+\s*|\s*))|(br\s*\/?))>.*"
        #table_begin_regexp = "<table"
        if re.match(table_begin_regexp, self.line):
            return True
        else:
            return False
    
    # what if a few images
    def img_width(self):
        '''
        returns None is no image tag
        '''
        pass
        return 0
    
    def obj_width(self):
        pass
        return 0
    

