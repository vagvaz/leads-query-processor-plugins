package eu.leads.processor.test;

import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.common.infinispan.InfinispanManager;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by vagvaz on 6/5/14.
 */
public class CachePutter {
    private final int period;
    private final ConcurrentMap<String,String> cache;
    private final int size;

    public CachePutter(String cacheName, int size, int period) {
        this.cache =
            InfinispanClusterSingleton.getInstance().getManager().getPersisentCache(cacheName);
        this.size = size;
        this.period = period;
    }

    public CachePutter(String cacheName, int size, int period, InfinispanManager manager) {
        this.cache = manager.getPersisentCache(cacheName);
        this.size = size;
        this.period = period;
    }


    public void putValues() {
        for (int counter = 0; counter < size; counter++) {
            this.cache.put(Integer.toString(counter), Integer.toString(counter * 10));
            try {
                Thread.sleep(period);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
