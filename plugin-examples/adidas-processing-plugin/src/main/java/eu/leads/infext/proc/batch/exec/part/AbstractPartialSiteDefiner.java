package eu.leads.infext.proc.batch.exec.part;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import eu.leads.datastore.AbstractDataStore;
import eu.leads.datastore.DataStoreSingleton;

public abstract class AbstractPartialSiteDefiner {
	
	protected String fqdn;
	protected HashMap<String, Integer> pagesNoMap;
	protected List<String> dirUris;
	
	public AbstractPartialSiteDefiner(String fqdn, HashMap<String,Integer> pagesNoMap, List<String> dirUris) {
		this.fqdn = fqdn;
		this.pagesNoMap = pagesNoMap;
		this.dirUris = dirUris;
	}

	protected AbstractDataStore dataStore = DataStoreSingleton.getDataStore();
	protected static Properties mapping = DataStoreSingleton.getMapping();
	protected static Properties parameters = DataStoreSingleton.getParameters();
	
	public abstract boolean defineAndStore();
	
}
