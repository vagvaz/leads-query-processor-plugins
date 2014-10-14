package eu.leads.processor;

import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Tuple;
import eu.leads.processor.plugins.PluginInterface;
import org.apache.commons.configuration.Configuration;
import org.infinispan.Cache;

/**
 * Created by vagvaz on 10/14/14.
 */
public class AdidasProcessingPlugin implements PluginInterface {
   private final String id = AdidasProcessingPlugin.class.getCanonicalName();
   private Configuration configuration;
   private InfinispanManager manager;
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
      return AdidasProcessingPlugin.class.getCanonicalName();
   }


   @Override
   public void initialize(Configuration config, InfinispanManager manager) {
      this.configuration = config;
      this.manager = manager;
     //READ Configuration for Cassandra?
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
      String urlKey = (String) key;
      String webpageJson = (String)value;
      Tuple webpage = new Tuple(webpageJson);

      // Here Do the heavy processing stuff
      System.out.println("########:"+getClassName().toString() + " heavily processed key " + key);


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
