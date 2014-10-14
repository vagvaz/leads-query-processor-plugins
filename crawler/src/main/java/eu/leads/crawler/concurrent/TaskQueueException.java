package eu.leads.crawler.concurrent;


/**
 * Task queue exception
 */
public class TaskQueueException extends Exception {

    private Task task;

    /**
     * Constructs a new TaskQueueException.
     */
    public TaskQueueException() {
    }

    public TaskQueueException(String message) {
        super(message);
    }

    public TaskQueueException(String message, Throwable t) {
        super(message, t);
    }

    public TaskQueueException(String message, Task task, Throwable t) {
        super(message, t);
        this.task = task;
    }

    /**
     * Getter for property 'task'.
     *
     * @return Value for property 'task'.
     */
    public Task getTask() {
        return task;
    }

    /**
     * Setter for property 'task'.
     *
     * @param task Value to set for property 'task'.
     */
    public void setTask(Task task) {
        this.task = task;
    }
}
