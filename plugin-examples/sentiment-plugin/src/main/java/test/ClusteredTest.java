package test;

import eu.leads.crawler.PersistentCrawl;
import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.infinispan.CacheManagerFactory;
import eu.leads.processor.common.infinispan.InfinispanCluster;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.common.utils.PrintUtilities;
import eu.leads.processor.conf.LQPConfiguration;
import eu.leads.processor.plugins.EventType;
import eu.leads.processor.plugins.PluginManager;
import eu.leads.processor.plugins.PluginPackage;
import eu.leads.processor.plugins.sentiment.SentimentAnalysisPlugin;
import org.infinispan.Cache;

import java.util.ArrayList;

/**
 * Created by vagvaz on 6/7/14.
 */
public class ClusteredTest {
    public static void main(String[] args) {
        String webCacheName = "webpages";
        int sleepingPeriod = 30;
        //Important Call to initialize System Configuration
        LQPConfiguration.initialize();


        //Put some configuration properties for crawler
        LQPConfiguration.getConf().setProperty("crawler.seed",
                                                  "http://www.bbc.co.uk"); //For some reason it is ignored news.yahoo.com is used by default
        LQPConfiguration.getConf().setProperty("crawler.depth", 3);
        //Set desired target cache
        LQPConfiguration.getConf().setProperty(StringConstants.CRAWLER_DEFAULT_CACHE, webCacheName);
        //Create Infinispan Cluster of 3 infinispan local nodes...
        ArrayList<InfinispanManager> clusters = new ArrayList<InfinispanManager>();
        clusters.add(InfinispanClusterSingleton.getInstance()
                .getManager());  //must add because it is used from the rest of the system
        clusters.add(CacheManagerFactory.createCacheManager());
        //        clusters.add(new InfinispanCluster(CacheManagerFactory.createCacheManager()));

        for (InfinispanManager cluster : clusters) {
            cluster.getPersisentCache("clustered");
        }


        //Create plugin package for upload (id,class name, jar file path, xml configuration)

        PluginPackage plugin = new PluginPackage(SentimentAnalysisPlugin.class.getCanonicalName(),
                                                    SentimentAnalysisPlugin.class
                                                        .getCanonicalName(),
                                                    "/data/workspace/sentiment-plugins/target/sentiment-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar",
                                                    "/data/workspace/sentiment-plugins/sentiment-conf.xml");

        //upload plugin
        PluginManager.uploadPlugin(plugin);

        //distributed deployment  ( plugin id, cache to install, events)
        PluginManager.deployPlugin(SentimentAnalysisPlugin.class.getCanonicalName(), webCacheName,
                                      EventType.CREATEANDMODIFY);


        //start crawler
        PersistentCrawl.main(null);
        //Sleep for an amount of time to test if everything is working fine
        try {
            Thread.sleep(sleepingPeriod * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Iterate through local cache entries to ensure things went as planned
        Cache cache = (Cache) InfinispanClusterSingleton.getInstance().getManager()
                                  .getPersisentCache("entities");
        PrintUtilities.printMap(cache);
        PersistentCrawl.stop();
        System.out
            .println("Local cache " + cache.entrySet().size() + " --- global --- " + cache.size());
        InfinispanClusterSingleton.getInstance().getManager().stopManager();
    }
}
