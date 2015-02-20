package eu.leads.infext.proc.com.maincontent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;

import eu.leads.infext.logging.ErrorStrings;
import eu.leads.infext.proc.com.categorization.newsblog.NewsBlogArticleAnalysis;
import eu.leads.infext.python.PythonQueueCall;
import eu.leads.utils.LEADSUtils;

public class LeadsMainContentExtraction {

	private static String vexName = "valuesextraction_clinterface";
	
	private String successfulExtractionJSON = null;

	private BoilerpipeMainContentExtraction boilerpipe = new BoilerpipeMainContentExtraction();
	
	public HashMap<String,List<String>> extract(final String content, String candidatesExtractionJSON) {
		HashMap<String,List<String>> extractedValues = new HashMap<String,List<String>>();
		
		HashMap<String, String> extractionCandidatesMap = LEADSUtils.retrieveExtractionCandidatesFromJSONString(candidatesExtractionJSON);
		int mapSize = extractionCandidatesMap.size();
		
		String [] extractionTypes  = new String[mapSize];
		String [] extractionPaths = new String[mapSize];
		int index = 0;
		
		for(Entry<String, String> e : extractionCandidatesMap.entrySet()) {
			String extractionGeneralName = e.getKey();
			String extractionDefinitionString = e.getValue();
			
			if(extractionGeneralName.equals("article_content")
					&& extractionDefinitionString.equals("boilerpipe")) {
				String article = boilerpipe.extractArticle(content);
				if(NewsBlogArticleAnalysis.isArticle(article)) {
					extractedValues.put(extractionGeneralName, 
							new ArrayList<String>() {{ add(boilerpipe.extractArticle(content)); }});
					extractionTypes[index] = extractionGeneralName;
					extractionPaths[index] = "boilerpipe";
				}
			}
			else {
				// then, we need to call the Python interface, sir!
				String cliName = vexName;
				
				PythonQueueCall pyCall = new PythonQueueCall();
				pyCall.sendViaFile(0);
				List<Object> retValues = pyCall.call(cliName, content, extractionGeneralName, extractionDefinitionString);
				
				if(retValues.size() >= 1) {
					String successfulExtractionTuple = (String) retValues.get(0);
					
					extractionTypes[index] = extractionGeneralName;
					extractionPaths[index]= successfulExtractionTuple;
					
					for(int i=1; i<retValues.size(); i+=2) {
						String extractedKey = (String) retValues.get(i);
						String extractedVal = (String) retValues.get(i+1);
						List<String> extractedValuesList = extractedValues.get(extractedKey);
						if(extractedValuesList==null)
							extractedValuesList = new ArrayList<>();
						extractedValuesList.add(extractedVal);
						extractedValues.put(extractedKey, extractedValuesList);
					}
				}
				else {
					System.err.println(ErrorStrings.getErrorString(ErrorStrings.pythonComponentErrorTempl, cliName));
				}
			}
			index++;
		}
		
		successfulExtractionJSON = LEADSUtils.prepareExtractionCandidatesJSONString(extractionPaths, extractionTypes);
		
		return extractedValues;
	}

	
	public String getLastSuccessfulExtractionJSON() {
		return successfulExtractionJSON;
	}
	
}
