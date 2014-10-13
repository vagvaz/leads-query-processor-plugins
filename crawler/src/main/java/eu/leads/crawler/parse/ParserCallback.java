package eu.leads.crawler.parse;

import eu.leads.crawler.model.Page;
import it.unimi.dsi.parser.callback.Callback;

/**
 * Represents page content parser
 *
 * @author ameshkov
 */
public interface ParserCallback extends Callback {

    /**
     * Receives notification of the beginning of the specified page parsing
     *
     * @param page
     */
    void startPage(Page page);

    /**
     * Receives notification of the end of the specified page parsing
     *
     * @param page
     */
    void endPage(Page page);
}
