package eu.leads.infext.proc.com.categorization.ecom;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Scanner;

import eu.leads.infext.logging.ErrorStrings;
import eu.leads.infext.python.PythonQueueCall;
import eu.leads.utils.LEADSUtils;

public class EcommerceClassification {
	
	private static String popcName = "productofferingpageclassifier_clinterface";
	private static String atbbcName= "addtobasketbuttonclassifier_clinterface";
	
	private String content;
	private String lang;
	
	private boolean isEcom = false;
	private String atbButtonXpath = null;
	private String prodNameXpath = null;
	private String prodPathExtr = null;
	
	private String ecomFeatures = null;
	private Boolean isEcomAssumption = null;
	private String basketNode = null;
	private String buttonNode = null;

	public String getEcomFeatures() {
		return ecomFeatures;
	}

	public Boolean isEcomAssumption() {
		return isEcomAssumption;
	}
	
	public String getBasketNode() {
		return basketNode;
	}
	
	public String getButtonNode() {
		return buttonNode;
	}

	public EcommerceClassification(String content, String lang) {
		this.content = content;
		this.lang = lang;
	}
	
	public boolean determinePageEcomFeatures() {
		
		String isEcomAssumption = "false";
		String ecomFeatures = null;
		String buttonNode = null;
		String basketNode = null;
		
		String cliName = popcName;
		
		PythonQueueCall pyCall = new PythonQueueCall();
		pyCall.sendViaFile(0);
		List<Object> retValues = pyCall.call(cliName, content, lang);
		
		if(retValues.size() >= 4) {
			isEcomAssumption = (String) retValues.get(0);
			ecomFeatures = (String) retValues.get(1);
			buttonNode = ((String)retValues.get(2)).trim().length()>0 ? ((String)retValues.get(2)) : null; // might be None, is that ok?
			basketNode = ((String)retValues.get(3)).trim().length()>0 ? ((String)retValues.get(3)) : null; // the same
			
			this.ecomFeatures = ecomFeatures;
			this.isEcomAssumption = Boolean.valueOf(isEcomAssumption);
			this.basketNode = basketNode;
			this.buttonNode = buttonNode;
			
			return true;
		}
		else {
			System.err.println(ErrorStrings.getErrorString(ErrorStrings.pythonComponentErrorTempl, cliName));
			return false;
		}
//		retValues.remove(0);
//		for(String retVal : retValues) {
//			long featureVal = LEADSUtils.stringToLong(retVal);
//			ecomFeatures.add(featureVal);
//		}
		
	}
	
	public String hasAddToBasketButton(String extractionCandidatesJSONString) {
		String atbButtonXpath = null;
		
		String name = atbbcName;
		
		PythonQueueCall pyCall = new PythonQueueCall();
		List<Object> retValues = new ArrayList<>();
		if(extractionCandidatesJSONString != null) {
			pyCall.sendViaFile(0,2);
			retValues = pyCall.call(name, content, lang, extractionCandidatesJSONString);
		}
		else {
			pyCall.sendViaFile(0);
			retValues = pyCall.call(name, content, lang);
		}
		if(retValues.get(0).equals("1"))
			atbButtonXpath = (String) retValues.get(1);
		
		return atbButtonXpath;
	}
	
	public String determineNamePriceExtraction() {
		return null;
	}
	
	
	
	public static void main(String[] args) throws FileNotFoundException {
		@SuppressWarnings("resource")
		String content = new Scanner(new File("/home/lequocdo/workspace/leadsdm/org/leads/root/ecom/extraction/webpage.html")).useDelimiter("\\Z").next();
		
		EcommerceClassification ec = new EcommerceClassification(content, "en");
		System.out.println(ec.determinePageEcomFeatures());
	}
	
}
