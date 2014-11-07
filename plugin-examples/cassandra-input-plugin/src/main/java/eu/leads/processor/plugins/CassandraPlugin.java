package eu.leads.processor.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import eu.leads.utils.LEADSUtils;
import eu.leads.datastore.AbstractDataStore;
import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Tuple;

import org.apache.commons.configuration.Configuration;
import org.infinispan.Cache;

/**
 * Created by vagvaz on 10/14/14.
 */
public class CassandraPlugin implements PluginInterface {
	private final String id = CassandraPlugin.class.getCanonicalName();
	private String intermediateCacheName;
	private Cache<Object, Object> intermediateCache;
	private InfinispanManager manager;
	private Configuration configuration;

	private AbstractDataStore ds;
	private Properties mapping; 

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		System.err.println("Cannot set ID default is the name of the class");
	}

	@Override
	public String getClassName() {
		return CassandraPlugin.class.getCanonicalName();
	}

	@Override
	public void initialize(Configuration config, InfinispanManager manager) {
		//Read Cassandra related configuration
		//Initialize Cassandra client
		this.configuration = config;
		this.manager = manager;
		intermediateCacheName = configuration.getString("intermediateCache");
		intermediateCache = (Cache) manager.getPersisentCache(intermediateCacheName);
	      
	    // READ Configuration for Cassandra
		try {
			DataStoreSingleton.configureDataStore(config);
		    ds = DataStoreSingleton.getDataStore();
		    mapping = DataStoreSingleton.getMapping();
		} catch(Exception e) {
			System.err.println(e);
		}
	}

	@Override
	public void cleanup() {
		//Cleanup Structures.
		manager.removePersistentCache(intermediateCacheName);
	}

	@Override
	public void modified(Object key, Object value, Cache<Object, Object> cache) {
		//Have one method for processing both events
		processTuple(key,value);
	}

	@Override
	public void created(Object key, Object value, Cache<Object, Object> cache) {
		processTuple(key,value);
	}

	private void processTuple(Object key, Object value) {
		String urlKey = (String) key;
		String webpageJson = (String)value;
		Tuple webpage = new Tuple(webpageJson);

		final String content = webpage.getAttribute("content");
		String timestamp = webpage.getNumberAttribute("timestamp").toString();

		// A test system out just to be sure:
		System.out.println("%%%%%%%:"+getClassName().toString() + " key " + key +  " " + webpage.getAttribute("title"));
		//Here we process process the tuple.
		//In this case there is no processing just input the webpage to Cassandra

		/*
		 * 1. Put content to Cassandra
		 */
		//infinispanStoreCall.put(reverseUrl, html, Useful.getTimestampString());
		List<Cell> cells = new ArrayList<Cell>() {{ 
			add(new Cell(mapping.getProperty("leads_crawler_data-content"),content,0));
		}};

		boolean succeed = true; 
		do {
			try {
				ds.putLeadsResourceMDFamily(
						urlKey, timestamp, 
						mapping.getProperty("leads_crawler_data"), cells);
				succeed = true;
			} catch (Exception e) {
				System.err.println(e.getMessage());
				succeed = false;
				try { Thread.sleep(10000); } catch (InterruptedException e1) {}
			}
		} while(!succeed);

		//After we successfully insert data to Cassandra we put data to intermediate cache in order to trigger the processing plugin
		//We output the data as key:String url and the webpage as JsonDocument.
		intermediateCache.put(urlKey,webpage.asString());
	}

	@Override
	public void removed(Object key, Object value, Cache<Object, Object> cache) {
		//Do Nothing probably never called.
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public void setConfiguration(Configuration config) {
		this.configuration = config;

	}
}
