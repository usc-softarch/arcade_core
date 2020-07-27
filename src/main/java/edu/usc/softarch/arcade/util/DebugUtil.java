package edu.usc.softarch.arcade.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.apache.log4j.Logger;

import edu.usc.softarch.arcade.facts.driver.RsfReader;

/**
 * @author joshua
 *
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

	public static String convertByteArrayOutputStreamToString(
			ByteArrayOutputStream sourceStream) {
		//Construct the BufferedReader object
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(sourceStream.toByteArray())));
        
        String line = null;
        String streamAsString = "";
        
        try {
			while ((line = bufferedReader.readLine()) != null) {
			    //Process the data, here we just print it out
			    streamAsString+=line + '\n';
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return streamAsString;
        
		
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
	
	public static void checkIntraPairSize(HashSet<String> intraPair,
			String element1, String element2) {
		if (intraPair.size() != 2) {
			logger.debug("Intrapair wrong size: " + intraPair);
			logger.debug("Expected element1: " + element1);
			logger.debug("Expected element2: " + element2);
			System.err.println("Invalid intrapair size");
			System.exit(1);
		}
		
	}
	
}
