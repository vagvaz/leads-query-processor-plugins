'''
Created on Feb 20, 2014

@author: nonlinear
'''
from eu.leads.infext.python.ops import xpathops
from eu.leads.infext.python.ecom.dictionaries.currency import curr_regex, price_regex,\
    currprice_regex, pricecurr_regex, price_any_regex
import re
from eu.leads.infext.python.myprint import prettyprint
import math






class PriceCandidateForSite(object):
    
    def __init__(self,nodepath):
        self.nodepath = nodepath
        self.fitting_schemas = set()
        self.fitting_schemas_variants = []
        self.fitting_price_candidates = set()
        self.fitting_pages = set()
        self.fitting_pages_temp = set()
        self.points = 0
        self.score = 0






class PriceNode(object):
    __slots__= "nodestring", "nodepath", "desc","nodealternativepaths"

    def items(self):
        "dict style items"
        return [
            (field_name, getattr(self, field_name))
            for field_name in self.__slots__]

    def __iter__(self):
        "iterate over fields tuple/list style"
        for field_name in self.__slots__:
            yield getattr(self, field_name)

    def __getitem__(self, index):
        "tuple/list style getitem"
        return getattr(self, self.__slots__[index])
    
    def findalternativepaths(self,tree):
        self.nodealternativepaths = xpathops.standardizexpath(tree, self.nodepath)
        print " ! all paths: ", ', '.join(x for x in self.nodealternativepaths)






class PriceCandidate(object):
    __slots__= "pricenodelist", "buttondist", "namedist", "bagdist", "points", "commonancestornodepath"

    def items(self):
        "dict style items"
        return [
            (field_name, getattr(self, field_name))
            for field_name in self.__slots__]

    def __iter__(self):
        "iterate over fields tuple/list style"
        for field_name in self.__slots__:
            yield getattr(self, field_name)

    def __getitem__(self, index):
        "tuple/list style getitem"
        return getattr(self, self.__slots__[index])
    
    def getpriceval(self):
        '''
        Returns a value of the price represented by that price candidate
        '''
        pricevalstr = ''
        for node in self.pricenodelist:
            if 'v' in node.desc:
                pricevalstr = node.nodestring
                break
        matched_price = re.search(price_any_regex,pricevalstr)
        if matched_price:
            new_price = matched_price.group(0)#str(matched_price)
            new_price = new_price.replace(',','.')
            new_price = new_price.replace('-','')
            print(new_price)
            price_val = float(new_price)
            if price_val > 0.0:
                return price_val
            else:
                return float("inf")
            
    def findcommonancestor(self):
        if len(self.pricenodelist) > 1:
            nodepaths = [node.nodepath for node in self.pricenodelist]
            self.commonancestornodepath = xpathops.getcommonancestor(nodepaths)
        else:
            self.commonancestornodepath = self.pricenodelist[0].nodepath
            
    def findalternativepathsfornodes(self,tree):
        for node in self.pricenodelist:
            node.findalternativepaths(tree)






