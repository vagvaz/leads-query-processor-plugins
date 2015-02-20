package test.eu.leads.infext.proc.realtime.hook.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import eu.leads.PropertiesSingleton;
import eu.leads.datastore.DataStoreSingleton;
import eu.leads.infext.proc.realtime.hook.impl.PageCheckHook;

public class PageCheckHookTest {

	public static void main(String[] args) throws ConfigurationException, IOException {
		String confPath = "/data/workspace/leads-query-processor-plugins/plugin-examples/adidas-processing-plugin/adidas-processing-plugin-conf.xml";
		XMLConfiguration config;
		try {
			config = new XMLConfiguration(confPath);
			PropertiesSingleton.setConfig(config);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		
      	String urlStr = args.length>0 ? args[0] : "http://thebullrunner.com/2015/01/dream-chasers-2015/#.VMpKBGjF-So";
      	URL url = new URL(urlStr);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

    	Properties mapping = new Properties();
    	mapping.load(new FileInputStream(
    			"/data/workspace/leads-query-processor-plugins/common/src/main/java/eu/leads/datastore/mapping/leads_schema.properties"));
        
        String content = "";

        String inputLine;
        while ((inputLine = in.readLine()) != null)
            content += inputLine;
        in.close();
		
		HashMap<String, HashMap<String, String>> parameters = new HashMap<>();
		
		HashMap<String,String> newParameters = new HashMap<>();
		HashMap<String,String> newCoreParameters = new HashMap<>();
		HashMap<String,String> newCrawlParameters = new HashMap<>();
		
		newParameters.put("uri",urlStr);
		newCoreParameters.put(mapping.getProperty("leads_core-lang"),"en");
		newCrawlParameters.put(mapping.getProperty("leads_crawler_data-content"),content);
		
		parameters.put("new",newParameters);
		parameters.put("new:leads_core",newCoreParameters);
		parameters.put("new:leads_crawler_data",newCrawlParameters);
		
		PageCheckHook hook = new PageCheckHook();
		hook.resetMapping(mapping);
		System.out.println(parameters.get("new")+" ||| "+parameters.get("new:leads_core"));
		HashMap<String, HashMap<String, String>> processed = hook.process(parameters);
		System.out.println(processed);
	}
	
}
