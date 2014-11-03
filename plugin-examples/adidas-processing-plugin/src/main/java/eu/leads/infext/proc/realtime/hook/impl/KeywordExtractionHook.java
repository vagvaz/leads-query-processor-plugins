package eu.leads.infext.proc.realtime.hook.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import eu.leads.datastore.datastruct.MDFamily;
import eu.leads.datastore.datastruct.UrlTimestamp;
import eu.leads.infext.proc.com.indexing.DocumentKeywordSearch;
import eu.leads.infext.proc.com.indexing.KeywordsListSingleton;
import eu.leads.infext.proc.com.keyword.RelevanceScore;
import eu.leads.infext.proc.com.keyword.SentimentScore;
import eu.leads.infext.proc.realtime.hook.AbstractHook;
import eu.leads.processor.sentiment.Sentiment;

public class KeywordExtractionHook extends AbstractHook {
	
	private SentimentScore sentimentScorer = new SentimentScore();
	private RelevanceScore relevanceScorer = new RelevanceScore();
	
	@Override
	public HashMap<String, HashMap<String, String>> retrieveMetadata(
			String url, String timestamp,
			HashMap<String, HashMap<String, String>> currentMetadata,
			HashMap<String, MDFamily> editableFamilies) {
		
		HashMap<String, HashMap<String, String>> newMetadata = new HashMap<>();		

		putLeadsMDIfNeeded(url, "new", "leads_core", 0, null, currentMetadata, newMetadata, null);
		putLeadsMDIfNeeded(url, "new", "leads_resourceparts", 0, null, currentMetadata, newMetadata, null);
		putLeadsMDIfNeeded(url, "new", "leads_keywords", 0, null, currentMetadata, newMetadata, editableFamilies);
		
		return newMetadata;
	}

	@Override
	public HashMap<String, HashMap<String, String>> process(
			HashMap<String, HashMap<String, String>> parameters) {

		HashMap<String, HashMap<String, String>> result = new HashMap<>();
		HashMap<String, String> newKeywordsResult = new HashMap<>();
		HashMap<String,HashMap<String, String>> newSpecificKeywordsResultsList = new HashMap<String,HashMap<String,String>>();
		
		List<String> keywordsList = KeywordsListSingleton.getInstance().getKeywordsList();
		
		DocumentKeywordSearch keywordSearch = new DocumentKeywordSearch();
		
		HashMap<String, String> newResourceParts = parameters.get("new:leads_resourceparts");
		HashMap<String, String> newCore = parameters.get("new:leads_core");
		HashMap<String, String> newMD = parameters.get("new");
		
		for(Entry<String, String> resPart : newResourceParts.entrySet()) {
			String resTypeNIndex = resPart.getKey();
			String resType  = resTypeNIndex.substring(0, resTypeNIndex.length()-4); // cut ':xxx'
			String resIndex = resTypeNIndex.substring(resTypeNIndex.length()-3);
			String resValue = resPart.getValue();
			
			keywordSearch.addDocument(resType, resIndex, resValue);
		}
		
		String lang = newCore.get(mapping.get(("leads_core-lang")));
		
		for(String keywords : keywordsList) {
			String [] keywordsArray = keywords.split("\\s+");
			
			// UrlTimestamp to be changed later! It's just about these are two strings. Parttype:Partid
			HashMap<UrlTimestamp, Double> partsIds = keywordSearch.searchKeywords(keywordsArray);
			
			for(UrlTimestamp partId : partsIds.keySet()) {
				String key = partId.url+":"+partId.timestamp;
				String content = newResourceParts.get(key);
				
				Double sentimentScore = Double.NaN;
				Double relevanceScore = Double.NaN;
				
				// if not an ecom-specific part, do it!
				if(!partId.url.toLowerCase().contains("ecom")) {
					// count sentiment
					Sentiment sentiment = sentimentScorer.getSentimentForEntity(keywords, content, lang);
					if(sentiment != null)
						sentimentScore = sentiment.getValue();
					// count relevance
					relevanceScore = partsIds.get(partId);
				}
				
				String keyWord = key+":"+keywords;
				
				HashMap<String, String> keywordMap = new HashMap<>();
				keywordMap.put(mapping.getProperty("leads_keywords-sentiment"), sentimentScore.toString());
				keywordMap.put(mapping.getProperty("leads_keywords-relevance"), relevanceScore.toString());
				
				newSpecificKeywordsResultsList.put("new:leads_keywords:"+keyWord, keywordMap);
				newKeywordsResult.put(keyWord, null);
			}
			
		}
		
		result.put("new:leads_keywords", newKeywordsResult);
		result.putAll(newSpecificKeywordsResultsList);
		
		return result;
	}

}
