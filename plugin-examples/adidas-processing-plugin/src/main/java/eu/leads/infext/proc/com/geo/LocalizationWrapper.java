package eu.leads.infext.proc.com.geo; 

import java.util.Arrays;

public class LocalizationWrapper {

	DomainSuffixLocalization loc1 = new DomainSuffixLocalization();
	SiteIPGeolocalization loc2 = new SiteIPGeolocalization();
	AllWhoIsLocalization loc3 = new AllWhoIsLocalization();
	
	/**
	 * 
	 * @param fqdn
	 * @return table with 3 strings:	
	 * 		[0] - Domain Suffix localization, 	
	 * 		[1] - GeoIP localization, 
	 * 		[2] - AllWhoIs.
	 */
	public String[] localizeSite(String fqdn) {
		
		String [] countryCodes = new String[3];
		countryCodes[0] = loc1.checkDomainSuffix(fqdn);
		countryCodes[1] = loc2.getDomainCountry(fqdn);
		countryCodes[2] = loc3.retrieveRegistrationCountryInfo(fqdn);
		
		for(int i=0; i<countryCodes.length; i++)
			if(countryCodes[i] != null && countryCodes[i].equals("GB"))
				countryCodes[i] = "UK";
		
		return countryCodes;
	}
	
	public static void main(String[] args) {
		System.out.println(Arrays.asList(new LocalizationWrapper().localizeSite("onet.pl")));
	}
	
}
