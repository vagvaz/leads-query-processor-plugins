'''
Created on Jun 23, 2014

@author: nonlinear
'''
from eu.leads.infext.python.ecom.classifier.productofferingpageclassifier import ProductOfferingPageClassifier
import requests
from eu.leads.infext.python.ecom.extrschema.siteschemaextractor import LeadsEcomSiteSchemaExtractor
from eu.leads.infext.python.ops import xpathops
from eu.leads.infext.python.ecom.extract.extractvalues import ExtractValues
import os
import sys
from eu.leads.infext.python.ecom.cluster.clusterproductcategorypages import ClusterProductCategoryPages
from eu.leads.infext.python.ecom.cluster.findpagecluster import FindPageCluster
from eu.leads.infext.python.ecom.extract.mine import stringminer, priceminer
from eu.leads.infext.python.ecom.extract.verify import defaultverifier

def create_list(urllist):
    page_dict_list = []
    
    for url in urllist:
        print url
        try:
            content = requests.get(url).text
        except requests.exceptions.ConnectionError():
            continue
        page_dict = {"url": url,
                     "content": content,
                     "tree": xpathops.content2tree(content),
                     "lang": "en"}
        
        classifier = ProductOfferingPageClassifier()
        classifier.classify(page_dict)
        
        ecom_features = classifier.getFeaturesVals()
        buttonnode, basketnode = classifier.getExtractedNodes()
        
        page_dict["buttonnode"] = buttonnode
        page_dict["basketnode"] = basketnode
        page_dict["ecom_features"] = ecom_features
        
        page_dict_list.append(page_dict)
        
    return page_dict_list


if __name__ == '__main__':
    filenames = os.listdir(os.path.dirname(os.path.realpath(__file__))+'/files')
    print filenames
    
    # block printing
    f = open(os.devnull,'w')
    sys.stdout = f
    
    wasreebok = False
    for filename in filenames:
        #if 'asics' in filename:
        #    wasreebok = True
        if filename.startswith('testecom_dataset') and '~' not in filename:
            sys.stderr.write(filename+'\n')
            
            dataset = "product"#"fulltest" #"full"
            urllist = []
            testlist = []
            testlinks = False
            with open('files/'+filename) as f:
                while True:
                    line = f.readline()
                    if not line:
                        break
                    if line.startswith("#categories") or line.startswith("# categories"):
                        if dataset in ("full","fulltest"):
                            continue
                        else:
                            break
                    if line.startswith("#testcluster") or line.startswith("# testcluster"):
                        if dataset in ("fulltest"):
                            testlinks = True
                            continue
                        else:
                            break
                    else:
                        line = str.strip(line)
                        if testlinks == False:
                            urllist.append(line)
                        else:
                            testlist.append(line)
            
            page_dict_list = create_list(urllist)
            test_dict_list = create_list(testlist)
                
            correct_clustering = True
            
            if "full" in dataset:
                clusterPages = ClusterProductCategoryPages()
                if clusterPages.cluster(page_dict_list):
                    product_page_dict_list = clusterPages.product_pages_dict_list
                    for dictio in product_page_dict_list:
                        print dictio.get("url"),":",dictio.get("ecom_kmeans_dist")
                        
                    if dataset == "fulltest":
                        for page_dict in test_dict_list:
                            findcluster = FindPageCluster(clusterPages.product_cluster_center,
                                                         clusterPages.category_cluster_center, 
                                                         clusterPages.product_cluster_50pc_dist, 
                                                         clusterPages.product_cluster_80pc_dist, 
                                                         clusterPages.category_cluster_50pc_dist, 
                                                         clusterPages.category_cluster_80pc_dist, 
                                                         clusterPages.pd.mean, clusterPages.pd.std) 
                            group = findcluster.find(page_dict)
                            print page_dict.get("url"), page_dict.get("ecom_kmeans_dist"), page_dict.get("category"), group
                else:
                    print 'incorrect clustering'
                    correct_clustering = False
            else:
                product_page_dict_list = page_dict_list
                
            schema_extractor = LeadsEcomSiteSchemaExtractor(product_page_dict_list)
            schema_extractor.find_schema()
             
            nameextractiontuples = schema_extractor.getNameExtractionTuples()
            priceextractiontuples= schema_extractor.getPriceExtractionTuples()
            
            for page_dict in page_dict_list:
                extractobj = ExtractValues(page_dict)
                ###
                extractionlist = nameextractiontuples
                verifier = defaultverifier.DefaultEcomNameVerifier()
                miner = stringminer.DefaultProductNameMiner()
                namestrings =  extractobj.extract(extractionlist, verifier, miner)
                ###
                extractionlist = priceextractiontuples
                verifier = defaultverifier.DefaultEcomPriceVerifier()
                miner = priceminer.DefaultPriceMiner()
                pricestrings = extractobj.extract(extractionlist, verifier, miner)
                 
                wrstr1 = ' '.join(stri[0] +' ('+stri[1]+') ' for stri in namestrings[0])
                wrstr2 = ' '.join(str(stri[0]) + ' ('+str(stri[1])+') ' for stri in pricestrings[0])
                
                sys.stderr.write(wrstr1 + ' :::: ' + wrstr2 + '\n')
             
            sys.stderr.write('\n\n')    
                
                
            
            
            
