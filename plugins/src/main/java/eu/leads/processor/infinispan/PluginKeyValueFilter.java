package eu.leads.processor.infinispan;

import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.common.utils.FSUtilities;
import eu.leads.processor.conf.ConfigurationUtilities;
import eu.leads.processor.plugins.EventType;
import eu.leads.processor.plugins.PluginInterface;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.infinispan.Cache;
import org.infinispan.filter.KeyValueFilter;
import org.infinispan.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by vagvaz on 8/31/14.
 */
public class PluginKeyValueFilter implements KeyValueFilter {
    InfinispanManager manager;
    PluginInterface plugin;
    String pluginsCache;
    String pluginName;
    EventType type;
    transient boolean initialized = false;
    Logger log = LoggerFactory.getLogger(PluginKeyValueFilter.class.getCanonicalName());
    private Cache<Object, Object> targetCache;

    @Override
    public boolean accept(Object key, Object value, Metadata metadata) {
        if (!initialized) {
            initializePlugin((Cache) manager.getPersisentCache(pluginsCache), pluginName);
        }
        switch (type) {
            case CREATED:
                plugin.created(key, value, targetCache);
                break;
            case MODIFIED:
                plugin.modified(key, value, targetCache);
                break;
            case REMOVED:
                plugin.removed(key, value, targetCache);
                break;
        }
        return false;
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
                plugin.initialize(pluginConfig, manager);

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
}
