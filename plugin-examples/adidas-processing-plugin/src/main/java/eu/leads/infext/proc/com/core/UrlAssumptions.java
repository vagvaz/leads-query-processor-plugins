package eu.leads.infext.proc.com.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.leads.datastore.DataStoreSingleton;
import eu.leads.utils.LEADSUtils;

public class UrlAssumptions {
	
	Properties mapping = DataStoreSingleton.getMapping();

	public UrlAssumptions() {}
	
	/**
	 * 
	 * @param urlFQDNParameters
	 * @return
	 */
	public List<String> getFQDNAssumption(HashMap<String,String> urlFQDNParameters) {
//		List<String> assumptionsLists = new ArrayList<>();
//		
//		long currentTimestamp = LEADSUtils.getTimestamp();
//		
//		List<String> assumptions = new ArrayList<>();
//		
//		boolean countingNeeded = false;
//		
//		String updateTimestampStr = urlFQDNParameters.get(mapping.get("dirAssumptionUpdateTS"));
//		String assumptionsListJsonString = urlFQDNParameters.get(mapping.get("dirAssumption"));
//		if(updateTimestampStr == null || assumptionsListJsonString == null) countingNeeded = true;
//		Long updateTimestamp = new Long(updateTimestampStr);
//		if(currentTimestamp-updateTimestamp > 1000*60*60) countingNeeded = true;
//		
//		if(countingNeeded)
//			assumptions = countUrlAssumptionsForDirectory(urlFQDNParameters);
//		else
//			assumptions = getCurrentAssumptions(assumptionsListJsonString);
//		
//		assumptionsLists.add(assumptions);
//		
//		if(/*assumptions.get(0).equals(mapping.getProperty("mapping_none")) && assumptions.size() == 1 
//				||*/ assumptions.isEmpty())
//			continue;
//		else
//				break;
//
//		return assumptionsLists;
		
		List<String> assumptionsList = new ArrayList<>();
		String assump = urlFQDNParameters.get(mapping.getProperty("leads_urldirectory-dir_assumption"));
		if(assump != null)
			assumptionsList.add(assump);
		
		return assumptionsList;
	}
	
	/**
	 * 
	 * @param urlDirsParametersList - list of parameters for parent directories of the url (starting from the deepest directory)
	 * @return
	 */
	public List<List<String>> getUrlAssumptionsFromDirectoriesMD(List<HashMap<String,String>> urlDirsParametersList) {
		List<List<String>> assumptionsLists = new ArrayList<>();
		
		long currentTimestamp = LEADSUtils.getTimestamp();
		
		for(HashMap<String,String> urlDirParameters : urlDirsParametersList) {
			List<String> assumptions = new ArrayList<>();
			
			boolean countingNeeded = false;
			
			String updateTimestampStr = urlDirParameters.get(mapping.get("dirAssumptionUpdateTS"));
			String assumptionsListJsonString = urlDirParameters.get(mapping.get("dirAssumption"));
			if(updateTimestampStr == null || assumptionsListJsonString == null) countingNeeded = true;
			Long updateTimestamp = new Long(updateTimestampStr);
			if(currentTimestamp-updateTimestamp > 1000*60*60) countingNeeded = true;
			
			if(countingNeeded)
				assumptions = countUrlAssumptionsForDirectory(urlDirParameters);
			else
				assumptions = getCurrentAssumptions(assumptionsListJsonString);
			
			assumptionsLists.add(assumptions);
			
			if(/*assumptions.get(0).equals(mapping.getProperty("mapping_none")) && assumptions.size() == 1 
					||*/ assumptions.isEmpty())
				continue;
			else
				break;
		}

		return assumptionsLists;
	}
	
	/**
	 * 
	 * @param urlDirParameters - parameters for one directory
	 * @return list of assumptions
	 */
	public List<String> countUrlAssumptionsForDirectory(HashMap<String,String> urlDirParameters) {
		List<String> assumptions = new ArrayList<String>();
		
		String pagesInDirCountString 		= urlDirParameters.get(mapping.get("pagesInDirCount"));
		String POEcomPagesInDirCountString 	= urlDirParameters.get(mapping.get("POEcomPagesInDirCount"));
		String EcomPagesInDirCountString   	= urlDirParameters.get(mapping.get("EcomPagesInDirCount"));
		long pagesInDirCount = LEADSUtils.stringToLong(pagesInDirCountString);
		long POEcomPagesInDirCount = LEADSUtils.stringToLong(POEcomPagesInDirCountString);
		long EcomPagesInDirCount = LEADSUtils.stringToLong(EcomPagesInDirCountString);
		
		String ecomPOAssumptionCoefString   = urlDirParameters.get(mapping.get("ecomPOAssumptionCoef"));
		String ecomPOVariaAssumptionCoefString= urlDirParameters.get(mapping.get("ecomPOVariaAssumptionCoef"));
		String ecomVariaAssumptionCoefString= urlDirParameters.get(mapping.get("ecomVariaAssumptionCoef"));
		double ecomPOAssumptionCoef   = LEADSUtils.stringToDouble(ecomPOAssumptionCoefString);
		double ecomPOVariaAssumptionCoef= LEADSUtils.stringToDouble(ecomPOVariaAssumptionCoefString);
		double ecomVariaAssumptionCoef= LEADSUtils.stringToDouble(ecomVariaAssumptionCoefString);
		
		String minPagesInDirCountString   = urlDirParameters.get(mapping.get("minPagesInDirCount"));
		long minPagesInDirCount = LEADSUtils.stringToLong(minPagesInDirCountString);
		
		double ecomPOValue = POEcomPagesInDirCount / pagesInDirCount;
		double ecomVariaValue = EcomPagesInDirCount / pagesInDirCount;
		
		// if more that minimum number of pages to count
		if(pagesInDirCount > minPagesInDirCount) {
			// if can be assumed that is Ecom product offerings dir
			if(ecomPOValue > ecomPOAssumptionCoef)
				assumptions.add(mapping.getProperty("ecomPO"));
			// if can be assumed that is a mix of Ecom pages
			else if(ecomVariaValue > ecomVariaAssumptionCoef) {
				if(ecomPOValue > ecomPOVariaAssumptionCoef)
					assumptions.add(mapping.getProperty("ecomPOVaria"));
				else
					assumptions.add(mapping.getProperty("ecomVaria"));
			}
			// TODO to be extended
			else {
				assumptions.add(mapping.getProperty("unidentified"));
			}
		}
		// if less than minimum number, go to the parent directory
//		else {
//			assumptions.add(mapping.getProperty("none"));
//		}
		
		// TODO store assumptions in DB
		
		return assumptions;
		
	}
	
	private List<String> getCurrentAssumptions(String assumptionsListJsonString) {
		List<String> currentAssumption = new ArrayList<>();
		try {
			JSONArray jsonArray = new JSONArray(assumptionsListJsonString);
			for(int i=0; i<jsonArray.length(); i++) {
				String assum = jsonArray.getString(i);
				currentAssumption.add(assum);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return currentAssumption;
	}
	
}
