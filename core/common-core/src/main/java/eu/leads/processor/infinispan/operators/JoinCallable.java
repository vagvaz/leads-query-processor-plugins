package eu.leads.processor.infinispan.operators;

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
 * Created by vagvaz on 9/28/14.
 */
public class JoinCallable<K,V> implements

    DistributedCallable<K, V, String>, Serializable {


    transient protected Cache<K,V> inputCache;
    transient protected Cache outerCache;
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
    protected boolean left;
    protected String innerColumn;
    protected String outerColumn;
    protected final String outerCacheName;


    public JoinCallable(String configString, String outputCacheName, String outerCacheName,boolean left) {
        this.configString = configString;
        this.output = outputCacheName;
        this.outerCacheName = outerCacheName;
        this.left = left;
    }

    @Override public void setEnvironment(Cache<K, V> cache, Set<K> set) {
      conf = new JsonObject(configString);
        this.inputCache = cache;
        ClusterInfinispanManager manager = new ClusterInfinispanManager(cache.getCacheManager());
        outputCache = (Cache) manager.getPersisentCache(output);
        outerCache = (Cache) manager.getPersisentCache(outerCacheName);

        JsonObject object = conf.getObject("body").getObject("joinQual");

        tree = new FilterOperatorTree(object);
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
        if(left) {
            innerColumn = conf.getObject("body").getObject("joinQual").getObject("body").getObject("leftExpr").getObject("body").getObject("column").getString("name");
            outerColumn = conf.getObject("body").getObject("joinQual").getObject("body").getObject("rightExpr").getObject("body").getObject("column").getString("name");
        }
        else
        {
            innerColumn = conf.getObject("body").getObject("joinQual").getObject("body").getObject("rightExpr").getObject("body").getObject("column").getString("name");
            outerColumn = conf.getObject("body").getObject("joinQual").getObject("body").getObject("leftExpr").getObject("body").getObject("column").getString("name");
        }
    }

    @Override public String call() throws Exception {
      try {
        Map<String, List<Tuple>> buffer = new HashMap<String, List<Tuple>>();
        int size = 0;
        ArrayList<String> ignoreColumns = new ArrayList<>();
//        ignoreColumns.add(innerColumn);
//        ignoreColumns.add(outerColumn);
        String prefix = output + ":";
        for (Map.Entry<K, V> entry : inputCache.entrySet()) {
          Tuple current = new Tuple((String) entry.getValue());
          String columnValue = current.getGenericAttribute(innerColumn).toString();
          String key = (String) entry.getKey();
          String currentKey = key.substring(key.indexOf(":") + 1);


          CloseableIterable<Map.Entry<String, String>> iterable =
            outerCache.getAdvancedCache().filterEntries(new AttributeFilter(outerColumn,
                                                                             columnValue));
          for (Map.Entry<String, String> outerEntry : iterable) {
            Tuple outerTuple = new Tuple(outerEntry.getValue());
            Tuple resultTuple = new Tuple(current, outerTuple, ignoreColumns);
            String outerKey = outerEntry.getKey().substring(outerEntry.getKey().indexOf(":") + 1);
            String combinedKey = prefix + outerKey + "-" + currentKey;
            resultTuple = prepareOutput(resultTuple);
            outputCache.put(combinedKey, resultTuple.asJsonObject().toString());
          }

        }
      }catch (Exception e) {

                System.err.println("Iterating over " + outerCacheName
                                       + " for batch resulted in Exception "
                                       + e.getMessage() + "\n from  " + outerCacheName);
              return "Iterating over " + outerCacheName
                       + " for batch resulted in Exception "
                       + e.getMessage() + "\n from  " + outerCacheName;
            }

        return "Successful run over " + inputCache.getCacheManager().getAddress().toString();
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
