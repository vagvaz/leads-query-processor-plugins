package eu.leads.processor.infinispan.operators;

import eu.leads.processor.common.*;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.*;
import eu.leads.processor.core.net.Node;
import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by tr on 19/9/2014.
 */
public abstract class MapReduceOperator extends BasicOperator{
    protected transient Cache<?, ?> inputCache;
    protected transient Cache<?, List<?>> intermediateCache;
    protected transient Cache<?, ?> outputCache;
    protected String inputCacheName;
    protected String outputCacheName;
    protected String intermediateCacheName;
    protected LeadsMapper<?, ?, ?, ?> mapper;
    protected LeadsCollector<?, ?> collector;
    protected LeadsReducer<?,?> reducer;


    public MapReduceOperator(Node com, InfinispanManager persistence, Action action) {

       super(com,persistence,action);
       inputCacheName = getInput();
       outputCacheName = action.getData().getObject("operator").getString("id");
       intermediateCacheName = action.getData().getObject("operator").getString("id")+".intermediate";
    }

    public void setMapper(LeadsMapper<?, ?, ?, ?> mapper) {
        this.mapper = mapper;

    }

    public void setReducer(LeadsReducer<?, ?> reducer) {
        this.reducer = reducer;
    }

    @Override
    public void init(JsonObject config) {
       conf.putString("output",getOutput());
        inputCache = (Cache<?, ?>) manager.getPersisentCache(inputCacheName);
        intermediateCache = (Cache<?, List<?>>) manager
                                                          .getPersisentCache(intermediateCacheName);
          outputCache = (Cache<?, ?>) manager.getPersisentCache(outputCacheName);
          collector = new LeadsCollector(0, intermediateCache);
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
       DistributedExecutorService des = new DefaultExecutorService(inputCache);
       LeadsMapperCallable mapperCallable = new LeadsMapperCallable(inputCache,collector,mapper);
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
       if(reducer != null) {
          LeadsReduceCallable reducerCacllable = new LeadsReduceCallable(outputCache, reducer);
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
        //Store Values for statistics
       UpdateStatistics(inputCache.size(),outputCache.size(),System.nanoTime()-startTime);
       cleanup();
    }


    @Override /// Example do not use
    public void execute() {
      super.start();
    }

    @Override
    public void cleanup() {
       super.cleanup();
       intermediateCache.stop();
    }
}
