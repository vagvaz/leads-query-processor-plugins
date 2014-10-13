package eu.leads.processor.infinispan.operators;

import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Action;
import eu.leads.processor.core.comp.LogProxy;
import eu.leads.processor.core.net.Node;
import eu.leads.processor.math.FilterOperatorTree;
import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: vagvaz
 * Date: 11/7/13
 * Time: 8:34 AM
 * To change this template use File | Settings | File Templates.
 */

public class JoinOperator extends BasicOperator {

    private FilterOperatorTree tree;
    private String innerCacheName;
    private String outerCacheName;
    private LogProxy logProxy;
    private String qualString;
    private boolean isLeft;
    public JoinOperator(Node com, InfinispanManager persistence, Action action) {
      super(com, persistence, action);

       JsonElement qual = conf.getObject("body").getElement("joinQual");
        if(!qual.asObject().getString("type").equals("EQUAL")){
            //TODO change to logProxy
            System.err.println("JOIN is not equal but " + qual.asObject().getString("type") );
        }
        tree = new FilterOperatorTree(qual);
   }


    @Override public void run() {
        long startTime = System.nanoTime();
        Cache innerCache = (Cache) manager.getPersisentCache(innerCacheName);
        Cache outerCache = (Cache) manager.getPersisentCache(outerCacheName);
        Cache outputCache = (Cache) manager.getPersisentCache(getOutput());
        DistributedExecutorService des = new DefaultExecutorService(innerCache);
        JoinCallable joinCallable = new JoinCallable(conf.toString(),getOutput(),outerCache.getName(),isLeft);
        List<Future<String>> res  =  des.submitEverywhere(joinCallable);
        List<String> addresses = new ArrayList<String>();
        try{
            if(res != null){
                for(Future<String> result : res){
                    addresses.add(result.get());
                  System.err.println(addresses.get(addresses.size()-1));
                }
                //TODO log
                System.err.println("Join Callable successfully run");
            }
            else{
                //TODO log
                System.err.println("Join Callable did not run");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        cleanup();
        //Store Values for statistics
        UpdateStatistics(innerCache.size(),outerCache.size(),System.nanoTime()-startTime);
    }

    @Override
    public void init(JsonObject config) {
//        super.init(config); //fix set correctly caches names
        //fix configuration
        JsonArray inputsArray = action.getData().getObject("operator").getArray("inputs");
       Iterator<Object> inputIterator = inputsArray.iterator();
       List<String> inputs = new ArrayList<String>(2);
       while(inputIterator.hasNext()){
           inputs.add((String)inputIterator.next());
       }
       Cache left = (Cache) manager.getPersisentCache(inputs.get(0));
       Cache right = (Cache) manager.getPersisentCache(inputs.get(1));
       if(left.size() >= right.size()){
           innerCacheName = left.getName();
           outerCacheName = right.getName();
           isLeft = true;
       }
       else{
           innerCacheName = right.getName();
           outerCacheName = left.getName();
           isLeft = false;
       }
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
}
