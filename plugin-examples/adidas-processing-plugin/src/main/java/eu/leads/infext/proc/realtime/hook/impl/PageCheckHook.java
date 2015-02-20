package eu.leads.infext.proc.realtime.hook.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.MDFamily;
import eu.leads.datastore.datastruct.URIVersion;
import eu.leads.infext.proc.com.categorization.ecom.EcomClassificationEnum;
import eu.leads.infext.proc.com.categorization.ecom.EcommerceClassification;
import eu.leads.infext.proc.com.categorization.ecom.EcommerceNewPageTypeEvaluation;
import eu.leads.infext.proc.com.categorization.newsblog.NewsBlogArticleAnalysis;
import eu.leads.infext.proc.com.core.UrlAssumptions;
import eu.leads.infext.proc.realtime.hook.AbstractHook;
import eu.leads.infext.python.PythonQueueCall;
import eu.leads.utils.LEADSUtils;

public class PageCheckHook extends AbstractHook {

	@Override
	public HashMap<String, HashMap<String, String>> retrieveMetadata(String url, String timestamp, 
			HashMap<String, HashMap<String, String>> currentMetadata, HashMap<String, MDFamily> editableFamilies) {
		
		HashMap<String, HashMap<String, String>> newMetadata = new HashMap<>();
		
		putLeadsMDIfNeeded(url, "new", "leads_internal", 0, null, currentMetadata, newMetadata, editableFamilies);
		putLeadsMDIfNeeded(url, "new", "leads_core", 0, null, currentMetadata, newMetadata, editableFamilies);
		putLeadsMDIfNeeded(url, "previous", "leads_internal", -1, timestamp, currentMetadata, newMetadata, null);
		
		List<String> urlGeneralizations = LEADSUtils.getAllResourceGeneralizations(url);
		int number = 0;
		for(String genUrl : urlGeneralizations) {
			putLeadsMDIfNeeded(genUrl, "general"+number, "leads_urldirectory", 0, timestamp, currentMetadata, newMetadata, null);
			putLeadsMDIfNeeded(genUrl, "general"+number, "leads_urldirectory_ecom", 0, timestamp, currentMetadata, newMetadata, null);
			number += 1;
		}
		
		return newMetadata;
	}
	

