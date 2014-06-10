package eu.leads.crawler.parse;

import eu.leads.crawler.model.Page;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Default {@link eu.leads.crawler.parse.ParserController} implementation. You can set one generic
 * parser and some custom parsers for specified domains. Generic parser will be used for {@link
 * eu.leads.crawler.model.Page} if there's no compatible custom parser.
 *
 * @author ameshkov
 */
public class DefaultParserController implements ParserController {

  private Log log = LogFactory.getLog(this.getClass());
  private Class<? extends Parser> genericParser;
  private Map<String, Class<? extends Parser>> customParsers = new HashMap<String, Class<? extends Parser>>();

  /**
   * Default constructor. {@link eu.leads.crawler.parse.DefaultParser} is used as generic parser.
   */
  public DefaultParserController() {
    this.genericParser = DefaultParser.class;
  }

  /**
   * Sets generic parser. Generic parser will be used if no compatible custom parsers found.
   *
   * @param genericParser
   */
  public void setGenericParser(Class<? extends Parser> genericParser) {
    this.genericParser = genericParser;
  }

  /**
   * Adds custom parser for the specified domain
   *
   * @param domainName
   * @param parser
   */
  public void addCustomParser(String domainName, Class<? extends Parser> parser) {
    customParsers.put(domainName, parser);
  }

  /**
   * Returns parser for the specified page
   *
   * @param page
   *
   * @return
   */
  public Parser getParser(Page page) {
    try {
      log.debug("Getting parser for " + page.getDomainName());

      // Searching for parser for this domain
      Class<? extends Parser> parserClass = customParsers.get(page.getDomainName());

      if ( parserClass != null ) {
        log.debug("Found custom parser for " + page.getDomainName());
        return parserClass.newInstance();
      }

      log.debug("Using generic parser for " + page.getDomainName());
      return genericParser.newInstance();
    } catch ( Exception ex ) {
      String message = "Cannot instanciniate parser for " + page.getDomainName();
      log.fatal(message, ex);
      throw new RuntimeException(message);
    }
  }
}