class PriceFinder(object):
    '''
    Implemented according to Leads/implementations/Find_price_20140219_193...
    '''


    def __init__(self, contents_list, namenode_list, buttonnode_list=None, basketnode_list=None):
        '''
        contents_list - content of n pages of the site offering product
        '''
        self.__contents_list = contents_list
        self.__trees_list = trees_list = [xpathops.content2tree(content) for content in contents_list]
        
        self.__namenode_list = namenode_list
        self.__buttonnode_list = buttonnode_list
        self.__basketnode_list = basketnode_list
                
        self.__prim_text_nodes, self.__prim_text_nodes_paths = xpathops.tree2textnodeslist(trees_list[0])
        
        self.__text_nodes_list = []
        self.__text_nodes_paths_list = []
        for tree in trees_list:
            text_nodes, text_nodes_paths = xpathops.tree2textnodeslist(tree)
            self.__text_nodes_list.append(text_nodes)
            self.__text_nodes_paths_list.append(text_nodes_paths)
        
       
    def __is_price_node(self,node_string,howtocontinue,path):
        '''
        returns is_price_node, howtocontinue, 
        '''
        node_string = node_string.strip()
        
        if xpathops.isdescendantorelemtype(path, 'a'):
            return False, None
        # MAYBE POINTS SHOULD BE TAKEN BUT DON'T REMOVE!
        #if xpathops.isdescendantorelemtype(path, 'li'):
        #    return False, None
        
        if howtocontinue:
            if howtocontinue == 'find_curr' and re.match(curr_regex, node_string):
                return True, None
            elif howtocontinue == 'find_price' and re.match(price_regex, node_string):
                return True, None
            else:
                return False, None
        else:
            if re.match(price_regex,node_string):
                return False, 'find_curr'
            elif re.match(curr_regex, node_string):
                return False, 'find_price'
            elif re.match(currprice_regex, node_string) or re.match(pricecurr_regex, node_string):
                return True, None
            else:
                return False, None
    
    
    def __is_regular_text_node(self,node_path,node_string):
        if not node_string:
            return False
        if "/li" in node_path:
            return False
        if "/select" in node_path:
            return False
        if node_string.isspace():
            return False
        else:
            return True
    
    
    
    def __evaluate_text_nodes(self,text_nodes,text_nodes_paths):
        '''
        evaluate which nodes are price nodes
        '''
        
        is_price_list =     [False] * len(text_nodes)
        price_ex_list =     [None]  * len(text_nodes)
        is_skip_node_list = [False] * len(text_nodes)
        
        isprice_temp = False
        howtocontinue = None
        
        for i,(text,path) in enumerate(zip(text_nodes,text_nodes_paths)):       
            # 1.      
            howtocontinue_old = howtocontinue
            isprice_temp,howtocontinue = self.__is_price_node(text, howtocontinue, path)
            if isprice_temp == True:
                if howtocontinue_old == 'find_curr':
                    is_price_list[i] = True
                    price_ex_list[i] = "c" # currency
                    is_price_list[i-1] = True
                    price_ex_list[i-1] = "v" # value  
                elif howtocontinue_old == 'find_curr':
                    is_price_list[i] = True
                    price_ex_list[i] = "c" # currency
                    is_price_list[i-1] = True
                    price_ex_list[i-1] = "v" # value  
                else:
                    is_price_list[i] = True
                    price_ex_list[i]= "cv" # currency, value
            else:
                is_price_list[i] = False
                price_ex_list[i] = None
                
            # 2.
            is_skip_node_list[i] = not self.__is_regular_text_node(path, text)
            
        return is_price_list,price_ex_list,is_skip_node_list
    
    
    def __getdist(self,nodepos,spnodepos,is_skip_node_list,isdouble=True):
        before = True if nodepos < spnodepos else False
        index1 = nodepos if before else spnodepos
        index2 = spnodepos if before else nodepos
        
        dist = is_skip_node_list[index1:index2].count(False)
        
        if before:
            dist = -dist
        
#         if isdouble and before:
#             dist -= 1
            
        return dist
            
    
    def __listcandidates(self,page_no,is_price_list,price_ex_list,is_skip_node_list,
                                                namenodepos,buttonnodepos,basketnodepos):
        text_nodes = self.__text_nodes_list[page_no]
        text_nodes_paths = self.__text_nodes_paths_list[page_no]
        tree = self.__trees_list[page_no]
        
        price_candidate_list = []
        
        iwasanalyzed = False
        
        #print ''
        #print '--- PAGE ---'
        
        for i,(text,path,isprice,priceex,isskip) in enumerate(zip(text_nodes,text_nodes_paths,is_price_list,price_ex_list,is_skip_node_list)):
            if iwasanalyzed == True:
                iwasanalyzed = False
                continue
           
            pricenodelist = []
            namedist = buttondist = bagdist = 0
            
            #if i+30 > buttonnodepos and i < buttonnodepos:
            #    print '"'+text+'"',path,isprice,priceex,isskip
           
            if isprice == True:
                if 'c' in priceex and 'v' in priceex:
                    namedist = self.__getdist(i,namenodepos,is_skip_node_list) if namenodepos else None
                    buttondist = self.__getdist(i,buttonnodepos,is_skip_node_list) if buttonnodepos else None
                    bagdist = self.__getdist(i,basketnodepos,is_skip_node_list) if basketnodepos else None
                    pricenodelist = [PriceNode()]
                    pricenodelist[0].nodestring = text
                    pricenodelist[0].nodepath = path
                    pricenodelist[0].desc = priceex
                else:
                    namedist = self.__getdist(i,namenodepos,is_skip_node_list,isdouble=True) if namenodepos else None
                    buttondist = self.__getdist(i,buttonnodepos,is_skip_node_list,isdouble=True) if buttonnodepos else None
                    bagdist = self.__getdist(i,basketnodepos,is_skip_node_list,isdouble=True) if basketnodepos else None
                    pricenodelist = [PriceNode(),PriceNode()]
                    pricenodelist[0].nodestring = text
                    pricenodelist[0].nodepath = path
                    pricenodelist[0].desc = priceex
                    pricenodelist[1].nodestring = text_nodes[i+1]
                    pricenodelist[1].nodepath = text_nodes_paths[i+1]
                    pricenodelist[1].desc = price_ex_list[i+1]
                    iwasanalyzed = True
                
                price_candidate = PriceCandidate()
                price_candidate.pricenodelist   = pricenodelist
                price_candidate.buttondist      = buttondist
                price_candidate.namedist        = namedist
                price_candidate.bagdist         = bagdist
                price_candidate.findcommonancestor()
                price_candidate.findalternativepathsfornodes(tree)
                
                price_candidate_list.append(price_candidate)
                
        return price_candidate_list
       
                
    def __getpricestringpath(self,cand):
        
        for node in cand.pricenodelist:
            if 'v' in node.desc:
                return node.nodestring, node.nodepath
       
            
    def __getpricepath(self,cand):
        
        for node in cand.pricenodelist:
            if 'v' in node.desc:
                return node.nodepath
                
            
