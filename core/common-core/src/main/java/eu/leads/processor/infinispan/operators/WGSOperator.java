package eu.leads.processor.infinispan.operators;

import eu.leads.processor.common.LeadsCollector;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Action;
import eu.leads.processor.core.LeadsMapperCallable;
import eu.leads.processor.core.LeadsReduceCallable;
import eu.leads.processor.core.net.Node;
import eu.leads.processor.infinispan.operators.mapreduce.WGSMapper;
import eu.leads.processor.infinispan.operators.mapreduce.WGSReducer;
import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by vagvaz on 9/26/14.
 */
public class WGSOperator extends MapReduceOperator {
   private Cache inputCache  ;
   private JsonArray attributesArray;
   public WGSOperator(Node com, InfinispanManager persistence, Action action) {
      super(com,persistence,action);
      attributesArray = new JsonArray();
      attributesArray.add("url");
      attributesArray.add("links");
      attributesArray.add("sentiment");
      attributesArray.add("pagerank");
   }
   @Override
   public void init(JsonObject config) {
      super.init(conf);
      init_statistics(this.getClass().getCanonicalName());
   }

   @Override
   public void run() {
      int count = 0;
      inputCacheName = getName() +".iter0";
      inputCache = (Cache) manager.getPersisentCache(inputCacheName);
      JsonObject configBody = conf.getObject("body");
      inputCache.put(configBody.getString("url"),configBody.getString("url"));
      Cache realOutput = (Cache) manager.getPersisentCache(conf.getString("realOutput"));
      for ( count = 0; count < configBody.getInteger("depth"); count++) {
         inputCache = (Cache)manager.getPersisentCache(getName()+".iter"+String.valueOf(count));
         System.out.println("realOutput " + conf.getString("realOutput") +" \nsize" + realOutput.size());
         JsonObject jobConfig = new JsonObject();
         jobConfig.putNumber("iteration", count);
         jobConfig.putNumber("depth", configBody.getInteger("depth"));
         jobConfig.putArray("attributes",attributesArray);
         if(count < configBody.getInteger("depth")){
            jobConfig.putString("outputCache",getName()+".iter"+String.valueOf(count+1));
         }
         else
         {
            jobConfig.putString("outputCache","");
         }
         jobConfig.putString("realOutput",conf.getString("realOutput"));
         jobConfig.putString("webCache","default.webpages");
         executeMapReducePhase(jobConfig);
      }
     cleanup();
   }

  private void executeMapReducePhase(JsonObject jobConfig) {
    DistributedExecutorService des = new DefaultExecutorService(inputCache);
    intermediateCacheName = inputCache.getName()+".intermediate";
    collector = new LeadsCollector(0, intermediateCache);
    LeadsMapperCallable mapperCallable = new LeadsMapperCallable(inputCache,collector,new WGSMapper(jobConfig.toString()));
    List<Future<?>> res = des.submitEverywhere(mapperCallable);
    try {
      if (res != null) {
        for (Future<?> result : res) {
          result.get();
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
    //Reduce

    LeadsReduceCallable reducerCacllable = new LeadsReduceCallable(outputCache, new WGSReducer(jobConfig.toString()));
    DistributedExecutorService des_inter = new DefaultExecutorService(intermediateCache);
    List<Future<?>> reducers_res;
    res = des_inter
            .submitEverywhere(reducerCacllable);
    try {
      if (res != null) {
        for (Future<?> result : res) {
          result.get();
        }
        System.out.println("reducer Execution is done");
      } else {
        System.out.println("reducer Execution not done");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

  }

  @Override
   public void execute() {
      super.execute();
   }

   @Override
   public void cleanup() {
      super.cleanup();
   }
}
