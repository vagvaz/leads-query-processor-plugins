package eu.leads.infext.proc.batch.mapreduce;

import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;

import org.infinispan.distexec.mapreduce.Collector;
import org.infinispan.distexec.mapreduce.Mapper;

import eu.leads.datastore.AbstractDataStore;
import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.URIVersion;
import eu.leads.infext.proc.com.categorization.ecom.EcommerceClassification;
import eu.leads.infext.proc.com.categorization.ecom.model.EcomPageDictionary;

public class EcomSiteDefinerMapper implements Mapper<Object, Object, Object, Object> {
	
	AbstractDataStore dataStore = DataStoreSingleton.getDataStore();
	Properties mapping = DataStoreSingleton.getMapping();
	
	
	@Override
	public void map(Object key, Object noOneCares, Collector<Object, Object> collector) {
		
		String uri = (String) key;
		
		String content = null;
		String lang = null;
		String timestamp1 = null;
		String timestamp2 = null;
		
		EcomPageDictionary ecomPageDictionary = null;
		
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
			EcommerceClassification ecomClassification = new EcommerceClassification(content, lang);
			boolean isCorrect = ecomClassification.determinePageEcomFeatures();
			// ------>
			
			System.out.println(uri+" -> "+ecomClassification.isEcomAssumption()+" -> "+ecomClassification.getEcomFeatures());
			
			if(isCorrect) {
				if(ecomClassification.isEcomAssumption()) {
					ecomPageDictionary = new EcomPageDictionary();
					ecomPageDictionary.url = uri;
					ecomPageDictionary.timestamp = timestamp1;
					ecomPageDictionary.lang = lang;
					ecomPageDictionary.content = content;
					ecomPageDictionary.ecom_features = ecomClassification.getEcomFeatures();
					ecomPageDictionary.basketnode = ecomClassification.getBasketNode();
					ecomPageDictionary.buttonnode = ecomClassification.getButtonNode();

					collector.emit(key, ecomPageDictionary);
				}
			}
		}
		
	}

}
