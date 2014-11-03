package eu.leads.infext.webgraph;

import java.util.ArrayList;
import java.util.List;

import eu.leads.utils.LEADSUtils;


public class TestClass {

	public static void main(String[] args) {
		String url = args[0];
		ReverseLink reverseLink = new DhanyaReverseLink();
		List<String> inboundUrls = reverseLink.getInboundUrls(url);
		List<SiteCountryStruct> siteCountryList = new ArrayList<>();
		for(String iurl : inboundUrls) {
			// e.g. iurl = "com.cnn.edition:http/2014/07/04/article.html";
			System.out.println(iurl);
			String standardDomainName = LEADSUtils.nutchUrlToFullyQualifiedDomainName(iurl);  
			System.out.println(standardDomainName);
			// e.g. standardDomainName = "edition.cnn.com"
			// TODO call your country extraction component with standardDomainName as an input
			SiteCountryStruct siteCountryStruct = null; // return val of your component
			siteCountryList.add(siteCountryStruct);
		}
	}
	
}