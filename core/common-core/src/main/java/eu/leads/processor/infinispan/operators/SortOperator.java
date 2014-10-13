package eu.leads.processor.infinispan.operators;

import eu.leads.processor.core.*;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.net.Node;
import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: vagvaz
 * Date: 10/29/13
 * Time: 1:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class SortOperator extends BasicOperator {
//    List<Column> columns;
   transient protected String[] sortColumns;
   transient protected Boolean[] asceding;
   transient protected String[] types;
    private LeadsMapper<String,String,String,String> mapper;


   public SortOperator(Node com, InfinispanManager persistence, Action action) {
      super(com, persistence, action);
      JsonArray sortKeys = conf.getObject("body").getArray("sortKeys");
      Iterator<Object> sortKeysIterator = sortKeys.iterator();
      sortColumns = new String[sortKeys.size()];
      asceding = new Boolean[sortKeys.size()];
      types = new  String[sortKeys.size()];
      int counter = 0;
      while(sortKeysIterator.hasNext()){
         JsonObject sortKey = (JsonObject) sortKeysIterator.next();
         sortColumns[counter] = sortKey.getObject("sortKey").getString("name");
         asceding[counter] = sortKey.getBoolean("ascending");
         types[counter] = sortKey.getObject("sortKey").getObject("dataType").getString("type");
         counter++;
      }
   }

   @Override
    public void init(JsonObject config) {
//        super.init(config); //fix set correctly caches names
        //fix configuration
       init_statistics(this.getClass().getCanonicalName());
    }

   @Override
   public void run() {
       long startTime = System.nanoTime();
      Cache inputCache = (Cache) this.manager.getPersisentCache(getInput());
      Cache beforeMerge = (Cache)this.manager.getPersisentCache(getOutput()+".merge");
      DistributedExecutorService des = new DefaultExecutorService(inputCache);
      SortCallable callable = new SortCallable(sortColumns,asceding,types,getOutput()+".merge");
      List<Future<String>> res = des.submitEverywhere(callable);
      List<String> addresses = new ArrayList<String>();
      try {
         if (res != null) {
            for (Future<?> result : res) {
               addresses.add((String) result.get());
            }
            System.out.println("mapper Execution is done");
         }
         else
         {
            System.out.println("mapper Execution not done");
         }
      } catch (InterruptedException e) {
         e.printStackTrace();
      } catch (ExecutionException e) {
         e.printStackTrace();
      }
      //Merge outputs
      TupleComparator comparator = new TupleComparator(sortColumns,asceding,types);
      SortMerger merger = new SortMerger(addresses, getOutput(),comparator,manager,conf);
      merger.merge();
//      for(String cacheName : addresses){
//         manager.removePersistentCache(cacheName);
//      }
      manager.removePersistentCache(beforeMerge.getName());
      cleanup();
      //Store Values for statistics
      UpdateStatistics(inputCache.size(), manager.getPersisentCache(getOutput()).size(),System.nanoTime()-startTime);
   }

   @Override
    public void execute() {  //Need Heavy testing
        super.execute();
    }

    @Override
    public void cleanup() {
       super.cleanup();

    }


    public Boolean[] getAscending() {
        return this.asceding;
    }

    public void setAscending(Boolean[] ascending) {
        this.asceding = ascending;
    }


/*
    List<Boolean> ascending;

    public SortOperator(String name) {
        super(name, OperatorType.SORT);
    }

    public SortOperator(PlanNode node) {
        super(node, OperatorType.SORT);
    }

    @JsonCreator
    public SortOperator(@JsonProperty("name") String name, @JsonProperty("output") String output, @JsonProperty("columns") List<Column> orderByColumns, @JsonProperty("asceding") List<Boolean> ascendingOrder) {
        super(name, OperatorType.SORT);
        setOutput(output);
        this.columns = orderByColumns;
        this.ascending = ascendingOrder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(" ");
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getTable() != null)
                builder.append(columns.get(i).getWholeColumnName() + " " + (ascending.get(i) ? " ASC " : " DESC "));
            else
                builder.append(columns.get(i).getColumnName() + " " + (ascending.get(i) ? " ASC " : " DESC "));
        }
        return getType() + builder.toString();
    }
*/}
