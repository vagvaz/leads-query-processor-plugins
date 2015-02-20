package eu.leads.infext.proc.com.categorization.newsblog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import eu.leads.PropertiesSingleton;
import eu.leads.infext.python.PythonQueueCall;

public class NewsBlogArticleAnalysis {
	
	private static final int CHAR_LENGTH = 500;
	private static final int WORD_LENGTH = 100;
	private static final int SENT_LENGTH =   6;
	private static final int PARA_LENGTH =   2;
	
	private static String apName = "getpublishdate_AP";

	public static String getPublishDate(String content, String url) {
		System.out.println(content != null && content.length() > 50 ? content.substring(0, 50) : "no content!" + " /// " + url);
		java.util.Calendar publishDateCalendar = null;
		String publishDateString = null;
		
		PythonQueueCall pyCall = new PythonQueueCall();
		pyCall.sendViaFile(0);
		List<Object> retValues = pyCall.call(apName, content, url);
		
		if(retValues.size()>=3) {		
			int year = (int) retValues.get(0);
			int month = (int) retValues.get(1);
			int day = (int) retValues.get(2);
			
			publishDateCalendar = Calendar.getInstance();
			publishDateCalendar.set(year, month-1, day);
			
			SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
			publishDateString = formatter.format(publishDateCalendar.getTime());
		}
		
		return publishDateString;
	}
	
	public static boolean isArticle(String content) {
		boolean isArticle = false;
		
		if(countChars(content) >= CHAR_LENGTH
				&& countWords(content) >= WORD_LENGTH 
				&& countSentences(content) >= SENT_LENGTH
				&& countParagraphs(content) >= PARA_LENGTH)
			isArticle = true;
		
		return isArticle;
	}
	
	private static int countChars(String text) {
		return text.trim().length();
	}
	
	private static int countWords(String text) {
		String trimmed = text.trim();
		return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
	}
	
	private static int countSentences(String text) {
		return text.split("[!?.:]+").length;
	}
	
	private static int countParagraphs(String text) {
		return text.split("[\r\n]+").length;
	}
	
	public static void main(String[] args) throws IOException {
		String confPath = "/data/workspace/leads-query-processor-plugins/plugin-examples/adidas-processing-plugin/adidas-processing-plugin-conf.xml";
		XMLConfiguration config;
		try {
			config = new XMLConfiguration(confPath);
			PropertiesSingleton.setConfig(config);
			
		      // Start Python ZeroMQ Server processes!
		      List<String> endpoints = config.getList("pzsEndpoints");
		      String pythonPath = "PYTHONPATH="+config.getString("pythonPath");
		      String commandBase = "/usr/bin/python2.7 -m eu.leads.infext.python.CLAPI.pzs ";
		      String[] envp = {pythonPath};
		      try {
		    	  for(int i=0; i<endpoints.size(); i++) {
			    	  String endpoint = endpoints.get(i);
			    	  String command  = commandBase+endpoint;
			    	  System.out.println(command);
			    	  Runtime.getRuntime().exec(command, envp);
			      }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      	String urlStr = args.length>0 ? args[0] : "http://thebullrunner.com/2015/01/dream-chasers-2015/#.VMpKBGjF-So";
		      	URL url = new URL(urlStr);
		        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		        
		        String content = "";
		
		        String inputLine;
		        while ((inputLine = in.readLine()) != null)
		            content += inputLine;
		        in.close();
		        
		        long start = System.currentTimeMillis();
				System.out.println(NewsBlogArticleAnalysis.getPublishDate(content,url.toExternalForm()));
				System.out.println(System.currentTimeMillis()-start);
		
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
}
