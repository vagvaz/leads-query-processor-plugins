package eu.leads.processor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import eu.leads.PropertiesSingleton;
import eu.leads.datastore.DataStoreSingleton;
import eu.leads.infext.proc.realtime.env.pojo.PageProcessingPojo;
import eu.leads.infext.python.PythonQueueCall;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Tuple;
import eu.leads.processor.plugins.PluginInterface;

import org.apache.commons.configuration.Configuration;
import org.infinispan.Cache;

/**
 * Created by vagvaz on 10/14/14.
 */
public class AdidasProcessingPlugin implements PluginInterface {
   private final String className = AdidasProcessingPlugin.class.getCanonicalName();
   private Configuration configuration;
   private InfinispanManager manager;
   private PageProcessingPojo pageProcessingPojo;
   @Override
   public String getId() {
      return className;
   }

   @Override
   public void setId(String id) {
      System.err.println("Cannot set ID default is the name of the class");
   }

   @Override
   public String getClassName() {
      return className;
   }

   @Override
   public void initialize(Configuration config, InfinispanManager manager) {
	  try {
	      this.configuration = config;
	      this.manager = manager;
	      
	      System.out.println("%%%%% Initializing the plugin");
	      
	      // KEEP config
	      PropertiesSingleton.setConfig(config);
	      
	      // READ Configuration for Cassandra
	//      DataStoreSingleton.configureDataStore(config);
	      
	      // READ Configuration for the plugin
	      PropertiesSingleton.setResourcesDir(config.getString("resources_path"));
	      // TODO something more ??
	      
	//      try {
	//    	  pageProcessingPojo = new PageProcessingPojo();
	//	  } catch (Exception e) {
	//		  e.printStackTrace();
			  // TODO
	//	  }
//	      try {
//	      System.setOut(outputFile("/data/leads.out"));
//	      System.setErr(outputFile("/data/leads.err"));
	      System.out.println("Let's start the party!");
//	      } catch (java.io.FileNotFoundException e) {
//	         e.printStackTrace();
//	      }
	      
	      // Start Python ZeroMQ Server processes!
	      List<String> endpoints = config.getList("pzsEndpoints");
	      String pythonPath = "PYTHONPATH="+config.getString("pythonPath");
	      String commandBase = "/usr/bin/python -m eu.leads.infext.python.CLAPI.pzs ";
	      String[] envp = {pythonPath};
		  for(int i=0; i<endpoints.size(); i++) {
	    	  String endpoint = endpoints.get(i);
	    	  String command  = commandBase+endpoint;
	    	  Runtime.getRuntime().exec(command, envp);
		  }
      } 
      catch (IOException e) {
    	  e.printStackTrace();
      }
   }

   protected java.io.PrintStream outputFile(String name) throws java.io.FileNotFoundException {
       return new java.io.PrintStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(name)));
   }

   @Override
   public void cleanup() {

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
	   try {
		  System.out.println("######## processTuple");
	      String uri = (String) key;
	      String webpageJson = (String)value;
	      Tuple webpage = new Tuple(webpageJson);
	      
	      String content = webpage.getAttribute("content");
	      String timestamp = webpage.getNumberAttribute("timestamp").toString();
	      HashMap<String,String> cacheColumns = new HashMap<>();
	      cacheColumns.put("content", content);
	      cacheColumns.put("fetchTime", timestamp);
	
	      // Here Do the heavy processing stuff
	      System.out.println("########:"+getClassName().toString() + " heavily processed key " + key);
	//		pageProcessingPojo.execute(uri, timestamp, "webpages", cacheColumns);
			PythonQueueCall pythonCall = new PythonQueueCall();
			pythonCall.call("eu.leads.infext.python.CLAPI.helloworld_clinterface","hello","world");
            System.out.println("Python called, no exceptions.");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

   @Override
   public void removed(Object key, Object value, Cache<Object, Object> cache) {
      // Do Nothing probably never called.

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
