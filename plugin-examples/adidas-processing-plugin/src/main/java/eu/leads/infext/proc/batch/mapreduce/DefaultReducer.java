package eu.leads.infext.proc.batch.mapreduce;

import java.util.Iterator;

import org.infinispan.distexec.mapreduce.Reducer;

public class DefaultReducer implements Reducer<Object, Object> {

	@Override
	public Object reduce(Object arg0, Iterator<Object> iterator) {
        Object firstValue = iterator.next();
        return firstValue;
	}

}
