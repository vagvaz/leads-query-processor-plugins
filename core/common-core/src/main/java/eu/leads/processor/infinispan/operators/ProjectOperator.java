package eu.leads.processor.infinispan.operators;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import eu.leads.processor.core.LeadsMapper;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.core.Action;
import eu.leads.processor.core.net.Node;
import eu.leads.processor.infinispan.operators.mapreduce.ProjectMapper;
import org.vertx.java.core.json.JsonObject;



@JsonAutoDetect
public class ProjectOperator extends MapReduceOperator {



   public ProjectOperator(Node com, InfinispanManager persistence, Action actionData) {
      super(com, persistence, actionData);

   }

   @Override
    public void init(JsonObject config) {
      super.init(conf);
      conf.putString("output",getOutput());
      LeadsMapper projectMapper = new ProjectMapper(conf.toString());
      setMapper(projectMapper);
      setReducer(null);
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