	@Override
	public HashMap<String, HashMap<String, String>> process(HashMap<String, HashMap<String, String>> parameters) {
		
		HashMap<String, HashMap<String, String>> result = new HashMap<>();
		HashMap<String, String> newInternal = new HashMap<>();
		HashMap<String, String> newCore     = new HashMap<>();
		
		HashMap<String,String> newParameters = parameters.get("new");
		HashMap<String,String> newCoreParameters = parameters.get("new:leads_core");
		HashMap<String,String> newCrawlParameters = parameters.get("new:leads_crawler_data");
		
		HashMap<String,String> previousParameters = parameters.get("previous:leads_internal");
		List<HashMap<String,String>> generalParametersList = LEADSUtils.getMetadataOfDirectories(parameters,"leads_urldirectory");
		List<HashMap<String,String>> generalEcomParametersList = LEADSUtils.getMetadataOfDirectories(parameters,"leads_urldirectory_ecom");
		
		String url     = newParameters.get("uri");
		String lang    = newCoreParameters.get(mapping.getProperty("leads_core-lang"));
		String content = newCrawlParameters.get(mapping.getProperty("leads_crawler_data-content"));
		
		String previousVersionType = null;
		// Is that a new version of the previously-crawled page?
		if(previousParameters != null && !previousParameters.isEmpty())
			previousVersionType = previousParameters.get(mapping.getProperty("leads_internal-page_type"));
		
		String extractionCandidatesJSONString = null;
		String ecomTypeAssumption = null;
		String pageType = mapping.getProperty("leads_internal-page_type-none");
		String ecomFeatures = null;
		String contentDate = null;
		
		boolean longPathNeeded = false;
		
		if(previousVersionType != null) { 
			if(previousVersionType.equals(mapping.getProperty("leads_internal-page_type-ecom_product_offering_page"))) {
				// QUICK PATH: copy successful schemas to the schema candidates
				extractionCandidatesJSONString = ecomFindExtractionQuickPath(previousParameters);
				
				if(extractionCandidatesJSONString == null)
					longPathNeeded = true;
			}
			else if(previousVersionType.equals(mapping.getProperty("leads_internal-page_type-ecom_category_page"))) {
				pageType = previousVersionType;
				longPathNeeded = false;
			}
			else if(previousVersionType.equals(mapping.getProperty("leads_internal-page_type-newsblog_article"))) {
				extractionCandidatesJSONString = LEADSUtils.prepareExtractionCandidatesJSONString(
						new String[]{"boilerpipe"}, new String[]{"article_content"});
				
				pageType = previousVersionType;
				longPathNeeded = false;
			}
			else if(previousVersionType.equals(mapping.getProperty("leads_internal-page_type-newsblog_other"))) {
				pageType = previousVersionType;
				longPathNeeded = false;
			}
		}
		else {
			longPathNeeded = true;
		}
		
		if(longPathNeeded) {
			// LONG PATH: determine if it is product offering, then try to determine schema
			 String [] returnStringsArray = ecomFindExtractionLongPath(content, lang, 
					generalParametersList, generalEcomParametersList, previousVersionType);
			 ecomTypeAssumption = returnStringsArray[1];
			 if( EcomClassificationEnum.ECOM_PRODUCT_OFFERING_PAGE.toString().equals(ecomTypeAssumption)) {
				 pageType = mapping.getProperty("leads_internal-page_type-ecom_product_offering_page");
				 extractionCandidatesJSONString = returnStringsArray[0];
			 }
			 else if( EcomClassificationEnum.ECOM_CATEGORY_PAGE.toString().equals(ecomTypeAssumption)) {
				 pageType = mapping.getProperty("leads_internal-page_type-ecom_category_page");
			 }
			 else {
				 contentDate = checkForNewsblogArticlePage(content, url);
				 if(contentDate != null) {
					 pageType = mapping.getProperty("leads_internal-page_type-newsblog_article");
					 extractionCandidatesJSONString = LEADSUtils.prepareExtractionCandidatesJSONString(
								new String[]{"boilerpipe"}, new String[]{"article_content"});
				 }
			 }
			 ecomFeatures = returnStringsArray[2];
		}
		
		// at an end, STORE extraction schema(s) candidates
		if(extractionCandidatesJSONString != null)
			newInternal.put(mapping.getProperty("leads_internal-extraction_candidates"), extractionCandidatesJSONString);
		if(ecomTypeAssumption != null)
			newInternal.put(mapping.getProperty("leads_internal-page_type"), pageType);
		if(ecomFeatures != null)
			newInternal.put(mapping.getProperty("leads_internal-ecom_features"), ecomFeatures);
		if(contentDate != null)
			newCore.put(mapping.getProperty("leads_core-contentdate"), contentDate);
		
		if(!newInternal.isEmpty()) result.put("new:leads_internal", newInternal);
		if(!newCore.isEmpty()) result.put("new:leads_core", newCore);
		
		return result;
//		if(extractionSchemaCandidatesJSON != null)
//			newParameters.put("leads_internal-extraction_candidates", extractionSchemaCandidatesJSON);
//		newParameters.put("leads_internal-page_type", ecomTypeAssumption);
//		newParameters.put("leads_internal-ecom_features", ecomFeatures);
//		parameters.put("new", newParameters);
//		
//		return parameters;
		
	}


	private String checkForNewsblogArticlePage(String content, String url) {
		return NewsBlogArticleAnalysis.getPublishDate(content, url);
	}


