package eu.leads.processor.infinispan;


import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryRemoved;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCustomEvent;

/**
 * Created by vagvaz on 8/31/14.
 */
@ClientListener(
                   converterFactoryName = "leads-processor-converter-factory",
                   filterFactoryName = "leads-processor-factory"
)
public class PluginListener {
    @ClientCacheEntryCreated
    @ClientCacheEntryModified
    @ClientCacheEntryRemoved
    public void handleCustomEvent(ClientCacheEntryCustomEvent<Object> event) {
        System.out.println("Free beer found in line " + event.toString());
    }
}
