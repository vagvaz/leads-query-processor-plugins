package eu.leads;

public class PropertiesSingleton {
	
	private static String resourcesDir = null;

	public static String getResourcesDir() {
		return resourcesDir;
	}
	public static void setResourcesDir(String resourcesDir) {
		if(PropertiesSingleton.resourcesDir==null) PropertiesSingleton.resourcesDir = resourcesDir;
	}
	
}