	private String ecomFindExtractionQuickPath(HashMap<String,String> previousParameters) {
		System.out.println("-> Quick extraction path");
		String successfulSchemasJsonString = previousParameters.get(mapping.get("leads_internal-successful_extractions"));
		return successfulSchemasJsonString;
	}
	
	
	private String [] ecomFindExtractionLongPath(String content, String lang, List<HashMap<String,String>> generalParametersList, 
			List<HashMap<String,String>> generalEcomParametersList, String previousVersionType) {
		System.out.println("-> Long extraction path");
		String extractionCandidatesJSONString = null;
		
		HashMap<String, String> fqdnParameters = null;
		HashMap<String,String> fqdnEcomParameters = null;
		
		EcommerceNewPageTypeEvaluation ecomPageEval = null;
		
		if(generalParametersList.size() > 0)
			fqdnParameters = generalParametersList.get(0);
		if(generalEcomParametersList.size() > 0)
			fqdnEcomParameters = generalEcomParametersList.get(0);
		
		if(fqdnEcomParameters != null) {
			String isBagButtonOnSite = fqdnEcomParameters.get(mapping.get("leads_urldirectory_ecom-is_atb_button_in_dir"));
			List<String> kMeansParams = new ArrayList<>();
			kMeansParams.add(fqdnEcomParameters.get(mapping.get("leads_urldirectory_ecom-product_cluster_center")));
			kMeansParams.add(fqdnEcomParameters.get(mapping.get("leads_urldirectory_ecom-category_cluster_center")));
			kMeansParams.add(fqdnEcomParameters.get(mapping.get("leads_urldirectory_ecom-product_cluster_50pc_dist")));
			kMeansParams.add(fqdnEcomParameters.get(mapping.get("leads_urldirectory_ecom-product_cluster_80pc_dist")));
			kMeansParams.add(fqdnEcomParameters.get(mapping.get("leads_urldirectory_ecom-category_cluster_50pc_dist")));
			kMeansParams.add(fqdnEcomParameters.get(mapping.get("leads_urldirectory_ecom-category_cluster_80pc_dist")));
			kMeansParams.add(fqdnEcomParameters.get(mapping.get("leads_urldirectory_ecom-scaler_mean")));
			kMeansParams.add(fqdnEcomParameters.get(mapping.get("leads_urldirectory_ecom-scaler_std")));
		
			ecomPageEval = new EcommerceNewPageTypeEvaluation(content,lang, isBagButtonOnSite, kMeansParams);
			
			// if the page has no older version and has url, check assumptions based on url's parent directories
			if(previousVersionType == null) {
				// assumption on the URL
				UrlAssumptions urlAssumptions = new UrlAssumptions();
				List<String> assumptionsList = urlAssumptions.getFQDNAssumption(fqdnParameters);
							
				//if(assumptionsList.contains(mapping.get("ecom"))) {
				if(!assumptionsList.isEmpty()) {
					if(assumptionsList.get(0).toLowerCase().startsWith("ecom")) {
						// GET EXTRACTION SCHEMAS FROM DIR
						boolean isCorrect = ecomPageEval.determinePageEcomFeatures();
						if(isCorrect) {
							if(ecomPageEval.getEcomAssumption() == EcomClassificationEnum.ECOM_PRODUCT_OFFERING_PAGE)
								// if there is a change that this is the product offering page, try to determine extraction schema
								// based on content and other "similar" pages
								extractionCandidatesJSONString = getEcomNamePriceExtractionSchemasFromDir(fqdnEcomParameters);
							else
								extractionCandidatesJSONString = new JSONObject().toString();
						}
					}
					else {
						extractionCandidatesJSONString = getArticleSchemaFromDir(fqdnEcomParameters);
					}
				}
				
			}
		}
		
		String [] returnArray = new String[3];
		
		returnArray[0] = extractionCandidatesJSONString;
		if(ecomPageEval != null) {
			EcomClassificationEnum ecomAssumption = ecomPageEval.getEcomAssumption();
			returnArray[1] = ecomAssumption==null ? null : ecomAssumption.toString();
			returnArray[2] = ecomPageEval.getEcomFeatures();
		}
		
		return returnArray;		
	}
	
	
	private String getEcomNamePriceExtractionSchemasFromDir(HashMap<String, String> dirEcomParameters) {		
		String nameExtractionTuples  = dirEcomParameters.get(mapping.getProperty("leads_urldirectory_ecom-name_extraction_tuples"));
		String priceExtractionTuples = dirEcomParameters.get(mapping.getProperty("leads_urldirectory_ecom-price_extraction_tuples"));	
		String extractionCandidatesJSONString = LEADSUtils.prepareExtractionCandidatesJSONString(
				new String[]{nameExtractionTuples,priceExtractionTuples}, new String[]{"ecom_product_name","ecom_product_price"});
		return extractionCandidatesJSONString;
	}
	
