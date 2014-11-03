package eu.leads.infext.proc.realtime.wrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.leads.datastore.datastruct.MDFamily;
import eu.leads.infext.proc.realtime.hook.AbstractHook;

public class AllAtOnceProcessing extends AbstractProcessing {

	public AllAtOnceProcessing(AbstractHook hook) {
		super(hook);
	}

	@Override
	public void process(String url, String timestamp, HashMap<String, HashMap<String,String>> metadata, HashMap<String, MDFamily> editableFamilies) {
		
		/* 2. Process accordingly  */
		HashMap<String, HashMap<String,String>> retrievedMetadata = hook.retrieveMetadata(url, timestamp, metadata, editableFamilies);
		extendMetadata(metadata, retrievedMetadata, true, editableFamilies);
		HashMap<String,HashMap<String, String>> newMetadata = hook.process(metadata);
		extendMetadata(metadata, newMetadata, true, editableFamilies); // the next hack... should be false and solved another way
	}
	
	public HashMap<String, HashMap<String, String>> extendMetadata(HashMap<String, HashMap<String, 
			String>> metadata, HashMap<String, HashMap<String, String>> newMetadata, boolean isRetrievedMetadata, HashMap<String, MDFamily> editableFamilies) {
		for(Entry<String, HashMap<String, String>> newMetaFamilyEntry : newMetadata.entrySet()) {
			String familyKey = newMetaFamilyEntry.getKey();
			HashMap<String, String> newMetaFamily = newMetaFamilyEntry.getValue();
			HashMap<String, String> metaFamily = metadata.get(familyKey);
			if(editableFamilies.containsKey(familyKey) && metadata.containsKey(familyKey)) {
				if(metadata.get(familyKey) == null)
					metadata.put(familyKey, newMetaFamily);
				else {
					for(Entry<String, String> metaColumn : newMetaFamily.entrySet()) {
						String columnKey = metaColumn.getKey();
						String columnValue = metaColumn.getValue();
						metaFamily.put(columnKey, columnValue);
						metadata.put(familyKey, metaFamily);
					}
				}
			}
			else {
				if(isRetrievedMetadata) {
					metadata.put(familyKey, newMetaFamily);
				} 
				else {
					// That means that some component wanted to edit data before it was retrieved from the DB
					throw new IllegalStateException("Stored metadata without checking if it exists in the database");
				}
			}
		}
		return metadata;
	}

}