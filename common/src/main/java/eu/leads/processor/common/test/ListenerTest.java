package eu.leads.processor.common.test;

import eu.leads.processor.common.infinispan.InfinispanCluster;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.conf.LQPConfiguration;
import org.infinispan.Cache;
import org.infinispan.remoting.transport.Address;

/**
 * Created by vagvaz on 6/3/14.
 */
public class ListenerTest {
  public static void main(String[] args) throws InterruptedException {
    int size = Integer.parseInt(args[1]);
    int mode = Integer.parseInt(args[0]);
    int period = Integer.parseInt(args[2]);
    LQPConfiguration.initialize();
    InfinispanCluster cluster2 = InfinispanClusterSingleton.getInstance().getCluster();
    Cache cache = (Cache) cluster2.getManager().getPersisentCache("testCache");
    switch ( mode ) {
      case 1:
        cluster2.getManager().addListener(new TestListener(cache.getCacheManager().getAddress().toString(), new ComplexType(cache.getCacheManager().getAddress().toString())), cache);
        break;
      default:
        cluster2.getManager().addListener(new TestListener(cache.getCacheManager().getAddress().toString(), new ComplexType(cache.getCacheManager().getAddress().toString())), cache);

    }
    for ( int i = 0; i < size; i++ ) {
      cache.put(cache.getCacheManager().getAddress().toString().toString() + Integer.toString(i), cache.getCacheManager().getAddress().toString().toString());
      Thread.sleep(period);

    }

    for ( Address a : cluster2.getManager().getMembers() ) {
      System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + a.toString());
      for ( int i = 0; i < size; i++ ) {
        String key = a.toString() + Integer.toString(i);
        System.out.println(Boolean.toString(cache.get(key) != null));
      }
    }
    cluster2.getManager().stopManager();
    System.out.println("Bye Bye ");
  }
}
