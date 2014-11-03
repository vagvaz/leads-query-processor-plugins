package eu.leads.datastore.impl;

import static ch.lambdaj.Lambda.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

import eu.leads.datastore.AbstractDataStore;
import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.URIVersion;
import eu.leads.utils.LEADSUtils;

public class CassandraCQLDataStore extends AbstractDataStore {

	private Cluster cluster;
	private Session session;
	
	public CassandraCQLDataStore(Properties mapping, int port, String... hosts) {
		super(mapping);
		
		cluster = Cluster.builder()
            .addContactPoints(hosts)
            .withPort(port)
            .build();
		
		session = cluster.connect();
		
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s\n", 
				metadata.getClusterName());
		for ( Host host : metadata.getAllHosts() ) {
			System.out.printf("Data center: %s; Host: %s; Rack: %s\n",
					host.getDatacenter(), host.getAddress(), host.getRack());
		}	
	}

	@Override
	public SortedSet<URIVersion> getLeadsResourceMDFamily(String uri,
			String family, int lastVersions, String beforeTimestamp) {
		SortedSet<URIVersion> uriVersions = new TreeSet<URIVersion>();
		
		boolean reverse = false;
		
		String queryP01 = "SELECT * FROM " + family;
		//
		String queryP02 = "\nWHERE ";
		//
		String queryP03 = "uri = '" + uri + "'";
		//
		String queryP04 = "";
		if(beforeTimestamp != null) {
			queryP04 += " AND ts < " + beforeTimestamp;
		}
		//
		String queryP05 = "\nORDER BY ts DESC";
		//
		String queryP06 = "\nLIMIT " + lastVersions;
		//
		String query = queryP01+queryP02+queryP03+queryP04+queryP05+queryP06;

		System.out.println(query);
		
		Iterable<Row> rs;
		try {
			rs = session.execute(query);
		} catch(Exception e) {
			rs = new TreeSet<Row>();
		}
		
		if(!rs.iterator().hasNext()) {
			reverse = true;
			//
			queryP04 = "";
			//
			queryP05 = "\nORDER BY ts ASC";
			//
			query = queryP01+queryP02+queryP03+queryP04+queryP05+queryP06;

			System.out.println(query);
			
			try {
				rs = session.execute(query);
			} catch(Exception e) {
				rs = new TreeSet<Row>();
			}			
		}
		
		for(Row row : rs) {
			ColumnDefinitions columns = row.getColumnDefinitions();
			Map<String,Cell> columnsMap = new HashMap<String, Cell>();
			for(Definition col : columns) {
				String name = col.getName();
				// To be extended to any type...
				if(col.getType() == DataType.varchar()){
					Object value = row.getString(name);
					
					Cell cell = new Cell(name, value, 0);
					columnsMap.put(name, cell);
				}
				else if(col.getType() == DataType.blob()) {
					Object value = row.getString(name);
					
					Cell cell = new Cell(name, value, 0);
					columnsMap.put(name, cell);
				}
			}
			Long ts = new Long(row.getLong("ts"));
			
			URIVersion uriVersion = new URIVersion(ts.toString(), columnsMap);
			uriVersions.add(uriVersion);
		}
		
		if(reverse) {
			List<URIVersion> uriVersionsList = new ArrayList<>(uriVersions);
			Collections.reverse(uriVersionsList);
			uriVersions = new TreeSet<>(uriVersionsList);
		}
		
		return uriVersions;
	}

	@Override
	public boolean putLeadsResourceMDFamily(String uri, String ts,
			String family, List<Cell> cells) {
		
		List<String> columnsList = new ArrayList<String>();
		List<Object> valuesList  = new ArrayList<Object>();
		for(Cell cell : cells) {
			columnsList.add(cell.getKey());
			valuesList.add(cell.getValue());
		}
		
		int i=0;
		
		String queryP01 = "INSERT INTO ";
		//
		String queryP02 = family;
		//
		String queryP03 = "\n (uri, ts, ";
		//
		String queryP04 = "";
		for(i=0; i<cells.size()-1; i++)
			queryP04 += columnsList.get(i) + ", ";
		queryP04 += columnsList.get(i) + ")\n";
		//
		String queryP05 = "VALUES (";
		//
		String queryP06 = "'" + uri + "', ";
		//
		String queryP07 = ts + ", ";
		//
		String queryP08 = "";
		for(i=0; i<cells.size()-1; i++)
			queryP08 += "?, ";
		queryP08 += "?);";
//		for(i=0; i<cells.size()-1; i++)
//			queryP08 += "'" + valuesList.get(i) + "', ";
//		queryP08 += "'" + valuesList.get(i) + "');";
		//
		String query = queryP01+queryP02+queryP03+queryP04+queryP05+queryP06+queryP07+queryP08;
		
		PreparedStatement writeStatement = session.prepare(query);
		BoundStatement boundStatement = new BoundStatement(writeStatement);
		for(i=0; i<cells.size(); i++)
			boundStatement.setString(i, valuesList.get(i).toString());
		
		try {
			session.execute(boundStatement);
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}

	@Override
	public HashMap<String, List<Object>> getLeadsResourcePartsMD(String uri,
			String ts, String partType) {

		HashMap<String, List<Object>> returnMap = new HashMap<String, List<Object>>();
		
		String queryP01 = "SELECT * FROM " + mapping.getProperty("leads_resourcepart");
		//
		String queryP02 = "\nWHERE ";
		//
		String queryP03 = "uri = '" + uri + "'";
		//
		String queryP04 = " AND ts = " + ts;
		//
		String queryP05 = "";
		if(partType != null)
			queryP05 += " AND " + mapping.getProperty("leads_resourcepart-type") + " = '" + partType + "';";
		//
		String query = queryP01+queryP02+queryP03+queryP04+queryP05;
		
		System.out.println(query);
		Iterable<Row> rs;
		try {
			rs = session.execute(query);
		} catch(Exception e) {
			rs = new TreeSet<Row>();
		}
		
		for(Row row : rs) {
			String type = row.getString(mapping.getProperty("leads_resourcepart-type"));
			String value= row.getString(mapping.getProperty("leads_resourcepart-value"));
			List<Object> values = returnMap.get(type);
			if(values == null)
				values = new ArrayList<Object>();
			values.add(value);
			returnMap.put(type, values);
		}
		
		return returnMap;
	}

	@Override
	public boolean putLeadsResourcePartsMD(String uri, String ts,
			HashMap<String, Object> partsTypeValuesMap) {
		
		String queryP01 = "INSERT INTO ";
		//
		String queryP02 = mapping.getProperty("leads_resourcepart");
		//
		String queryP03 = "\n (uri, ts, partid, ";
		//
		String queryP04 = mapping.getProperty("leads_resourcepart-type") + ", ";
		queryP04       += mapping.getProperty("leads_resourcepart-value") + ")\n";
		//
		for(Entry<String, Object> partTypeValues : partsTypeValuesMap.entrySet()) {
			String queryP05 = "VALUES ";
			String keyId = partTypeValues.getKey();
			String [] keyIdArray = keyId.split(":");
			String key = keyIdArray[0];
			String id = keyIdArray[1];
			Object value = partTypeValues.getValue();
			queryP05   += "(";
			queryP05   += "'" + uri + "', ";
			queryP05   += ts + ", ";
			queryP05   += "'" + id + "', ";
			queryP05   += "'" + key + "', ";
			queryP05   += "?";			
			queryP05   += ");";
			//queryP05 = new StringBuilder(queryP05).replace(queryP05.length()-2, queryP05.length(), "; ").toString();
			//
			String query = queryP01+queryP02+queryP03+queryP04+queryP05;
			
			PreparedStatement writeStatement = session.prepare(query);
			BoundStatement boundStatement = new BoundStatement(writeStatement);
			boundStatement.setString(0, value.toString());
			
			System.out.println();
			try {
				session.execute(boundStatement);
			} catch(Exception e) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public Map<String, SortedSet<URIVersion>> getLeadsResourceElementsMDFamily(
			String uri, String family, int lastVersions, String beforeTimestamp) {
		throw new UnsupportedOperationException("getLeadsResourceElementsMDFamily not implemented for CassandraCQLDataStore");
	}
	
	public List<Map<String,Object>> getLeadsResourcesOfElement(String element) {
		List<Map<String,Object>> returnMapsList = new ArrayList<>();
		
		String query = "SELECT * FROM " + mapping.getProperty("leads_keywords")
				+ "\nWHERE keywords = '" + element + "'";
		System.out.println(query);
		session.execute(query);
		
		Iterable<Row> rs;
		try {
			rs = session.execute(query);
		} catch(Exception e) {
			rs = new TreeSet<Row>();
		}
		
		for(Row row : rs) {
			Map<String,Object> keysValues = new HashMap<>();
			String url		 = row.getString("uri");
			Long ts		 	 = row.getLong("ts");
			String partid	 = row.getString("partid");
			String sentiment = row.getString(mapping.getProperty("leads_keywords-sentiment"));
			String relevance = row.getString(mapping.getProperty("leads_keywords-relevance"));
			
			keysValues.put("uri"	, url);
			keysValues.put("ts" 	, ts);
			keysValues.put("partid"	, partid);
			keysValues.put(mapping.getProperty("leads_keywords-sentiment"), sentiment);
			keysValues.put(mapping.getProperty("leads_keywords-relevance"), relevance);
			returnMapsList.add(keysValues);
		}
		
		return returnMapsList;
	}

	@Override
	public boolean putLeadsResourceElementsMDFamily(String uri, String ts, String partid, 
			String element, String familyName, List<Cell> cells) {
		
		int i=0;
		
		List<String> columnsList = new ArrayList<String>();
		List<Object> valuesList  = new ArrayList<Object>();
		for(Cell cell : cells) {
			columnsList.add(cell.getKey());
			valuesList.add(cell.getValue());
		}
		
		String queryP01 = "INSERT INTO ";
		//
		String queryP02 = mapping.getProperty("leads_keywords");
		//
		String queryP03 = "\n (uri, ts, partid, keywords, ";
		//
		String queryP04 = "";
		for(i=0; i<cells.size()-1; i++)
			queryP04 += columnsList.get(i) + ", ";
		queryP04 += columnsList.get(i) + ")\n";
		//
		String queryP05 = "VALUES (";
		//
		String queryP06 = "'" + uri + "', " + ts + ", '" + partid + "', '" + element + "'";
		//
		String queryP07 = ", ";
		for(i=0; i<cells.size()-1; i++)
			queryP07 += "'" + valuesList.get(i) + "', ";
		queryP07 += "'" + valuesList.get(i) + "')\n";
		//
		String query = queryP01+queryP02+queryP03+queryP04+queryP05+queryP06+queryP07;
		
		System.out.println(query);
		try {
			session.execute(query);
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}

	@Override
	public List<String> getResourceURIsOfDirectory(String dirUri) {
		List<String> uris = new ArrayList<String>();
		
		String queryP01 = "SELECT uri FROM " + mapping.getProperty("leads_core");
		//
		String queryP02 = "\nWHERE ";
		//
		String queryP03 = mapping.getProperty("leads_core-fqdnurl") + " = '" + dirUri + "'";
		//
		String query = queryP01+queryP02+queryP03;
		
		System.out.println(query);
		Iterable<Row> rs;
		try {
			rs = session.execute(query);
		} catch(Exception e) {
			rs = new TreeSet<Row>();
		}
		
		for(Row row : rs) {
			String uri= row.getString("uri");
			uris.add(uri);
		}
		
		return uris;
	}
	
	private Iterator<Row> urisIterator = null;
	
	public String getFamilyNextUri(String family) {
		
		if(urisIterator == null) {
			String queryP01 = "SELECT uri FROM " + family;
			Statement stmt = new SimpleStatement(queryP01);
			stmt.setFetchSize(500);
			Iterable<Row> rs;
			try {
				rs = session.execute(stmt);
			} catch(Exception e) {
				rs = new TreeSet<Row>();
			}
			urisIterator = rs.iterator();
		}
		
		if(urisIterator.hasNext())
			return urisIterator.next().getString(0);
		else
			return null;
	}

	@Override
	public List<String> getFQDNList() {
		throw new UnsupportedOperationException("getFQDNList not implemented for CassandraCQLDataStore");
	}

	@Override
	public Object getFamilyStorageHandle(String familyName) {
		return session;
	}

}