	private String getArticleSchemaFromDir(HashMap<String, String> dirEcomParameters) {	
		String extractionCandidatesJSONString = LEADSUtils.prepareExtractionCandidatesJSONString(
				new String[]{"boilerpipe"}, new String[]{"article_content"});
		return extractionCandidatesJSONString;
	}
	
//	@Override
//	public HashMap<String, HashMap<String, String>> retrieveMetadata(String url, String timestamp, HashMap<String, HashMap<String, String>> currentMetadata) {
//		
//		HashMap<String, HashMap<String, String>> newMetadata = new HashMap<>();
//		
//		String familyKey = "previous:leads_internal";
//		if(currentMetadata.get(familyKey) == null) {
//			HashMap<String, String> previousInternalMetadata = new HashMap<String, String>();
//			URIVersion uriVersion = DataStoreSingleton.getDataStore().getLeadsResourceMDFamily(url, mapping.getProperty("leads_internal"), 2, timestamp).last();
//			for(Entry<String, Cell> e : uriVersion.getFamily().entrySet()) {
//				Cell cell = e.getValue();
//				previousInternalMetadata.put(cell.getKey(), (String) cell.getValue());
//			}
//			newMetadata.put(familyKey, previousInternalMetadata);
//		}
//
//		List<String> urlGeneralizations = LEADSUtils.getAllResourceGeneralizations(url);
//		
//		int number = 0;
//		for(String genUrl : urlGeneralizations) {	
//			
//			familyKey = "general"+number+":leads_urldirectory";
//			
//			if(currentMetadata.get(familyKey) == null) {
//				HashMap<String, String> generalUrlMetadata = new HashMap<String, String>();
//				SortedSet<URIVersion> generalizationUriVersionSet = DataStoreSingleton.getDataStore().getLeadsResourceMDFamily(genUrl, mapping.getProperty("leads_urldirectory"), 1, timestamp);
//				if(generalizationUriVersionSet.size() > 0) {
//					for(Entry<String, Cell> e : generalizationUriVersionSet.first().getFamily().entrySet()) {
//						Cell cell = e.getValue();
//						generalUrlMetadata.put(cell.getKey(), (String) cell.getValue());
//					}
//				}
//				newMetadata.put(familyKey, generalUrlMetadata);
//			}
//			
//			familyKey = "general"+number+":leads_urldirectory_ecom";
//			
//			if(currentMetadata.get(familyKey) == null) {
//				HashMap<String, String> generalUrlEcomMetadata = new HashMap<String, String>();
//				SortedSet<URIVersion> generalizationUriVersionEcomSet = DataStoreSingleton.getDataStore().getLeadsResourceMDFamily(genUrl, mapping.getProperty("leads_urldirectory_ecom"), 1, timestamp);
//				if(generalizationUriVersionEcomSet.size() > 0) {
//					for(Entry<String, Cell> e : generalizationUriVersionEcomSet.first().getFamily().entrySet()) {
//						Cell cell = e.getValue();
//						generalUrlEcomMetadata.put(cell.getKey(), (String) cell.getValue());
//					}
//				}
//				newMetadata.put(familyKey, generalUrlEcomMetadata);
//			}
//			
//		}
//		
//		return newMetadata;
//	}


}





