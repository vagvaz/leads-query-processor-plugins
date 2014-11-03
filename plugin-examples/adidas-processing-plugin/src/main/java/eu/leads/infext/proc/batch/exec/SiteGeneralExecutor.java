package eu.leads.infext.proc.batch.exec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeMap;

import org.json.JSONArray;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import eu.leads.datastore.AbstractDataStore;
import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.URIVersion;
import eu.leads.datastore.impl.CassandraCQLDataStore;
import eu.leads.infext.logging.redirect.StdLoggerRedirect;
import eu.leads.infext.proc.batch.exec.part.ops.MapValueComparator;
import eu.leads.infext.proc.com.geo.LocalizationWrapper;
import eu.leads.utils.LEADSUtils;

/***
 * 
 * The batch process with general queries for the site to be run in the LEADS environment
 * 
 * @author amo remix
 *
 */
public class SiteGeneralExecutor {

	
	private static AbstractDataStore dataStore = DataStoreSingleton.getDataStore();
	private static Properties mapping = DataStoreSingleton.getMapping();
	private static Properties parameters = DataStoreSingleton.getParameters();
	
	
	public static void main(String[] args) throws Exception {
		
		String fqdn = args[0];
		
		StdLoggerRedirect.initLogging();
		
		/////////////////////////
		/////////////////////////
		/////////////////////////
		
		// a. see if already defined
		SortedSet<URIVersion> dirMdFamilyVersions = dataStore.getLeadsResourceMDFamily(fqdn, mapping.getProperty("leads_site"), 1, null);
		Map<String, Cell> dirMdFamily = null;
		
		boolean isKnown = false;
		String[] countryCodes = new String [3];
		if(dirMdFamilyVersions != null && !dirMdFamilyVersions.isEmpty()) {
			URIVersion dirMdFamilyVersion = dirMdFamilyVersions.first();
			dirMdFamily = dirMdFamilyVersion.getFamily();
			
			Cell whoisCountryCell = dirMdFamily.get(mapping.getProperty("leads_site-whois_country"));
			Cell domainCountryCell = dirMdFamily.get(mapping.getProperty("leads_site-domain_country"));
			Cell ipCountryCell = dirMdFamily.get(mapping.getProperty("leads_site-ip_geo"));
			
			if(whoisCountryCell!=null || domainCountryCell!=null || ipCountryCell!=null) {
				isKnown = true;
			}
		}
		
		if(!isKnown) {
			String site = LEADSUtils.nutchUrlToFullyQualifiedDomainName(fqdn);
			LocalizationWrapper locWrapper = new LocalizationWrapper();
			countryCodes = locWrapper.localizeSite(site);
		}
		
		Session session = (Session) ((CassandraCQLDataStore)dataStore).getFamilyStorageHandle(null);
		
		// Run query: SELECT lang, count(*) from webpages where fqdn = fqdn GROUP BY lang;
		HashMap<String, Integer> languagesCounters = new HashMap<>();
		String query = "SELECT lang FROM "+ mapping.getProperty("leads_core") +" WHERE fqdnurl='"+fqdn+"';";
		ResultSet rs = session.execute(query);
		for(Row row : rs) {
			String lang = row.getString(0);
			if(lang != null) {
				Integer occurencesNo = languagesCounters.get(lang);
				if(occurencesNo == null) occurencesNo = 0;
				languagesCounters.put(lang, ++occurencesNo);
			}
		}
		MapValueComparator bvc =  new MapValueComparator(languagesCounters);
        TreeMap<String,Integer> languagesCountersSorted = new TreeMap<String,Integer>((Comparator<String>) bvc);
        languagesCountersSorted.putAll(languagesCounters);
        JSONArray jsonArray = new JSONArray();
        for(Entry<String, Integer> langVal : languagesCountersSorted.entrySet()) {
        	jsonArray.put(langVal.getKey());
        }
        String mainLanguagesJSON = jsonArray.toString();
        
        // Assume country
        String country = null;
        if(dirMdFamily!=null && dirMdFamily.get(mapping.getProperty("leads_site-country"))== null) {
	        if(languagesCountersSorted.firstKey().equals("en")) {
	        	Set<String> englishCountries = new HashSet<String>() {{ add("UK"); add("US"); add("AU"); }};
	        	if(englishCountries.contains(countryCodes[2]))
	        		country = countryCodes[2];
	        	else if(englishCountries.contains(countryCodes[0]))
	        		country = countryCodes[0];
	        	else {
		        	int fUs = Collections.frequency(Arrays.asList(countryCodes), "US");
		        	int fUk = Collections.frequency(Arrays.asList(countryCodes), "UK");
		        	if(fUs > fUk)
		        		country = "US";
		        	else if(fUs < fUk)
		        		country = "UK";
	        	}
	        }
	        else if(languagesCountersSorted.firstKey().equals("de")) {
	        	Set<String> deCountries = new HashSet<String>() {{ add("DE"); add("AT"); }};
	        	if(deCountries.contains(countryCodes[2]))
	        		country = countryCodes[2];
	        	else if(deCountries.contains(countryCodes[0]))
	        		country = countryCodes[0];
	        	else {
		        	int fDe = Collections.frequency(Arrays.asList(countryCodes), "DE");
		        	int fAt = Collections.frequency(Arrays.asList(countryCodes), "AT");
		        	if(fDe > fAt)
		        		country = "DE";
		        	else if(fDe < fAt)
		        		country = "AT";
	        	}
	        }
        }
		
		// Store everything
		List<Cell> cells = new ArrayList<>();
		if(countryCodes[2]!=null) cells.add(new Cell(mapping.getProperty("leads_site-whois_country"), countryCodes[2], 0));
		if(countryCodes[0]!=null) cells.add(new Cell(mapping.getProperty("leads_site-domain_country"), countryCodes[0], 0));
		if(countryCodes[1]!=null) cells.add(new Cell(mapping.getProperty("leads_site-ip_geo"), countryCodes[1], 0));
		if(country!=null) cells.add(new Cell(mapping.getProperty("leads_site-country"), country, 0));
		cells.add(new Cell(mapping.getProperty("leads_site-main_languages"), mainLanguagesJSON, 0));
		
		dataStore.putLeadsResourceMDFamily(fqdn, LEADSUtils.getTimestampString(), mapping.getProperty("leads_site"), cells);
	}
	
}
