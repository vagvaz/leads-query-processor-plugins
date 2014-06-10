package eu.leads.crawler.frontier;

import eu.leads.crawler.model.CrawlerTask;

/**
 * Schedules page crawl
 */
public interface Scheduler {

  /**
   * Schedulles crawling of page {@code url}
   *
   * @param url
   */
  void schedule(CrawlerTask crawlerTask);
}
