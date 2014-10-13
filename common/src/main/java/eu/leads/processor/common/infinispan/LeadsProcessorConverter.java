package eu.leads.processor.common.infinispan;

import org.infinispan.filter.Converter;
import org.infinispan.metadata.Metadata;

/**
 * Created by vagvaz on 9/29/14.
 */
public class LeadsProcessorConverter implements Converter {
    @Override public Object convert(Object key, Object value, Metadata metadata) {
        return new ProcessorEntry(key,value);
    }
}