#     def __filtercandidates_old(self,pricecandidates):
#         '''
#         Checks price node paths against other pages
#         '''
#         other_trees_list = self.__trees_list[1:]
#         pages_no = len(self.__trees_list)
#         
#         
#         newpricecandidates = []
#         
#         for cand in pricecandidates:
#             stringsset = set()
#             string, path = self.__getpricestringpath(cand)
#             
#             stringsset.add(string.strip())
#             
#             for page_tree in other_trees_list:
#                 # see how many various values are in this field of pages
#                 stringsonpage = page_tree.xpath(path+"/text()")
#                 if stringsonpage and stringsonpage[0] != None:
#                     stringsset.add(stringsonpage[0].strip())
#             
#             if len(stringsset)>properties.DIFFERENT_PRICE_VALS_FACTOR*pages_no:
#                 newpricecandidates.append(cand)
#                 
#         return newpricecandidates
    
    
    def __filtercandidates(self,pricecandidates_set):
        '''
        Checks price node paths against every of the sample pages
        '''
        
        trees_list = self.__trees_list[:]
        newpricecandidates = []
        
        for cand in pricecandidates_set:
            
            pageswithnodeno = 0
            stringsset = set()
            pricepath = self.__getpricepath(cand)
            for page_tree in trees_list:
                
                # see how many various values are in this field of pages
                stringsonpage = page_tree.xpath(pricepath+"/text()")
                if stringsonpage and stringsonpage[0] != None:
                    stringsset.add(stringsonpage[0].strip())
                    pageswithnodeno += 1
           
                 
            # here basically assuming that nodes with some constant price would often be candidates
            if pageswithnodeno <= 3:
                newpricecandidates.append(cand)
            else:
                if 2*len(stringsset) > pageswithnodeno:
                    newpricecandidates.append(cand)
                    
        return newpricecandidates            
                    
            
    def __countscoreforpagecandidate(self,cand):
        y = 1.5
        cand.points = 0
        
        # points for the distance from the name node
        if cand.namedist is not None:
            if cand.namedist in range (-10,0):
                cand.points += math.pow(10+cand.namedist,1.5)
            elif cand.namedist in range (0,20):
                cand.points += math.pow(20-cand.namedist,1.5)
        
        # points for the distance from the button node
        if cand.buttondist is not None:
            if cand.buttondist in range (-20,1):
                cand.points += math.pow(20+cand.buttondist,1.5)
            elif cand.buttondist in range (1,10):
                cand.points += math.pow(10-cand.buttondist,1.5)
            
        # points for the distance from the basket node
        if cand.bagdist is not None:
            if cand.bagdist < 0:
                cand.points -= math.pow(10,1.5)
            elif cand.bagdist in range (0,10):
                cand.points -= math.pow(10-cand.bagdist,1.5)
                
        return cand.points
    
    
    def __evalcandidatesbyposition(self,pricecandidates):
        for cand in pricecandidates:
            cand.points = self.__countscoreforpagecandidate(cand)
                
        if len(pricecandidates)>1:
            pricecandidates = sorted(pricecandidates,key=lambda cand: -cand.points)
        
            # might be that an old price is taken
            # take a look if two first candidates are close to each other
            if abs(pricecandidates[0].bagdist - pricecandidates[1].bagdist)<=3:
                # if that is the case, check which one has the lower price value
                if pricecandidates[1].getpriceval() < pricecandidates[0].getpriceval():
                    pricecandidates[0],pricecandidates[1] = pricecandidates[1],pricecandidates[0]    
            
        return pricecandidates
    
    
    def __evalsitecandidates(self,pricecandidates_perpage_list):
        '''
        returns site candidates objects
        '''
        candidatespaths = []
        candidatesancestors_dict = dict()
        for pricecandidates_page in pricecandidates_perpage_list:
            for pricecandidate in pricecandidates_page:
                candidatecommonpath = pricecandidate.commonancestornodepath
                candidatespaths.append(candidatecommonpath)
        
        # get list of up to 2 ancestors of every node with number of pages for which they work
        for path in candidatespaths:
            pathslist = [path]
            pathslist.extend(xpathops.path2ancestorpaths(path, 2))
            for p in pathslist:
                p_no = candidatesancestors_dict.get(p)
                if p_no is None:
                    p_no = 1
                else:
                    p_no += 1
                candidatesancestors_dict[p] = p_no
        
        # filter ancestors with the same score
        dict_items = candidatesancestors_dict.items()
        for path1,score1 in dict_items:
            if not candidatesancestors_dict.get(path1):
                continue
            cont = True
            for path2,score2 in dict_items:
                if not candidatesancestors_dict.get(path2):
                    continue
                if not path1 is path2:
                    if score1 == score2:
                        if xpathops.isancestor(path1, path2):
                            candidatesancestors_dict.pop(path2)
                        elif xpathops.isancestor(path2, path1):
                            candidatesancestors_dict.pop(path1)
                            cont = False
                if cont == False:
                    break
            
        for path,no in candidatesancestors_dict.items():
            print path + ": " + str(no)
        print "\n"
        
        # needed schemas to extract from any page from the site (together with rules for url)
        solving_group = []
        
        orig_page_numbers = range(len(pricecandidates_perpage_list))
        
        while True:
            candidates_for_site = []
            # create price candidates per site
            for path in candidatesancestors_dict.keys():
                candidate_for_site = PriceCandidateForSite(path)
                #print "Candidate for site:", path
                page_no = 0
                for pricecandidates_page in pricecandidates_perpage_list:
                    for pricecandidate in pricecandidates_page:
                        if xpathops.isancestororself(pricecandidate.commonancestornodepath, path):
                            candidate_for_site.fitting_price_candidates.add(pricecandidate)
                            candidate_for_site.points += pricecandidate.points
                            candidate_for_site.fitting_pages_temp.add(page_no)
                            candidate_for_site.fitting_pages.add(orig_page_numbers[page_no])
                            #print pricecandidate.points,'->',candidate_for_site.points,'(',pricecandidate.pricenodelist[0].nodestring,pricecandidate.pricenodelist[0].nodepath,pricecandidate.pricenodelist[0].desc,pricecandidate.bagdist,pricecandidate.namedist,pricecandidate.buttondist,')'
                            for node in pricecandidate.pricenodelist:
                                candidate_for_site.fitting_schemas.add(node.nodepath)
                    page_no += 1
                if len(candidate_for_site.fitting_price_candidates) == 0:
                    candidate_for_site.score = 0
                else:
                    candidate_for_site.score = candidate_for_site.points * candidate_for_site.points / len(candidate_for_site.fitting_price_candidates)
                candidates_for_site.append(candidate_for_site)
            
            if not candidates_for_site:
                break
                    
            candidates_for_site = sorted(candidates_for_site, key=lambda cand: -cand.score)
            
            solver = candidates_for_site[0]
            
            if solver.score == 0:
                break
            
            solving_group.append((solver,''))
            
            for page_no in reversed(sorted(list(solver.fitting_pages_temp))):
                print page_no
                del pricecandidates_perpage_list[page_no]
                del orig_page_numbers[page_no]
            del candidatesancestors_dict[solver.nodepath]
            
            print "-----------------------------"
            print "---------SITE SOLVER---------"
            print "-----------------------------"
            
            print solver.nodepath
            
            print "Pages:"
            print len(solver.fitting_pages)
            for page_no in solver.fitting_pages:
                print page_no,
            print "\n"
            
            for schema in solver.fitting_schemas:
                print schema
            
            print "Price candidates:"
            print len(solver.fitting_price_candidates)
            printme(solver.fitting_price_candidates)
            print "\n"
            
            print "Score:"
            print solver.score
            print "\n"
            
            if len(pricecandidates_perpage_list) == 0:
                break
            
        pages_with_schema = 0
        for s in solving_group:
            pages_with_schema += len(s[0].fitting_pages)
            for sch in s[0].fitting_schemas:
                allxpaths = xpathops.standardizexpath(self.__trees_list[next(iter(s[0].fitting_pages))], sch)
                print 'all xpaths',
                print ', '.join(x for x in allxpaths)
                s[0].fitting_schemas_variants.append(allxpaths)
        is_extraction_sufficient = True if pages_with_schema / len(self.__contents_list) > 0.5 else False

        return [(list(s[0].fitting_schemas_variants), len(s[0].fitting_pages)) for s in solving_group], is_extraction_sufficient
            
        
    def find_price(self):
        prim_tree = self.__trees_list[0]
        prim_paths = self.__prim_text_nodes_paths
        
        trees_list = self.__trees_list
        text_nodes_list = self.__text_nodes_list
        text_nodes_paths_list = self.__text_nodes_paths_list
        
        pricecandidates_perpage_list = []
        pricecandidates_set = set()
        pricecandidates_list = []
        
        # For every page, make a list of candidates
        for i, (tree, text_nodes, text_nodes_paths) in enumerate(zip(trees_list, text_nodes_list, text_nodes_paths_list)):
            
            # 1. Prepare lists describing nodes
            is_price_list,price_ex_list,is_skip_node_list = self.__evaluate_text_nodes(text_nodes, text_nodes_paths)
            
            # 2. Find position of closest text nodes of nodes of the graph
            namenodepos   = xpathops.getposintextnodeslist(tree, text_nodes_paths, self.__namenode_list[i][0]) if self.__namenode_list and self.__namenode_list[i] else None
            buttonnodepos = xpathops.getposintextnodeslist(tree, text_nodes_paths, self.__buttonnode_list[i]) if self.__buttonnode_list and self.__buttonnode_list[i] else None
            basketnodepos = xpathops.getposintextnodeslist(tree, text_nodes_paths, self.__basketnode_list[i]) if self.__basketnode_list and self.__basketnode_list[i] else None
            
            print(i,":",namenodepos,buttonnodepos,basketnodepos)
            
            # 3. Count distances, create new list of objects
            pricecandidates_set = self.__listcandidates(i,is_price_list,price_ex_list,is_skip_node_list,
                                                    namenodepos,buttonnodepos,basketnodepos)
            #printme(pricecandidates_set)
            #print "\n"
            
            
            pricecandidates_perpage_list.append(pricecandidates_set)
        
        
        #for cands in pricecandidates_perpage_list:
        #    pricecandidates_set.extend(cands)
        
        for pricecandidates_page in pricecandidates_perpage_list:
            for pricecandidate in pricecandidates_page:
                pricecandidate.points = self.__countscoreforpagecandidate(pricecandidate)
            printme(pricecandidates_page)
            print "\n"
            
        prettyprint.print_space() 
           
        solving_nodepaths, is_effective_enough = self.__evalsitecandidates(pricecandidates_perpage_list)                
        
        return solving_nodepaths, is_effective_enough
    
