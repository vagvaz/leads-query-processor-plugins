package eu.leads.crawler;

import eu.leads.crawler.concurrent.*;
import eu.leads.crawler.frontier.*;
import eu.leads.crawler.model.CrawlerTask;
import eu.leads.crawler.model.Page;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages crawler workers. {@link CrawlerConfiguration} should be passed to constructor. Starts
 * crawler threads (one thread for each crawler in {@link CrawlerConfiguration}). Also makes
 * crawlers satisfy constraints set in {@link CrawlerConfiguration} object ({@code
 * maxParallelRequests}, {@code maxHttpErrors}, etc.).
 *
 * @author ameshkov
 */
public class CrawlerController {

    private final Object syncRoot = new Object();
    private final Object workerSyncRoot = new Object();
    private StatisticsService statisticsService = new DefaultStatisticsService();
    private Log log = LogFactory.getLog(this.getClass());
    private CrawlerConfiguration crawlerConfiguration;
    private Scheduler scheduler;
    private List<URL> seeds = new ArrayList<URL>();
    private TaskQueue taskQueue = new TaskQueueImpl();
    private Queue queue;
    private boolean started = false;
    private boolean initialized = false;

    public CrawlerController(CrawlerConfiguration crawlerConfiguration) {
        this.crawlerConfiguration = crawlerConfiguration;
    }

    /**
     * Starts crawling
     */
    public void start() throws CrawlerException {
        synchronized (syncRoot) {
            try {
                log.info("Starting crawling..");

                if (seeds.isEmpty() && queue == null) {
                    throw new CrawlerException("There's no crawler seeds");
                } else if (seeds.isEmpty()) {
                    log.warn("Crawler seeds are not set, but task queue was overriden");
                }

                if (!initialized) {
                    init();
                    initialized = true;

                    for (URL url : seeds) {
                        CrawlerTask task = new CrawlerTask(url.toString(), 0);
                        taskQueue.enqueue(task);
                    }

                    // Removing seeds
                    seeds = null;
                }

                if (!started) {
                    taskQueue.start();
                    started = true;
                }
            } catch (Exception ex) {
                log.error("Error starting crawler controller", ex);
                throw new CrawlerException("Error starting crawler controller", ex);
            }
        }
    }

    /**
     * Initializes controller
     */
    private void init() throws CrawlerException {
        log.info("Initializing crawler controller");

        try {
            log.info("Initializing task queue");
            if (queue != null) {
                log.info("Queue is overriden, setting it instead of default inner task queue");
                ((TaskQueueImpl) taskQueue).setQueue(queue);
                ((TaskQueueImpl) taskQueue).setMaxParallelProcessingSequences(crawlerConfiguration
                                                                                  .getMaxParallelRequests());
            }

            log.info("Initializing scheduler");
            scheduler = new DefaultScheduler(taskQueue, statisticsService);

            log.info("Initializing crawler workers");
            for (Crawler crawler : crawlerConfiguration.getCrawlers()) {
                CrawlerWorker worker = new CrawlerWorker(crawler);
                taskQueue.addWorker(worker);
            }
            log.info("Task queue initialized");
        } catch (Exception ex) {
            log.error("Error initializing crawler controller", ex);
            throw new CrawlerException("Error initializing crawler controller", ex);
        }

        log.info("Crawler controller initialized");
    }

    /**
     * Stops crawling
     */
    public void stop() throws CrawlerException {
        synchronized (syncRoot) {
            try {
                log.info("Stopping crawling");

                if (started) {
                    taskQueue.stop();
                    started = false;
                }
            } catch (Exception ex) {
                log.error("Error stopping crawler controller", ex);
                throw new CrawlerException("Error starting crawler controller", ex);
            }
        }
    }

    /**
     * Disposes crawler controller
     *
     * @throws CrawlerException
     */
    public void dispose() throws CrawlerException {
        synchronized (syncRoot) {
            try {
                log.info("Disposing crawler controller");
                taskQueue.dispose();
                statisticsService.dispose();
                log.info("Crawler controller has been disposed");
            } catch (Exception ex) {
                log.error("Error disposing crawler controller", ex);
                throw new CrawlerException("Error disposing crawler controller");
            }
        }
    }

    /**
     * Joins crawler workers threads and waits for them to finish their work
     */
    public void join() throws CrawlerException {
        try {
            taskQueue.join();
        } catch (Exception ex) {
            throw new CrawlerException("Error joining crawler workers threads", ex);
        }
    }

    /**
     * Joins crawler workers threads and waits for them to finish their work (or for timeout to
     * exceed)
     *
     * @param timeout
     *
     * @throws CrawlerException
     */
    public void join(long timeout) throws CrawlerException {
        try {
            taskQueue.join(timeout);
        } catch (Exception ex) {
            throw new CrawlerException("Error joining crawler workers threads", ex);
        }
    }

    /**
     * Sets {@link Queue} implementation.is used by default
     *
     * @param queue
     */
    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    /**
     * Gets domain statistics
     *
     * @param domainName
     */
    public DomainStatistics getDomainStatistics(String domainName) {
        return statisticsService.getDomainStatistics(domainName);
    }

    /**
     * Adds crawler seed
     *
     * @param url
     */
    public void addSeed(URL url) {
        seeds.add(url);
    }

