package eu.leads.datastore;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;

import eu.leads.datastore.impl.CassandraCQLDataStore;

//import root.datastore.impl.HBaseDataStore;
//import root.datastore.impl.HiveDataStore;

public class DataStoreSingleton {
	
	static String parametersFile = "parameters/parameters.properties";
	static String mappingFile = "mapping/casscql.properties";
	static String storagePropsFile = "prop/HiveDataStore.properties";
	
	static String storagePropertiesDir = "/temp";
	
	static AbstractDataStore dataStore = null;
	static Properties prop = new Properties();
	static Properties mapping = new Properties();
	static Properties parameters = new Properties();
	
	public static void configureDataStore(Configuration conf) {
		if(dataStore == null) {
			storagePropertiesDir = conf.getString("storagePropertiesDir");
			String technology = conf.getString("technology");
			if(technology.toLowerCase().equals("cassandra")) {
				mappingFile = "mapping/casscql.properties";
				initProperties();
				initMapping();
				initParameters();
				int port = conf.getInt("port");
				String [] hosts = conf.getStringArray("host");
				dataStore = new CassandraCQLDataStore(mapping,port,hosts);
			}
		}
	}
	
	public static AbstractDataStore getDataStore() {
		return dataStore;
	}
	
	public static Properties getParameters() {
		if(dataStore == null) {
			return null;
		}
		return parameters;
	}
	
	public static Properties getMapping() {
		if(dataStore == null) {
			return null;
		}
		return mapping;
	}
	
	private static void initParameters() {
		InputStream input = null;
		 
		try {
			String filePath = storagePropertiesDir+"/"+parametersFile;
			input = new FileInputStream(filePath);
			//input =  DataStoreSingleton.class.getClassLoader().getResourceAsStream(parametersFile);
			// load a properties file
			parameters.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void initMapping() {
		InputStream input = null;
		 
		try {
			String filePath = storagePropertiesDir+"/"+mappingFile;
			input = new FileInputStream(filePath);
			//input =  DataStoreSingleton.class.getClassLoader().getResourceAsStream(mappingFile);
			// load a properties file
			mapping.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void initProperties() {
		InputStream input = null;
	 
		try {
			String filePath = storagePropertiesDir+"/"+storagePropsFile;
			input = new FileInputStream(filePath);
			//input =  DataStoreSingleton.class.getClassLoader().getResourceAsStream(storagePropsFile);
			// load a properties file
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
}














