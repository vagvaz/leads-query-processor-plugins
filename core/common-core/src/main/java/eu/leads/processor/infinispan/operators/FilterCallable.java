package eu.leads.processor.infinispan.operators;

import eu.leads.processor.core.Tuple;
import eu.leads.processor.math.FilterOperatorTree;
import org.infinispan.Cache;
import org.infinispan.distexec.DistributedCallable;
import org.vertx.java.core.json.JsonObject;

import java.io.Serializable;
import java.util.*;

/**
 * Created by vagvaz on 9/24/14.
 */
public class FilterCallable<K,V> implements

        DistributedCallable<K, V, String>, Serializable {
   transient protected Cache<K,V> inputCache;
   transient protected Cache outputCache;
   transient protected FilterOperatorTree tree;
   transient protected JsonObject inputSchema;
   transient protected JsonObject outputSchema;
   transient protected Map<String,String> outputMap;
   transient protected Map<String,JsonObject> targetsMap;
   transient protected JsonObject conf;
   protected String configString;
   protected String output;
   protected String qualString;

   public FilterCallable(String configString, String output, String qualString){
      this.configString = configString;
      this.output= output;
      this.qualString = qualString;
   }
   @Override
   public void setEnvironment(Cache<K, V> cache, Set<K> inputKeys) {
      inputCache = cache;
      outputCache = cache.getCacheManager().getCache(output);
      JsonObject object = new JsonObject(qualString);
      conf = new JsonObject(configString);
      tree = new FilterOperatorTree(object);
      outputSchema = conf.getObject("body").getObject("outputSchema");
      inputSchema = conf.getObject("body").getObject("inputSchema");
      targetsMap = new HashMap();
      outputMap = new HashMap<>();
//      JsonArray targets = conf.getObject("body").getArray("targets");
//      Iterator<Object> targetIterator = targets.iterator();
//      while (targetIterator.hasNext()) {
//         JsonObject target = (JsonObject) targetIterator.next();
//         targetsMap.put(target.getObject("expr").getObject("body").getObject("column").getString("name"), target);
//      }
   }

   @Override
   public String call() throws Exception {
      for(Map.Entry<K,V> entry : inputCache.entrySet()){
         String key = (String) entry.getKey();
         String value = (String) entry.getValue();
         Tuple tuple = new Tuple(value);
         if(tree.accept(tuple)){
//            tuple = prepareOutput(tuple);
            outputCache.put(key,tuple.asString());
         }

      }
      return inputCache.getCacheManager().getAddress().toString();
   }

   protected Tuple prepareOutput(Tuple tuple){
      if(outputSchema.toString().equals(inputSchema.toString())){
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
   protected  void handlePagerank(Tuple t) {

      if (t.hasField("default.webpages.pagerank")) {
         if (!t.hasField("url"))
            return;
         String pagerankStr = t.getAttribute("pagerank");
//            Double d = Double.parseDouble(pagerankStr);
//            if (d < 0.0) {
//
//                try {
////                    d = LeadsPrGraph.getPageDistr(t.getAttribute("url"));
//                    d = (double) LeadsPrGraph.getPageVisitCount(t.getAttribute("url"));
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                t.setAttribute("pagerank", d.toString());
//        }
      }
   }
}
