package eu.leads.crawler.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.leads.crawler.parse.URLNormalizer;
import eu.leads.crawler.utils.UrlUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a web page
 *
 * @author ameshkov, pierre sutra
 */

@JsonAutoDetect

public class Page {

  static Log log = LogFactory.getLog(Page.class);

  private
  @JsonProperty("url")
  URL url;
  private
  @JsonProperty("domainName")
  String domainName;
  private
  @JsonProperty("content")
  String content;
  private
  @JsonProperty("headers")
  Map<String, String> headers = new HashMap<String, String>();
  private
  @JsonProperty("responseCode")
  int responseCode;
  private
  @JsonProperty("charset")
  String charset;
  private
  @JsonProperty("responseTime")
  long responseTime;

  private
  @JsonProperty("links")
  List<URL> links;
  private
  @JsonProperty("title")
  String title;
  private
  @JsonIgnore
  Map<String, Object> parseResults = new HashMap<String, Object>();


  /** Constructs a new Page. */
  public Page() {
  }

  /**
   * Creates an instance of the Page class.
   *
   * @param url          Page url
   * @param headers      Response headers map
   * @param responseCode Reposne code
   * @param charset      Page encoding
   * @param responseTime Reponse time
   * @param content      Page content
   */
  public Page(URL url,
              Map<String, String> headers,
              int responseCode,
              String charset,
              long responseTime,
              byte[] content) {
    this.url = url;
    this.headers = headers;
    this.responseCode = responseCode;
    this.charset = charset;
    this.responseTime = responseTime;

    try {
      this.content = Charset.forName(charset).newDecoder().
                                                                  onMalformedInput(CodingErrorAction.REPLACE).
                                                                                                                     onUnmappableCharacter(CodingErrorAction.REPLACE).
                                                                                                                                                                             decode(ByteBuffer.wrap(content)).toString();
    } catch ( Exception e ) {
      log.debug("Unable to retrieve content for " + url);
      this.content = new String();
    }
  }

  /**
   * Returns normalized domain name
   *
   * @return
   */
  public String getDomainName() {
    if ( url == null ) {
      return null;
    }

    if ( domainName == null ) {
      domainName = UrlUtils.getDomainName(url);
    }

    return domainName;
  }

  /**
   * Returns Page url
   *
   * @return
   */
  public URL getUrl() {
    return url;
  }


  /**
   * Setter for property 'url'.
   *
   * @param url Value to set for property 'url'.
   */
  public void setUrl(URL url) {
    this.url = url;
  }

  /**
   * Returns response headers
   *
   * @return
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * Returns response code
   *
   * @return
   */
  public int getResponseCode() {
    return responseCode;
  }

  /**
   * Returns page charset
   *
   * @return
   */
  public String getCharset() {
    return charset;
  }

  /**
   * Sets page charset
   *
   * @param charset
   */
  public void setCharset(String charset) {
    this.charset = charset;
  }

  /**
   * Returns response time
   *
   * @return
   */
  public long getResponseTime() {
    return responseTime;
  }

  /**
   * Returns page content
   *
   * @return
   */
  public String getContent() {
    return content;
  }

  /**
   * Location URL if request was redirected. Otherwise returns {@code null}
   *
   * @return
   */
  @JsonIgnore
  public URL getRedirectUrl() {
    if ( responseCode >= 300 && responseCode < 400 ) {
      try {
        // Response was redirected, returning "Location" header
        URL redirectUrl = URLNormalizer.normalize(getHeader("location"), url, charset);
        return redirectUrl;
      } catch ( Exception ex ) {
        // Ignoring exception
        return null;
      }
    }

    return null;
  }

  /**
   * Returns specified header's value
   *
   * @param header
   *
   * @return
   */
  public String getHeader(String header) {
    return headers.get(header == null ? null : header.toLowerCase());
  }

  /**
   * Returns Content-Encoding header
   *
   * @return
   */
  @JsonIgnore
  public String getContentEncoding() {
    return headers == null ? null : getHeader("content-encoding");
  }

  /**
   * Returns links parsed from this page. <b>Attention: </b> links are available only after using
   * default parser. Links collection used to create next crawler tasks.
   *
   * @return
   */
  public List<URL> getLinks() {
    return links;
  }

  /**
   * Sets links
   *
   * @param links
   */
  public void setLinks(List<URL> links) {
    this.links = links;
  }

  /**
   * Returns page title
   *
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets page title
   *
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns parse results. Any parser can save its result in parse results
   *
   * @return
   */
  public Object getParseResults(String key) {
    return parseResults.get(key);
  }

  /**
   * Saves parse result
   *
   * @param key
   * @param result
   */
  @JsonIgnore
  public void addParseResult(String key, Object result) {
    this.parseResults.put(key, result);
  }

  /** {@inheritDoc} */
  public String toString() {
    return this.url.toString();
  }
}
