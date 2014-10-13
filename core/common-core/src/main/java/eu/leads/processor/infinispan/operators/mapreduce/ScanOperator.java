package eu.leads.processor.infinispan.operators.mapreduce;

import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Action;
import eu.leads.processor.core.net.Node;
import eu.leads.processor.infinispan.operators.BasicOperator;
import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by vagvaz on 9/22/14.
 */
public class ScanOperator extends BasicOperator {
   private Cache inputCache;

   public ScanOperator(Node com, InfinispanManager persistence, Action action) {
      super(com,persistence,action);
   }

   //  public FilterOperator(PlanNode node) {
   //      super(node, OperatorType.FILTER);
   //  }




   @Override
   public void run() {
      System.err.println("RUNNNING SCAN OPERATOR");
      inputCache = (Cache) manager.getPersisentCache(getInput());

      Cache outputCache = (Cache)manager.getPersisentCache(getOutput());

      if(inputCache.size() == 0){
         cleanup();
         return;
      }
      DistributedExecutorService des = new DefaultExecutorService(inputCache);

      ScanCallable callable = new ScanCallable(conf.toString(),getOutput());
      List<Future<String>> res = des.submitEverywhere(callable);
      List<String> addresses = new ArrayList<String>();
      try {
         if (res != null) {
            for (Future<?> result : res) {
               System.out.println(result.get());
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
      System.err.println("FINISHED RUNNING SCAN " + outputCache.size());
      cleanup();
   }

   @Override
   public void init(JsonObject config) {
      String inputCacheName = getInput();
      inputCache = (Cache) manager.getPersisentCache(getInput());
   }

   @Override
   public void execute() {
      super.execute();
   }

   @Override
   public void cleanup() {

      System.err.println("CLEANING UP " );
      super.cleanup();
   }


}
