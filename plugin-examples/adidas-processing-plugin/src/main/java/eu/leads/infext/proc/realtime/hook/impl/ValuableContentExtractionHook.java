package eu.leads.infext.proc.realtime.hook.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import eu.leads.datastore.datastruct.MDFamily;
import eu.leads.infext.proc.com.maincontent.LeadsMainContentExtraction;
import eu.leads.infext.proc.realtime.hook.AbstractHook;

public class ValuableContentExtractionHook extends AbstractHook {
	
	private LeadsMainContentExtraction leadsMainContentExtraction = new LeadsMainContentExtraction();

	@Override
	public HashMap<String, HashMap<String, String>> retrieveMetadata(
			String url, String timestamp,
			HashMap<String, HashMap<String, String>> currentMetadata,
			HashMap<String, MDFamily> editableFamilies) {
		
		HashMap<String, HashMap<String, String>> newMetadata = new HashMap<>();
		
		putLeadsMDIfNeeded(url, "new", "leads_internal", 0, null, currentMetadata, newMetadata, editableFamilies);
		putLeadsMDIfNeeded(url, "new", "leads_resourceparts", 0, null, currentMetadata, newMetadata, editableFamilies);
		
		return newMetadata;
	}

	@Override
	public HashMap<String, HashMap<String, String>> process(
			HashMap<String, HashMap<String, String>> parameters) {
		
		HashMap<String, HashMap<String, String>> result = new HashMap<>();
		HashMap<String, String> newInternalResult = new HashMap<>();
		HashMap<String, String> newResourcePartResult = new HashMap<>();
		
		HashMap<String, String> newMD = parameters.get("new");
		HashMap<String, String> newCrawled = parameters.get("new:leads_crawler_data");
		HashMap<String, String> newInternal = parameters.get("new:leads_internal");
		
		String content = newCrawled.get(mapping.get("leads_crawler_data-content"));
		String candidatesExtractionJSON = newInternal.get(mapping.get("leads_internal-extraction_candidates"));
		
		if(candidatesExtractionJSON != null) {
			HashMap<String, List<String>> extractedValues = leadsMainContentExtraction.extract(content, candidatesExtractionJSON);
			String successfulExtractionJSON = leadsMainContentExtraction.getLastSuccessfulExtractionJSON();
			
			System.out.println("--- EXTRACTION SUCCESSFUL ---");
			System.out.println("Extracted values for keys:" + extractedValues.keySet());
			//System.out.println(successfulExtractionJSON);
			System.out.println("--- ----------------- ---");

			for(Entry<String, List<String>> e : extractedValues.entrySet()) {
				String extractedType = e.getKey();
				List<String> extractedVals = e.getValue();
				int index = 0;
				for(int i=0; i<extractedVals.size(); i++) {
					String val = extractedVals.get(i);
					if(!val.trim().isEmpty()) {
						newResourcePartResult.put(extractedType+String.format(":%03d", index), extractedVals.get(i));
						index++;
					}
				}
			}
			
			newInternalResult.put(mapping.getProperty("leads_internal-successful_extractions"), successfulExtractionJSON);
			result.put("new:leads_internal", newInternalResult);
			result.put("new:leads_resourceparts", newResourcePartResult);
		}
		else {
			System.out.println("--- NOTHING 2 EXTRACT ---");
			System.out.println("--- ----------------- ---");
		}
		
		return result;
	}

}
