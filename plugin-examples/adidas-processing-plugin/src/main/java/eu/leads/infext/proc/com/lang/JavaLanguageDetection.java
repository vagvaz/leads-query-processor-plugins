package eu.leads.infext.proc.com.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.configuration.Configuration;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import eu.leads.PropertiesSingleton;

public class JavaLanguageDetection {
	
	
	private static JavaLanguageDetection javaLanguageDetection = new JavaLanguageDetection();
	
	
	public static JavaLanguageDetection getInstance() {
		return javaLanguageDetection;
	}
	
	private JavaLanguageDetection() {
		try {
			// String resourcePrefix = "eu/leads/infext/proc/com/lang/profiles";
			String resourcePrefix = "lang/profiles";
			Configuration config = PropertiesSingleton.getConfig();
			String resourcesPath = config.getString("resources_path");
			
			List<String> jsonProfiles = new ArrayList<>();
			File langDir = new File(resourcesPath+"/"+resourcePrefix);
			File[] langFiles = langDir.listFiles();
		    //String[] resourcesNames = getResourceListing(getClass(), resourcePrefix);
			for(File langFile : langFiles) {
				InputStream is = new FileInputStream(langFile);
				//InputStream is = JavaLanguageDetection.class.getClassLoader().getResourceAsStream(resourcePrefix+"/"+resName);
			    String jsonProfile = convertStreamToString(is);
				jsonProfiles.add(jsonProfile);
			   
			}
			DetectorFactory.loadProfile(jsonProfiles);
		} catch (LangDetectException e) {
			System.err.println(this.getClass().getName()+" exception: SOMETHING WRONG WITH PROFILE PATH IN loadProfile()");
		} catch (IOException e) {
	    	System.err.println(this.getClass().getName()+" exception: Problem reading language profile from the library!");
		}
	}

	public String detectLanguage(String content) {
		Detector detector;
		String lang = null;
		try {
			detector = DetectorFactory.create();
			detector.append(content);
			lang = detector.detect();
		} catch (LangDetectException e) {
			System.out.println(this.getClass().getName()+" - No features in text to extract language.");
		}
		return lang;
	}
	
	
	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	  
	  
	  /**
	   * List directory contents for a resource folder. Not recursive.
	   * This is basically a brute-force implementation.
	   * Works for regular files and also JARs.
	   * 
	   * @author Greg Briggs
	   * @param clazz Any java class that lives in the same place as the resources you want.
	   * @param path Should end with "/", but not start with one.
	   * @return Just the name of each member item, not the full paths.
	   * @throws URISyntaxException 
	   * @throws IOException 
	   */
	  String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
	      URL dirURL = clazz.getClassLoader().getResource(path);
	      if (dirURL != null && dirURL.getProtocol().equals("file")) {
	        /* A file path: easy enough */
	        return new File(dirURL.toURI()).list();
	      } 

	      if (dirURL == null) {
	        /* 
	         * In case of a jar file, we can't actually find a directory.
	         * Have to assume the same jar as clazz.
	         */
	        String me = clazz.getName().replace(".", "/")+".class";
	        dirURL = clazz.getClassLoader().getResource(me);
	      }

	      if (dirURL.getProtocol().equals("jar")) {
	        /* A JAR path */
	        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
	        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
	        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
	        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
	        while(entries.hasMoreElements()) {
	          String name = entries.nextElement().getName();
	          if (name.startsWith(path)) { //filter according to the path
	            String entry = name.substring(path.length());
	            int checkSubdir = entry.lastIndexOf("/");
	            if (checkSubdir > 0) {
	              // if it is a subdirectory, we just return the directory name
	            	entry = entry.substring(1, checkSubdir);
	            }
	            else {
	            	entry = entry.substring(1,entry.length());
	            }
	            if(!entry.isEmpty())
	            	result.add(entry);
	          }
	        }
	        return result.toArray(new String[result.size()]);
	      } 

	      throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
	  }
	  
	  
	  
	public static void main(String[] args) {
		System.out.print(JavaLanguageDetection.getInstance().detectLanguage("This is an example text."));
	}
	
}
