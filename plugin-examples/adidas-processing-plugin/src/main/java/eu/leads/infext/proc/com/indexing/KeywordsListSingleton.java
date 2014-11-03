package eu.leads.infext.proc.com.indexing;

import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import eu.leads.datastore.DataStoreSingleton;

public class KeywordsListSingleton {

	private static KeywordsListSingleton klSingleton = null;
	
	private List<String> keywordsList = new ArrayList<>();
	
	public static KeywordsListSingleton getInstance() {
		if(klSingleton == null)
			klSingleton = new KeywordsListSingleton();
		return klSingleton;
	}
	
	private KeywordsListSingleton() {
		init();
	}
	
	private void init() {
		Session session = (Session) DataStoreSingleton.getDataStore().getFamilyStorageHandle(null);
		
		ResultSet rs = session.execute("SELECT * FROM adidas.keywords");
		
		for(Row row : rs) {
			keywordsList.add(row.getString(0).toLowerCase());
		}
	}
	
	public List<String> getKeywordsList() {
		return keywordsList;
	}
	
}
