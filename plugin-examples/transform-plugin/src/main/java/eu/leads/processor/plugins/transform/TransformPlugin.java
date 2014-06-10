package eu.leads.processor.plugins.transform;

import eu.leads.processor.common.Tuple;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.plugins.PluginInterface;
import org.apache.commons.configuration.Configuration;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by vagvaz on 6/8/14.
 */
public class TransformPlugin implements PluginInterface {
  private String id;
  private Cache targetCache;
  private List<String> attributes;
  private Logger log = LoggerFactory.getLogger(TransformPlugin.class);

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String s) {
    this.id = s;
  }

  @Override
  public String getClassName() {
    return TransformPlugin.class.getCanonicalName();
  }

  @Override
  public void initialize(Configuration configuration, InfinispanManager infinispanManager) {
    String targetCacheName = configuration.getString("cache");
    if ( targetCacheName != null || !targetCacheName.equals("") ) {
      targetCache = (Cache) infinispanManager.getPersisentCache(targetCacheName);
    } else {
      log.error("TargetCache is not defined using default for not breaking");
      targetCache = (Cache) infinispanManager.getPersisentCache("default");
    }
    attributes = configuration.getList("attributes");
  }

  @Override
  public void cleanup() {

  }

  @Override
  public void modified(Object key, Object value, Cache<Object, Object> objectObjectCache) {
    Tuple t = new Tuple(value.toString());
    processTuple(key.toString(), t);
  }

  protected void processTuple(String key, Tuple tuple) {
    tuple.keepOnly(attributes);
    targetCache.put(key, tuple.asString());
  }

  @Override
  public void created(Object key, Object value, Cache<Object, Object> objectObjectCache) {
    Tuple t = new Tuple(value.toString());
    processTuple(key.toString(), t);
  }

  @Override
  public void removed(Object o, Object o2, Cache<Object, Object> objectObjectCache) {

  }

  @Override
  public Configuration getConfiguration() {
    return null;
  }

  @Override
  public void setConfiguration(Configuration configuration) {

  }
}
