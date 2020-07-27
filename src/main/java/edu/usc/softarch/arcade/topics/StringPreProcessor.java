package edu.usc.softarch.arcade.topics;

import java.util.Iterator;
import java.util.Vector;

import edu.usc.softarch.arcade.util.Stemmer;


/**
 * @author joshua
 *
 */
public class StringPreProcessor {
	
	public static String camelCaseSeparateAndStem(String str) {
		String processedStr = stem(camelCaseSeparate(str));
		System.out.println("camel case separated and stemmed: " + processedStr);
		return processedStr;
	}
	
	public static String stem(String str) {
		String[] splitStrings = str.split(" ");
		Stemmer stemmer = new Stemmer();
		String stemmedStr = "";
		for (String splitStr : splitStrings) {
			stemmer.add(splitStr.toCharArray(), splitStr.length());
			stemmer.stem();
			stemmedStr += stemmer.toString() + " ";
		}
		return stemmedStr.substring(0,stemmedStr.length()-1);
	}

	public static String camelCaseSeparate(String str) {
		char prevChar = '\0';
		String camelCaseSepStr = "";
		Vector<Integer> splitPositions = new Vector<Integer>();
		for (int i=0;i<str.length();i++) {
			char currChar = str.charAt(i);
			if (!Character.isLetter(currChar)) {
				continue;
			}
			else if (Character.isLowerCase(prevChar) && Character.isUpperCase(currChar)) {
				camelCaseSepStr += " " + currChar;
			}
			else {
				camelCaseSepStr += currChar;
			}
			prevChar = currChar;
		}
		
		camelCaseSepStr = camelCaseSepStr.toLowerCase();
		//System.out.println(camelCaseSepStr);
		return camelCaseSepStr;
		
	}
}
