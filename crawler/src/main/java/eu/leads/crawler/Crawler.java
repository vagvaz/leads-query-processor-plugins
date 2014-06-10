package eu.leads.crawler;

import eu.leads.crawler.model.CrawlerTask;
import eu.leads.crawler.model.Page;

/**
 * Represents crawler
 *
 * @author ameshkov
 */
public interface Crawler {

  /**
   * Crawls url from the {@link eu.leads.crawler.model.CrawlerTask}. Returns parsed {@link
   * eu.leads.crawler.model.Page}.
   *
   * @param crawlerTask
   *
   * @return
   *
   * @throws Exception
   */
  Page crawl(CrawlerTask crawlerTask) throws Exception;

  /**
   * Checks if this crawler should process next task
   *
   * @param crawlerTask
   * @param parent
   *
   * @return
   */
  boolean shouldCrawl(CrawlerTask crawlerTask, CrawlerTask parent);
}
