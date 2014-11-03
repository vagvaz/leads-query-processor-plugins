package eu.leads.infext.proc.realtime.env.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import eu.leads.datastore.AbstractDataStore;
import eu.leads.datastore.DataStoreSingleton;
import eu.leads.infext.logging.redirect.StdLoggerRedirect;
import eu.leads.infext.proc.realtime.wrapper.AbstractProcessing;

public abstract class AbstractExecutionPojo {
	
	protected AbstractDataStore dataStore = DataStoreSingleton.getDataStore();
	protected Properties mapping = DataStoreSingleton.getMapping();
	protected Properties parameters = DataStoreSingleton.getParameters();
	
	protected List<AbstractProcessing> processingQueue = new ArrayList<>();
	
	public AbstractExecutionPojo() throws Exception {
		StdLoggerRedirect.initLogging();
	}

	public abstract void execute(String uri, String timestamp, String cacheName, HashMap<String, String> cacheColumns);
	
}
