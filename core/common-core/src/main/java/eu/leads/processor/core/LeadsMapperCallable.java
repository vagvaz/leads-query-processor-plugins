package eu.leads.processor.core;

import eu.leads.processor.common.LeadsCollector;
import org.infinispan.Cache;
import org.infinispan.distexec.DistributedCallable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class LeadsMapperCallable<K, V, kOut, vOut> implements

		DistributedCallable<K, V, List<K>>, Serializable {

	/**
	 * tr
	 */
	private static final long serialVersionUID = 1242145345234214L;
	 
	LeadsCollector<kOut, vOut> collector = null;
	transient Cache<K, V> cache;
	Set<K> keys;
	LeadsMapper<K, V, kOut, vOut> mapper = null;

	public LeadsMapperCallable(Cache<K, V> cache,
			LeadsCollector<kOut, vOut> collector,
			LeadsMapper<K, V, kOut, vOut> mapper) {
		super();
		this.cache = cache;
		this.collector = collector;
		this.mapper = mapper;
	}

	public void setEnvironment(Cache<K, V> cache, Set<K> inputKeys) {

		this.cache =  cache;
		this.keys = inputKeys;
		collector.initialize_cache(cache.getCacheManager());
	}
	
	public List<K> call() throws Exception {
		
		if (mapper == null) {
			System.out.println(" Mapper not initialized ");
		} else {
         mapper.setCacheManager(cache.getCacheManager());
			List<K> result = new ArrayList<K>();

			for (Entry< K, V  > entry : cache.entrySet()){
				V value = entry.getValue();
				if (value != null) {
					mapper.map(entry.getKey(), value, collector);
				}
			}
			
			return result;
		}
		return null;
	}

}
