package eu.leads.crawler;

import java.io.Serializable;

/**
 * @author P. Sutra
 */
public class CrawlResult implements Serializable, Comparable<CrawlResult> {

    public double pagerank;
    public double sentiment;

    public CrawlResult(double p, double s) {
        pagerank = p;
        sentiment = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(CrawlResult crawlResult) {
        if (pagerank > crawlResult.pagerank) {
            return 1;
        } else if (pagerank == crawlResult.pagerank) {
            if (sentiment > crawlResult.sentiment) {
                return 1;
            } else if (sentiment == crawlResult.sentiment) {
                return 0;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + pagerank + "," + sentiment + ")";
    }

}

