package eu.leads.infext.logging;

import java.util.Formatter;

public class ErrorStrings {
	public static final String pythonComponentErrorTempl = "Python component %s didn't work properly.";
	
	public static String getErrorString(String errorTempl, Object... args) {
		Formatter formatter = new Formatter();
		formatter.format(errorTempl, args);
		String returnable = formatter.toString();
		formatter.close();
		return returnable;
	}
}
