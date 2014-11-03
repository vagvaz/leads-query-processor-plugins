package eu.leads.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.leads.datastore.datastruct.Cell;
import eu.leads.datastore.datastruct.URIVersion;

public class LEADSUtils {

	public static String getDomainName(String url) {
//	    URI uri;
//		try {
//			uri = new URI(url);
//		    String domain = uri.getHost();
//		    return domain;
//		} catch (URISyntaxException e) {
//			return null;
//		}
		return getResourceGeneralization(url, 0);
	}
	
	public static List<String> getAllResourceGeneralizations(String url) {
		List<String> generalizationsList = new ArrayList<>();
		
		String urlparts[] = url.split("/");
		String result = "";
		for(int i=0; i<=urlparts.length-1; i++) {
			result += urlparts[i] + "/";
			generalizationsList.add(result);
		}
		
		result = result.substring(0,result.length()-1);
		
		return generalizationsList;
	}
	
	public static String getResourceGeneralization(String url, int level) {
		String urlparts[] = url.split("/", level+2);
		String result = "";
		for(int i=0; i<=level; i++) {
			result += urlparts[i] + "/";			
		}
		result = result.substring(0,result.length()-1);
		return result;
	}
	
	public static String nutchUrlBaseToStandardUrlBase(String nutchUrlBase) {
		String standardUrlBase = "";
		
		String [] parts = nutchUrlBase.split("[/:.]*");
		
		boolean first = true;		
		for(int i=parts.length-1; i>=0; i--) {
			standardUrlBase += parts[i];
			if(first) {
				standardUrlBase += "://";
				first = false;
			}
			else {
				standardUrlBase += ".";
			}
		}
		
		standardUrlBase = standardUrlBase.substring(0,standardUrlBase.length()-1);
		
		return standardUrlBase;
	}
	
	
	public static String standardUrlToNutchUrl(String standardUrl) {
		String nutchUrl = "";
		URL url_;
		try {
			url_ = new URL(standardUrl);

			String authority = url_.getAuthority();
			String protocol  = url_.getProtocol();
			String file      = url_.getFile();
			
			String [] authorityParts = authority.split("\\.");
			for(int i=authorityParts.length-1; i>=0; i--)
				nutchUrl += authorityParts[i] + ".";
			nutchUrl = nutchUrl.substring(0, nutchUrl.length()-1);
			nutchUrl += ":" + protocol;
			nutchUrl += file;			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		
		return nutchUrl;
	}
	
	public static String fqdnToNutchUrl(String fqdn) {
		return standardUrlToNutchUrl("http://"+fqdn+"/");
	}
	
//	public static void main(String[] args) {
//		System.out.println(fqdnToNutchUrl("store.nike.com"));
//	}
	
	
	public static String nutchUrlToFullyQualifiedDomainName(String nutchUrlBase) {
		String domainName = "";
		
		String [] parts = nutchUrlBase.split(":");
		String nutchDomainName = parts[0];
		
		String [] words = nutchDomainName.split("\\.");
		
		for(int i=words.length-1; i>=0; i--) {
			domainName += words[i] + ".";
		}
		domainName = domainName.substring(0,domainName.length()-1);
		
		return domainName;
	}
	
	public static String nutchUrlToFullyQualifiedDomainNameUrl(String nutchUrl) {
		String domainName = "";
		
		String [] parts = nutchUrl.split("/");
		String nutchDomainName = parts[0];
				
		return nutchDomainName+"/";
	}
	
//	public static void main(String[] args) {
//		System.out.println(nutchUrlToFullyQualifiedDomainNameUrl("com.nike.store:http/lalala"));
//	}	
//	
	public static long getTimestamp() {
		return System.currentTimeMillis();
	}
	
	public static String getTimestampString() {
		return new Long(System.currentTimeMillis()).toString();
	}
	
	public static long stringToLong(String numberStr) {
		if(numberStr != null)
			try {
				return new Long(numberStr);
			} catch(NumberFormatException e) {
				return 0;
			}
		else
			return 0;
	}
	
	public static double stringToDouble(String numberStr) {
		if(numberStr != null)
			try {
				return new Double(numberStr);
			} catch(NumberFormatException e) {
				return 1.0;
			}
		else
			return 1.0;
	}
	
	public static List<HashMap<String,String>> getMetadataOfDirectories(HashMap<String, HashMap<String, String>> urlParameters, String suffix) {
		List<HashMap<String,String>> generalParametersList = new ArrayList<>();
		
		int number = 0;
		boolean isMoreGeneral = true;
		while(isMoreGeneral) {
			String key = "general"+number;
			key += (suffix == null) ? "" : ":"+suffix;
			HashMap<String,String> generalParameters = urlParameters.get(key);
			if(generalParameters == null)
				isMoreGeneral = false;
			else
				generalParametersList.add(generalParameters);
			number++;
		}
		// Collections.reverse(generalParametersList);	// reverse so that the deepest directory in first
		
		return generalParametersList;
	}
	
	public static boolean any(List<?> list, Object... objects) {
		List<?> listCopy = new ArrayList<>(list);
		listCopy.retainAll(Arrays.asList(objects));
		return !listCopy.isEmpty();
	}
	
	public static boolean string2Boolean(String str) {
		if(str==null || str.equals("0") || str.equals("false"))
			return false;
		else
			return true;
	}
	
	public static String prepareExtractionCandidatesJSONString(String [] extractionTuplesArray, String [] extractionTuplesTypes) {
		JSONArray jsonArray = new JSONArray();
		for(int i=0; i<extractionTuplesArray.length; i++) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put(extractionTuplesTypes[i],extractionTuplesArray[i]);
				jsonArray.put(jsonObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jsonArray.toString();
	}
	
	public static HashMap<String,String> retrieveExtractionCandidatesFromJSONString(String candidatesJSONString) {
		HashMap<String,String> extractionCandidatesStringsMap = new HashMap<>();
		
		try {
			JSONArray extractionPartsArray = new JSONArray(candidatesJSONString);
			
			for(int i=0; i<extractionPartsArray.length(); i++) {
				JSONObject obj = extractionPartsArray.getJSONObject(i);
				Iterator<String> objIterator = obj.keys();
				while(objIterator.hasNext()) {
					String key = objIterator.next();
					String value = obj.getString(key);
					extractionCandidatesStringsMap.put(key, value);
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return extractionCandidatesStringsMap;
	}
	
//	public static void main(String[] args) {
//		System.out.println(prepareExtractionCandidatesJSONString(new String[]{"[('/html/xxx',5,7),[('/html/xxx',5,7)]","val2"}, new String[]{"a","b"}));
//	}
	
	public static String propertyValueToKey(Properties p, String v) {
		String key = null;
		Collection<Object> values = p.values();
		if(values.contains(v)) {
			for(Entry<Object, Object> e : p.entrySet()) {
				if(e.getValue().equals(v)) {
					key = (String) e.getKey();
					break;
				}
			}
		}
		
		return key;
	}
	
	
	public static HashMap<String,String> uriVersionToFamilyMap(URIVersion uriVersion) {
		HashMap<String,String> map = new HashMap<>();
		
		for(Entry<String, Cell> e : uriVersion.getFamily().entrySet())
			map.put(e.getKey(), (String) e.getValue().getValue());
		
		return map;
	}
	
	/////////////////////////////////////
	
	static class StringMatchesMatcher extends TypeSafeMatcher<Object> {

		private String regex;

	    StringMatchesMatcher(String regex) {
	      this.regex = regex;
	    }

	    @Override
	    public boolean matchesSafely(Object obj) {
	    	String string = (String) obj;
	      boolean m = string.matches(regex);
	      return m;
	    }
	
	    @Override
	    public void describeTo(Description description) {
	      description.appendText("matches " + regex);
	    }
	}

	@Factory
	public static Matcher<Object> matches(String regex) {
		return new StringMatchesMatcher(regex);
	}
	
	public static boolean isUUID(String uuidCand) {
		String pattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
		Pattern r = Pattern.compile(pattern);
		java.util.regex.Matcher m = r.matcher(uuidCand);
		return m.matches();
	}
	
}










