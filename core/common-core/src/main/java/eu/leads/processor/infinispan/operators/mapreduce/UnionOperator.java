package eu.leads.processor.infinispan.operators.mapreduce;

import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Action;
import eu.leads.processor.core.net.Node;
import eu.leads.processor.infinispan.operators.MapReduceOperator;

/**
 * Created by vagvaz on 9/22/14.
 */
public class UnionOperator extends MapReduceOperator {
   public UnionOperator(Node com, InfinispanManager persistence, Action action) {
      super(com, persistence, action);
   }
}
