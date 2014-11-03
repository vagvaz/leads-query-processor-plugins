package eu.leads.datastore.datastruct;

import java.util.Map;

public class URIVersion implements Comparable<URIVersion> {

	private Long timestampLong;
	private String timestamp;
	private Map<String, Cell> family;

	/**
	 * Constructor
	 * 
	 * @param timestamp - timestamp of the URI's version
	 * @param family - map of keys (column names) to cells (values + versions)
	 */
	public URIVersion(String timestamp, Map<String, Cell> family) {
		this.timestamp = timestamp;
		this.timestampLong = new Long(timestamp);
		this.family = family;
	}
	
	public String getTimestamp() {
		return timestamp;
	}

	public Map<String, Cell> getFamily() {
		return family;
	}

	@Override
	public int compareTo(URIVersion o) {
		//descending
		return (int) (o.timestampLong - this.timestampLong);
	}
	
	@Override
	public String toString() {
		String ret = "";
		ret += timestamp+": {";
		for(Cell cell : family.values())
			ret += cell + ", ";
		ret += "}";
		return ret;
	}
	
	
}
