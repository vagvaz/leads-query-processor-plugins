package eu.leads.processor.infinispan.operators;

import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Action;
import eu.leads.processor.core.ActionStatus;
import eu.leads.processor.core.net.Node;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.infinispan.Cache;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by tr on 30/8/2014.
 */
public abstract class BasicOperator extends Thread implements Operator{
    protected JsonObject conf;
    protected Action action;
    protected InfinispanManager manager;
    protected Node com;
    protected Cache statisticsCache;
    private String finalOperatorName, statInputSizeKey, statOutputSizeKey, statExecTimeKey;


    protected BasicOperator(Action action) {
        conf = action.getData();
        this.action = action;
    }
    protected BasicOperator(Node com, InfinispanManager manager,Action action){
       super(com.getId()+"-operator-thread");
       System.err.println(this.getClass().getCanonicalName());

       this.com = com;
       this.manager = manager;
       this.action = action;
       this.conf = action.getData().getObject("operator").getObject("configuration");
       this.statisticsCache = (Cache) manager.getPersisentCache(StringConstants.STATISTICS_CACHE);
    }
    protected void init_statistics(String finalOperatorName ){
        this.finalOperatorName=finalOperatorName;
        this.statInputSizeKey = finalOperatorName+"inputSize";
        this.statOutputSizeKey = finalOperatorName+"outputSize";
        this.statExecTimeKey = finalOperatorName+"timeSize";
    }

   @Override
   public void init(JsonObject config) {
     this.conf = config;
   }

   @Override
   public void execute() {
      start();
   }

   @Override
   public void cleanup() {
      action.setStatus(ActionStatus.COMPLETED.toString());
      if(com != null)
         com.sendTo(action.getData().getString("owner"),action.asJsonObject());
      else
         System.err.println("PROBLEM Uninitialized com");
   }

    public void UpdateStatistics(double inputSize, double outputSize, double executionTime){
        UpdateSpecificStatistic(statInputSizeKey, inputSize);
        UpdateSpecificStatistic(statOutputSizeKey, outputSize);
        UpdateSpecificStatistic(statExecTimeKey, executionTime);
    }

    public void UpdateSpecificStatistic(String StatNameKey, double NewValue){
        DescriptiveStatistics  stats;
        if(!statisticsCache.containsKey(StatNameKey)) {
             stats = new DescriptiveStatistics();
            //stats.setWindowSize(1000);
        }
        else
            stats=(DescriptiveStatistics)statisticsCache.get(StatNameKey);
        stats.addValue(NewValue);
        statisticsCache.put(StatNameKey,stats);
    }

   @Override
    public JsonObject getConfiguration() {
        return conf;
    }

    @Override
    public void setConfiguration(JsonObject config) {
        conf = config;
    }

   @Override
   public String getInput() {
      return action.getData().getObject("operator").getArray("inputs").iterator().next().toString();
   }

   @Override
   public void setInput(String input) {
      conf.putString("input",input);
   }

   @Override
   public String getOutput() {
      return action.getData().getObject("operator").getString("id");
   }


   @Override
   public void setOutput(String output) {
      conf.putString("output",output);
   }

   @Override
   public void setOperatorParameters(JsonObject parameters) {
      conf = parameters;
   }

   @Override
   public JsonObject getOperatorParameters() {
      return conf;
   }
}
