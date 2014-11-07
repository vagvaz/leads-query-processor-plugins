'''
Created on Dec 13, 2013

@author: nonlinear
'''

from countforline import HTMLCodeLine

ptratio = []

for line in open("../../ecom/extraction/webpage_ama.html",'r'):
    if not line.strip():
        continue
    htmlCodeLine = HTMLCodeLine(line)
    line_ptratio = 0
    tags_no = htmlCodeLine.tags_no()
    non_tag_chars_no = htmlCodeLine.non_tag_chars_no()
    print(line[0:-1],">>>",tags_no,non_tag_chars_no,htmlCodeLine.has_table_begin_tag(),htmlCodeLine.has_table_close_tag(),
          htmlCodeLine.has_pbrbq_begin_tag(),htmlCodeLine.has_pbrbq_end_tag())
    img_width = htmlCodeLine.img_width()
    obj_width = htmlCodeLine.obj_width()
    if tags_no > 0:
        img_width = htmlCodeLine.img_width()
        if htmlCodeLine.has_table_begin_tag():
            pass # TODO
        else:
            if img_width:
                if img_width < 150 or img_width > 600:
                    img_width = 0
            if obj_width:
                if obj_width < 250:
                    obj_width = 0
            ''' this part needs to be smoothed '''
            if htmlCodeLine.has_pbrbq_begin_tag() and htmlCodeLine.has_pbrbq_end_tag():
                non_tag_chars_no *= 2
            
            # if is a subset of pbrbg    
            #    line_ptratio = max(ptratio[-1],ptratio[-2])
            ''' '''
            if non_tag_chars_no > 20: # TODO: elif
                line_ptratio = img_width + obj_width + (non_tag_chars_no / tags_no)
            else:
                line_ptratio = (img_width + obj_width + non_tag_chars_no) / tags_no
    else:
        line_ptratio = non_tag_chars_no
    ptratio.append(line_ptratio)
                    
                
                
                
                