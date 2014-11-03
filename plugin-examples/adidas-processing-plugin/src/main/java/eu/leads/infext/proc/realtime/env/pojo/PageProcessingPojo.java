package eu.leads.infext.proc.realtime.env.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.MDFamily;
import eu.leads.infext.proc.realtime.hook.impl.FQDNDefiningHook;
import eu.leads.infext.proc.realtime.hook.impl.KeywordExtractionHook;
import eu.leads.infext.proc.realtime.hook.impl.LanguageDetectionHook;
import eu.leads.infext.proc.realtime.hook.impl.PageCheckHook;
import eu.leads.infext.proc.realtime.hook.impl.TextContentExtractionHook;
import eu.leads.infext.proc.realtime.hook.impl.ValuableContentExtractionHook;
import eu.leads.infext.proc.realtime.wrapper.AbstractProcessing;
import eu.leads.infext.proc.realtime.wrapper.AllAtOnceProcessing;
import eu.leads.utils.LEADSUtils;


public class PageProcessingPojo extends AbstractExecutionPojo {
	
	public PageProcessingPojo() throws Exception {
		AbstractProcessing fqdnProc			  = new AllAtOnceProcessing(new FQDNDefiningHook());
		AbstractProcessing textContentProc 	  = new AllAtOnceProcessing(new TextContentExtractionHook());
		AbstractProcessing languageProc       = new AllAtOnceProcessing(new LanguageDetectionHook());
		AbstractProcessing pageContentCheck   = new AllAtOnceProcessing(new PageCheckHook());
		AbstractProcessing extractionProc	  = new AllAtOnceProcessing(new ValuableContentExtractionHook());
		AbstractProcessing keywordExtr		  = new AllAtOnceProcessing(new KeywordExtractionHook());
		processingQueue.add(fqdnProc);
		processingQueue.add(textContentProc);
		processingQueue.add(languageProc);
		processingQueue.add(pageContentCheck);
		processingQueue.add(extractionProc);
		processingQueue.add(keywordExtr);
	}

