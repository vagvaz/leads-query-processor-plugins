package eu.leads.infext.proc.realtime.hook.impl;

import java.util.HashMap;

import eu.leads.datastore.datastruct.MDFamily;
import eu.leads.infext.proc.realtime.hook.AbstractHook;
import eu.leads.utils.LEADSUtils;

public class FQDNDefiningHook extends AbstractHook {

	@Override
	public HashMap<String, HashMap<String, String>> retrieveMetadata(
			String url, String timestamp,
			HashMap<String, HashMap<String, String>> currentMetadata,
			HashMap<String, MDFamily> editableFamilies) {
		
		HashMap<String, HashMap<String, String>> newMetadata = new HashMap<>();

		putLeadsMDIfNeeded(url, "new", "leads_core", 0, null, currentMetadata, newMetadata, editableFamilies);
		
		return newMetadata;
	}

	@Override
	public HashMap<String, HashMap<String, String>> process(
			HashMap<String, HashMap<String, String>> parameters) {
		
		HashMap<String, String> newVersionParams = parameters.get("new:leads_core");

		HashMap<String, String> newVersionResult = new HashMap<String, String>();
		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
		
		HashMap<String, String> newPage = parameters.get("new");
		String url = newPage.get("uri");
		
		String fqdn = LEADSUtils.nutchUrlToFullyQualifiedDomainNameUrl(url);
		
		newVersionResult.put(mapping.getProperty("leads_core-fqdnurl"), fqdn);
		
		result.put("new:leads_core", newVersionResult);		
		
		return result;
	}

}
