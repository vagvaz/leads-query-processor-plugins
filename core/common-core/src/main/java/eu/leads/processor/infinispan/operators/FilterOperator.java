package eu.leads.processor.infinispan.operators;

import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Action;
import eu.leads.processor.core.net.Node;
import eu.leads.processor.math.FilterOperatorTree;
import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: vagvaz
 * Date: 10/29/13
 * Time: 7:06 AM
 * To change this template use File | Settings | File Templates.
 */
//Filter Operator
public class FilterOperator extends BasicOperator {


    private FilterOperatorTree tree;
    private Cache inputCache;


    public FilterOperator(Node com, InfinispanManager persistence, Action action) {

       super(com, persistence, action);
       JsonElement qual = conf.getObject("body").getElement("qual");
       tree = new FilterOperatorTree(qual);
        inputCache = (Cache) manager.getPersisentCache(getInput());
    }

  //  public FilterOperator(PlanNode node) {
  //      super(node, OperatorType.FILTER);
  //  }


    public FilterOperatorTree getTree() {
        return tree;
    }

    public void setTree(FilterOperatorTree tree) {
        this.tree = tree;
    }

   @Override
   public void run() {
       long startTime = System.nanoTime();
      inputCache = (Cache) manager.getPersisentCache(getInput());
      Cache outputCache = (Cache)manager.getPersisentCache(getOutput());

      DistributedExecutorService des = new DefaultExecutorService(inputCache);
      FilterCallable callable = new FilterCallable(conf.toString(),getOutput(),conf.getObject("body").getObject("qual").toString());
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

       // webCache.size() , outputCache.size() , DT

      cleanup();
      //Store Values for statistics
      UpdateStatistics(inputCache.size(),outputCache.size(),System.nanoTime()-startTime);
   }

   @Override
    public void init(JsonObject config) {
        super.init(conf);
        inputCache = (Cache) manager.getPersisentCache(getInput());
        conf.putString("output",getOutput());
        init_statistics(this.getClass().getCanonicalName());
    }

    @Override
    public void execute() {
      super.execute();
    }

    @Override
    public void cleanup() {
      super.cleanup();
    }

//    @Override
//    public String toString() {
//        return getType() + tree.toString();
//    }
}
