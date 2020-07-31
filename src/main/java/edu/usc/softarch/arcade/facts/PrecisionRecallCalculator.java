package edu.usc.softarch.arcade.facts;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class PrecisionRecallCalculator {
	private static Logger logger = Logger.getLogger(PrecisionRecallCalculator.class);
	
	public static double computePrecision(Set<Set<String>> testIntraPairs, Set<Set<String>> correctIntraPairs) {
		Set<Set<String>> intersection = new HashSet<>(testIntraPairs);
		intersection.retainAll(correctIntraPairs);
		logger.debug("intersection size: " + intersection.size());
		logger.debug("testIntraPairs size: " + testIntraPairs.size());
		logger.debug("correctIntraPairs size: " + correctIntraPairs.size());

		return (double)intersection.size()/(double)testIntraPairs.size();
	}
	
	public static double computeRecall(Set<Set<String>> testIntraPairs, Set<Set<String>> correctIntraPairs) {
		Set<Set<String>> intersection = new HashSet<>(testIntraPairs);
		intersection.retainAll(correctIntraPairs);
		logger.debug("intersection size: " + intersection.size());
		logger.debug("testIntraPairs size: " + testIntraPairs.size());
		logger.debug("correctIntraPairs size: " + correctIntraPairs.size());
		
		return (double)intersection.size()/(double)correctIntraPairs.size();
	}
}
