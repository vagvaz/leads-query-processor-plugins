package eu.leads.processor.plugins;

import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.common.utils.FSUtilities;
import eu.leads.processor.common.utils.FileLockWrapper;
import org.apache.commons.configuration.XMLConfiguration;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.infinispan.remoting.transport.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by vagvaz on 6/5/14.
 */
public class PluginManager {
    private static Logger log = LoggerFactory.getLogger(PluginManager.class);

    public static boolean uploadPlugin(PluginPackage plugin) {
        if(!plugin.getId().equals("eu.leads.processor.plugins.pagerank.PagerankPlugin") &&  !plugin.getId().equals("eu.leads.processor.plugins.sentiment")) {
            if (validatePlugin(plugin)) {
                upload(StringConstants.PLUGIN_CACHE, plugin);
                return true;
            }
            return false;
        }
        else{
            PluginPackage systemPlugin = new PluginPackage(plugin.getId(),plugin.getId());
            systemPlugin.setJar(new byte[1]);
            systemPlugin.setConfig(plugin.getConfig());
            systemPlugin.setId(plugin.getId());
            upload(StringConstants.PLUGIN_CACHE, systemPlugin);
        }
        return true;
    }

    private static boolean validatePlugin(PluginPackage plugin) {
        if(plugin.getId().equals("eu.leads.processor.plugins.pagerank.PagerankPlugin")  ||  plugin.getId().equals("eu.leads.processor.plugins.sentiment.SentimentAnalysisPlugin"))
            return true;
        if (plugin.getJar().length == 0) {
            log.error("Tried to upload plugin " + plugin.getId() + " without a jar file");
            return false;
        }
        if (!checkPluginName(plugin.getClassName(), plugin.getJar())) {
            log.error("Plugin " + plugin.getId() + " failed validation test. Class.getClassByName("
                          + plugin.getClassName() + ")");
            return false;
        }
        if (!checkInstantiate(plugin.getClassName(), plugin.getJar(), plugin.getConfig())) {
            log.error("Plugin " + plugin.getClassName() + " could not create an instance");
            return false;
        }

        return true;
    }

    private static void upload(String pluginCache, PluginPackage plugin) {
        Cache cache = (Cache) InfinispanClusterSingleton.getInstance().getManager()
                                  .getPersisentCache(pluginCache);
        if(!plugin.getId().equals("eu.leads.processor.plugins.pagerank.PagerankPlugin") &&  ! plugin.getId().equals("eu.leads.processor.plugins.sentiment.SentimentAnalysisPlugin")) {
            cache.put(plugin.getId(), plugin);
        }
        else
        {
            plugin.setJar( new byte[1]);
            cache.put(plugin.getId(),plugin);
        }
    }

    private static boolean checkPluginName(String className, byte[] jar) {
        boolean result = true;
        String name = System.getProperty("java.io.tmpdir") + "/leads/processor/tmp/testplugin.jar";
        //FileLock
        FileLockWrapper lock = new FileLockWrapper(name);
        lock.lock();

        FSUtilities.flushToDisk(name, jar);
        File file = new File(name);
        ClassLoader cl = null;
        try {
            cl = new URLClassLoader(new URL[] {file.toURI().toURL()});

            Class<?> plugClass = null;

            plugClass = Class.forName(className, true, cl);
        } catch (ClassNotFoundException e) {
            result = false;
        } catch (MalformedURLException e) {
            result = false;
        } finally {
            lock.release();
        }
        return result;
    }

    private static boolean checkInstantiate(String className, byte[] jar, byte[] config) {
        boolean result = true;
        String name = System.getProperty("java.io.tmpdir") + "/leads/processor/tmp/testplugin.jar";
        String configName =
            System.getProperty("java.io.tmpdir") + "/leads/processor/tmp/testplugin.xml";
        FileLockWrapper jarLock = new FileLockWrapper(name);
        FileLockWrapper confLock = new FileLockWrapper(configName);
        jarLock.lock();
        confLock.lock();
        FSUtilities.flushToDisk(name, jar);
        FSUtilities.flushToDisk(configName, config);
        File file = new File(name);
        ClassLoader cl = null;
        try {
            cl = new URLClassLoader(new URL[] {file.toURI().toURL()});


            Class<?> plugClass = null;

            plugClass = Class.forName(className, true, cl);
            Constructor<?> con = plugClass.getConstructor();
            PluginInterface plug = (PluginInterface) con.newInstance();
            XMLConfiguration pluginConfig = new XMLConfiguration();
            if (config.length > 0) {
                pluginConfig.load(configName);
            }

            plug.initialize(pluginConfig, InfinispanClusterSingleton.getInstance().getManager());
            plug.cleanup();
        } catch (MalformedURLException e) {
            result = false;
        } catch (Exception e) {
            result = false;
        } finally {
            confLock.release();
            jarLock.release();
        }
        return result;
    }

    public static boolean deployPlugin(String pluginId, String cacheName, EventType[] events) {
        Cache configCache = (Cache) InfinispanClusterSingleton.getInstance().getManager()
                                        .getPersisentCache(StringConstants.PLUGIN_ACTIVE_CACHE);
        Cache pluginsCache = (Cache) InfinispanClusterSingleton.getInstance().getManager()
                                         .getPersisentCache(StringConstants.PLUGIN_CACHE);
        boolean result = true;
        PluginPackage plugin = (PluginPackage) pluginsCache.get(pluginId);
        if (plugin == null) {
            result = false;
            log.warn("Plugin " + pluginId + " was not found in plugin cache");
        }
        if (!validatePlugin(plugin)) {
            log.warn("Plugin " + plugin.getClassName()
                         + " could not be validated so it could not be deployed");
            result = false;
        }
        int eventmask = 0;
        if (events == null || events.length == 0) {
            eventmask = 7;
        } else {
            for (EventType e : events) {
                eventmask += e.getValue();
            }
        }
        addPluginToCache(plugin, eventmask, configCache,cacheName);
        deployPluginListener(plugin.getId(), cacheName,
                                InfinispanClusterSingleton.getInstance().getManager());
 
        return result;
    }

