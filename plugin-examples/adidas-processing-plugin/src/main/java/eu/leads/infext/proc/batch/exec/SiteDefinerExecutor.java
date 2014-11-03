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
import eu.leads.infext.proc.batch.exec.part.AbstractPartialSiteDefiner;
import eu.leads.infext.proc.batch.exec.part.EcomSiteDefiner;
import eu.leads.infext.proc.batch.exec.part.BlogNewsSiteDefiner;
import eu.leads.infext.proc.batch.mapreduce.DefaultReducer;
import eu.leads.infext.proc.batch.mapreduce.EcomSiteDefinerMapper;
import eu.leads.infext.proc.com.categorization.ecom.EcommerceSiteExtractionSchemaDeterminer;
import eu.leads.infext.proc.com.categorization.ecom.model.EcomPageDictionary;
import eu.leads.infext.proc.com.categorization.ecom.model.EcomSiteDictionary;
import eu.leads.utils.LEADSUtils;


/***
 * 
 * The batch process defining the site to be run in the LEADS environment
 * 
 * @author amo remix
 *
 */
public class SiteDefinerExecutor {
	
	
	private static AbstractDataStore dataStore = DataStoreSingleton.getDataStore();
	private static Properties mapping = DataStoreSingleton.getMapping();
	private static Properties parameters = DataStoreSingleton.getParameters();
	
    
	public static void main(String[] args) throws Exception {
		
		String fqdn = args[0];
		
		StdLoggerRedirect.initLogging();
		
		/////////////////////////
		/////////////////////////
		/////////////////////////
		
		// a. see if already defined
		SortedSet<URIVersion> dirMdFamilyVersions = dataStore.getLeadsResourceMDFamily(fqdn, mapping.getProperty("leads_urldirectory"), 1, null);
		
		boolean isEcom = false;
		boolean isNewsOrBlog = true;
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
				else if (Arrays.asList(mapping.getProperty("leads_urldirectory-dir_assumption-google_news"), 
									   mapping.getProperty("leads_urldirectory-dir_assumption-news"), 
									   mapping.getProperty("leads_urldirectory-dir_assumption-blog"))
									   	.contains(dirAssumption))	{
					isNewsOrBlog = true;
				}
				isKnown = true;
			}
		}
		
		// b. if unknown...
		if(!isKnown) {
			
			List<String> dirUris = dataStore.getResourceURIsOfDirectory(fqdn);
			int dirPagesNo = dirUris.size();
			HashMap<String,Integer> pagesNoMap = new HashMap<>();
			pagesNoMap.put("", dirPagesNo);
			
			// Check if Ecom
			AbstractPartialSiteDefiner partDefiner = new EcomSiteDefiner(fqdn,pagesNoMap,dirUris);
			isEcom = partDefiner.defineAndStore();
			
			if(!isEcom) {	
				// Check if in the Google News stream
				partDefiner = new BlogNewsSiteDefiner(fqdn,pagesNoMap,dirUris);
				isNewsOrBlog = partDefiner.defineAndStore(); // <- add inside "article_extraction":"boilerpipe"
			}
			
		}
				
		/////////////////////////
		/////////////////////////
		/////////////////////////
   	
	}
	
	
	
}
