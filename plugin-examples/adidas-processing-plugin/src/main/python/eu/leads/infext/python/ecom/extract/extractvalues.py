'''
Created on Jun 27, 2014

@author: nonlinear
'''
from eu.leads.infext.python.ops import xpathops
from lxml import html
import lxml

class ExtractValues():
    
    def __init__(self,page_dict):
        self.__tree = page_dict.get("tree")
        
        
    def extract(self,extractionlist,verify_obj,mine_obj):
        '''
        will extract text from all of the descendants of the nodes  
        of the first extraction candidate
        TODO: it could be done much more effectively if the css file was attached as well...
        '''
        text_list = []
        worked = False
        working_schema_tuple = None
        for schema_tuple in extractionlist:
            if schema_tuple[1] == 0:
                paths_versions = schema_tuple[0]
                usednodes = set()
                for path_versions in paths_versions:
                    extracted = False
                    if type(path_versions) is str:
                        path_versions = [path_versions]
                    for path in path_versions:
                        nodes = self.__tree.xpath(path)
                        if nodes is not None and nodes:
                            strnode = lxml.etree.tostring(nodes[0])
                            if not strnode in usednodes:
                                usednodes.add(strnode)
                                worked = True
                                text_nodes = self.__tree.xpath(path+"//text()") # when it's a parent of many nodes (like article), // - is needed
                                parent_element = self.__tree.xpath(path)
                                for nft in parent_element:
                                    text_node_tail = nft.tail
                                    if text_node_tail is not None:
                                        text_nodes.append(text_node_tail)
                                text_nodes = [text.strip() for text in text_nodes]
                                text_nodes = ' '.join(node for node in text_nodes) # TODO what string shall join the nodes
                                text_nodes_metadata = parent_element[0].values()
                                text_nodes_metadata2 = parent_element[0].keys()
                                text_nodes_metadata = ' '.join(key+"='"+node+"'" for node,key in zip(text_nodes_metadata,text_nodes_metadata2))
                                text_tuple = (text_nodes,text_nodes_metadata)
                                text_list.append(text_tuple)
                                extracted = True
                        if extracted == True:
                            break
            elif schema_tuple[1] == 1:
                surrounding_str = schema_tuple[0]
                ''' TODO - how? '''
            if worked == True:
                if verify_obj.verify(text_list) == True:
                    working_schema_tuple = (schema_tuple[0], schema_tuple[1], 1)
                    break
                else:
                    text_list = []
                    worked = False
        return mine_obj.extractWhatMattersAndNameIt(text_list), working_schema_tuple
    
    
    
    
    
    
    
    