#         prettyprint.print_space()
#         
#         if pricecandidates_set:
#             # 4. Check nodepaths against other pages
#             ''' TODO TAKE A LOOK HERE! '''
#             pricecandidates_list = self.__filtercandidates(pricecandidates_set) 
#             printme(pricecandidates_list)
#             prettyprint.print_space()
#             
#             if pricecandidates_list:
#                 # 5. Sort candidates by the position
#                 pricecandidates_list = self.__evalcandidatesbyposition(pricecandidates_list)
#                 print("sorted:")
#                 printme(pricecandidates_list)
#                 prettyprint.print_space()
#                 
# #         if pricecandidates_list[0].points > 0:
# #             return pricecandidates_list[0].pricenodelist
# #         else:
# #             return None
# 
#             pricenodes_list = []
#             i = 0
#             for cand in pricecandidates_list:
#                 if cand.points > 0:
#                     pricenodes_list.append(cand.pricenodelist)
#                 else:
#                     break
#                 
#             return pricenodes_list
        
        
        
def printme(pricecandidates):
    if pricecandidates:        
        for cand in pricecandidates:
            print(cand.pricenodelist[0].nodestring,cand.pricenodelist[0].nodepath,cand.pricenodelist[0].desc,cand.bagdist,cand.namedist,cand.buttondist,cand.points)  
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        