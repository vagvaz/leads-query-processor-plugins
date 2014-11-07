'''
Created on Dec 10, 2013

@author: nonlinear
'''

from lxml import html
import re
from eu.leads.infext.python.ecom.extraction.maxsumseq import maxsumseq, max_sum_subsequence

tree = html.parse('webpage_ama.html')

# take the title
title = tree.xpath('/html/head/title/text()')[0].strip()
print(title)

# split the title into words
title_words = title.split()
print(title_words)

# boilerplate recognition ;)

# get a list of all nonempty strings from body
text_nodes = [ x.strip() for x in tree.xpath('//body//text()') ]
for tnode in text_nodes[:]:
    if not tnode:
        text_nodes.remove(tnode)
print(text_nodes)

# find the product name node
max_candidate_rate = 0
sequence = []
product_name = ''
for candidate in text_nodes:
    found_words_no = 0
    for title_word in title_words:
        searchstr = r'\b{0}\b'.format(title_word)
        if re.search(searchstr, candidate, re.I):
            found_words_no += 1
    if found_words_no > 0:
        text_lenght = len(re.findall(r'\w+', candidate))
        candidate_rate = found_words_no * found_words_no / text_lenght
        
        print(candidate,':',found_words_no, text_lenght, candidate_rate, max_candidate_rate)
        sequence.append(candidate_rate)
        if candidate_rate > max_candidate_rate:
            max_candidate_rate = candidate_rate
            product_name = candidate
print('product name:', product_name)

print(sequence)
print(max_sum_subsequence(sequence))




