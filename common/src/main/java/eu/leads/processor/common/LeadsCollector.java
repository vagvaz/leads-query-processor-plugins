package eu.leads.processor.common;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;


import org.infinispan.Cache;
import org.infinispan.distexec.mapreduce.Collector;
import org.infinispan.manager.EmbeddedCacheManager;

public class LeadsCollector<KOut, VOut> implements Collector<KOut, VOut>,
        Serializable {

    private static final long serialVersionUID = -602082107893975415L;
    private final AtomicInteger emitCount;
    private final int maxCollectorSize;
    private transient Cache<KOut, List<VOut>> store_cache;
    private String cache_name;

    public LeadsCollector(int maxCollectorSize,
                          Cache<KOut, List<VOut>> collectorCache) {
        super();

        emitCount = new AtomicInteger();
        this.maxCollectorSize = maxCollectorSize;
        store_cache = collectorCache;
        cache_name = collectorCache.getName();
    }

    public Cache<KOut, List<VOut>> getCache() {
        return store_cache;
    }

    public void emit(KOut key, VOut value) {


        List<VOut> list = store_cache.get(key);
        if (list == null) {
            list = new ArrayList<VOut>(128);
            store_cache.put(key, list);
        }
        list.add(value);
        emitCount.incrementAndGet();
        // if (isOverflown() && mcc.hasCombiner()) {
        // combine(mcc, this);
        // }
    }


    public void initialize_cache(EmbeddedCacheManager manager) {
        store_cache = manager.getCache(cache_name);
    }

    public void reset() {
        store_cache.clear();
        emitCount.set(0);
    }

    public void emit(Map<KOut, List<VOut>> combined) {
        for (Entry<KOut, List<VOut>> e : combined.entrySet()) {
            KOut k = e.getKey();
            List<VOut> values = e.getValue();
            for (VOut v : values) {
                emit(k, v);
            }
        }
    }

    public boolean isOverflown() {
        return emitCount.get() > maxCollectorSize;
    }

}
