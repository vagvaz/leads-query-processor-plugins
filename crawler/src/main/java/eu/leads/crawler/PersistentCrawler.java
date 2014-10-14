package eu.leads.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.leads.crawler.model.CrawlerTask;
import eu.leads.crawler.model.Page;
import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.conf.LQPConfiguration;
//import eu.leads.processor.sentiment.Sentiment;
//import eu.leads.processor.sentiment.SentimentAnalysisModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.vertx.java.core.json.JsonObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

/**
 * @author P.Sutra
 */
public class PersistentCrawler extends DefaultCrawler {

    private static ConcurrentMap preprocessingMap;
    private static String prefix;
    private static ObjectMapper mapper;
    private static Double minusOne = new Double(-1.9);
    private static Double zero = new Double(0.0);
    private static String englishLang = "en";
    private static Log log = LogFactory.getLog(PersistentCrawler.class.getName());

//  private static JavaLanguageDetection det = JavaLanguageDetection.getInstance();
//   private SentimentAnalysisModule sentimentAnalysisModule;
    /**
     * Constructs a new PersistentCrawler.
     */
    public PersistentCrawler() {

        preprocessingMap = InfinispanClusterSingleton.getInstance().getManager()
                               .getPersisentCache(LQPConfiguration.getConf()
                                                      .getString(StringConstants.CRAWLER_DEFAULT_CACHE));
      prefix = LQPConfiguration.getConf()
                 .getString(StringConstants.CRAWLER_DEFAULT_CACHE);
        mapper = new com.fasterxml.jackson.databind.ObjectMapper();
//      sentimentAnalysisModule = new SentimentAnalysisModule("classifiers/english.all.3class.distsim.crf.ser.gz");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterCrawl(CrawlerTask crawlerTask, Page page) {
        super.afterCrawl(crawlerTask, page);

        if (page == null
                || page.getResponseCode() != HttpURLConnection.HTTP_OK
                || page.getContent()
                       .isEmpty()) {  // this pages violated the crawler constraints (size, etc..).
            return;
        }

        log.info("Crawled: " + page.getUrl().toString());

        try {

            String htmlString = new String(page.getContent());
            Document doc = Jsoup.parse(htmlString);
            String body = doc.body().text();
            Page page2 = new Page(page.getUrl(), page.getHeaders(), page.getResponseCode(),
                                     page.getCharset(), page.getResponseTime(), body.getBytes());
            page2.setLinks(page.getLinks());
            page2.setTitle(page.getTitle());
            JsonObject object = new JsonObject(mapper.writeValueAsString(page2));
            additionalAttributes(object);
            preprocessingMap
                .putIfAbsent(prefix+ ":" + page2.getUrl()
                                                                             .toString(),
                              object.toString());
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }

    }

  private void additionalAttributes(JsonObject object) {

    object.putNumber("pagerank", minusOne);
    object.putNumber("sentiment",zero);
    object.putString("language",englishLang);
    String dateAsString = object.getObject("headers").getString("date");
     SimpleDateFormat f = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
     Date date = null;
     try {
        date = f.parse(dateAsString);
     } catch (ParseException e) {
        e.printStackTrace();
     }
     if(date == null){
        date = new Date(System.currentTimeMillis());
     }
     object.putNumber("timestamp",date.getTime());
//    Sentiment sentiment = sentimentAnalysisModule.getOverallSentiment(object.getString("content"));
//    object.putValue("sentiment",sentiment.getValue());
//    String language = det.detectLanguage(object.getString("content"));
//    object.putString("language",language);
  }

  /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldCrawl(CrawlerTask task, CrawlerTask parent) {
        if (preprocessingMap.containsKey(task.getUrl().toString())) {
            log.debug("Page already crawled: " + task.getUrl().toString() + " ; thrashing.");
            return false;
        }
        return super.shouldCrawl(task, parent);
        // return task.getDomain() != null && parent.getDomain() != null;
    }

}
