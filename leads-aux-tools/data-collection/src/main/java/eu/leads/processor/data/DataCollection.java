package eu.leads.processor.data;

import eu.leads.crawler.PersistentCrawl;
import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.infinispan.CacheManagerFactory;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.conf.LQPConfiguration;
import eu.leads.processor.plugins.EventType;
import eu.leads.processor.plugins.PluginManager;
import eu.leads.processor.plugins.PluginPackage;
import org.infinispan.Cache;

import java.util.ArrayList;

/**
 * Created by vagvaz on 9/25/14.
 */
public class DataCollection {

   public static void main(String[] args) {
      String seed = "";
      int numOfPages = 20;
       if(args.length > 1){
           seed = args[0];
       }
       if(args.length > 2){
           numOfPages = Integer.parseInt(args[1]);
       }
      if(seed == null || seed.equals("")){
          seed = "http://www.bbc.co.uk/";
      }
      long sleepingPeriod = 5;
      String webCacheName = "default.webpages";//StringConstants.CRAWLER_DEFAULT_CACHE;

      //Important Call to initialize System Configuration
      LQPConfiguration.initialize();


      //Put some configuration properties for crawler
      LQPConfiguration.getConf().setProperty("crawler.seed",
                                                    seed); //For some reason it is ignored news.yahoo.com is used by default
      LQPConfiguration.getConf().setProperty("crawler.depth", 3);
     String pagerankPluginClassName = "eu.leads.processor.plugins.pagerank.PagerankPlugin";
     String sentimentPluginClassName = "eu.leads.processor.plugins.sentiment.SentimentAnalysisPlugin";

     String pagerankJar = "/data/workspace/leads-query-processor/nqe/system-plugins/pagerank-plugin/target/pagerank-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar";
     String pagerankConf = "/data/workspace/leads-query-processor/nqe/system-plugins/pagerank-plugin/pagerank-conf.xml";
     String sentimentConf = "/data/workspace/leads-query-processor/nqe/system-plugins/sentiment-plugin/sentiment-conf.xml";
     String sentimentJar = "/data/workspace/leads-query-processor/nqe/system-plugins/sentiment-plugin/target/sentiment-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar";

      //Set desired target cache
      LQPConfiguration.getConf().setProperty(StringConstants.CRAWLER_DEFAULT_CACHE, webCacheName);
      //Create Infinispan Cluster of 3 infinispan local nodes...
      ArrayList<InfinispanManager> clusters = new ArrayList<InfinispanManager>();

     clusters.add(InfinispanClusterSingleton.getInstance().getManager());

//      for (InfinispanManager cluster : clusters) {
//         cluster.getPersisentCache(StringConstants.CRAWLER_DEFAULT_CACHE);
//      }
       try {
           PluginPackage pagerankPlugin = new PluginPackage(pagerankPluginClassName, pagerankPluginClassName, pagerankJar, pagerankConf);
           PluginPackage sentimentPlugin = new PluginPackage(sentimentPluginClassName, sentimentPluginClassName, sentimentJar, sentimentConf);
           PluginManager.uploadPlugin(pagerankPlugin);
           PluginManager.deployPlugin(pagerankPluginClassName, webCacheName, EventType.CREATEANDMODIFY);
           PluginManager.uploadPlugin(sentimentPlugin);
           PluginManager.deployPlugin(sentimentPluginClassName, webCacheName, EventType.CREATEANDMODIFY);


       }catch(Exception e){
           System.err.println(e.getMessage());
       }
      //start crawler
       Cache pages = (Cache) clusters.get(0).getPersisentCache(webCacheName);
       System.out.println("pages size is " + pages.size());
      //Sleep for an amount of time to test if everything is working fine
      boolean stop = pages.size() >= numOfPages;
      PersistentCrawl.main(null);
      try {
         while(!stop)
         {
            Thread.sleep(sleepingPeriod * 1000);
            System.out.println("pages size is " + pages.size());
            if(pages.size() >= numOfPages)
            {
               PersistentCrawl.stop();
               stop = true;
            }
         }

      } catch (InterruptedException e) {
         e.printStackTrace();
      }

    InfinispanClusterSingleton.getInstance().getManager().stopManager();


   }

}
