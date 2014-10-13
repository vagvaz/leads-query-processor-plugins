package eu.leads.processor.common.test;


import java.util.Iterator;

import org.vertx.java.core.json.JsonObject;

public class WordCountReducer extends LeadsReducer<String, Integer> {

    private static final long serialVersionUID = 1901016598354633256L;

    public WordCountReducer(JsonObject configuration) {
        super(configuration);

    }

    public Integer reduce(String key, Iterator<Integer> iter) {
        int sum = 0;
        while (iter.hasNext()) {
            Integer i = iter.next();
            sum += i;
        }
        return sum;
    }
}
