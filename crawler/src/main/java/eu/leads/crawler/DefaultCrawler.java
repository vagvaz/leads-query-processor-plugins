package eu.leads.crawler;

import eu.leads.crawler.download.Downloader;
import eu.leads.crawler.download.DownloaderController;
import eu.leads.crawler.model.CrawlerTask;
import eu.leads.crawler.model.Page;
import eu.leads.crawler.parse.Parser;
import eu.leads.crawler.parse.ParserController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Default crawler implementation. It is highly recommended to inherit your own crawlers from the
 * {@code DefaultCrawler}.
 *
 * @author ameshkov
 */
public class DefaultCrawler implements Crawler {

  private Log log = LogFactory.getLog(this.getClass());
  private DownloaderController downloaderController;
  private ParserController parserController;

  /**
   * Returns {@link DownloaderController} of this crawler
   */
  protected DownloaderController getDownloaderController() {
    return downloaderController;
  }

  /**
   * Sets {@link DownloaderController} for this crawler
   *
   * @param downloaderController
   */
  public void setDownloaderController(DownloaderController downloaderController) {
    this.downloaderController = downloaderController;
  }

  /**
   * Returns {@link ParserController} of this crawler
   *
   * @return
   */
  protected ParserController getParserController() {
    return parserController;
  }

  /**
   * Sets {@link ParserController} for this crawler
   *
   * @param parserController
   */
  public void setParserController(ParserController parserController) {
    this.parserController = parserController;
  }

  /**
   * Returns this crawler logger
   *
   * @return
   */
  protected Log getLogger() {
    return log;
  }

  /**
   * Crawls url from the specified {@link eu.leads.crawler.model.CrawlerTask}.
   *
   * @param crawlerTask
   *
   * @return
   *
   * @throws Exception
   */
  public Page crawl(CrawlerTask crawlerTask) throws Exception {
    log.debug("Starting processing " + crawlerTask.getUrl() + "...");

    beforeCrawl(crawlerTask);

    URL url = new URL(crawlerTask.getUrl());
    log.debug("Getting downloader for " + url + "...");
    Downloader downloader = downloaderController.getDownloader(url);
    log.debug("Downloading from " + url + "...");

    Page page = null;

    try {
      page = downloader.download(url);

      if ( page != null ) {
        crawlerTask.setTimeDownloaded(new Date());

        if ( page.getResponseCode() == HttpURLConnection.HTTP_OK ) {
          log.debug(url + " was downloaded successfully. Download time " + page.getResponseTime());
          Parser parser = parserController.getParser(page);
          log.debug("Parsing " + url);
          parser.parse(page);
          crawlerTask.setTimeParsed(new Date());
          log.debug(url + " has been parsed. " + page.getLinks().size() + " links were found");
        }
      }
    } finally {
      afterCrawl(crawlerTask, page);
    }

    return page;
  }

  /**
   * Method is called before processing specified task
   *
   * @param crawlerTask
   */
  protected void beforeCrawl(CrawlerTask crawlerTask) {
    // Override this
  }

  /**
   * Method is called after processing specified task
   *
   * @param crawlerTask
   */
  protected void afterCrawl(CrawlerTask crawlerTask, Page page) {
    // Override this
  }

  /**
   * Default implementation returns {@code true} if domains for {@code crawlerTask} and {@code
   * parent} are equal.
   *
   * @param crawlerTask
   * @param parent
   *
   * @return
   */
  public boolean shouldCrawl(CrawlerTask crawlerTask, CrawlerTask parent) {
    return crawlerTask.getDomain() != null && parent.getDomain() != null && crawlerTask.getDomain().equals(parent.getDomain());
  }
}
