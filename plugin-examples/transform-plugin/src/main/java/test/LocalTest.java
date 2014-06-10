package test;

import eu.leads.crawler.PersistentCrawl;
import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.common.utils.PrintUtilities;
import eu.leads.processor.conf.LQPConfiguration;
import eu.leads.processor.plugins.EventType;
import eu.leads.processor.plugins.PluginInterface;
import eu.leads.processor.plugins.PluginManager;
import eu.leads.processor.plugins.transform.TransformPlugin;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.Map;

/**
 * Created by vagvaz on 6/8/14.
 */
public class LocalTest {
  public static void main(String[] args) {

    String webCacheName = "webpages";
    int sleepingPeriod = 20;
    //Important Call to initialize System Configuration
    LQPConfiguration.initialize();
    //Set CacheMode to get LcoalImplementation only
    LQPConfiguration.getConf().setProperty("processor.infinispan.mode", "local");
    //Put some configuration properties for crawler
    LQPConfiguration.getConf().setProperty("crawler.seed", "http://www.bbc.co.uk");
    LQPConfiguration.getConf().setProperty("crawler.depth", 3);
    //Set desired target cache
    LQPConfiguration.getConf().setProperty(StringConstants.CRAWLER_DEFAULT_CACHE, webCacheName);


    XMLConfiguration config = new XMLConfiguration();

    //Set plugin configuration (could be loaded from file
    config.setProperty("cache", "mycache");
    config.setProperty("attributes", "domainName,url,responseCode");
    PluginInterface plugin = new TransformPlugin();
    //deploy plugin to local cache
    PluginManager.deployLocalPlugin(plugin, config, webCacheName, EventType.CREATEANDMODIFY, InfinispanClusterSingleton.getInstance().getManager());
    //start crawler
    PersistentCrawl.main(null);
    //Sleep for an amount of time to test if everything is working fine
    try {
      Thread.sleep(sleepingPeriod * 1000);
    } catch ( InterruptedException e ) {
      e.printStackTrace();
    }

    //Iterate through local cache entries to ensure things went as planned
    Map cache = InfinispanClusterSingleton.getInstance().getManager().getPersisentCache("mycache");
    PrintUtilities.printMap(cache);
    PersistentCrawl.stop();
    InfinispanClusterSingleton.getInstance().getManager().stopManager();
    System.exit(0);
  }
}