    private static void addPluginToCache(PluginPackage plugin, int eventmask, Cache configCache,String prefix) {

//        configCache.put(prefix+":"+plugin.getId() + ":conf", plugin.getConfig());
//        configCache.put(prefix+":"+plugin.getId() + ":conf", plugin.getConfig());
//        configCache.put(prefix+":"+plugin.getId() + ":events", eventmask);
//        configCache.put(prefix+":"+plugin.getId() + ":className", plugin.getClassName());
        configCache.getAdvancedCache().withFlags(Flag.FORCE_ASYNCHRONOUS,Flag.SKIP_LISTENER_NOTIFICATION,Flag.SKIP_LOCKING,Flag.SKIP_STATISTICS,Flag.IGNORE_RETURN_VALUES);
        if(!plugin.getId().equals("eu.leads.processor.plugins.pagerank.PagerankPlugin") &&  ! plugin.getId().equals("eu.leads.processor.plugins.sentiment.SentimentAnalysisPlugin"))
        configCache.put(plugin.getId() + ":jar", plugin.getJar());

      configCache.put(plugin.getId() + ":conf", plugin.getConfig());

      configCache.put(plugin.getId() + ":events", eventmask);
      configCache.put(plugin.getId() + ":className", plugin.getClassName());
    }

    private static void deployPluginListener(String pluginId, String cacheName,
                                                InfinispanManager manager) {
        Properties conf = new Properties();
        conf.put("target", cacheName);
        conf.put("config", StringConstants.PLUGIN_ACTIVE_CACHE);
        LinkedList<String> alist = new LinkedList<String>();
        alist.add(pluginId);
        conf.put("pluginNames", alist);

        SimplePluginRunner runner = new SimplePluginRunner("TestSimplePluginDeployer", conf);
        manager.addListener(runner, cacheName);

    }

    public static boolean deployPlugin(String pluginId, XMLConfiguration config, String cacheName,
                                          EventType[] events) {
        Cache configCache = (Cache) InfinispanClusterSingleton.getInstance().getManager()
                                        .getPersisentCache(StringConstants.PLUGIN_ACTIVE_CACHE);
        Cache pluginsCache = (Cache) InfinispanClusterSingleton.getInstance().getManager()
                                         .getPersisentCache(StringConstants.PLUGIN_CACHE);
        boolean result = true;
        PluginPackage plugin = (PluginPackage) pluginsCache.get(pluginId);

        if (plugin == null) {
            result = false;
            log.warn("Plugin " + pluginId + " was not found in plugin cache");
        }

        //Set configuration for the plugin to deploy
        plugin.setConfig(FSUtilities.getBytesFromConfiguration(config));
        if (!validatePlugin(plugin)) {
            log.warn("Plugin " + plugin.getClassName()
                         + " could not be validated so it could not be deployed");
            result = false;
        }

        int eventmask = computeEventMask(events);
        addPluginToCache(plugin, eventmask, configCache,cacheName);
        deployPluginListener(plugin.getId(), cacheName,
                                InfinispanClusterSingleton.getInstance().getManager());

        return true;
    }

    private static int computeEventMask(EventType[] events) {
        int eventmask = 0;
        if (events == null || events.length == 0) {
            eventmask = 7;
        } else {
            for (EventType e : events) {
                eventmask += e.getValue();
            }
        }
        return eventmask;
    }

    public static boolean undeploy(String pluginId, String cacheName) {
        InfinispanManager manager = InfinispanClusterSingleton.getInstance().getManager();
        Cache active = (Cache) manager.getPersisentCache(StringConstants.PLUGIN_ACTIVE_CACHE);
        Cache cache = (Cache) manager.getPersisentCache(cacheName);
        DistributedExecutorService des = new DefaultExecutorService(cache);
        List<Future<Void>> list = new LinkedList<Future<Void>>();
        for (Address a : manager.getMembers()) {
            //            des.submitEverywhere(new AddListenerCallable(cache.getName(),listener));
            try {
                list.add(des.submit(a, new UndeployPluginCallable(cache.getName(), pluginId)));
            } catch (Exception e) {
                log.error(e.getMessage());
                return false;
            }
        }


        for (Future<Void> future : list) {
            try {
                future.get(); // wait for task to complete
            } catch (InterruptedException e) {
                return false;
            } catch (ExecutionException e) {
                return false;
            }
        }
        return true;
    }

    public static void deployLocalPlugin(PluginInterface plugin, XMLConfiguration config,
                                            String cacheName, EventType[] events,
                                            InfinispanManager manager) {
        Properties conf = new Properties();
        conf.put("target", cacheName);
        conf.put("config", StringConstants.PLUGIN_ACTIVE_CACHE);
        LinkedList<String> alist = new LinkedList<String>();
        conf.put("pluginNames", alist);
        SimplePluginRunner runner = new SimplePluginRunner("test-local", conf);
        runner.initialize(manager);
        int eventmask = computeEventMask(events);
        runner.addPlugin(plugin, config, eventmask);
        manager.addListener(runner, cacheName);

    }
}
