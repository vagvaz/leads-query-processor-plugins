package eu.leads.crawler.concurrent;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task Queue implementation
 */
public class TaskQueueImpl implements TaskQueue {

    // Maximum loops count while searching for suitable task
    private final static int MAX_LOOPS = 1;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Queue queue = new DefaultQueue();
    private List<TaskQueueWorker> workers = new ArrayList<TaskQueueWorker>();
    private boolean started;
    private AtomicInteger processingTasksCount = new AtomicInteger(0);
    private int maxParallelProcessingSequences;
    private Map<String, Integer> processingSequences = new ConcurrentHashMap<String, Integer>();

    /**
     * Sets maximum number of parallel thread processing tasks with the same sequence name. Default
     * value - 0 (unlimited). Limit is ignored for tasks with sequence name equal to {@code null}.
     *
     * @param maxParallelProcessingSequences
     */
    public void setMaxParallelProcessingSequences(int maxParallelProcessingSequences) {
        this.maxParallelProcessingSequences = maxParallelProcessingSequences;
    }

    /**
     * Sets inner queue ({@link DefaultQueue} is used by default.)
     *
     * @param queue
     */
    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    /**
     * {@inheritDoc}
     */
    public void addWorker(TaskQueueWorker worker) throws TaskQueueException {
        if (!isStarted()) {
            worker.setTaskQueue(this);
            workers.add(worker);
        } else {
            String message = "Error while adding task queue worker. Task queue is already started.";
            log.error(message);
            throw new TaskQueueException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void start() throws TaskQueueException {
        if (!isStarted()) {
            started = true;
            for (TaskQueueWorker worker : workers) {
                worker.start();
            }
        } else {
            log.error("TaskQueue is already started");
            throw new TaskQueueException("TaskQueue is already started");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() throws TaskQueueException {
        started = false;
        for (TaskQueueWorker worker : workers) {
            worker.stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() throws TaskQueueException {
        stop();
        queue.dispose();
    }

    /**
     * {@inheritDoc}
     */
    public void join() throws TaskQueueException {
        for (TaskQueueWorker worker : workers) {
            worker.join();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void join(long timeout) throws TaskQueueException {
        long workerTimeout = timeout / workers.size();

        for (TaskQueueWorker worker : workers) {
            worker.join(workerTimeout);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return queue.size() + processingTasksCount.get();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * {@inheritDoc}
     */
    public void enqueue(Task task) throws TaskQueueException {
        try {
            queue.add(task);
        } catch (Exception ex) {
            String message = "Cannot enqueue specified task";
            log.error(message);
            throw new TaskQueueException(message, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void defer(Task task) {
        queue.defer(task);
    }

    /**
     * Method is called by a worker after task is processed
     *
     * @param task
     */
    public void taskProcessed(Task task) {
        processingTasksCount.decrementAndGet();
        stopProcessingTask(task);
    }

    /**
     * {@inheritDoc}
     */
    public Task dequeue() {

        // Looping through the queue until suitable task is found
        for (int i = 0; i < MAX_LOOPS; i++) {
            Task task = (Task) queue.poll();
            if (task != null) {
                if (startProcessingTask(task)) {
                    // Task is ok, returning it
                    processingTasksCount.incrementAndGet();
                    return task;
                } else {
                    // Max parallel processing sequences limit was hit, adding task to the end of queue
                    queue.add(task);
                }
            } else if (task == null) {
                return task;
            }
        }

        // Nothing found, returning null
        return null;
    }

    protected boolean startProcessingTask(Task task) {
        return true;
    }

    /**
     * Method is called when task has been processed. Decrements processing sequences counter.
     *
     * @param task
     */
    protected void stopProcessingTask(Task task) {
        if (task.getSequenceName() == null) {
            return;
        }
        Integer count = processingSequences.get(task.getSequenceName());
        processingSequences.put(task.getSequenceName(), count == null ? 0 : count - 1);
    }
}
