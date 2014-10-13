package eu.leads.processor.infinispan.operators.mapreduce;

import eu.leads.processor.core.LeadsMapper;
import eu.leads.processor.core.Tuple;
import org.infinispan.distexec.mapreduce.Collector;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import org.infinispan.distexec.mapreduce.Collector;

/**
 * Created with IntelliJ IDEA.
 * User: vagvaz
 * Date: 11/3/13
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class GroupByMapper extends LeadsMapper<String, String, String, String> {

    transient List<String> columns;

    public GroupByMapper(JsonObject configuration) {
        super(configuration);
        columns = new ArrayList<String>();
    }

   public GroupByMapper(String configString) {
      super(configString);
   }


   @Override
    public void map(String key, String value, Collector<String, String> collector) {
        if (!isInitialized)
            intialize();
        StringBuilder builder = new StringBuilder();
//        String tupleId = key.substring(key.indexOf(":"));
        Tuple t = new Tuple(value);
        progress();
        for (String c : columns) {
            builder.append(t.getGenericAttribute(c).toString() + ",");
        }
        collector.emit(builder.toString(), t.asString());
    }

    private void intialize() {
       isInitialized = true;
       super.initialize();
       JsonArray columnArray = conf.getObject("body").getArray("groupingColumns");
       Iterator<Object> columnsIterator = columnArray.iterator();
       columns = new ArrayList<String>(columnArray.size());
       while(columnsIterator.hasNext()){
          JsonObject current = (JsonObject) columnsIterator.next();
          columns.add(current.getString("name"));
       }

    }


}
