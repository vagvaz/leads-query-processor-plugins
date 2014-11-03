package eu.leads.datastore.datastruct;

public class UrlTimestamp {
	public String url = null;
	public String timestamp= null;
	
	public UrlTimestamp(String url, String timestamp) {
		this.url = url;
		this.timestamp = timestamp;
	}
	
	@Override
	public String toString() {
		return url+":"+timestamp;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof UrlTimestamp) {
			UrlTimestamp o = (UrlTimestamp) obj;
			if(o.timestamp.equals(timestamp) && o.url.equals(url))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return url.hashCode() + timestamp.hashCode();
	}
}
