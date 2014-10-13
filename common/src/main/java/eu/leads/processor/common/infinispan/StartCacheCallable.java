package eu.leads.processor.common.infinispan;

import org.infinispan.Cache;
import org.infinispan.distexec.DistributedCallable;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by vagvaz on 5/23/14.
 */
public class StartCacheCallable<K, V> implements DistributedCallable<K, V, Void>, Serializable {
    private static final long serialVersionUID = 8331682008912636780L;
    private final String cacheName;
    //    private final Configuration configuration;
    private transient Cache<K, V> cache;


    public StartCacheCallable(String cacheName) {
        this.cacheName = cacheName;
        //        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void call() throws Exception {
        //        cache.getCacheManager().defineConfiguration(cacheName);
        //        cache.getCacheManager().defineConfiguration(cacheName, new ConfigurationBuilder().clustering().cacheMode(CacheMode.DIST_ASYNC).async().l1().lifespan(100000L).hash().numOwners(3).build());

        cache.getCacheManager().defineConfiguration(cacheName, cache.getCacheManager()
                                                                   .getCacheConfiguration("clustered"));
        cache.getCacheManager().getCache(cacheName); // start the cache
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnvironment(Cache<K, V> cache, Set<K> inputKeys) {
        this.cache = cache;
    }

}
