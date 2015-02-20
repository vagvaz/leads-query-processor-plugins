package eu.leads.infext.python;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.CharSet;
import org.apache.commons.lang.StringUtils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import eu.leads.PropertiesSingleton;

public class PythonQueueCall {

	Configuration config = PropertiesSingleton.getConfig();
	private JZC jzc;
	
	private SecureRandom random;


	public PythonQueueCall() {
		random = new SecureRandom();
        jzc = new JZC(config.getList("pzsEndpoints"));
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
		String fileBase = config.getString("pythonFilebasename");
		String fileName = fileBase + fileExt;
		
		File file = new File(fileName);
		if(!file.exists()) {
			try {
				file.createNewFile();
				PrintWriter out = new PrintWriter(file);
				String outputStr = "";
				for(String param : params)
					outputStr += param + "\n";
				outputStr = outputStr.substring(0, outputStr.length()-1);
				out.print(outputStr);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				fileName = null;
			}
		}
		return fileName;
	}
	
	
	public List<Object> call(String moduleName, Object... args) {
		
		List<Object> retValue = null;
		
		List<String> params = new ArrayList<>();
		
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
		String path;
		if(moduleName.contains(".")) // full package name
			path = moduleName;
		else
			path = config.getString("pythonCLIPackage")+"."+moduleName;
		params.add(path);
		params.addAll(paramsList);
		
		int REQUEST_RETRIES = 3;
		int REQUEST_TIMEOUT = 2500;
		String SERVER_ENDPOINT = config.getString("pythonQueueAddress");
		
		String paramsString = StringUtils.join(params.toArray(),' ');
		
		System.out.println(paramsString);
        
        List<Object> reply = jzc.send(params);
        if(reply != null) {
	        System.out.println("Received reply:\n\n" + reply);			
//	        List<String> returned = new ArrayList<>();
//			String strtemp = "";
//			boolean areRetsStarted = false;
//			for(Object line : reply) {
//				if(line.startsWith("[leadsret:] ")) {
//					if(areRetsStarted) {
//						// put the last one into the list
//						returned.add(strtemp);
//						strtemp = "";
//					}
//					else
//						areRetsStarted = true;
//					strtemp += line.substring(12);
//				}
//				else {
//					if(areRetsStarted)
//						strtemp += "\n" + line;
//				}
//			}
//			if(areRetsStarted)
//				returned.add(strtemp);
			retValue = reply;
        }
		return retValue;		
	}

	public static void main(String [] args) {
		String confPath = "/data/workspace/leads-query-processor-plugins/plugin-examples/adidas-processing-plugin/adidas-processing-plugin-conf.xml";
		XMLConfiguration config;
		try {
			config = new XMLConfiguration(confPath);
			PropertiesSingleton.setConfig(config);
			
//		      // Start Python ZeroMQ Server processes!
//		      List<String> endpoints = config.getList("pzsEndpoints");
//		      String pythonPath = "PYTHONPATH="+config.getString("pythonPath");
//		      String commandBase = "/usr/bin/python2.7 -m eu.leads.infext.python.CLAPI.pzs ";
//		      String[] envp = {pythonPath};
//		      try {
//		    	  for(int i=0; i<endpoints.size(); i++) {
//			    	  String endpoint = endpoints.get(i);
//			    	  String command  = commandBase+endpoint;
//			    	  System.out.println(command);
//			    	  Runtime.getRuntime().exec(command, envp);
//			      }
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			PythonQueueCall pythonCall = new PythonQueueCall();
			pythonCall.sendViaFile(0);
			pythonCall.call("eu.leads.infext.python.CLAPI.helloworld_clinterface","hello","world");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
}
