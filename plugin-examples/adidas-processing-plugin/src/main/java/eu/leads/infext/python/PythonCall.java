package eu.leads.infext.python;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.security.SecureRandom;

public class PythonCall {

	private Properties prop = new Properties();
	
	SecureRandom random;


	public PythonCall() {
		InputStream input = getClass().getClassLoader().getResourceAsStream("eu/leads/infext/python/python.properties");
		
		// load a properties file
		try {
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		random = new SecureRandom();
	}
	
	
	boolean isHtml(String text) {
		//return text.matches("[\\S\\s]*\\<html[\\S\\s]*\\>[\\S\\s]*\\<\\/html[\\S\\s]*\\>[\\S\\s]*");
		return text.matches("[\\S\\s]*\\<html[\\S\\s]*\\>[\\S\\s]*"); // amazon has not html closing... o_0
	}
	
	
	private List<Integer> argsViaFile = new ArrayList<>();
	private List<Integer> cutSpaces = new ArrayList<>();

	
	public void sendViaFile(Integer... argNumber) {
		argsViaFile = Arrays.asList(argNumber);
	}
	
	public void cutSpaces(Integer... argNumber) {
		cutSpaces = Arrays.asList(argNumber);
	}
	
	public String paramInFile(String... params) {
		String fileExt = new java.text.SimpleDateFormat("yyMMddHHmmssSSS").format(new Date())+random.nextInt(1000);
		String fileBase = prop.getProperty("filebasename");
		String fileName = fileBase + fileExt;
		
		File file = new File(fileName);
		if(!file.exists()) {
			try {
				file.createNewFile();
				PrintWriter out = new PrintWriter(file);
				for(String param : params)
					out.println(param);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				fileName = null;
			}
		}
		return fileName;
	}
	
	
	public List<String> call(String moduleName, Object... args) {
		
		List<String> retValue = null;
		
		List<String> params = new ArrayList<>();
		
		String python = "python";
		
		String path;
		if(moduleName.contains(".")) // full package name
			path = moduleName;
		else
			path = prop.getProperty("package")+"."+moduleName;
			
		//System.out.println(path);
		
		List<String> paramsList = new ArrayList<>();
		
		for (int i=0; i<args.length; i++) {
			Object arg = args[i];
			
			List<String> nextParamsList = null;
			
			if(arg == null || arg instanceof String) {
				nextParamsList = new ArrayList<>();
				nextParamsList.add((String)arg);
			} 
			else if(arg instanceof List) {
				nextParamsList = (List<String>) arg;
			}
			else
				continue;
			
			for(String param : nextParamsList) {
				if (param == null)
					param = "None";
				if (argsViaFile.contains(i) || isHtml(param) || param.contains("\n")) {
					// create a temp file with the content
					String fileName = paramInFile(param);
					if(fileName != null)
						paramsList.add("file:"+fileName);
				}
				else if(cutSpaces.contains(i)) {
					param = param.replaceAll("\\s","");
					paramsList.add(param);
				}
				else {
					paramsList.add(param);
				}
			}
			
		}
		
		if(paramsList.size() > 20) {
			String fileName = paramInFile(paramsList.toArray(new String[paramsList.size()]));
			if(fileName != null) {
				paramsList.clear();
				paramsList.add("paramsfile:"+fileName);
			}
		}
		
		params.add(python);
		params.add("-m");
		params.add(path);
		params.addAll(paramsList);
		
		System.out.println(params);
		
		int len = 0;
		for(String param : params)
			len += param.length()+1;
		System.out.println(len);
		
//		for(String param : params)
//			System.out.print(param+" ");
//		System.out.println();
		
		ProcessBuilder pb = new ProcessBuilder(params);
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();

			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			List<String> returned = new ArrayList<>();
			String strtemp = "";
			String line = in.readLine();
			boolean areRetsStarted = false;
			while(line != null) {
				// TODO turn off when not debugging
				System.out.println(line);
				if(line.startsWith("[leadsret:] ")) {
					if(areRetsStarted) {
						// put the last one into the list
						returned.add(strtemp);
						strtemp = "";
					}
					else
						areRetsStarted = true;
					strtemp += line.substring(12);
				}
				else {
					if(areRetsStarted)
						strtemp += "\n" + line;
				}
				line = in.readLine();
			}
			if(areRetsStarted)
				returned.add(strtemp);
			retValue = returned;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return retValue;		
	}
}
