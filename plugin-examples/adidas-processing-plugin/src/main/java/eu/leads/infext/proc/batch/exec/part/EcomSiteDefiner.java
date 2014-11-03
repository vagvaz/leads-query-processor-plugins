package eu.leads.infext.proc.batch.exec.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import eu.leads.datastore.AbstractDataStore;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.URIVersion;
import eu.leads.infext.proc.batch.mapreduce.DefaultReducer;
import eu.leads.infext.proc.batch.mapreduce.EcomSiteDefinerMapper;
import eu.leads.infext.proc.com.categorization.ecom.EcommerceClassification;
import eu.leads.infext.proc.com.categorization.ecom.EcommerceSiteExtractionSchemaDeterminer;
import eu.leads.infext.proc.com.categorization.ecom.model.EcomPageDictionary;
import eu.leads.infext.proc.com.categorization.ecom.model.EcomSiteDictionary;
import eu.leads.utils.LEADSUtils;

/***
 * 
 * 
 * 
 * @author a'moremix
 *
 */
public class EcomSiteDefiner extends AbstractPartialSiteDefiner {

	public EcomSiteDefiner(String fqdn, HashMap<String,Integer> pagesNoMap, List<String> dirUris) {
		super(fqdn, pagesNoMap, dirUris);
	}

	public boolean defineAndStore() {
		
		EcomSiteDictionary ecomSiteDictionary = null;
		List<EcomPageDictionary> ecomPageList = new ArrayList<>();
		List<EcomPageDictionary> productEcomPageList = null;
		List<EcomPageDictionary> categoryEcomPageList = null;
		
		int dirPagesNo = pagesNoMap.get("");
		
		// i. check whether Ecom
		//    - check if enough URIs of this domain to be checked if Ecom
		int dirPagesNoEcomThreshold = new Integer(parameters.getProperty("leads-urldirectory-is_ecom_assumption_pages_no-threshold_count"));
		if(dirPagesNo > dirPagesNoEcomThreshold) {
			
			int countedPagesNo = dirPagesNo;
			int ecomPagesNo = 0;
			
//	        MapReduceTask<Object, Object, Object, Object> task = new MapReduceTask<Object, Object, Object, Object>((Cache<Object, Object>) dataStore.getFamilyStorageHandle(null));
//	        task.onKeys(dirUris.toArray());
//			
//	        Mapper<Object, Object, Object, Object> ecomSiteDefinerMapper = new EcomSiteDefinerMapper();
//	        Reducer<Object, Object> ecomSiteDefinerReducer = new DefaultReducer();
//	        
//	        task.mappedWith(ecomSiteDefinerMapper).reducedWith(ecomSiteDefinerReducer);
//	        Map<Object,Object> valuesMap = task.execute();	
//			
//	        countedPagesNo = valuesMap.size();
//	        ecomPagesNo = countedPagesNo - Collections.frequency(valuesMap.values(), null);
			
			//    - get features for URIs
			for(String uri : dirUris) {
				String content = null;
				String lang = null;
				String timestamp1 = null;
				String timestamp2 = null;
				
				SortedSet<URIVersion> uriCrawlerMdFamilyVersions = dataStore.getLeadsResourceMDFamily(uri, mapping.getProperty("leads_crawler_data"), 1, null);
				if(uriCrawlerMdFamilyVersions != null && !uriCrawlerMdFamilyVersions.isEmpty()) {
					URIVersion uriCrawlerMdFamilyVersion = uriCrawlerMdFamilyVersions.first();
					timestamp1 = uriCrawlerMdFamilyVersion.getTimestamp();
					Map<String, Cell> uriCrawlerMdFamily = uriCrawlerMdFamilyVersion.getFamily();
					Cell contentCell = uriCrawlerMdFamily.get(mapping.getProperty("leads_crawler_data-content"));
					if(contentCell != null) {
						content = (String) contentCell.getValue();
					}
				}
				
				SortedSet<URIVersion> uriCoreMdFamilyVersions = dataStore.getLeadsResourceMDFamily(uri, mapping.getProperty("leads_core"), 1, null);
				if(uriCoreMdFamilyVersions != null && !uriCoreMdFamilyVersions.isEmpty()) {
					URIVersion uriCoreMdFamilyVersion = uriCoreMdFamilyVersions.first();
					timestamp2 = uriCoreMdFamilyVersion.getTimestamp();
					Map<String, Cell> uriCoreMdFamily = uriCoreMdFamilyVersion.getFamily();
					Cell langCell = uriCoreMdFamily.get(mapping.getProperty("leads_core-lang"));
					if(langCell != null) {
						lang = (String) langCell.getValue();
					}
				}
				
				if(lang != null && content != null && timestamp1.compareTo(timestamp2)<=0) {
					// <------ Call of the determinePageEcomFeatures() method
					System.out.println("content.length() "+content.length());
					EcommerceClassification ecomClassification = new EcommerceClassification(content, lang);
					boolean isCorrect = ecomClassification.determinePageEcomFeatures();
					// ------>
					
					System.out.println(uri+" -> "+ecomClassification.isEcomAssumption()+" -> "+ecomClassification.getEcomFeatures());
					
					if(isCorrect) {
						if(ecomClassification.isEcomAssumption()) {
							EcomPageDictionary ecomPageDictionary = new EcomPageDictionary();
							ecomPageDictionary.url = uri;
							ecomPageDictionary.timestamp = timestamp1;
							ecomPageDictionary.lang = lang;
							ecomPageDictionary.content = content;
							ecomPageDictionary.ecom_features = ecomClassification.getEcomFeatures();
							ecomPageDictionary.basketnode = ecomClassification.getBasketNode();
							ecomPageDictionary.buttonnode = ecomClassification.getButtonNode();
							
							ecomPageList.add(ecomPageDictionary);
							ecomPagesNo++;
						}
					}
					else {
						countedPagesNo--;
					}
				}
				else {
					countedPagesNo--;
				}
			}
			
//			
//			
			
			if(countedPagesNo > 0) {
				double ecomPagesFactor = (double)ecomPagesNo / (double)countedPagesNo;
				double ecomPagesFactorThreshold = new Double(parameters.getProperty("leads-urldirectory-is_ecom_assumption_pages_no-threshold_factor"));
				if(ecomPagesFactor >= ecomPagesFactorThreshold) {
					
					// Get n random numbers from the list
					int pagesForCheck = (int) (dirPagesNoEcomThreshold  * ecomPagesFactorThreshold);
					Collections.shuffle(ecomPageList);
					List<EcomPageDictionary> shortEcomPageList = ecomPageList.subList(0, pagesForCheck);
					
					// <------ Call of the determineExtractionSchema() method
					EcommerceSiteExtractionSchemaDeterminer schemaDeterminer = new EcommerceSiteExtractionSchemaDeterminer();
					schemaDeterminer.determineExtractionSchema(shortEcomPageList);
					// ------>
					
					pagesNoMap.put("ecom", ecomPagesNo);
					ecomSiteDictionary = schemaDeterminer.getEcomSiteDictionary();
					productEcomPageList = schemaDeterminer.getProductEcomPageList();
					categoryEcomPageList = schemaDeterminer.getCategoryEcomPageList();						
				}
			}
		}
		
		// SO THIS IS ECOM - if site evaluated as Ecom and everything extracted properly
		if(ecomSiteDictionary != null) {
			
			String url = fqdn;
			String timestamp = LEADSUtils.getTimestampString();
			String family = mapping.getProperty("leads_urldirectory_ecom");
			List<Cell> cells = new ArrayList<>();
			cells.add( new Cell(mapping.getProperty("leads_urldirectory_ecom-name_extraction_tuples"), ecomSiteDictionary.nameExtractionTuples, 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory_ecom-price_extraction_tuples"), ecomSiteDictionary.priceExtractionTuples, 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory_ecom-product_cluster_center"), ecomSiteDictionary.productClusterCenter, 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory_ecom-category_cluster_center"), ecomSiteDictionary.categoryClusterCenter, 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory_ecom-product_cluster_50pc_dist"), ecomSiteDictionary.productCluster50pcDist, 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory_ecom-product_cluster_80pc_dist"), ecomSiteDictionary.productCluster80pcDist, 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory_ecom-category_cluster_50pc_dist"), ecomSiteDictionary.categoryCluster50pcDist, 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory_ecom-category_cluster_80pc_dist"), ecomSiteDictionary.categoryCluster80pcDist, 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory_ecom-scaler_mean"), ecomSiteDictionary.scalerMean, 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory_ecom-scaler_std"), ecomSiteDictionary.scalerStd, 0) );
			dataStore.putLeadsResourceMDFamily(url, timestamp, family, cells);
			
