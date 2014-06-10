package eu.leads.processor.common.infinispan;

/**
 * Created by vagvaz on 5/23/14.
 */

import org.infinispan.Cache;
import org.infinispan.distexec.DistributedCallable;

import java.io.Serializable;
import java.util.Set;

public class StopCacheCallable<K, V> implements DistributedCallable<K, V, Void>, Serializable {
  private static final long serialVersionUID = 8331682008912636781L;
  private final String cacheName;
  private transient Cache<K, V> cache;


  public StopCacheCallable(String cacheName) {
    this.cacheName = cacheName;
  }

  /** {@inheritDoc} */
  @Override
  public Void call() throws Exception {
    cache.getCacheManager().removeCache(cacheName);
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void setEnvironment(Cache<K, V> cache, Set<K> inputKeys) {
    this.cache = cache;
  }

}