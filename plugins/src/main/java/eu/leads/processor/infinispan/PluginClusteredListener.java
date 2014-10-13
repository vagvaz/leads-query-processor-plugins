package eu.leads.processor.infinispan;

import org.infinispan.notifications.Listener;

/**
 * Created by vagvaz on 8/31/14.
 */
@Listener(
             primaryOnly = true,
             clustered = true
)
public class PluginClusteredListener {
}
