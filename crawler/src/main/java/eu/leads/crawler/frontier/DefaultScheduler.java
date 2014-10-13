package eu.leads.crawler.frontier;

import eu.leads.crawler.concurrent.TaskQueue;
import eu.leads.crawler.model.CrawlerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Standard scheduler implementation. Starts an asyncronous worker thread that reads urls from the
 * queue and adds them to the {@code TaskQueue}.
 */
public class DefaultScheduler implements Scheduler {

    private final Object syncRoot = new Object();
    private final Queue<CrawlerTask> schedulerQueue = new LinkedList<CrawlerTask>();
    private final Thread workerThread;
    private Log log = LogFactory.getLog(this.getClass());
    private TaskQueue taskQueue;
    private StatisticsService statisticsService;

    public DefaultScheduler(TaskQueue taskQueue, StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
        this.taskQueue = taskQueue;

        workerThread = new Thread(new Runnable() {

            public void run() {
                doWorkLoop();
            }
        });
        workerThread.setDaemon(true);
        workerThread.start();
        log.info("Scheduler was successfully initialized and started");
    }

    /**
     * Reads urls from queue and adds them to the TaskQueue (if url was not crawled yet)
     */
    private void doWorkLoop() {
        while (true) {
            CrawlerTask task = null;

            try {
                synchronized (syncRoot) {
                    task = schedulerQueue.poll();
                }
                if (task != null) {
                    if (!statisticsService.isCrawled(task.getUrl())) {
                        taskQueue.enqueue(task);
                        statisticsService.afterScheduling(task);
                        log.debug("Scheduled crawling of the " + task.getUrl());
                    } else {
                        log.debug("Url " + task.getUrl() + " was already crawled");
                    }
                }

                // Yielding context to another thread
                Thread.sleep(1);
            } catch (Exception ex) {
                log.error("Error processing task " + task == null ?
                              "NOTASK" :
                              task.getUrl() + " from the scheduler queue", ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void schedule(CrawlerTask crawlerTask) {
        synchronized (syncRoot) {
            log.debug("Enqueueing task " + crawlerTask.getUrl() + " to the scheduler queue");
            schedulerQueue.add(crawlerTask);
        }
    }
}
