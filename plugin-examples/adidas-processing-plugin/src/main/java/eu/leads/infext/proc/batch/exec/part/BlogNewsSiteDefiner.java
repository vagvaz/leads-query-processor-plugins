package eu.leads.infext.proc.batch.exec.part;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.leads.datastore.datastruct.Cell;
import eu.leads.infext.python.PythonQueueCall;
import eu.leads.utils.LEADSUtils;

public class BlogNewsSiteDefiner extends AbstractPartialSiteDefiner {

	public BlogNewsSiteDefiner(String fqdn, HashMap<String, Integer> pagesNoMap, List<String> dirUris) {
		super(fqdn, pagesNoMap, dirUris);
	}

	@Override
	public boolean defineAndStore() {
		
		PythonQueueCall pythonCall = new PythonQueueCall();
		String site = LEADSUtils.nutchUrlToFullyQualifiedDomainName(fqdn);
		List<Object> retList = pythonCall.call("googlenewsfeedschecker_clinterface", site);
		
		if(retList.isEmpty()) {
			return false;
		}
		// IT IS in the FEED -> STORE
		else if (retList.get(0).equals("true")) {
			String url = fqdn;
			String timestamp = LEADSUtils.getTimestampString();
			String family = mapping.getProperty("leads_urldirectory_ecom");
			List<Cell> cells = new ArrayList<>();
			family = mapping.getProperty("leads_urldirectory");
			cells.add( new Cell(mapping.getProperty("leads_urldirectory-dir_assumption"), mapping.getProperty("leads_urldirectory-dir_assumption-google_news"), 0) );
			cells.add( new Cell(mapping.getProperty("leads_urldirectory-pages_no"), pagesNoMap.get(""), 0) );
			dataStore.putLeadsResourceMDFamily(url, timestamp, family, cells);
			
			return true;
		}
		
		return false;
	}
	
	
	public static void main(String[] args) {
		PythonQueueCall pythonCall = new PythonQueueCall();
		List<Object> retList = pythonCall.call("googlenewsfeedschecker_clinterface", "wiadomosci.onet.pl");
		System.out.println(retList.get(0));
	}

}
