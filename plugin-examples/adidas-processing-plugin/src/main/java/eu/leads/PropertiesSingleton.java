package eu.leads;

import org.apache.commons.configuration.Configuration;

public class PropertiesSingleton {
	
	private static Configuration config = null;
	private static String resourcesDir = null;

	public static String getResourcesDir() {
		return resourcesDir;
	}
	public static void setResourcesDir(String resourcesDir) {
		if(PropertiesSingleton.resourcesDir==null) PropertiesSingleton.resourcesDir = resourcesDir;
	}
	
	public static Configuration getConfig() {
		return PropertiesSingleton.config;
	}
	public static void setConfig(Configuration config) {
		PropertiesSingleton.config = config;
	}
	
}
