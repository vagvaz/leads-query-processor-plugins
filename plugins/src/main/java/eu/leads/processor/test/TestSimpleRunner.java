package eu.leads.processor.test;

import eu.leads.crawler.PersistentCrawl;
import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.infinispan.ClusterInfinispanManager;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.common.utils.PrintUtilities;
import eu.leads.processor.conf.LQPConfiguration;
import eu.leads.processor.common.infinispan.PluginHandlerListener;
import eu.leads.processor.plugins.EventType;
import eu.leads.processor.plugins.PluginBaseImpl;
import eu.leads.processor.plugins.PluginManager;
import eu.leads.processor.plugins.PluginPackage;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.File;

/**
 * Created by vagvaz on 6/3/14.
 */
public class TestSimpleRunner {
    public static void main(String[] args) {
        LQPConfiguration.initialize();
        LQPConfiguration.getInstance().getConfiguration()
            .setProperty(StringConstants.CRAWLER_DEFAULT_CACHE, "webpages:");

//        PersistentCrawl.main(null);
        File pluginf =
            new File(PluginBaseImpl.class.getProtectionDomain().getCodeSource().getLocation()
                         .getPath());
        String configPath = null;
        String jarPath = null;
        if (args.length == 0) {
            configPath = "/data/workspace/basic_plugin.xml";
            jarPath =
                "/data/workspace/leads-query-processor/plugins/target/leads-query-processor-plugins-1.0.0-SNAPSHOT.jar";

        } else {
            configPath = args[0];
        }
        PluginPackage plugin = new PluginPackage(PluginBaseImpl.class.getCanonicalName(),
                                                    PluginBaseImpl.class.getCanonicalName(),
                                                    jarPath, configPath);
        PluginManager.uploadPlugin(plugin);
        CachePutter putter = new CachePutter("testCache", 100, 100);
        XMLConfiguration config = null;
        try {
            config = new XMLConfiguration(configPath);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        PluginManager.deployPlugin(PluginBaseImpl.class.getCanonicalName(), config, "testCache",
                                      EventType.CREATEANDMODIFY);

        putter.putValues();
        ConfigurationBuilder builder = new ConfigurationBuilder();
        ClusterInfinispanManager manager =
            (ClusterInfinispanManager) InfinispanClusterSingleton.getInstance().getManager();
        builder.addServer().host(manager.getHost()).port(manager.getServerPort());

        RemoteCacheManager remoteCacheManager = new RemoteCacheManager(builder.build());
        RemoteCache<Object,Object> remoteCache = remoteCacheManager.getCache("testCache");
        JsonObject filterconfiguration = new JsonObject();
        filterconfiguration.putString("pluginName",PluginBaseImpl.class.getCanonicalName());
        filterconfiguration.putString("activePluginCache",StringConstants.PLUGIN_ACTIVE_CACHE);
        filterconfiguration.putArray("types", new JsonArray());
        filterconfiguration.putString("targetCache","testCache");
        PluginHandlerListener listener = new PluginHandlerListener();
        remoteCache.addClientListener(listener,new Object[]{filterconfiguration.toString()},new Object[0]);
        System.out.println("Listener registered. Press <enter> to exit.");
        System.out.println("Exiting");



        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Cache logCache = (Cache) InfinispanClusterSingleton.getInstance().getManager()
                                     .getPersisentCache("logCache");
        System.out.println("--------------------_LOG CACHE_--------------------");
        Cache c = (Cache) InfinispanClusterSingleton.getInstance().getManager()
                              .getPersisentCache("testCache");

        remoteCache.removeClientListener(listener);

        PrintUtilities.printMap(c);
        PersistentCrawl.stop();
        System.exit(0);
    }

    public static void oldmain(String[] args) {
        LQPConfiguration.initialize();
        LQPConfiguration.getInstance().getConfiguration()
            .setProperty(StringConstants.CRAWLER_DEFAULT_CACHE, "webpages:");

        PersistentCrawl.main(null);
        File pluginf =
            new File(PluginBaseImpl.class.getProtectionDomain().getCodeSource().getLocation()
                         .getPath());
        String configPath = null;
        String jarPath = null;
        if (args.length == 0) {
            configPath = "/data/workspace/basic_plugin.xml";
            jarPath =
                "/data/workspace/leads-query-processor/plugins/target/leads-query-processor-plugins-1.0-SNAPSHOT.jar";

        } else {
            configPath = args[0];
        }
        PluginPackage plugin = new PluginPackage(PluginBaseImpl.class.getCanonicalName(),
                                                    PluginBaseImpl.class.getCanonicalName(),
                                                    jarPath, configPath);
        PluginManager.uploadPlugin(plugin);
        CachePutter putter = new CachePutter("testCache", 100, 100);
        XMLConfiguration config = null;
        try {
            config = new XMLConfiguration(configPath);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        PluginManager.deployPlugin(PluginBaseImpl.class.getCanonicalName(), config, "webpages:",
                                      EventType.CREATEANDMODIFY);
        putter.putValues();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Cache logCache = (Cache) InfinispanClusterSingleton.getInstance().getManager()
                                     .getPersisentCache("logCache");
        System.out.println("--------------------_LOG CACHE_--------------------");
        Cache c = (Cache) InfinispanClusterSingleton.getInstance().getManager()
                              .getPersisentCache("webpages:");
        PrintUtilities.printMap(c);
        PersistentCrawl.stop();
        System.exit(0);

    }
}
