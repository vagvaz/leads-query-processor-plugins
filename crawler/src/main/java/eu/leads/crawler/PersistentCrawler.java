package eu.leads.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.leads.crawler.model.CrawlerTask;
import eu.leads.crawler.model.Page;
import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.conf.LQPConfiguration;
import net.htmlparser.jericho.Source;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ConcurrentMap;


/**
 * @author P.Sutra
 */
public class PersistentCrawler extends DefaultCrawler {

  private static ConcurrentMap preprocessingMap;

  private static ObjectMapper mapper;

  private static Log log = LogFactory.getLog(PersistentCrawler.class.getName());

  /** Constructs a new PersistentCrawler. */
  public PersistentCrawler() {

    preprocessingMap = InfinispanClusterSingleton.getInstance().getManager().getPersisentCache(LQPConfiguration.getConf().getString(StringConstants.CRAWLER_DEFAULT_CACHE));
    mapper = new com.fasterxml.jackson.databind.ObjectMapper();
  }

  /** {@inheritDoc} */
  @Override
  protected void afterCrawl(CrawlerTask crawlerTask, Page page) {
    super.afterCrawl(crawlerTask, page);

    if ( page == null
                 || page.getResponseCode() != HttpURLConnection.HTTP_OK
                 || page.getContent().isEmpty() ) {  // this pages violated the crawler constraints (size, etc..).
      return;
    }

    log.info("Crawled: " + page.getUrl().toString());

    try {
      Source src = new Source(page.getContent());
      String body = src.getTextExtractor().setIncludeAttributes(false).toString();
      Page page2 = new Page(page.getUrl(), page.getHeaders(), page.getResponseCode(), page.getCharset(), page.getResponseTime(), body.getBytes());
      preprocessingMap.putIfAbsent(page.getUrl().toString(), mapper.writeValueAsString(page2));
    } catch ( IOException e ) {
      e.printStackTrace();  // TODO: Customise this generated block
    }

  }

  /** {@inheritDoc} */
  @Override
  public boolean shouldCrawl(CrawlerTask task, CrawlerTask parent) {
    if ( preprocessingMap.containsKey(task.getUrl().toString()) ) {
      log.debug("Page already crawled: " + task.getUrl().toString() + " ; thrashing.");
      return false;
    }
    return super.shouldCrawl(task, parent);
    // return task.getDomain() != null && parent.getDomain() != null;
  }

}
