package edu.usc.softarch.arcade.facts;

import java.util.HashSet;

import org.apache.log4j.Logger;

public class PrecisionRecallCalculator {
	static Logger logger = Logger.getLogger(PrecisionRecallCalculator.class);
	
	public static double computePrecision(HashSet<HashSet<String>> testIntraPairs, HashSet<HashSet<String>> correctIntraPairs) {
		HashSet<HashSet<String>> intersection = new HashSet<HashSet<String>>(testIntraPairs);
		intersection.retainAll(correctIntraPairs);
		logger.debug("intersection size: " + intersection.size());
		logger.debug("testIntraPairs size: " + testIntraPairs.size());
		logger.debug("correctIntraPairs size: " + correctIntraPairs.size());
		
		/*logger.debug("Printing elements in the correct intrapairs...");
		int intraPairCounter = 0;
		for (HashSet<String> intraPair : correctIntraPairs) {
			logger.debug("\t" + intraPairCounter + ":" + intraPair);
			intraPairCounter++;
		}
		
		logger.debug("Printing elements in the correct intrapairs that match libraries of bash...");
		for (HashSet<String> intraPair : correctIntraPairs) {
			boolean foundMatch = false;
			for (String element : intraPair) {
				if (element.contains("lib/")) {
					logger.debug("\t" + intraPairCounter + ":" + intraPair);
					foundMatch = true;
				}
			}
			if (foundMatch) {
				intraPairCounter++;
			}
		}*/
		
		//System.exit(0);
		
		
		return (double)intersection.size()/(double)testIntraPairs.size();
	}
	
	public static double computeRecall(HashSet<HashSet<String>> testIntraPairs, HashSet<HashSet<String>> correctIntraPairs) {
		HashSet<HashSet<String>> intersection = new HashSet<HashSet<String>>(testIntraPairs);
		intersection.retainAll(correctIntraPairs);
		logger.debug("intersection size: " + intersection.size());
		logger.debug("testIntraPairs size: " + testIntraPairs.size());
		logger.debug("correctIntraPairs size: " + correctIntraPairs.size());
		
		return (double)intersection.size()/(double)correctIntraPairs.size();
	}

	private static void testNonDestructiveCopyOfHashSetHashSetString(
			HashSet<HashSet<String>> testIntraPairs) {
		HashSet<HashSet<String>> intersection = new HashSet<HashSet<String>>(testIntraPairs);
		logger.debug("intersection size: " + intersection.size());
		logger.debug("testIntraPairs size: " + testIntraPairs.size());
		logger.debug("removing all from intersection...");
		intersection.clear();
		logger.debug("intersection size: " + intersection.size());
		logger.debug("testIntraPairs size: " + testIntraPairs.size());
	}
}
