package eu.leads.processor.plugins.sentiment;

import eu.leads.processor.common.Tuple;
import eu.leads.processor.common.infinispan.InfinispanManager;
import eu.leads.processor.plugins.PluginInterface;
import org.apache.commons.configuration.Configuration;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by vagvaz on 6/6/14.
 */
public class SentimentAnalysisPlugin implements PluginInterface {
  private String id;
  private Logger log;
  private Cache targetCache;
  private SentimentAnalysisModule module;
  private int threshold;
  private boolean debug = false;

  @Override
  public String getId() {
    return SentimentAnalysisPlugin.class.getCanonicalName();
  }

  @Override
  public void setId(String s) {
    this.id = s;
  }

  @Override
  public String getClassName() {
    return SentimentAnalysisPlugin.class.getCanonicalName();
  }


  @Override
  public void initialize(Configuration configuration, InfinispanManager manager) {
    log = LoggerFactory.getLogger(SentimentAnalysis.class);
    threshold = configuration.getInt("threshold");
    targetCache = (Cache) manager.getPersisentCache(configuration.getString("cache"));
    module = new SentimentAnalysisModule("classifiers/english.all.3class.distsim.crf.ser.gz");
    debug = configuration.getBoolean("debug");
  }

  @Override
  public void cleanup() {

  }

  @Override
  public void modified(Object key, Object value, Cache<Object, Object> objectObjectCache) {
    processTuple(key.toString(), new Tuple(value.toString()));
  }

  private void processTuple(String key, Tuple value) {

    String url = key;
    String body = value.getAttribute("content");
    Set<Entity> entities = module.getEntities(body);
    for ( Entity e : entities ) {
      Tuple tuple = new Tuple("{}"); //create a tuple with no attributes
      Sentiment s = module.getSentimentForEntity(e.getName(), body);
      tuple.setAttribute("name", e.getName());
      tuple.setAttribute("sentimentScore", Double.toString(s.getValue()));
      tuple.setAttribute("webpageURL", url);
      this.targetCache.put(url + ":" + e.getName(), tuple.asString());
      if ( debug )
        log.debug(url + ":" + e.getName() + "\n" + tuple.asString());
    }
  }

  @Override
  public void created(Object key, Object value, Cache<Object, Object> objectObjectCache) {
    processTuple(key.toString(), new Tuple(value.toString()));
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
