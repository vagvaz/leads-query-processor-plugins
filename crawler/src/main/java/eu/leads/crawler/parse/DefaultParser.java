package eu.leads.crawler.parse;

import eu.leads.crawler.model.Page;
import it.unimi.dsi.parser.BulletParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Default parser implementation. Extracts links from page.
 *
 * @author ameshkov
 */
public class DefaultParser implements Parser {

    private List<ParserCallback> parserCallbacks = new ArrayList<ParserCallback>();

    /**
     * Creates an instance of the {@code DefaultParser}. Two {@link eu.leads.crawler.parse.ParserCallback}
     * used by default: {@link eu.leads.crawler.parse.LinkExtractorCallback} and {@link
     * eu.leads.crawler.parse.TextExtractorCallback}.
     */
    public DefaultParser() {
        parserCallbacks.add(new LinkExtractorCallback());
        parserCallbacks.add(new TextExtractorCallback());
    }

    /**
     * Adds parser callback
     *
     * @param parserCallback
     */
    public void addParserCallback(ParserCallback parserCallback) {
        this.parserCallbacks.add(parserCallback);
    }

    /**
     * {@inheritDoc}
     */
    public void parse(Page page) {
        for (ParserCallback parserCallback : parserCallbacks) {
            parserCallback.startPage(page);

            BulletParser bulletParser = new BulletParser();
            bulletParser.setCallback(parserCallback);
            bulletParser.parse(page.getContent().toCharArray());

            parserCallback.endPage(page);
        }
    }
}
