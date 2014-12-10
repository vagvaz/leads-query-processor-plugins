package test;

import eu.leads.crawler.PersistentCrawl;
import eu.leads.processor.AdidasProcessingPlugin;
import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.infinispan.CacheManagerFactory;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.common.utils.PrintUtilities;
import eu.leads.processor.conf.LQPConfiguration;
import eu.leads.processor.plugins.EventType;
import eu.leads.processor.plugins.PluginManager;
import eu.leads.processor.plugins.PluginPackage;
import org.infinispan.Cache;

import java.util.ArrayList;

public class ClusteredTest {
  public static void main(String[] args) {
     LQPConfiguration.initialize();
     ArrayList<InfinispanManager> cluster = new ArrayList<InfinispanManager>();
     cluster.add(InfinispanClusterSingleton.getInstance().getManager());  //must add because it is used from the rest of the system
     //Crucial for joining infinispan cluster
     for ( InfinispanManager manager : cluster ) {
        manager.getPersisentCache("clustered");
     }
     //Create plugin package for upload (id,class name, jar file path, xml configuration)
        /*PluginPackage plugin = new PluginPackage();*/
     PluginPackage plugin = new PluginPackage(AdidasProcessingPlugin.class.getCanonicalName(), AdidasProcessingPlugin.class.getCanonicalName(),
                                                     "/home/ubuntu/leads-query-processor-plugins/plugin-examples/adidas-processing-plugin/target/adidas-processing-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar",
                                                     "/home/ubuntu/leads-query-processor-plugins/plugin-examples/adidas-processing-plugin/adidas-processing-plugin-conf.xml");


     //upload plugin
     PluginManager.uploadPlugin(plugin);

     //distributed deployment  ( plugin id, cache to install, events)
     //PluginManager.deployPlugin();
     PluginManager.deployPlugin(AdidasProcessingPlugin.class.getCanonicalName(), "webpages", EventType.CREATEANDMODIFY);

        /*Start putting values to the cache */

     //Put some configuration properties for crawler
     LQPConfiguration.getConf().setProperty("crawler.seed", "http://www.bbc.co.uk"); //For some reason it is ignored news.yahoo.com is used by default
     LQPConfiguration.getConf().setProperty("crawler.depth", 1);
     //Set desired target cache
     LQPConfiguration.getConf().setProperty(StringConstants.CRAWLER_DEFAULT_CACHE, "webpages");
     //start crawler
     PersistentCrawl.main(null);
     //Sleep for an amount of time to test if everything is working fine
     try {
        int sleepingPeriod = 20;
        Thread.sleep(sleepingPeriod * 1000);
     } catch ( InterruptedException e ) {
        e.printStackTrace();
     }

     //Iterate through local cache entries to ensure things went as planned


	    /*Cleanup and close....*/
     PersistentCrawl.stop();
//     System.out.println("Local cache " + cache.entrySet().size() + " --- global --- " + cache.size());
     InfinispanClusterSingleton.getInstance().getManager().stopManager();
     System.exit(0);

  }
}
