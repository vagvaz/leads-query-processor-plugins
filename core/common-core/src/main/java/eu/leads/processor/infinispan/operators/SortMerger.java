package eu.leads.processor.infinispan.operators;

import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Tuple;
import eu.leads.processor.core.TupleComparator;
import org.infinispan.Cache;
import org.vertx.java.core.json.JsonObject;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: vagvaz
 * Date: 12/3/13
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class SortMerger {

//    private Map<String, String> input;
//    private String output;
    private final String prefix;
    private final Map<String, String> outputCache;
    private Vector<Integer> counters;
    private Vector<Tuple> values;
    private Vector<String> keys;
    private Vector<Map<String, String>> caches;
    private final TupleComparator comparator;
    private Vector<String> cacheNames;
   protected JsonObject inputSchema;
   protected JsonObject outputSchema;
   protected Map<String,String> outputMap;
   protected Map<String,JsonObject> targetsMap;
   InfinispanManager manager;
    public SortMerger(List<String> inputCaches, String output, TupleComparator comp, InfinispanManager manager, JsonObject conf) {

        prefix = output+":";
//        this.output = output;
//        input = inputMap;
       this.manager = manager;
        outputCache = manager.getPersisentCache(output);
        counters = new Vector<Integer>(inputCaches.size());
        values = new Vector<Tuple>(inputCaches.size());
        caches = new Vector<Map<String, String>>();
        cacheNames = new Vector<String>(inputCaches.size());
        keys = new Vector<String>(inputCaches.size());
        comparator = comp;
        for (String entry : inputCaches) {
            Cache cache  = (Cache) manager.getPersisentCache((entry));
            if(cache.size() == 0)
            {
              manager.removePersistentCache(entry);
              continue;
            }
            counters.add(0);
            keys.add(entry);
            caches.add(cache);
            Tuple t = getCurrentValue(keys.size() - 1);
            values.add(t);
            cacheNames.add(entry);

        }
//      outputSchema = conf.getObject("body").getObject("outputSchema");
//      inputSchema = conf.getObject("body").getObject("inputSchema");
//      targetsMap = new HashMap();
//      outputMap = new HashMap<>();
//      JsonArray targets = conf.getObject("body").getArray("targets");
//      Iterator<Object> targetIterator = targets.iterator();
//      while (targetIterator.hasNext()) {
//        JsonObject target = (JsonObject) targetIterator.next();
//        targetsMap.put(target.getObject("expr").getObject("body").getObject("column").getString("name"), target);
//      }
    }

    private Tuple getCurrentValue(int cacheIndex) {
        String key = keys.get(cacheIndex);
        Integer counter = counters.get(cacheIndex);
        String tmp = caches.get(cacheIndex).get(key  + counter.toString());
        return new Tuple(tmp);
    }

    private Tuple getNextValue(int cacheIndex) {
        String key = keys.get(cacheIndex);
        Integer counter = counters.get(cacheIndex);
        counter = counter + 1;
        if (counter >= caches.get(cacheIndex).size()) {
            counters.remove(cacheIndex);
            caches.remove(cacheIndex);
            manager.removePersistentCache(cacheNames.elementAt(cacheIndex));
            cacheNames.removeElementAt(cacheIndex);
            keys.remove(cacheIndex);
            values.remove(cacheIndex);
            return null;
        }
        counters.set(cacheIndex, counter);
        String tmp = caches.get(cacheIndex).get(key +  counter.toString());
        return new Tuple(tmp);
    }

    public void merge() {
        Tuple nextValue = null;
        Tuple t = null;
        while (caches.size() > 0) {
            int minIndex = findMinIndex(values);

            t = values.get(minIndex);
//            t = prepareOutput(t);
            outputCache.put(prefix + outputCache.size(), t.asString());

            nextValue = getNextValue(minIndex);
            if (nextValue != null)
                values.set(minIndex, nextValue);
        }
        counters.clear();
        counters = null;
        for(String cache : keys){
            manager.removePersistentCache(cache);
        }
        keys.clear();
        keys = null;
        values.clear();
        values = null;
        cacheNames.clear();
        cacheNames = null;
        for (Map<String, String> map : caches) {
            map.clear();
        }
        caches.clear();
        caches = null;
    }

    private int findMinIndex(Vector<Tuple> values) {
        int result = 0;
        Tuple curMin = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            int cmp = comparator.compare(curMin, values.get(i));
            if (cmp > 0) {
                curMin = values.get(i);
                result = i;
            }

        }
        return result;

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
}
