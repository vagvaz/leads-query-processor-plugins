package eu.leads.infext.proc.com.categorization.ecom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.leads.infext.logging.ErrorStrings;
import eu.leads.infext.proc.com.categorization.ecom.model.EcomPageDictionary;
import eu.leads.infext.proc.com.categorization.ecom.model.EcomSiteDictionary;
import eu.leads.infext.python.PythonQueueCall;
import eu.leads.utils.LEADSUtils;

public class EcommerceSiteExtractionSchemaDeterminer {

    private EcomSiteDictionary ecomSiteDictionary = new EcomSiteDictionary();
	private List<EcomPageDictionary> productEcomPageList = new ArrayList<>();
	private List<EcomPageDictionary> categoryEcomPageList = new ArrayList<>();	
	
	private static String dsfsName = "determineschemaforthesite_clinterface";
	private static String testName = "clusteringtest_clinterface";
	
	public boolean determineExtractionSchema(List<EcomPageDictionary> ecomPageList) {
		
		String cliName = dsfsName;
		//cliName = testName;
		
		List<String> paramsList = new ArrayList<>();
		for(EcomPageDictionary ecomPage : ecomPageList) {
			paramsList.add(ecomPage.url);
			paramsList.add(ecomPage.content); 
			paramsList.add(ecomPage.lang);
			paramsList.add(ecomPage.ecom_features);
			paramsList.add(ecomPage.buttonnode); 
			paramsList.add(ecomPage.basketnode);
		}
		
		PythonQueueCall pyCall = new PythonQueueCall();
		List<Object> retValues = pyCall.call(cliName, paramsList);
		
		if(retValues.size() >= 12) {
			ecomSiteDictionary.nameExtractionTuples = (String) retValues.get(0);
			ecomSiteDictionary.priceExtractionTuples = (String) retValues.get(1);
			ecomSiteDictionary.productClusterCenter = (String) retValues.get(2);
			ecomSiteDictionary.categoryClusterCenter = (String) retValues.get(3);
			ecomSiteDictionary.productCluster50pcDist = (String) retValues.get(4);
			ecomSiteDictionary.productCluster80pcDist = (String) retValues.get(5);
			ecomSiteDictionary.categoryCluster50pcDist = (String) retValues.get(6);
			ecomSiteDictionary.categoryCluster80pcDist = (String) retValues.get(7);
			ecomSiteDictionary.scalerMean = (String) retValues.get(8);
			ecomSiteDictionary.scalerStd = (String) retValues.get(9);
			
			String prodIndicesArrayString = (String) retValues.get(10);
			String [] prodIndicesArray = prodIndicesArrayString.split("[^\\d.]"); // TODO
			for(String prodIndexString : prodIndicesArray) {
				if(!prodIndexString.isEmpty()) {
					int index = new Integer(prodIndexString);
					productEcomPageList.add(ecomPageList.get(index));
				}
			}
			
			String catIndicesArrayString = (String) retValues.get(11);
			String [] catIndicesArray = catIndicesArrayString.split("[^\\d.]"); // TODO
			for(String catIndexString : catIndicesArray) {
				if(!catIndexString.isEmpty()) {
					int index = new Integer(catIndexString);
					categoryEcomPageList.add(ecomPageList.get(index));
				}
			}
			
			return true;
			
//			ecomPageList.removeAll(productEcomPageList);
//			categoryEcomPageList = ecomPageList;
		}
		else {
			System.err.println(ErrorStrings.getErrorString(ErrorStrings.pythonComponentErrorTempl, cliName));
			
			ecomSiteDictionary = null;
			productEcomPageList = null;
			categoryEcomPageList = null;
			
			return false;
		}
		
	}

	public EcomSiteDictionary getEcomSiteDictionary() {
		return ecomSiteDictionary;
	}

	public List<EcomPageDictionary> getProductEcomPageList() {
		return productEcomPageList;
	}

	public List<EcomPageDictionary> getCategoryEcomPageList() {
		return categoryEcomPageList;
	}
	
}
