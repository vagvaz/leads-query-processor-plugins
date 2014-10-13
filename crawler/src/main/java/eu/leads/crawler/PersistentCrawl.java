package eu.leads.crawler;

import eu.leads.crawler.concurrent.DefaultQueue;
import eu.leads.crawler.concurrent.Queue;
import eu.leads.crawler.download.DefaultDownloader;
import eu.leads.crawler.download.DefaultDownloaderController;
import eu.leads.crawler.download.DefaultProxyController;
import eu.leads.crawler.parse.DefaultParser;
import eu.leads.crawler.parse.DefaultParserController;
import eu.leads.processor.conf.LQPConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Pierre Sutra
 */
public class PersistentCrawl {

    static List<Proxy> proxies = new ArrayList<Proxy>();
    static String seed = "http://www.economist.com/";
    static ArrayList<String> words = new ArrayList<String>();
    static int depth = 0;
    static int ncrawlers = 1;
    static int ndays = 365;
    static CrawlerController crawlerController = null;
    private static Log log = LogFactory.getLog(PersistentCrawl.class.getName());

    public static void main(String[] args) {


        LQPConfiguration.getInstance().loadFile("config.properties");
        try {
            seed = LQPConfiguration.getConf().getString("crawler.seed");
        } catch (NoSuchElementException e) {
           seed = "http://news.yahoo.com";
        } finally {

        }
        log.info("Seed : " + seed);

        //        if(getProperties().containsKey("words")){
        //            for(String w : getProperties().get("words").toString().split(",")){
        //                log.info("Adding word :"+w);
        //                words.add(w);
        //            }
        //        }else{
        //            words.add("Obama");
        //        }
        try {
            depth = LQPConfiguration.getConf().getInt("crawler.depth");
        } catch (NoSuchElementException e) {
            depth = 1;
        }
        log.info("Depth " + depth);


        try {
            ncrawlers = LQPConfiguration.getConf().getInt("crawler.threads");
        } catch (NoSuchElementException e) {
            ncrawlers = 1;
        }
        log.info("Number of crawler threads " + ncrawlers);
        try {
            ndays = LQPConfiguration.getConf().getInt("crawler.days");
        } catch (NoSuchElementException e) {
            ndays = 10;
        }
        log.info("Document earler than " + ndays + " day(s)");


        proxies.add(Proxy.NO_PROXY);
        DefaultProxyController proxyController = new DefaultProxyController(proxies);

        DefaultDownloader downloader = new DefaultDownloader();
        downloader.setAllowedContentTypes(new String[] {"text/html", "text/plain"});
        downloader.setMaxContentLength(100000);
        downloader.setTriesCount(3);
        downloader.setProxyController(proxyController);

        DefaultDownloaderController downloaderController = new DefaultDownloaderController();
        downloaderController.setGenericDownloader(downloader);

        DefaultParserController defaultParserController = new DefaultParserController();
        defaultParserController.setGenericParser(DefaultParser.class);

        CrawlerConfiguration configuration = new CrawlerConfiguration();
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_BAD_GATEWAY, 10);
        configuration.setMaxParallelRequests(5);
        configuration.setPolitenessPeriod(100);
        configuration.setMaxLevel(depth);

        try {

            for (int i = 0; i < ncrawlers; i++) {
                PersistentCrawler crawler = new PersistentCrawler();
                crawler.setDownloaderController(downloaderController);
                crawler.setParserController(defaultParserController);
                configuration.addCrawler(crawler);
            }

            crawlerController = new CrawlerController(configuration);

            Queue q = new DefaultQueue();
            log.info(q.size());
            crawlerController.setQueue(q);
            if (!seed.equals("") && q.size() == 0)
                crawlerController.addSeed(new URL(seed));
            crawlerController.start();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void stop() {
        try {
            crawlerController.stop();
            crawlerController.join();
        } catch (CrawlerException e) {
            e.printStackTrace();
        }
    }
}
