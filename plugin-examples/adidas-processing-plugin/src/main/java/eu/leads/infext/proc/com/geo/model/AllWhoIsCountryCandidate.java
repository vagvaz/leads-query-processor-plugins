package eu.leads.infext.proc.com.geo.model;

public class AllWhoIsCountryCandidate implements Comparable<AllWhoIsCountryCandidate> {
	
	private String countryCode = null;
	private String countryName = null;
	private int countryNamesFound = 0;
	private int countryCodesFound = 0;
	
	public AllWhoIsCountryCandidate(String countryCode, String countryName) {
		this.countryCode = countryCode;
		this.countryName = countryName;
	}
	public AllWhoIsCountryCandidate(String countryCode) {
		this.countryCode = countryCode;
		this.countryName = countryName;
	}
	
	public void incrementNamesCounter() {
		countryNamesFound++;
	}
	public void incrementCodesCounter() {
		countryCodesFound++;
	}
	
	public String getCountryCode() {
		return this.countryCode;
	}
	public String getCountryName() {
		return this.countryName;
	}
	
	@Override
	public int compareTo(AllWhoIsCountryCandidate other) {
		// DESCENDING SORT (the best choice will be at the beginning)
		if(this.countryNamesFound > other.countryNamesFound)
			return -1;
		else if(this.countryNamesFound < other.countryNamesFound)
			return 1;
		else {
			if(this.countryCodesFound > other.countryCodesFound)
				return -1;
			else if(this.countryCodesFound < other.countryCodesFound)
				return 1;
			else
				return 0;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AllWhoIsCountryCandidate) {
			AllWhoIsCountryCandidate other = (AllWhoIsCountryCandidate) obj;
			if(other.countryCode.equals(this.countryCode))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return countryCode + " (" + countryName + "): " + countryCodesFound + " + " + countryNamesFound;
	}
	
}
