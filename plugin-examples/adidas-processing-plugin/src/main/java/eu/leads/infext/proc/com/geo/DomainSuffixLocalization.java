package eu.leads.infext.proc.com.geo;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.leads.PropertiesSingleton;

public class DomainSuffixLocalization {
	
	private static File fXmlFile = new File(PropertiesSingleton.getResourcesDir()+"/geo/domain-suffixes.xml");

	public String checkDomainSuffix(String fqdn) {
		return findSuffix(getDomainSuffix(fqdn));
	}

	private String findSuffix(String domainSuffix) {
		
		String retVal = null;

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("tld");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					if (eElement.getAttribute("domain").equals(domainSuffix)) {
						System.out.println("country name is : "
								+ eElement.getElementsByTagName("country")
										.item(0).getTextContent());
						System.out.println("country id is : "
								+ eElement.getAttribute("domain"));

						retVal = eElement.getAttribute("domain").toUpperCase();
						
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return retVal;
	}

	private String getDomainSuffix(String url) {
		String domain;
		domain = url.substring(url.lastIndexOf(".") + 1, url.length());
		return domain;
	}

	//
	// TEST
	//
	public static void main(String[] args) {
		DomainSuffixLocalization dsl = new DomainSuffixLocalization(); 
		System.out.println(dsl.checkDomainSuffix("www.google.ca"));
	}

}
