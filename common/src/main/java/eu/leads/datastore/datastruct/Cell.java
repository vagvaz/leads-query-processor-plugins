package eu.leads.datastore.datastruct;

public class Cell {
	
	private String key;
	private Object value;
	private long cellVersion;
	
	/**
	 * Constructor
	 * 
	 * @param key - cell key (column name)
	 * @param value - value (to be) stored in the cell
	 * @param version - version of the record (to be incremented before writing - for correctness)
	 */
	public Cell(String key, Object value, long version) {
		this.key = key;
		this.value = value;
		this.cellVersion = version;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public long getVersion() {
		return cellVersion;
	}
	
	@Override
	public String toString() {
		return key + ": " + value;
	}
	
}
