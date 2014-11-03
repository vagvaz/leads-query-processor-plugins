package eu.leads.infext.proc.com.keyword.model;

public class Relevance {
	private String tag;
	private Double value;
	
	public String getTag() {
		return tag;
	}

	public Double getValue() {
		return value;
	}

	public Relevance(String tag, Double value) {
		this.tag = tag;
		this.value = value;
	}

	@Override
	public String toString() {
		return "Relevance [tag=" + tag + ", value=" + value + "]";
	}
	
}
