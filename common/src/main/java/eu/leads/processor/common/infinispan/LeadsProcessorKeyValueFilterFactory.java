package eu.leads.processor.common.infinispan;

import org.infinispan.filter.KeyValueFilter;
//import org.infinispan.filter.KeyValueFilterFactory;
//import org.infinispan.filter.NamedFactory;
import org.infinispan.manager.EmbeddedCacheManager;

import org.infinispan.server.hotrod.event.KeyValueFilterFactory;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by vagvaz on 9/29/14.
 */


public class LeadsProcessorKeyValueFilterFactory implements KeyValueFilterFactory {
    private final EmbeddedCacheManager manager;

    public LeadsProcessorKeyValueFilterFactory(EmbeddedCacheManager cacheManager){
        this.manager = cacheManager;
    }

    @SuppressWarnings("unchecked")
    @Override public KeyValueFilter<String,String> getKeyValueFilter(Object[] objects) {
        if(objects.length != 1){
            throw new IllegalArgumentException();
        }
        JsonObject conf = new JsonObject((String)objects[0]);
        return new PluginRunnerFilter(manager,conf.toString());
    }
}
