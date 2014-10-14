package test;

import eu.leads.processor.conf.LQPConfiguration;

/**
 * Created by vagvaz on 6/4/14.
 */
public class LocalTest {

  public static void main(String[] args) {
    String targetCache = "mycache";
    int sleepingPeriod = 20;
    //Important Call to initialize System Configuration
    LQPConfiguration.initialize();

    //Set CacheMode to get LcoalImplementation only
    LQPConfiguration.getConf().setProperty("processor.infinispan.mode", "local");

	/*Configuration for plugin*/

	/*deploy plugin locally*/
        /*PluginManager.deployLocalPlugin(plugin,config,"mytargetCacheName",
					  EventType.CREATEANDMODIFY,
					 InfinispanClusterSingleton.getInstance().getManager());*/


	/*Start putting values to the cache */

	/*Cleanup and close....*/
    //        InfinispanClusterSingleton.getInstance().getManager().stopManager();
  }
}