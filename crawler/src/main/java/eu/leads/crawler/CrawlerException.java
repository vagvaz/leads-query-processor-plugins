package eu.leads.crawler;

/**
 * @author ameshkov
 */
public class CrawlerException extends Exception {

    /**
     * Constructs a new CrawlerException.
     */
    public CrawlerException() {
        super();
    }

    public CrawlerException(String message) {
        super(message);
    }

    public CrawlerException(Throwable cause) {
        super(cause);
    }

    public CrawlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
