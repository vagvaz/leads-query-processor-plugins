package eu.leads.crawler.c4j;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import eu.leads.datastore.AbstractDataStore;
import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.utils.LEADSUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.Header;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class LeadsWP5DemoCrawler extends WebCrawler {

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
      + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
  	
	private static String parametersFile = "eu/leads/crawler/c4j/seedlist.properties";
	private Properties properties = new Properties();
	
	private String domainPattern = null;
	
	AbstractDataStore ds = DataStoreSingleton.getDataStore();
	Properties mapping   = DataStoreSingleton.getMapping(); 
	
	private void init() {
		InputStream input = null;
		try {
			input =  LeadsWP5DemoCrawler.class.getClassLoader().getResourceAsStream(parametersFile);
			// load a properties file
			properties.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public LeadsWP5DemoCrawler() {
		init();
	}
	
  /**
   * You should implement this function to specify whether the given url
   * should be crawled or not (based on your crawling logic).
   */
    @Override
	public boolean shouldVisit(WebURL url) {
    	String href = url.getURL().toLowerCase();
    	String domain = url.getDomain();
    	
    	if(domainPattern == null) {
	    	String propString = properties.getProperty(ExchangeInfoAntipattern.getDomain());
	    	if(propString != null) {
	    		String [] propValues = propString.split(";");
	    		domainPattern = propValues[0]+".*";
	    	}
	    	else {
	    		domainPattern = ".*";
	    	}
    	}
    	
    	boolean domainPatternMatch = href.matches(domainPattern);
    	boolean filtersMatch = FILTERS.matcher(href).matches();
    	
    	if(!filtersMatch && domainPatternMatch) {
    		//System.out.println("OK: "+url.getURL());
            return true;
    	}
    	else {
    		//System.out.println("SKIP: "+url.getURL());
    		return false;
    	}
    	
	}

  /**
   * This function is called when a page is fetched and ready to be processed
   * by your program.
   */
  @Override
  public void visit(Page page) {
	
    int docid = page.getWebURL().getDocid();
    String url = page.getWebURL().getURL();
	String reverseUrl = LEADSUtils.standardUrlToNutchUrl(url);
    String domain = page.getWebURL().getDomain();
    String path = page.getWebURL().getPath();
    String subDomain = page.getWebURL().getSubDomain();
    String parentUrl = page.getWebURL().getParentUrl();
    String anchor = page.getWebURL().getAnchor();

    System.out.println("URL: " + url);

    if (page.getParseData() instanceof HtmlParseData) {
      HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
      final String html = htmlParseData.getHtml();
      final ByteBuffer htmlBlob = ByteBuffer.wrap(html.getBytes());
      List<WebURL> links = htmlParseData.getOutgoingUrls();
      
      
      
      /*
       * 1. Put content to Infinispan
       */
      //infinispanStoreCall.put(reverseUrl, html, LEADSUtils.getTimestampString());
      List<Cell> cells = new ArrayList<Cell>() {{ 
    	  add(new Cell(mapping.getProperty("leads_crawler_data-content"),html,0));
    	  }};
      
      boolean succeed = true; 
      do {
    	  try {
	      ds.putLeadsResourceMDFamily(
	    		  reverseUrl, LEADSUtils.getTimestampString(), 
	    		  mapping.getProperty("leads_crawler_data"), cells);
	      succeed = true;
    	  } catch (Exception e) {
    		  System.err.println(e.getMessage());
    		  succeed = false;
    		  try { Thread.sleep(10000); } catch (InterruptedException e1) {}
    	  }
      } while(!succeed);
      
      /*
       * 2. Put outgoing links to Infinispan
       */
      // TODO
      // idea: additional cache with: url1::url2 and versions (each contains timestamp, anchor text etc.)
      // then - no need for 3.
      // linkcache1: url1 -> [ts], url2 -> [ts]
      // linkcache2: url1:ts -> {url2->[o,until], ...}, url2:ts -> {url1->[i,until]}
      // every time a list of links should be compared with a last version on the page (to annotate the gone inbound links)
      
      /*
       * 3. For every outgoing link, put incoming one
       */
      // TODO
      
    }
  }
  
}

