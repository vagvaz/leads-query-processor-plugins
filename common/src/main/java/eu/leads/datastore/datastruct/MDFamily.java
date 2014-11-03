package eu.leads.datastore.datastruct;

public class MDFamily {
	public UrlTimestamp urlTimestamp = null;
	public String family = null;
	
	public MDFamily(UrlTimestamp urlTimestamp, String family) {
		this.urlTimestamp = urlTimestamp;
		this.family = family;
	}
	public MDFamily(String url, String timestamp, String family) {
		this.urlTimestamp = new UrlTimestamp(url, timestamp);
		this.family = family;
	}
}