	@Override
	public void execute(String uri, String timestamp, String cacheName, HashMap<String, String> cacheColumns) {
		
		HashMap<String, String> newMain = new HashMap<>();
		newMain.put("uri", uri);
		newMain.put("timestamp", timestamp);
		
		HashMap<String, HashMap<String, String>> metadata = new HashMap<>();
		metadata.put("new",	newMain);
		metadata.put("new:"+LEADSUtils.propertyValueToKey(mapping,cacheName), cacheColumns);
		HashMap<String,MDFamily> editableFamilies = new HashMap<>();
		
		try {
			/* Process */
			System.out.println("Processing...");
			for(AbstractProcessing proc : processingQueue)
				proc.process(uri, timestamp, metadata, editableFamilies);
			
			/* Store */
			System.out.println("Storing...");
			store(metadata, editableFamilies, timestamp);
			
		} catch (IllegalStateException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void store(HashMap<String, HashMap<String, String>> metadata, HashMap<String,MDFamily> editableFamilies, String timestamp) {
		
		String now = timestamp;
		
		for(Entry<String,MDFamily> editedFamily : editableFamilies.entrySet()) {
			String familyKey = editedFamily.getKey();
			
			String url = editedFamily.getValue().urlTimestamp.url;
			String ts = editedFamily.getValue().urlTimestamp.timestamp == null ? now : editedFamily.getValue().urlTimestamp.timestamp;
			String family = editedFamily.getValue().family;
			String familyName = mapping.getProperty(family);
			System.out.println(familyKey);
			System.out.println(url);
			System.out.println(ts);
			System.out.println(familyName);
			
			if(familyKey.equals("new:leads_resourceparts")) {
				HashMap<String, String> resourceParts = metadata.get("new:leads_resourceparts");
				
				HashMap<String, Object> partsTypeValuesMap = new HashMap<>();
				for(Entry<String, String> resPart : resourceParts.entrySet()) {
					String resTypeNIndex = resPart.getKey();
//					String resType = resTypeNIndex.substring(0, resTypeNIndex.length()-4); // cut ':xxx'
//					String restIndex = resTypeNIndex.substring(0, resTypeNIndex.length()-4);
					String resValue = resPart.getValue();
					partsTypeValuesMap.put(resTypeNIndex, resValue);
				}
				DataStoreSingleton.getDataStore().putLeadsResourcePartsMD(url, ts, partsTypeValuesMap);
			}
			else if(familyKey.equals("new:leads_keywords")) {
				HashMap<String, String> keywords = metadata.get("new:leads_keywords");
				
				for(String key : keywords.keySet()) {
					String [] keyparts = key.split(":");
					String element = keyparts[2];
					String partid = keyparts[0]+":"+keyparts[1];
					HashMap<String, String> keywordFamilyMap = metadata.get("new:leads_keywords:"+key);
					List<Cell> cells = new ArrayList<>();
					for(Entry<String, String> newMetaColumn : keywordFamilyMap.entrySet()) {
						cells.add(new Cell(newMetaColumn.getKey(), newMetaColumn.getValue(), 0));
						System.out.println(newMetaColumn.getKey()+" -> "+ (newMetaColumn.getValue().length()>80 ? newMetaColumn.getValue().replace("\n", "").replace("\r", "").substring(0, 80) : newMetaColumn.getValue()));
					}					
					DataStoreSingleton.getDataStore().putLeadsResourceElementsMDFamily(url, ts, partid, element, null, cells);
				}
				
			}
			else {
				if(familyKey.startsWith("new:leads_keywords:")) continue;
				
				HashMap<String,String> mdFamilyMap = metadata.get(familyKey);
				
				List<Cell> cells = new ArrayList<>();
				for(Entry<String, String> newMetaColumn : mdFamilyMap.entrySet()) {
					if(newMetaColumn.getValue() != null && !newMetaColumn.getKey().equals("uri") && !newMetaColumn.getKey().equals("ts")) {
					cells.add(new Cell(newMetaColumn.getKey(), newMetaColumn.getValue(), 0));
					System.out.println(newMetaColumn.getKey()+" -> "+ (newMetaColumn.getValue().length()>80 ? newMetaColumn.getValue().replace("\n", "").replace("\r", "").substring(0, 80) : newMetaColumn.getValue()));
					}
				}
				
				DataStoreSingleton.getDataStore().putLeadsResourceMDFamily(url, ts, familyName, cells);
			}
			
		}
		

		
		
//		for(Entry<String, HashMap<String, String>> metaFamilyEntry : metadata.entrySet()) {
//			
//			String familyKey = metaFamilyEntry.getKey();
//			HashMap<String, String> newMetaFamily = metaFamilyEntry.getValue();
//			String [] familyKeyParts = familyKey.split(":");
//			if(familyKeyParts.length == 2) {
//				
//				String version = familyKeyParts[0];
//				String family  = familyKeyParts[1];
//				
//				if(version.equals("new")) {
//					family =  mapping.getProperty(family);
//					System.out.println(family);			
//					
//					List<Cell> cells = new ArrayList<>();
//					for(Entry<String, String> newMetaColumn : newMetaFamily.entrySet()) {
//						cells.add(new Cell(newMetaColumn.getKey(), newMetaColumn.getValue(), 0));
//						System.out.println(newMetaColumn.getKey()+" -> "+ (newMetaColumn.getValue().length()>80 ? newMetaColumn.getValue().replace("\n", "").replace("\r", "").substring(0, 80) : newMetaColumn.getValue()));
//					}
//					
//					DataStoreSingleton.getDataStore().putLeadsResourceMDFamily(url, ts, family, cells);
//				}
//			}
//		}	
		
		System.out.println();
		
	}

//	public static void main(String[] args) {
//		PageProcessingPojo ep = new PageProcessingPojo();
//		ep.execute();
//	}

}
