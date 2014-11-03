package eu.leads.datastore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.URIVersion;

public abstract class AbstractDataStore {

	protected Properties mapping = null;

	/**
	 * Constructor
	 * 
	 * @param mapping - file containing mappings between the tables/columns names in the DB and names used in the code
	 */
	public AbstractDataStore(Properties mapping) {
		this.mapping = mapping;
	}	
	
	/**
	 * Gets one group / family / table of columns defined per URI
	 * 
	 * @param uri - URI defining the object (e.g. a page URL)
	 * @param family - group/family/table of columns defined per URI
	 * @param lastVersions - how many last versions of URI metadata family should be returned
	 * @param beforeTimestamp - if we want versions before some version, give this version's timestamp (can be null)
	 * 
	 * @return set of versions of URI family, starting with the freshest one (descending timestamp), if none - returns an empty set
	 */
	public abstract SortedSet<URIVersion> getLeadsResourceMDFamily(String uri, String family, int lastVersions, String beforeTimestamp);
	
	/**
	 * Puts cell(s) of URI's family into the storage
	 * 
	 * @param uri - URI for which  new data should be stored
	 * @param ts - timestamp of URI for which new data should be stored
	 * @param family - family/group/table in which new data should be stored
	 * @param cells - list of cells to be updated (together with expected version of every cell
	 * 
	 * @return whether the update was completed or not, parameter cells should contain 
	 * those cells that caused the versioning problem if the returned value is false
	 */
	public abstract boolean putLeadsResourceMDFamily(String uri, String ts, String family, List<Cell> cells);
	
	/**
	 * 
	 * @param uri
	 * @param ts
	 * @param partsTypeValuesMap
	 * @return
	 */
	public abstract HashMap<String, List<Object>> getLeadsResourcePartsMD(String uri, String ts, String partType);
	
	/**
	 * 
	 * @param uri
	 * @param ts
	 * @param partsTypeValuesMap
	 * @return
	 */
	//public abstract boolean putLeadsResourcePartsMD(String uri, String ts, HashMap<String,List<Object>> partsTypeValuesMap);
	public abstract boolean putLeadsResourcePartsMD(String uri, String ts, HashMap<String, Object> partsTypeValuesMap);
	
	/**
	 * Get one group/family/table of columns for every element (e.g. keyword) defined per URI (e.g. a page URL)
	 * 
	 * @param uri - URI defining the object (e.g. a page URL)
	 * @param family - group/family/table of columns defined per URI's element
	 * @param lastVersions - how many last versions of URI's elements' metadata family should be returned
	 * @param beforeTimestamp - if we want versions before some version, give this version's timestamp (can be null)
	 * 
	 * @return map of elements identified by keywords, holding metadata family per every version in value
	 */
	public abstract Map<String,SortedSet<URIVersion>> getLeadsResourceElementsMDFamily(String uri, String family, int lastVersions, String beforeTimestamp);
	
	/**
	 * Puts cell(s) of URI element's family into the storage
	 * 
	 * @param uri - URI for which new data should be stored
	 * @param ts - timestamp of URI for which new data should be stored
	 * @param element - identifier of the element (keyword)
	 * @param family - family/group/table in which new data should be stored
	 * @param familyName 
	 * @param cells - list of cells to be updated (together with expected version of every cell
	 * 
	 * @return whether the update was completed or not, parameter cells should contain 
	 * those cells that caused the versioning problem if the returned value is false
	 */
	public abstract boolean putLeadsResourceElementsMDFamily(String uri, String ts, String partid, String element, String familyName, List<Cell> cells);
	
	/**
	 * 
	 * @param dirUri
	 * @return
	 */
	public abstract List<String> getResourceURIsOfDirectory(String dirUri);
	
	/**
	 * 	
	 * @return
	 */
	public abstract List<String> getFQDNList();
	
	/**
	 * Returns an object representing family storage handle, in case some method runs some custom retrieval/storage functionality
	 * 
	 * @return
	 */
	public abstract Object getFamilyStorageHandle(String familyName);

	
	
}
