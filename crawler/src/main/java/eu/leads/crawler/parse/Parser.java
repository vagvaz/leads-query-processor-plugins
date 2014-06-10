package eu.leads.crawler.parse;

import eu.leads.crawler.model.Page;

/**
 * Page parser. Parsers should have default constructors.
 *
 * @author ameshkov
 */
public interface Parser {

  /**
   * Parses specified page. Should set {@code Page.links} property if you want to schedulle next
   * crawler tasks.
   *
   * @param page
   */
  void parse(Page page);
}
