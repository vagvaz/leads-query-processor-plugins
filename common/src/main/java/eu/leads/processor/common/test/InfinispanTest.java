package eu.leads.processor.common.test;


import com.fasterxml.jackson.databind.ObjectMapper;
import eu.leads.processor.common.infinispan.CacheManagerFactory;
import eu.leads.processor.common.infinispan.InfinispanCluster;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.common.utils.PrintUtilities;
import eu.leads.processor.conf.LQPConfiguration;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by vagvaz on 6/2/14.
 */
public class InfinispanTest {
  public static void main(String[] args) {
    LQPConfiguration.initialize();
    ObjectMapper mapper = new ObjectMapper();
    InfinispanCluster cluster = InfinispanClusterSingleton.getInstance().getCluster();

//        cluster.initialize();
    InfinispanManager man = cluster.getManager();

    ConcurrentMap map = man.getPersisentCache("queries:");
    map.put("1", "11");
    map.put("2", "22");
    InfinispanCluster cluster2 = new InfinispanCluster(CacheManagerFactory.createCacheManager());

    PrintUtilities.printMap(map);
    ConcurrentMap map2 = cluster2.getManager().getPersisentCache("queries:");
    map2.put("4", "33");
    PrintUtilities.printMap(map2);
    System.out.println("cl");
    PrintUtilities.printList(cluster.getManager().getMembers());
    System.out.println("cl2");
    PrintUtilities.printList(cluster2.getManager().getMembers());

    cluster2.shutdown();
    cluster.shutdown();
    System.exit(0);
  }
}
