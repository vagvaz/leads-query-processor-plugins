package eu.leads.processor.common.infinispan;

import eu.leads.processor.common.infinispan.ClusterInfinispanManager;
import eu.leads.processor.common.utils.FSUtilities;
import eu.leads.processor.conf.ConfigurationUtilities;
import eu.leads.processor.plugins.EventType;
import eu.leads.processor.plugins.PluginInterface;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.infinispan.Cache;
import org.infinispan.filter.KeyValueFilter;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static eu.leads.processor.plugins.EventType.*;
import static eu.leads.processor.plugins.EventType.CREATED;
import static eu.leads.processor.plugins.EventType.MODIFIED;
import static eu.leads.processor.plugins.EventType.REMOVED;

/**
 * Created by vagvaz on 9/29/14.
 */
public class PluginRunnerFilter implements KeyValueFilter {
    private final EmbeddedCacheManager manager;
    private JsonObject conf;
    private ClusterInfinispanManager imanager;
    private Cache pluginsCache;
    transient Cache targetCache;
    transient String targetCacheName;
    transient Logger log ;
    transient PluginInterface plugin;
    transient String pluginsCacheName;
    transient String pluginName;
    transient List<EventType> type;
    public PluginRunnerFilter(EmbeddedCacheManager manager,String confString){
        this.manager = manager;
        this.conf = new JsonObject(confString);
        imanager = new ClusterInfinispanManager(manager);
        initialize();
    }

    private void initialize() {

        pluginsCacheName = conf.getString("activePluginCache");
        pluginName = conf.getString("pluginName");
        JsonArray types = conf.getArray("types");
        //InferTypes
        type = new ArrayList<EventType>(3);
        Iterator<Object> iterator = types.iterator();
        if(iterator.hasNext()){
            type.add(valueOf((String) iterator.next()));
        }
        if(type.size() == 0){
            type.add(CREATED);
            type.add(REMOVED);
            type.add(MODIFIED);
        }
        pluginsCache = (Cache) imanager.getPersisentCache(pluginsCacheName);
        log = LoggerFactory.getLogger( "PluginRunner."+pluginName+":"+ pluginsCacheName);
        initializePlugin(pluginsCache,pluginName);
        targetCacheName = conf.getString("targetCache");
        targetCache = (Cache) imanager.getPersisentCache(targetCacheName);
        System.err.println("Initialized plugin " + pluginName + " on " + targetCacheName);
    }



     public boolean accept2(String key, String value, Metadata metadata) {
        EventType actionType = inferEventType(targetCache,key,value,metadata);
        switch (actionType) {
            case CREATED:
                if(type.contains(CREATED))
                    plugin.created(key, value, targetCache);
                break;
            case MODIFIED:
                if(type.contains(MODIFIED))
                    plugin.modified(key, value, targetCache);
                break;
            case REMOVED:
                if(type.contains(REMOVED))
                    plugin.removed(key, value, targetCache);
                break;
        }
        return false;
    }

    private EventType inferEventType(Cache targetCache, String key, String value,
                                        Metadata metadata) {
        if(targetCache.containsKey(key)){
            return MODIFIED;
        }
        else{
            return REMOVED;
        }
    }

    private void initializePlugin(Cache cache, String plugName) {
        byte[] jarAsBytes = (byte[]) cache.get(plugName + ":jar");
        FSUtilities.flushPluginToDisk(plugName + ".jar", jarAsBytes);

        ConfigurationUtilities
            .addToClassPath(System.getProperty("java.io.tmpdir") + "/leads/plugins/" + plugName
                                + ".jar");
        byte[] config = (byte[]) cache.get(plugName + ":conf");
        FSUtilities.flushToTmpDisk("/leads/tmp/" + plugName + "-conf.xml", config);
        XMLConfiguration pluginConfig = null;
        try {
            pluginConfig =
                new XMLConfiguration(System.getProperty("java.io.tmpdir") + "/leads/tmp/" + plugName
                                         + "-conf.xml");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        String className = (String) cache.get(plugName + ":className");
        if (className != null && !className.equals("")) {
            try {
                Class<?> plugClass =
                    Class.forName(className, true, this.getClass().getClassLoader());
                Constructor<?> con = plugClass.getConstructor();
                plugin = (PluginInterface) con.newInstance();
                plugin.initialize(pluginConfig, imanager);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            log.error("Could not find the name for " + plugName);
        }
    }

    @Override public boolean accept(Object key, Object value, Metadata metadata) {
//        String o1 = (String)key;
//        String o2 = (String)value;
//        accept2(o1,o2,metadata);
        return false;
    }
}
