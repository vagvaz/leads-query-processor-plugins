package eu.leads.infext.proc.realtime.hook.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.MDFamily;
import eu.leads.datastore.datastruct.URIVersion;
import eu.leads.infext.proc.com.maincontent.JerichoTextContentExtraction;
import eu.leads.infext.proc.realtime.hook.AbstractHook;

public class TextContentExtractionHook extends AbstractHook {
	
	JerichoTextContentExtraction textExtr = new JerichoTextContentExtraction();

	@Override
	public HashMap<String, HashMap<String, String>> retrieveMetadata(String url, String timestamp, 
			HashMap<String, HashMap<String, String>> currentMetadata, HashMap<String, MDFamily> editableFamilies) {
			
		HashMap<String, HashMap<String, String>> newMetadata = new HashMap<>();

		putLeadsMDIfNeeded(url, "new", "leads_crawler_data", 0, null, currentMetadata, newMetadata, null);
		putLeadsMDIfNeeded(url, "new", "leads_core", 0, null, currentMetadata, newMetadata, editableFamilies);
		
		return newMetadata;
	}
	
	
	@Override
	public HashMap<String, HashMap<String, String>> process(HashMap<String, HashMap<String, String>> parameters) {
		
		HashMap<String, HashMap<String, String>> result = null;
		
		HashMap<String, String> newMetadata = parameters.get("new:leads_crawler_data");
		if(newMetadata!=null) {
			String content = newMetadata.get(mapping.getProperty("leads_crawler_data-content"));

			result = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> newVersionResult = new HashMap<String, String>();
			
			String textContent = textExtr.extractText(content);
			
			newVersionResult.put(mapping.getProperty("leads_core-textcontent"), textContent);
			newVersionResult.put(mapping.getProperty("leads_core-sentiment"), "0.0");
			result.put("new:leads_core", newVersionResult);
		}
		
		return result;
	}
	

//	@Override
//	public HashMap<String, HashMap<String, String>> retrieveMetadata(String url, String timestamp, HashMap<String, HashMap<String, String>> currentMetadata) {
//			
//		HashMap<String, HashMap<String, String>> newMetadata = new HashMap<>();
//
//		String familyKey = "new:leads_crawler_data";
//		if(currentMetadata.get(familyKey) == null) {
//			HashMap<String, String> crawlerMetadata = new HashMap<String, String>();
//			URIVersion uriVersion = DataStoreSingleton.getDataStore().getLeadsResourceMDFamily(url, mapping.getProperty("leads_crawler_data"), 1, null).last();
//			for(Entry<String, Cell> e : uriVersion.getFamily().entrySet()) {
//				Cell cell = e.getValue();
//				crawlerMetadata.put(cell.getKey(), (String) cell.getValue());
//			}
//			newMetadata.put("new:leads_crawler_data", crawlerMetadata);
//		}
//		
//		return newMetadata;
//	}

}
