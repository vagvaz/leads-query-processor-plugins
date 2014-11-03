package eu.leads.infext.proc.com.geo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import eu.leads.infext.proc.com.geo.model.AllWhoIsCountryCandidate;
import eu.leads.infext.proc.com.geo.model.USLocale;
import eu.leads.infext.proc.com.maincontent.JerichoTextContentExtraction;

public class AllWhoIsLocalization {
	
	private static String [] countryCodes;
	private static String [] countryNames;
	private static Pattern [] countryCodesPatterns;
	private static Pattern [] countryNamesPatterns;
	
	static {
		countryCodes = Locale.getISOCountries();
		
		countryNames = new String[countryCodes.length];
		
		for(int i=0; i<countryCodes.length; i++) {
			Locale countryLocale = new Locale("", countryCodes[i]);
			countryNames[i] = countryLocale.getDisplayCountry();
		}
		
		countryCodesPatterns = new Pattern[countryCodes.length];
		countryNamesPatterns = new Pattern[countryCodes.length];
		for(int i=0; i<countryCodes.length; i++) {
			countryCodesPatterns[i] = Pattern.compile("(^|[^a-zA-Z0-9]+)"+countryCodes[i].toUpperCase()+"([^a-zA-Z0-9]+|$)");
			countryNamesPatterns[i] = Pattern.compile("(^|[^a-zA-Z0-9]+)"+countryNames[i]+"([^a-zA-Z0-9]+|$)",Pattern.CASE_INSENSITIVE);
		}
	}

	public String retrieveRegistrationCountryInfo(String fqdn) {
		URL url;
		String domain = fqdn;
		String countryCode = null;
		while(true) {
			String dataUrl = "http://all-whois.com/"+fqdn;
			try {
				countryCode = getData(dataUrl);
			} catch (IllegalArgumentException e) {
				int index = fqdn.indexOf(".");
				fqdn = fqdn.substring(index+1);
				continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		return countryCode;
	}

	private String getData(String dataUrl) throws IOException {

		String usrInput;
		String contents;
		int beginIndex, endIndex;
		
		String regInfo = null;
		
		Document doc;
		doc = Jsoup.connect(dataUrl).get();
		Elements elements = doc.select("div[class=span7 well] p");

//		elements = userAgent.doc.findEvery("<div class=\"span7 well\">")
//				.findEvery("<p>");

		contents = elements.html();
		
		if(contents.isEmpty())
			throw new IllegalArgumentException();
		
		contents = contents.replaceAll("<br.{0,3}>", "\n");
		contents = contents.replaceAll("<[^>]*>", "");
		
		int [] codeMentionsCounter = new int[countryCodes.length];
		int [] nameMentionsCounter = new int[countryCodes.length];
		
		List<AllWhoIsCountryCandidate> candidatesList = new ArrayList<>();
					
		for(int i=0; i<countryCodesPatterns.length; i++) {
			AllWhoIsCountryCandidate cand = null;
			
			Matcher countryCodesMatcher = countryCodesPatterns[i].matcher(contents);
			if(countryCodesMatcher.find()) {
				cand = new AllWhoIsCountryCandidate(countryCodes[i],countryNames[i]);
				cand.incrementNamesCounter();
			}
			while(countryCodesMatcher.find())
				cand.incrementNamesCounter();
			
			Matcher countryNamesMatcher = countryNamesPatterns[i].matcher(contents);
			if(countryNamesMatcher.find()) {
				cand = new AllWhoIsCountryCandidate(countryCodes[i],countryNames[i]);
				cand.incrementCodesCounter();
			}
			while(countryNamesMatcher.find())
				cand.incrementCodesCounter();
			
			if(cand != null)
				candidatesList.add(cand);
		}
			
			Collections.sort(candidatesList);
			
			// US fix
			fixUS(candidatesList);
			
			// misleading codes fix
			fixMisleadingCodes(candidatesList);
			
			System.out.println(candidatesList);
			
			if(!candidatesList.isEmpty())
				regInfo = candidatesList.get(0).getCountryCode();
			
		return regInfo;
		
	}
	
	private void fixUS(List<AllWhoIsCountryCandidate> candidatesList) {
		if(candidatesList.contains(new AllWhoIsCountryCandidate("US"))) {
			List<String> stateCodesList = Arrays.asList(USLocale.getISOStates());
			for(int i=0; i<candidatesList.size(); ) {
				AllWhoIsCountryCandidate cand = candidatesList.get(i);
				if(cand.getCountryCode().equals("US"))
					i++;			
				else if(stateCodesList.contains(cand.getCountryCode()))
					candidatesList.remove(i);
				else
					i++;
			}
					
		}	
	}

	private void fixMisleadingCodes(List<AllWhoIsCountryCandidate> candidatesList) {
		candidatesList.remove(new AllWhoIsCountryCandidate("ID"));
		candidatesList.remove(new AllWhoIsCountryCandidate("TM"));
		candidatesList.remove(new AllWhoIsCountryCandidate("IQ"));
		candidatesList.remove(new AllWhoIsCountryCandidate("AG"));
	}
	
	//
	// TEST
	//
	public static void main(String args[]) throws Exception {
		AllWhoIsLocalization awil = new AllWhoIsLocalization();
		awil.retrieveRegistrationCountryInfo("edition.cnn.com");
	}
	
}
