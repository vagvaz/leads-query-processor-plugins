package eu.leads.crawler;

import eu.leads.crawler.model.Page;
import eu.leads.crawler.utils.Web;
import eu.leads.processor.common.StringConstants;
import eu.leads.processor.common.infinispan.InfinispanClusterSingleton;
import eu.leads.processor.conf.LQPConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * @author P. Sutra
 */
@Listener
public class PersistentListener {

    private static ConcurrentMap preprocessingMap =
        InfinispanClusterSingleton.getInstance().getManager()
            .getPersisentCache(LQPConfiguration.getConf()
                                   .getString(StringConstants.CRAWLER_DEFAULT_CACHE));
    private static ConcurrentMap postprocessingMap =
        InfinispanClusterSingleton.getInstance().getManager()
            .getPersisentCache("postprocessingMap");
    private static Log log = LogFactory.getLog(PersistentListener.class.getName());

    private List<String> words;
    private int ndays;


    public PersistentListener(List<String> l, int d) throws IOException {

        words = l;
        ndays = d;
        InfinispanClusterSingleton.getInstance().getManager().addListener(this, LQPConfiguration
                                                                                    .getConf()
                                                                                    .getString(StringConstants.CRAWLER_DEFAULT_CACHE));
    }

    @CacheEntryModified
    public void postProcessPage(CacheEntryModifiedEvent<Object, Object> event) {

        if (event.isPre())
            return;

        try {

            URL url = new URL((String) event.getKey());
            Page page = (Page) event.getValue();

            if (!isMatching(page))
                return;

            // Page rank
            Double pagerank = (double) Web.pagerank("http://" + url.toURI().getHost());

            // Sentiment analysis
            //             SentimentCall call = new SentimentCall(new CallTypeUrl(url.toString()));
            //             Response response = client.call(call);
            //             SentimentAlchemyEntity entity = (SentimentAlchemyEntity) response.iterator().next();
            //             Double sentiment = Double.valueOf(entity.getScore().toString());

            //             CrawlResult result = new CrawlResult(pagerank,sentiment);
            //             log.info("Processed " + url + " " + result);

            // Insert the result into the output set
            //             postprocessingMap.putIfAbsent(url.toString(), result);

        } catch (Exception e) {
            log.debug("An error while parsing a page.");
        }

    }

    public boolean isMatching(Page page) {

        // check for key words.
        String content = page.getContent().toLowerCase();
        for (String w : words) {
            if (!content.contains(w.toLowerCase()))
                return false;
        }

        // check for the appropriate date.
        try {
            String header = page.getHeaders().get("last-modified");
            if (header == null)
                return false;
            Date now = new Date();
            Date publication = org.apache.http.impl.cookie.DateUtils.parseDate(header);
            Calendar cal = Calendar.getInstance();
            cal.setTime(publication);
            cal.add(Calendar.DATE, ndays);
            publication = cal.getTime();
            log.debug("Valid format for " + page.getHeaders());
            if (publication.after(now)) {
                return true;
            }
        } catch (Exception e) {
            log.debug("Invalid format for " + page.getHeaders());
        }

        return false;

    }

}
