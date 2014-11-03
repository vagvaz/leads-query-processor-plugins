package eu.leads.infext.proc.batch.exec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;

import eu.leads.datastore.AbstractDataStore;
import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.URIVersion;
import eu.leads.infext.logging.redirect.StdLoggerRedirect;
import eu.leads.infext.proc.com.categorization.ecom.EcommerceClassification;
import eu.leads.infext.proc.com.categorization.ecom.EcommerceSiteExtractionSchemaDeterminer;
import eu.leads.infext.proc.com.categorization.ecom.model.EcomPageDictionary;
import eu.leads.infext.proc.com.categorization.ecom.model.EcomSiteDictionary;
import eu.leads.utils.LEADSUtils;

public class EcomSiteDefinerBatch {

	private static AbstractDataStore dataStore = DataStoreSingleton.getDataStore();
	private Properties mapping = DataStoreSingleton.getMapping();
	private Properties parameters = DataStoreSingleton.getParameters();

	public void execute(String fqdn) {
		
		HashMap<String,Integer> pagesNoMap = new HashMap<>();
		
		// a. see if already evaluated as Ecom
		SortedSet<URIVersion> dirMdFamilyVersions = dataStore.getLeadsResourceMDFamily(fqdn, mapping.getProperty("leads_urldirectory"), 1, null);
		
		boolean isEcom = false;
		boolean isKnown = false;
		if(dirMdFamilyVersions != null && !dirMdFamilyVersions.isEmpty()) {
			URIVersion dirMdFamilyVersion = dirMdFamilyVersions.first();
			Map<String, Cell> dirMdFamily = dirMdFamilyVersion.getFamily();
			Cell dirAssumptionCell = dirMdFamily.get(mapping.getProperty("leads_urldirectory-dir_assumption"));
			if(dirAssumptionCell != null) {
				String dirAssumption = (String) dirAssumptionCell.getValue();
				if (Arrays.asList(mapping.getProperty("leads_urldirectory-dir_assumption-ecom_po"), 
									mapping.getProperty("leads_urldirectory-dir_assumption-ecom_po_varia"), 
									mapping.getProperty("leads_urldirectory-dir_assumption-ecom_varia"))
										.contains(dirAssumption))	{
					isEcom = true;
				}
				isKnown = true;					
			}
		}
		
		// b. if unknown...
		if(!isKnown) {
			
			EcomSiteDictionary ecomSiteDictionary = null;
			List<EcomPageDictionary> ecomPageList = new ArrayList<>();
			List<EcomPageDictionary> productEcomPageList = null;
			List<EcomPageDictionary> categoryEcomPageList = null;
						
			List<String> dirUris = dataStore.getResourceURIsOfDirectory(LEADSUtils.fqdnToNutchUrl(fqdn));
			
			int dirPagesNo = dirUris.size();
			pagesNoMap.put("", dirPagesNo);
			
			// i. check whether Ecom
			//    - check if enough URIs of this domain to be checked by Ecom
			int dirPagesNoEcomThreshold = new Integer(parameters.getProperty("leads-urldirectory-is_ecom_assumption_pages_no-threshold_count"));
			if(dirPagesNo > dirPagesNoEcomThreshold) {
				
				int countedPagesNo = dirPagesNo;
				int ecomPagesNo = 0;
			
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
				
				if(countedPagesNo > 0) {
					double ecomPagesFactor = (double)ecomPagesNo / (double)countedPagesNo;
					double ecomPagesFactorThreshold = new Double(parameters.getProperty("leads-urldirectory-is_ecom_assumption_pages_no-threshold_factor"));
					double ecomPagesToProcessFactor = new Double(parameters.getProperty("leads-urldirectory-is_ecom_assumption_pages_no-factor_to_process"));
					if(ecomPagesFactor >= ecomPagesFactorThreshold) {
						
						// Get n random numbers from the list
						List<EcomPageDictionary> shortEcomPageList;
						if(System.getProperty("prog_version").equals("development")) {
							shortEcomPageList = ecomPageList;
						}
						else {
							int pagesForCheck = (int) (dirPagesNoEcomThreshold * ecomPagesToProcessFactor);
							Collections.shuffle(ecomPageList);
							shortEcomPageList = ecomPageList.subList(0, pagesForCheck);
						}
						
						// <------ Call of the determineExtractionSchema() method
						EcommerceSiteExtractionSchemaDeterminer schemaDeterminer = new EcommerceSiteExtractionSchemaDeterminer();
						boolean isCorrect = schemaDeterminer.determineExtractionSchema(shortEcomPageList);
						// ------>
						
						if(isCorrect) {
							pagesNoMap.put("ecom", ecomPagesNo);
							ecomSiteDictionary = schemaDeterminer.getEcomSiteDictionary();
							productEcomPageList = schemaDeterminer.getProductEcomPageList();
							categoryEcomPageList = schemaDeterminer.getCategoryEcomPageList();
						}
					}
				}
			}
			
			// if site evaluated as Ecom and everything extracted properly
			if(ecomSiteDictionary != null) {
				
				String url = LEADSUtils.fqdnToNutchUrl(fqdn);
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
					
					System.out.println(url + " -> product offering page");
				}
				for(EcomPageDictionary ecomPage : categoryEcomPageList) {						
					family = mapping.getProperty("leads_internal");
					cells = new ArrayList<>();
					cells.add( new Cell(mapping.getProperty("leads_internal-ecom_features"), ecomPage.ecom_features, 0) );
					cells.add( new Cell(mapping.getProperty("leads_internal-page_type"), mapping.getProperty("leads_internal-page_type-ecom_category_page"), 0) );
					url = ecomPage.url;
					timestamp = ecomPage.timestamp;
					dataStore.putLeadsResourceMDFamily(url, timestamp, family, cells);
					
					System.out.println(url + " -> category page");
				}
				
				
			}
			
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		EcomSiteDefinerBatch batch = new EcomSiteDefinerBatch();
		// 1. Get all the fully qualified domain names
		List<String> fqdnList = dataStore.getFQDNList();

		StdLoggerRedirect.initLogging();
		
		// 2. For every FQDN...
		for(String fqdn : fqdnList) {
			batch.execute(fqdn);
		}
	}
	
	
}
