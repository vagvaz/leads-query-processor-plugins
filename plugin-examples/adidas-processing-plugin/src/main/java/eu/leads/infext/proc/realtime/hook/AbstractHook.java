package eu.leads.infext.proc.realtime.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;

import eu.leads.datastore.AbstractDataStore;
import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.MDFamily;
import eu.leads.datastore.datastruct.URIVersion;
import eu.leads.utils.LEADSUtils;

public abstract class AbstractHook {
	
	protected Properties mapping = DataStoreSingleton.getMapping();	
	AbstractDataStore abstractDataStore;

//	String queryTempl = "SELECT %s FROM website WHERE url='%s'";
//	String queryTimeTempl = queryTempl + " AND timestamp='%s'";
//	String fieldTempl = "%s AS %s";

	public abstract HashMap<String, HashMap<String, String>> retrieveMetadata(String url, String timestamp, HashMap<String, HashMap<String, String>> currentMetadata, HashMap<String, MDFamily> editableFamilies);
	public abstract HashMap<String, HashMap<String, String>> process(HashMap<String, HashMap<String, String>> parameters);
	
	private List<String> nonRetrievableFamilies = new ArrayList<String>()
			{{ add("leads_resourceparts"); add("leads_keywords"); }};
	
	public void resetMapping(Properties mapping) {
		this.mapping = mapping;
	}
			
	/**
	 * 
	 * @param url
	 * @param versionName
	 * @param family
	 * @param versionOfInterest: 0 for the newest one, -1,-2,... for previous ones
	 * @param beforeTimestamp
	 * @param currentMetadata
	 * @param editableFamilies - if null, the family will be "read-only"
	 * @return
	 */
	protected void putLeadsMDIfNeeded(String url, String versionName, String family, int versionOfInterest, 
			String beforeTimestamp, HashMap<String, HashMap<String, String>> currentMetadata,  
			HashMap<String, HashMap<String, String>> newMetadata, HashMap<String, MDFamily> editableFamilies) {
		
		String familyKey = versionName+":"+family;
		int lastVersionsNeeded = 1 - versionOfInterest;
		
		HashMap<String, String> newMetadataFamily = null;
		
		if(currentMetadata.get(familyKey) == null) {
			newMetadataFamily = new HashMap<String, String>();
			
			if(nonRetrievableFamilies.contains(family)) {
				System.err.println("Not retrieving from family "+family);
				newMetadata.put(familyKey, newMetadataFamily);
				MDFamily mdFamily = new MDFamily(url,null,family);
				editableFamilies.put(familyKey, mdFamily);
			}
			else {
				String ts = null;
				
				SortedSet<URIVersion> uriVersionSet = DataStoreSingleton.getDataStore().getLeadsResourceMDFamily(url, mapping.getProperty(family), lastVersionsNeeded, beforeTimestamp);
				if(uriVersionSet.size() == lastVersionsNeeded) {
					List<URIVersion> uriVersionList = new ArrayList<>(uriVersionSet);
					URIVersion uriVersion = uriVersionList.get(-versionOfInterest);
					ts = uriVersion.getTimestamp();
					for(Entry<String, Cell> e : uriVersion.getFamily().entrySet()) {
						Cell cell = e.getValue();
						newMetadataFamily.put(cell.getKey(), cell.getValue()==null ? null : cell.getValue().toString());
					}
				}
				if(editableFamilies != null) {
					MDFamily mdFamily = new MDFamily(url,ts,family);
					editableFamilies.put(familyKey, mdFamily);
				}
				newMetadata.put(familyKey, newMetadataFamily);
			 }
		}
	}
	
//	public HashMap<String,String> getFromDatabase(String family, String uri, int version, List<String> fieldsGeneralList, List<String> fieldsAliasesList) {
//		SortedSet<URIVersion> leadsResourceMDFamily = DataStoreSingleton.getDataStore().getLeadsResourceMDFamily(uri, family, 1, null);
//		URIVersion latestVersion = 
//	}
	
//	public void updateMetadata(String url, String timestamp,
//			HashMap<String, HashMap<String, String>> newVals) {
//		
//		for(Entry<String, HashMap<String, String>> row : newVals.entrySet()) {
//			
//			String key = row.getKey();
//			
//			switch (key) {
//				case "new":
//					DataStoreSingleton.getDataStore().putToDatabase(url,timestamp,row.getValue());
//				case "general0":
//					DataStoreSingleton.getDataStore().putToDatabase(LEADSUtils.getDomainName(url),timestamp,row.getValue());
//			}
//			
//		}
//		
//	};
	
}