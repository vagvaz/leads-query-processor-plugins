package eu.leads.processor.common.infinispan;

import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.utils.PrintUtilities;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by vagvaz on 5/23/14.
 */


/**
 * Implementation of InfinispanManager interface This class uses the Distributed Execution of
 * infinispan in order to perform the operations for caches and listeners.
 */
public class ClusterInfinispanManager implements InfinispanManager {


    private Logger log = LoggerFactory.getLogger(this.getClass());
    private EmbeddedCacheManager manager;
    private String configurationFile;
    private HotRodServer server;
    private int serverPort;
    private String host;

    /**
     * Constructs a new ClusterInfinispanManager.
     */
    public ClusterInfinispanManager() {
        host = "0.0.0.0";
        serverPort = 11000;
    }

    public ClusterInfinispanManager(EmbeddedCacheManager manager) {
        this.manager = manager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startManager(String configurationFile)  {

//        server = new HotRodServer();
        ParserRegistry registry = new ParserRegistry();
        ConfigurationBuilderHolder holder = null;
        ConfigurationBuilder builder = null;
        try {
            if (configurationFile != null && !configurationFile.equals("")) {
                holder = registry.parseFile(configurationFile);

            } else {
                System.err.println("\n\n\nUSING DEFAULT FILE ERROR\n\n");
                holder = registry.parseFile(StringConstants.ISPN_CLUSTER_FILE);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        manager = new DefaultCacheManager(holder, true);
        getPersisentCache("clustered");
        //I might want to sleep here for a little while
        PrintUtilities.printList(manager.getMembers());
//        startHotRodServer(manager,host, serverPort);

        System.out.println("We have started");

    }

    public HotRodServer getServer() {
        return server;
    }

    public void setServer(HotRodServer server) {
        this.server = server;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    private void startHotRodServer(EmbeddedCacheManager targetManager, String localhost, int port) {
        serverPort = port;
        boolean isStarted = false;
        while (!isStarted) {
            HotRodServerConfigurationBuilder serverConfigurationBuilder =
                new HotRodServerConfigurationBuilder();
            serverConfigurationBuilder.host(localhost).port(serverPort).keyValueFilterFactory("leads-processor-filter-factory",new LeadsProcessorKeyValueFilterFactory(manager))
            .converterFactory("leads-processor-converter-factory",new LeadsProcessorConverterFactory());
            try {
                server.start(serverConfigurationBuilder.build(), targetManager);
                isStarted = true;
            } catch (Exception e) {
                System.out.println("Exception e " + e.getLocalizedMessage());
                serverPort++;
                isStarted = false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EmbeddedCacheManager getCacheManager() {
        return this.manager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopManager() {
        for(String cacheName : this.manager.getCacheNames())
        {
            Cache cache= this.manager.getCache(cacheName,false);
            cache.stop();
        }
        this.manager.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentMap getPersisentCache(String name) {
        if (manager.cacheExists(name))
            manager.getCache(name);
        else {
            createCache(name, manager.getDefaultCacheConfiguration());
        }
        return manager.getCache(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentMap getPersisentCache(String name, Configuration configuration) {
        if (manager.cacheExists(name))
            manager.getCache(name);
        else {
            createCache(name, configuration);
        }
        return manager.getCache(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePersistentCache(String name) {
        removeCache(name);
    }

    private void removeCache(String name) {
        DistributedExecutorService des = new DefaultExecutorService(manager.getCache());
        List<Future<Void>> list = des.submitEverywhere(new StopCacheCallable(name));
        for (Future<Void> future : list) {
            try {
                future.get(); // wait for task to complete
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(Object listener, Cache cache) {
        DistributedExecutorService des = new DefaultExecutorService(cache);
        List<Future<Void>> list = new LinkedList<Future<Void>>();
        for (Address a : getMembers()) {

            try {
                des.submitEverywhere(new AddListenerCallable(cache.getName(),listener));
//                list.add(des.submit(a, new AddListenerCallable(cache.getName(), listener)));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }


        for (Future<Void> future : list) {
            try {
                future.get(); // wait for task to complete
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(Object listener, String name) {
        Cache c = (Cache) this.getPersisentCache(name);
        addListener(listener, c);
    }

    //  /** {@inheritDoc} */
    //  @Override
    //  public void addListener(Object listener, String name, KeyFilter filter) {
    //    Cache c = (Cache) this.getPersisentCache(name);
    //    c.addListener(listener, filter);
    //  }
    //
    //  /** {@inheritDoc} */
    //  @Override
    //  public void addListener(Object listener, String name, KeyValueFilter filter, Converter converter) {
    //    Cache c = (Cache) this.getPersisentCache(name);
    //    c.addListener(listener, filter, converter);
    //
    //  }
    //
    //  /** {@inheritDoc} */
    //  @Override
    //  public void addListener(Object listener, Cache cache, KeyFilter filter) {
    //    cache.addListener(listener, filter);
    //  }
    //
    //  /** {@inheritDoc} */
    //  @Override
    //  public void addListener(Object listener, Cache cache, KeyValueFilter filter, Converter converter) {
    ////        cache.addListener(listener,filter,converter);
    //  }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(Object listener, Cache cache) {
        DistributedExecutorService des = new DefaultExecutorService(cache);
        List<Future<Void>> list = new LinkedList<Future<Void>>();
        for (Address a : getMembers()) {
            //            des.submitEverywhere(new AddListenerCallable(cache.getName(),listener));
            try {
                list.add(des.submit(a, new RemoveListenerCallable(cache.getName(), listener)));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }


        for (Future<Void> future : list) {
            try {
                future.get(); // wait for task to complete
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(Object listener, String cacheName) {
        Cache cache = (Cache) getPersisentCache(cacheName);
        removeListener(listener, cache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Address> getMembers() {
        return manager.getCache("clustered").getAdvancedCache().getRpcManager().getMembers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Address getMemberName() {
        return manager.getAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return manager.getStatus().equals(ComponentStatus.RUNNING);
    }

    private void createCache(String cacheName, Configuration cacheConfiguration) {
        if (!cacheConfiguration.clustering().cacheMode().isClustered()) {
            log.error("Configuration given for " + cacheName
                          + " is not clustered so using default cluster configuration");
            //            cacheConfiguration = new ConfigurationBuilder().clustering().cacheMode(CacheMode.DIST_ASYNC).async().l1().lifespan(100000L).hash().numOwners(3).build();
        }
        DistributedExecutorService des = new DefaultExecutorService(manager.getCache());
        List<Future<Void>> list = des.submitEverywhere(new StartCacheCallable(cacheName));

        System.out.println("list " + list.size());
        for (Future<Void> future : list) {
            try {
                future.get(); // wait for task to complete
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
    }
}