    /**
     * Checks maximum http errors limit
     *
     * @param statistics
     *
     * @return
     */
    private boolean checkMaxHttpErrors(DomainStatistics statistics) {
        synchronized (workerSyncRoot) {
            for (Integer code : crawlerConfiguration.getMaxHttpErrors().keySet()) {
                int limit = crawlerConfiguration.getMaxHttpErrors().get(code);
                long errorsCount = statistics.getHttpErrors(code);

                if (errorsCount >= limit) {
                    log.debug("Errors limit for domain " + statistics.getDomainName()
                                  + " is exceeded, omitting task");
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Checks politeness period
     *
     * @param statistics
     *
     * @return
     */
    private boolean checkPolitenessPeriod(DomainStatistics statistics) {
        synchronized (workerSyncRoot) {
            // Searching for specific constraints
            DomainConstraints domainConstraints =
                crawlerConfiguration.getDomainConstraints(statistics.getDomainName());
            int politenessPeriod = domainConstraints == null ?
                                       crawlerConfiguration.getPolitenessPeriod() :
                                       domainConstraints.getPolitenessPeriod();

            // Checking politeness period
            if (politenessPeriod > 0
                    && statistics.getLastTimeDownloaded() > 0
                    && (System.currentTimeMillis() - statistics.getLastTimeDownloaded())
                           < crawlerConfiguration.getPolitenessPeriod()) {
                return false;
            }

            return true;
        }
    }

    /**
     * Schedules links for crawl. Returns count of scheduled.
     *
     * @param crawler
     * @param parentTask
     * @param links
     *
     * @return
     */
    private void scheduleTasks(Crawler crawler, CrawlerTask parentTask, List<URL> links) {
        for (URL url : links) {
            CrawlerTask task = new CrawlerTask(url.toString(), parentTask.getLevel() + 1);
            scheduleTask(crawler, task, parentTask);
        }
    }

    /**
     * Schedulles a crawler task
     *
     * @param crawler
     * @param task
     * @param parentTask
     *
     * @return
     */
    private void scheduleTask(Crawler crawler, CrawlerTask task, CrawlerTask parentTask) {
        synchronized (workerSyncRoot) {
            if (crawler.shouldCrawl(task, parentTask)) {
                scheduler.schedule(task);
            } else {
                log.debug("Igoring task " + task.getUrl()
                              + " - crawler returned false from shouldCrawl method");
            }
        }
    }

    /**
     * Enqueues task into the queue, deferring its execution
     *
     * @param task
     */
    private void deferCrawlerTask(CrawlerTask task) {
        try {
            log.debug("Deferring task " + task.getUrl());
            taskQueue.enqueue(task);
        } catch (Exception ex) {
            log.error("Error enqueuing task " + task.getUrl(), ex);
        }
    }

    /**
     * Worker used in the inner {@link TaskQueue}
     */
    private class CrawlerWorker extends BaseTaskQueueWorker {

        private Crawler crawler;

        /**
         * Creates an instance of the crawler worker
         *
         * @param crawler
         */
        public CrawlerWorker(Crawler crawler) {
            this.crawler = crawler;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void doWork(Task task) throws Exception {
            if (!(task instanceof CrawlerTask)) {
                log.warn("Task is of wrong class, omitting it");
                return;
            }

            CrawlerTask crawlerTask = (CrawlerTask) task;
            log.debug("Processing task " + crawlerTask.getUrl());

            // Getting domain statistics
            DomainStatistics statistics =
                statisticsService.getDomainStatistics(crawlerTask.getDomain());

            if (!checkMaxHttpErrors(statistics)) {
                log.warn(crawlerTask.getDomain() + " has exceeded http errors limit");
                return;
            }

            if (!checkPolitenessPeriod(statistics)) {
                log.debug("Waiting for politeness period for domain " + statistics.getDomainName());
                deferCrawlerTask(crawlerTask);
                return;
            }

            try {
                Page page = crawler.crawl(crawlerTask);
                processPage(page, crawlerTask);
            } finally {
                log.debug("Stopping processing task " + crawlerTask.getUrl());
            }
        }

        /**
         * Processes specified page
         *
         * @param page
         * @param crawlerTask
         */
        private void processPage(Page page, CrawlerTask crawlerTask) {
            if (page == null) {
                log.debug("Page " + crawlerTask.getUrl() + " was not downloaded");
                return;
            }

            // Page was downloaded, tracking statistics
            statisticsService.afterDownloading(crawlerTask, page);

            if (page.getResponseCode() == HttpURLConnection.HTTP_OK) {
                log.debug(crawlerTask.getUrl()
                              + " has been successfully crawled, schedulling tasks");

                // Page was successfully parsed, tracking statistics
                statisticsService.afterParsing(crawlerTask, page);

                log.debug("Scheduling new tasks");

                // Getting specific domain constraints
                DomainConstraints domainConstraints =
                    crawlerConfiguration.getDomainConstraints(crawlerTask.getDomain());
                int maxLevel = domainConstraints == null ?
                                   crawlerConfiguration.getMaxLevel() :
                                   domainConstraints.getMaxLevel();

                // Checking crawling depth
                if (maxLevel > 0 && crawlerTask.getLevel() == maxLevel) {
                    log.debug(crawlerTask.getUrl() + " has exceeded maximum depth limit");
                    return;
                }

                // Schedulling new tasks
                if (page.getLinks() != null) {
                    scheduleTasks(crawler, crawlerTask, page.getLinks());
                }
                log.debug(page.getLinks() == null ?
                              0 :
                              page.getLinks().size() + " tasks were passed to the scheduler");
            } else if (page.getResponseCode() >= 300 && page.getResponseCode() < 400) {
                log.debug("Processing redirect from " + crawlerTask.getUrl() + " to "
                              + page.getRedirectUrl());
                CrawlerTask task =
                    new CrawlerTask(page.getRedirectUrl().toString(), crawlerTask.getLevel());
                // Passing custom data further
                task.setCustomData(crawlerTask.getCustomData());
                scheduleTask(crawler, task, crawlerTask);
            } else {
                log.debug(crawlerTask.getUrl() + " was processed with errors, response code: "
                              + page.getResponseCode());
            }
        }
    }
}
