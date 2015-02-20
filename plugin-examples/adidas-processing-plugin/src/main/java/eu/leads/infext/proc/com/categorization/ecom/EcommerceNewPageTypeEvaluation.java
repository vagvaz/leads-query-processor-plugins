package eu.leads.infext.proc.com.categorization.ecom;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import eu.leads.infext.logging.ErrorStrings;
import eu.leads.infext.python.PythonQueueCall;
import eu.leads.utils.LEADSUtils;

public class EcommerceNewPageTypeEvaluation {
	
	private static String popcName = "productofferingpageclassifier_clinterface";
	private static String atbbcName= "addtobasketbuttonclassifier_clinterface";
	private static String npedName = "productofferingpageclassifier_clinterface";
	private static String enptcName= "ecomnewpagetypeclassifier_clinterface";
	
	private String content;
	private String lang;
	private String isBagButtonOnSite;
	private List<String> kMeansParams;
	
	private boolean isEcom = false;
	private String atbButtonXpath = null;
	private String prodNameXpath = null;
	private String prodPathExtr = null;
	
	private String ecomFeatures = null;
	private EcomClassificationEnum ecomAssumption = EcomClassificationEnum.NONE;

	public String getEcomFeatures() {
		return ecomFeatures;
	}

	public EcomClassificationEnum getEcomAssumption() {
		return ecomAssumption;
	}
	
	private void setEcomAssumption(String string) {
		if(string.equals("ecom_product"))
			ecomAssumption = EcomClassificationEnum.ECOM_PRODUCT_OFFERING_PAGE;
		else if(string.equals("ecom_category"))
			ecomAssumption = EcomClassificationEnum.ECOM_CATEGORY_PAGE;
		else
			ecomAssumption = EcomClassificationEnum.ECOM_OTHER;
	}

	public EcommerceNewPageTypeEvaluation(String content, String lang, String isBagButtonOnSite, List<String> kMeansParams) {
		this.content = content;
		this.lang = lang;
		this.isBagButtonOnSite = isBagButtonOnSite;
		this.kMeansParams = kMeansParams;
	}
	
	public boolean determinePageEcomFeatures() {
		
		String name = enptcName;
		
		PythonQueueCall pyCall = new PythonQueueCall();
		pyCall.sendViaFile(0);
		List<Object> retValues = pyCall.call(name, content, lang, isBagButtonOnSite, kMeansParams);
		
		if(retValues.size() >= 2) {
			setEcomAssumption((String) retValues.get(0));
			this.ecomFeatures = (String) retValues.get(1);
			return true;
		}
		else {
			System.err.println(ErrorStrings.getErrorString(ErrorStrings.pythonComponentErrorTempl, name));
			return false;
		}
		
	}
	
}
