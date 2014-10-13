package eu.leads.processor.infinispan.operators.mapreduce;

import eu.leads.processor.common.infinispan.ClusterInfinispanManager;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.LeadsReducer;
import eu.leads.processor.core.Tuple;
import eu.leads.processor.math.MathUtils;
import org.infinispan.Cache;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.*;

//import eu.leads.processor.common.utils.SQLUtils;

/**
 * Created with IntelliJ IDEA.
 * User: vagvaz
 * Date: 11/4/13
 * Time: 9:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class GroupByReducer extends LeadsReducer<String, String> {



    transient Cache<String, String> data;
    transient String prefix;
    transient List<Object> aggregateValues;
    transient List<String> functionType;
    transient List<String> columnTypes;
    transient List<String> columnParams;
    transient  private ArrayList<String> groupByColumns;
    transient  private ArrayList<JsonObject> aggregates;
    transient Map<String,Integer> typesOfaggregates;
    transient List<String> aggregateNames;
  transient Set<String> inputFields;
  transient ArrayList<String> aggregateInferred;
 transient InfinispanManager imanager;

   public GroupByReducer(JsonObject configuration) {
        super(configuration);
    }

   public GroupByReducer(String configString) {
      super(configString);
   }

   @Override
    public void initialize() {

        isInitialized = true;
        super.initialize();
        imanager = new ClusterInfinispanManager(manager);
      prefix = outputCacheName+":";
      data = (Cache<String, String>) imanager.getPersisentCache(outputCacheName);
      aggregateValues = new ArrayList<>();
      functionType = new ArrayList<>();
      columnTypes = new ArrayList<>();
      columnParams = new ArrayList<>();
      typesOfaggregates = new HashMap<>();
      aggregateNames = new ArrayList<>();
      aggregates = new ArrayList<>();
      JsonArray columns = conf.getObject("body").getArray("groupingColumns");
      Iterator<Object> columnIterator = columns.iterator();
      groupByColumns = new ArrayList<>(columns.size());

      while(columnIterator.hasNext()){
         JsonObject columnObject = (JsonObject) columnIterator.next();
         groupByColumns.add(columnObject.getString("name"));
      }

      JsonArray functions = conf.getObject("body").getArray("aggrFunctions");
      Iterator<Object> funcIterator = functions.iterator();
      while(funcIterator.hasNext()){
         JsonObject current = (JsonObject) funcIterator.next();
         aggregates.add(current);
         String funcType = current.getObject("funcDesc").getString("signature");
         JsonObject argument = (JsonObject) current.getArray("argEvals").iterator().next();
         columnParams.add(argument.getObject("body").getObject("column").getString("name"));
         functionType.add(funcType);
         Integer count = typesOfaggregates.get(funcType);
         if(count == null){
            aggregateNames.add("?"+funcType);
            count = 1;
         }
         else{
            aggregateNames.add("?"+funcType+"_"+count.toString());
            count++;
         }
         typesOfaggregates.put(funcType,count);
         JsonObject parameter = (JsonObject) current.getObject("funcDesc").getArray("params").iterator().next();
         columnTypes.add(parameter.getString("type"));
         Object object = MathUtils.getInitialValue(parameter.getString("type"),current.getObject("funcDesc").getString("signature"));
         aggregateValues.add(object);
      }
      aggregateInferred = inferFinalAggNames();
    }

  private ArrayList<String> inferFinalAggNames() {
    ArrayList<String> result = new ArrayList<>(aggregateNames.size());
    Iterator<Object> inputIterator = conf.getObject("body").getObject("inputSchema").getArray("fields").iterator();
    Set<String> inputColumns = new HashSet<String>();
    while(inputIterator.hasNext()){
      JsonObject field = (JsonObject) inputIterator.next();
      inputColumns.add(field.getString("name"));
    }
    Iterator<Object> targetIterator = conf.getObject("body").getArray("targets").iterator();
    int counter = 0;
    while(targetIterator.hasNext()){
      JsonObject target = (JsonObject) targetIterator.next();
      String targetName = target.getObject("expr").getObject("body").getObject("column").getString("name");

      if(!inputColumns.contains(targetName)){
        result.add(targetName);
        counter++;
      }
    }

    return result;
  }

  @Override
    public String reduce(String key, Iterator<String> iterator) {
       //Reduce takes all the grouped Typles per key
      if(key == null || key.equals(""))
        return "";
        if (!isInitialized) initialize();
        resetValues();
        Tuple t = null;
        progress();
       //Iterate overall values
        while (iterator.hasNext()) {

            t = new Tuple(iterator.next());
           //handle pagerank
           handlePagerank(t);
           Iterator<String> funcTypeIterator= functionType.iterator();
//           Iterator<Object> aggValuesIterator = aggregateValues.iterator();
           Iterator<String> columnTypesIterator = columnTypes.iterator();
           Iterator<String> columnNameiterator =  columnParams.iterator();
           int counter = 0;
           //for each function
           while(funcTypeIterator.hasNext()){

              String funcType= funcTypeIterator.next();
              String columnType = columnTypesIterator.next();
              String column = columnNameiterator.next();
              //set new aggvalue according to function type, columnt Type, old agg value, currentValue
              aggregateValues.set(counter,MathUtils.updateFunctionValue(funcType, columnType, aggregateValues.get(counter),t.getGenericAttribute(column)));
              counter++; //inc counter for the next agg value
           }

        }

       Iterator<String> nameIterator= aggregateInferred.iterator();
       Iterator<Object> aggValuesIterator = aggregateValues.iterator();
       Iterator<String> funcTypeIterator = functionType.iterator();

       //compute final values and put agg values to tuple
       while(nameIterator.hasNext()){
          String name = nameIterator.next();
          Object value = aggValuesIterator.next();
          String funcType = funcTypeIterator.next();
          Object tupleValue = value;
          //if function is avg compute value
          if(funcType.equals("avg")){
             Map<String,Object> avgMap = (Map<String, Object>) value;
             Double avgValue = MathUtils.computeAvg(avgMap);
             tupleValue = avgValue;
          }
          t.setAttribute(name,tupleValue);
       }
       //prepare output
//        System.err.println("t: " + t.toString());
        t = prepareOutput(t);
//        System.err.println("tout: " + t.toString());
        data.put(prefix + key, t.asString());
        return "";
    }

   private void resetValues() {
      for (int i = 0; i < aggregateValues.size(); i++) {
         aggregateValues.set(i,MathUtils.getInitialValue(columnTypes.get(i),functionType.get(i)));
      }
   }
}
