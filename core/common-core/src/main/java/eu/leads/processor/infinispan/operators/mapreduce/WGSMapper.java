package eu.leads.processor.infinispan.operators.mapreduce;

import eu.leads.processor.common.infinispan.AcceptAllFilter;
import eu.leads.processor.common.infinispan.ClusterInfinispanManager;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.LeadsMapper;
import eu.leads.processor.core.Tuple;
import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterable;
import org.infinispan.distexec.mapreduce.Collector;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by vagvaz on 9/26/14.
 */
public class WGSMapper extends LeadsMapper<String, String, String, String> {
   public WGSMapper(String configString) {
      super(configString);
   }

  protected transient String prefix;
   protected transient List<String> attributes;
   protected transient Cache webCache;
   protected transient Cache outputCache;
   protected transient int depth;
   protected transient int iteration;
  protected transient double totalSum;
    protected transient Cache pagerankCache;
    protected transient InfinispanManager imanager;
  public  void initialize() {
      imanager = new ClusterInfinispanManager(manager);
      isInitialized = true;
      super.initialize();
        totalSum = -1.0;
      iteration = conf.getInteger("iteration");
      depth = conf.getInteger("depth");
      webCache = (Cache) imanager.getPersisentCache(conf.getString("webCache"));
       pagerankCache = (Cache) imanager.getPersisentCache("pagerankCache");
      if(iteration < depth){
         outputCache = (Cache) imanager.getPersisentCache(conf.getString("outputCache"));
      }
      else{
         outputCache = null;
      }

      prefix = webCache.getName()+":";
      JsonArray array = conf.getArray("attributes");
      Iterator<Object> iterator = array.iterator();
      attributes = new ArrayList<String>(array.size());
      while(iterator.hasNext()){
         attributes.add((String) iterator.next());
      }
   }

   @Override
   public void map(String key, String value, Collector<String, String> collector) {
     if(!isInitialized)
       this.initialize();
      String jsonString = (String) webCache.get(prefix+key);
      if (jsonString==null || jsonString.equals("")){
         return;
      }
      Tuple t = new Tuple(jsonString);
      handlePagerank(t);
      JsonObject result = new JsonObject();
      result.putString("url", t.getAttribute("url"));
      result.putString("pagerank", computePagerank(result.getString("url")));
      result.putString("sentiment", t.getGenericAttribute("sentiment").toString());
      result.putValue("links",t.getGenericAttribute("links"));
      collector.emit(String.valueOf(iteration),result.toString());
      if(outputCache != null){
         JsonArray links = result.getArray("links");
         Iterator<Object> iterator = links.iterator();
         while(iterator.hasNext()){
            String link = (String) iterator.next();
            outputCache.put(link,link);
         }
      }

   }

    private String computePagerank(String url) {
        double result = 0.0;
                return Double.toString(0.0f);

    }

    private void computeTotalSum() {
        Cache approxSumCache = (Cache) imanager.getPersisentCache("approx_sum_cache");
        CloseableIterable<Map.Entry<String, Integer>> iterable =
                approxSumCache.getAdvancedCache().filterEntries(new AcceptAllFilter());

        for (Map.Entry<String, Integer> outerEntry : iterable) {
            totalSum += outerEntry.getValue() ;
        }
        if(totalSum > 0){
            totalSum+=1;
        }
    }
}
