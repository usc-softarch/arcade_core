package edu.usc.softarch.arcade.util;

import org.apache.log4j.Logger;

/**
 * @author joshua
 */
public class DebugUtil {
	static Logger logger = Logger.getLogger(DebugUtil.class);
	
	public static String getLimitedString(String string, int characterLimit) {
		if (string.length() > characterLimit) {
			return string.substring(0,characterLimit-1);
		}
		else {
			return string;
		}
	}

	public static String addMethodInfo(String s) {
		return "In, "
				+ Thread.currentThread().getStackTrace()[1]
						.getMethodName()
				+ ", " + s;
	}

	public static String addTabs(int depth) {
		String tabs = "";
		for (int i=0;i<depth;i++) {
			tabs +="\t";
		}
		return tabs;
	}
	
	public static void earlyExit()  {
		System.out.println("Exiting early for debugging purposes...");
		System.exit(0);
	}
}
