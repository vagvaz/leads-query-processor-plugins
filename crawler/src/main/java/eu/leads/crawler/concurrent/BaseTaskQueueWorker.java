package eu.leads.crawler.concurrent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base task queue worker class. Better inherit from this class neither from the interface.
 */
public abstract class BaseTaskQueueWorker implements TaskQueueWorker {

    private final static int DEFAULT_IDLE_TIMEOUT = 100;
    private final static int DEFAULT_STOP_TIMEOUT = 10000;
    private final Object syncRoot = new Object();
    private Log log = LogFactory.getLog(this.getClass());
    private TaskQueue taskQueue;
    private Thread workerThread;
    private boolean started;

    /**
     * Constructs a new BaseTaskQueueWorker.
     */
    public BaseTaskQueueWorker() {
        workerThread = new Thread(new Runnable() {
            public void run() {
                doWorkLoop();
            }
        });

        workerThread.setDaemon(true);
    }

    /**
     * Main worker loop
     */
    private void doWorkLoop() {
        log.info(workerThread.getName() + " start do work loop");
        Task task = null;

        while (started) {
            try {
                task = getTaskQueue().dequeue();
                if (task != null) {
                    doWork(task);
                }
                // Switch context
                Thread.yield();
            } catch (Exception ex) {
                ex.printStackTrace();
                TaskQueueException e = new TaskQueueException("Exception in doWork", task, ex);
                doWorkHandleException(task, e);
            } finally {
                if (task != null) {
                    getTaskQueue().taskProcessed(task);
                } else {
                    try {
                        // Worker is idle (no tasks found), so waiting for IDLE timeout
                        Thread.sleep(DEFAULT_IDLE_TIMEOUT);
                    } catch (InterruptedException ex) {
                        // Ignoring
                    }
                }
            }
        }

        log.info(workerThread.getName() + " finished its work");
    }

    /**
     * @return the taskQueue
     */
    protected TaskQueue getTaskQueue() {
        return taskQueue;
    }

    /**
     * {@inheritDoc}
     */
    public void setTaskQueue(TaskQueue taskQueue) {
        log.info(workerThread.getName() + " set task queue");
        this.taskQueue = taskQueue;
    }

    /**
     * Implement this method in your workers
     *
     * @param task
     *
     * @throws Exception
     */
    public abstract void doWork(Task task) throws Exception;

    /**
     * Handles exception in do work. Doing nothing by default
     *
     * @param ex
     */
    protected void doWorkHandleException(Task task, Exception ex) {
        log.error("Error in doWork", ex);
    }

    /**
     * {@inheritDoc}
     */
    public void start() {
        synchronized (syncRoot) {
            if (!started) {
                started = true;
                log.info(workerThread.getName() + " is started");
                workerThread.start();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        synchronized (syncRoot) {
            try {
                started = false;
                log.info(workerThread.getName() + " is joined, waiting until it stops");
                workerThread.join(DEFAULT_STOP_TIMEOUT);
                if (workerThread.isAlive()) {
                    log.warn(workerThread.getName() + " has not stopped for " + DEFAULT_STOP_TIMEOUT
                                 + " ms. Interrupting thread.");
                    workerThread.interrupt();
                    workerThread.join();
                }
                log.info(workerThread.getName() + " is stopped");
            } catch (InterruptedException ex) {
                // Ignoring
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void join() {
        try {
            workerThread.join();
        } catch (InterruptedException ex) {
            // Ignoring
        }
    }

    /**
     * {@inheritDoc}
     */
    public void join(long timeout) {
        try {
            workerThread.join(timeout);
        } catch (InterruptedException ex) {
            // Ignoring
        }
    }

    /**
     * Returns logger
     *
     * @return
     */
    protected Log getLogger() {
        return log;
    }
}
