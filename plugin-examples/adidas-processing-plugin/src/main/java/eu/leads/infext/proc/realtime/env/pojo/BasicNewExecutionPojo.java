package eu.leads.infext.proc.realtime.env.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.MDFamily;
import eu.leads.infext.proc.realtime.hook.impl.FQDNDefiningHook;
import eu.leads.infext.proc.realtime.hook.impl.LanguageDetectionHook;
import eu.leads.infext.proc.realtime.hook.impl.PageCheckHook;
import eu.leads.infext.proc.realtime.hook.impl.TextContentExtractionHook;
import eu.leads.infext.proc.realtime.wrapper.AbstractProcessing;
import eu.leads.infext.proc.realtime.wrapper.AllAtOnceProcessing;
import eu.leads.utils.LEADSUtils;


public class BasicNewExecutionPojo extends AbstractExecutionPojo {
	
	public BasicNewExecutionPojo() throws Exception {
		AbstractProcessing fqdnProc			  = new AllAtOnceProcessing(new FQDNDefiningHook());
		AbstractProcessing textContentProc 	  = new AllAtOnceProcessing(new TextContentExtractionHook());
		AbstractProcessing languageProc       = new AllAtOnceProcessing(new LanguageDetectionHook());
		processingQueue.add(fqdnProc);
		processingQueue.add(textContentProc);
		processingQueue.add(languageProc);
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
			store(uri, metadata, timestamp);
			
		} catch (IllegalStateException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void store(String url, HashMap<String, HashMap<String, String>> metadata, String ts) {
		
		System.out.println(url);
		System.out.println(ts);
		
		for(Entry<String, HashMap<String, String>> metaFamilyEntry : metadata.entrySet()) {
			String familyKey = metaFamilyEntry.getKey();
			if(!familyKey.equals("new")) {
				HashMap<String, String> newMetaFamily = metaFamilyEntry.getValue();
				String family = familyKey.split(":")[1];
				family =  mapping.getProperty(family);
				System.out.println(family);			
				
				List<Cell> cells = new ArrayList<>();
				for(Entry<String, String> newMetaColumn : newMetaFamily.entrySet()) {
					cells.add(new Cell(newMetaColumn.getKey(), newMetaColumn.getValue(), 0));
					System.out.print(newMetaColumn.getKey());
					System.out.print(" -> ");
					if(newMetaColumn.getValue()==null)
						System.out.print("null");
					else if(newMetaColumn.getValue().length()>80) {
						System.out.print(newMetaColumn.getValue().replace("\n", "").replace("\r", "").substring(0, 80));
					}
					else {
						System.out.print(newMetaColumn.getValue());
					}
					System.out.println();
				}
				DataStoreSingleton.getDataStore().putLeadsResourceMDFamily(url, ts, family, cells);
			}
		}	
		
		System.out.println();
		
	}

//	public static void main(String[] args) {
//		PageProcessingPojo ep = new PageProcessingPojo();
//		ep.execute();
//	}

}
