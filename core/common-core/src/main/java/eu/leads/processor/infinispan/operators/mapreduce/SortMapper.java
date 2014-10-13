package eu.leads.processor.infinispan.operators.mapreduce;

import eu.leads.processor.core.LeadsMapper;
import org.infinispan.distexec.mapreduce.Collector;
import org.vertx.java.core.json.JsonObject;

/**
 * Created with IntelliJ IDEA.
 * User: vagvaz
 * Date: 12/3/13
 * Time: 10:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class SortMapper extends LeadsMapper<String, String, String, String> {

    transient protected String[] sortColumns;
    transient protected Boolean[] asceding;
    transient protected String[] types;
    Integer counter = 0;
    Integer numParts = 0;

    public SortMapper(JsonObject configuration) {
        super(configuration);
    }

    public void initialize() {
        counter = 0;
        isInitialized = true;
        super.initialize();
        String columns = conf.getString("sortColumns");

    }

    @Override
    public void map(String key, String value, Collector<String, String> collector) {
//        if (!isInitialized)
//            initialize();
//        progress();
////        Tuple tuple = new Tuple(value);
//       ArrayList<Tuple> tuples = new ArrayList<>();
//       Comparator<Tuple> comparator = new TupleComparator(sortColumns,asceding,types);
//       Collections.sort(tuples,comparator);
//       for (Tuple t : tuples) {
//          handlePagerank(t);
//          out.put(key + ":" + counter, t.asString());
//          counter++;
//       }
//       tuples.clear();
//       return output + key;
     }
}