			family = mapping.getProperty("leads_urldirectory");
			cells = new ArrayList<>();
			cells.add( new Cell(mapping.getProperty("leads_urldirectory-dir_assumption"), mapping.getProperty("leads_urldirectory-dir_assumption-ecom_varia"), 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory-pages_no"), pagesNoMap.get(""), 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory-is_ecom_assumption_pages_no"), pagesNoMap.get("ecom"), 0) );
			dataStore.putLeadsResourceMDFamily(url, timestamp, family, cells);
			
			ecomPageList.removeAll(categoryEcomPageList);
			ecomPageList.removeAll(productEcomPageList);

			for(EcomPageDictionary ecomPage : ecomPageList) {						
				family = mapping.getProperty("leads_internal");
				cells = new ArrayList<>();
				cells.add( new Cell(mapping.getProperty("leads_internal-ecom_features"), ecomPage.ecom_features, 0) );
				url = ecomPage.url;
				timestamp = ecomPage.timestamp;
				dataStore.putLeadsResourceMDFamily(url, timestamp, family, cells);
			}
			for(EcomPageDictionary ecomPage : productEcomPageList) {						
				family = mapping.getProperty("leads_internal");
				cells = new ArrayList<>();
				cells.add( new Cell(mapping.getProperty("leads_internal-ecom_features"), ecomPage.ecom_features, 0) );
				cells.add( new Cell(mapping.getProperty("leads_internal-page_type"), mapping.getProperty("leads_internal-page_type-ecom_product_offering_page"), 0) );
				url = ecomPage.url;
				timestamp = ecomPage.timestamp;
				dataStore.putLeadsResourceMDFamily(url, timestamp, family, cells);
			}
			for(EcomPageDictionary ecomPage : categoryEcomPageList) {						
				family = mapping.getProperty("leads_internal");
				cells = new ArrayList<>();
				cells.add( new Cell(mapping.getProperty("leads_internal-ecom_features"), ecomPage.ecom_features, 0) );
				cells.add( new Cell(mapping.getProperty("leads_internal-page_type"), mapping.getProperty("leads_internal-page_type-ecom_category_page"), 0) );
				url = ecomPage.url;
				timestamp = ecomPage.timestamp;
				dataStore.putLeadsResourceMDFamily(url, timestamp, family, cells);
			}
			
			// TADA!
			return true;	
		}
		
		return false;
	}
	
	
}
