package eu.leads.processor.test;

import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.conf.LQPConfiguration;
import eu.leads.processor.plugins.PluginBaseImpl;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * Created by vagvaz on 6/7/14.
 */
public class LocalTest {

    public static void main(String[] args) {
        LQPConfiguration.initialize();
        //Override Configuration for local only execution
        //        LQPConfiguration.getConf().setProperty("processor.infinispan.mode","local");
        PluginBaseImpl plugin = new PluginBaseImpl();
        String configPath = null;

        //IF you want you can load config from file
        if (args.length == 0) {
            configPath = "/data/workspace/basic_plugin.xml";
        } else {
            configPath = args[0];
        }

        //        XMLConfiguration config = new XMLConfiguration(configPath);

        //OR you can set it up here...
        XMLConfiguration config = new XMLConfiguration();
        config.setProperty("plugin.id", "myplugin");
        config.setProperty("plugin.version", "theversion");

        //        PluginManager.deployLocalPlugin(plugin,config,"webpages:", EventType.CREATEANDMODIFY, InfinispanClusterSingleton.getInstance().getManager());
        CachePutter putter = new CachePutter("testCache", 1000, 1000);
        putter.putValues();
        InfinispanClusterSingleton.getInstance().getManager().stopManager();
        System.exit(0);
    }
}
