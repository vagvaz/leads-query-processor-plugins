package eu.leads.processor.infinispan.operators.mapreduce;

import eu.leads.processor.common.infinispan.AcceptAllFilter;
import eu.leads.processor.common.infinispan.ClusterInfinispanManager;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Tuple;
import eu.leads.processor.math.FilterOperatorTree;
import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterable;
import org.infinispan.distexec.DistributedCallable;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.Serializable;
import java.util.*;

/**
 * Created by vagvaz on 9/25/14.
 */
public class ScanCallable <K,V> implements

        DistributedCallable<K, V, String>, Serializable {
   transient protected Cache<K, V> inputCache;
   transient protected Cache outputCache;
    transient  protected Cache pageRankCache;
   transient protected FilterOperatorTree tree;
   transient protected JsonObject inputSchema;
   transient protected JsonObject outputSchema;
   transient protected Map<String, String> outputMap;
   transient protected Map<String, JsonObject> targetsMap;
   transient protected JsonObject conf;
   transient protected double totalSum;
   protected String configString;
   protected String output;
   protected String qualString;
   transient protected InfinispanManager manager;
   public ScanCallable(String configString, String output) {
      this.configString = configString;
      this.output = output;
   }

   @Override
   public void setEnvironment(Cache<K, V> cache, Set<K> inputKeys) {
      inputCache = cache;
      manager =  new ClusterInfinispanManager(cache.getCacheManager());
      outputCache = (Cache) manager.getPersisentCache(output);
      pageRankCache = (Cache) manager.getPersisentCache("pagerankCache");
      totalSum = -1f;

      conf = new JsonObject(configString);
      if(conf.getObject("body").containsField("qual"))
      {
         tree = new FilterOperatorTree(conf.getObject("body").getObject("qual"));
      }
      else{
         tree =null;
      }

      outputSchema = conf.getObject("body").getObject("outputSchema");
      inputSchema = conf.getObject("body").getObject("inputSchema");
      targetsMap = new HashMap();
      outputMap = new HashMap<>();
         JsonArray targets = conf.getObject("body").getArray("targets");
      Iterator<Object> targetIterator = targets.iterator();
      while (targetIterator.hasNext()) {
         JsonObject target = (JsonObject) targetIterator.next();
         targetsMap.put(target.getObject("expr").getObject("body").getObject("column").getString("name"), target);
      }
   }

   @Override
   public String call() throws Exception {
      for (Map.Entry<K, V> entry : inputCache.entrySet()) {
         String key = (String) entry.getKey();
         String value = (String) entry.getValue();
         Tuple tuple = new Tuple(value);
          namesToLowerCase(tuple);
          renameAllTupleAttributes(tuple);
         if (tree != null) {
            if(tree.accept(tuple)) {
               tuple = prepareOutput(tuple);
               outputCache.putIfAbsent(key, tuple.asString());
            }
         }
         else{
            tuple = prepareOutput(tuple);
            outputCache.putIfAbsent(key, tuple.asString());
         }

      }
      return inputCache.getCacheManager().getAddress().toString();
   }

  private void namesToLowerCase(Tuple tuple) {
    Set<String> fieldNames  =  new HashSet<>(tuple.getFieldNames());
    for(String field : fieldNames){
      tuple.renameAttribute(field,field.toLowerCase());
    }
  }

  private void renameAllTupleAttributes(Tuple tuple) {
       JsonArray fields = inputSchema.getArray("fields");
        Iterator<Object> iterator = fields.iterator();
        String columnName = null;
        while(iterator.hasNext()){
            JsonObject tmp = (JsonObject) iterator.next();
            columnName = tmp.getString("name");
            int lastPeriod = columnName.lastIndexOf(".");
            String attributeName = columnName.substring(lastPeriod+1);
            tuple.renameAttribute(attributeName,columnName);
        }

        handlePagerank(columnName.substring(0,columnName.lastIndexOf(".")),tuple);
    }

    protected Tuple prepareOutput(Tuple tuple) {
      if (outputSchema.toString().equals(inputSchema.toString())) {
         return tuple;
      }
      JsonObject result = new JsonObject();
      List<String> toRemoveFields = new ArrayList<String>();
      Map<String,String> toRename = new HashMap<String,String>();
      for (String field : tuple.getFieldNames()) {
         JsonObject ob = targetsMap.get(field);
         if (ob == null)
            toRemoveFields.add(field);
         else {
            toRename.put(field, ob.getObject("column").getString("name"));
         }
      }
      tuple.removeAtrributes(toRemoveFields);
      tuple.renameAttributes(toRename);
      return tuple;
   }

   protected void handlePagerank(String substring, Tuple t) {
       if(conf.getObject("body").getObject("tableDesc").getString("tableName").equals("default.webpages")){
            String url = t.getAttribute("default.webpages.url");
                t.setAttribute("default.webpages.pagerank",0f);
                return;
      }
   }

    
}
