package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.util.FileUtil;

public class RsfReader {
	private static Logger logger = LogManager.getLogger(RsfReader.class);
	
	public static List<List<String>> loadRsfDataFromFile(String rsfFilename)
			throws IOException {
		// List of facts extracted from RSF File
		List<List<String>> facts = new ArrayList<>();

		logger.debug("Attempting to read " + rsfFilename);
		
		try (BufferedReader in = new BufferedReader(new FileReader(rsfFilename))) {
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				logger.debug(line);
				
				if (line.trim().isEmpty())
					continue;

				Scanner s = new Scanner(line);
				String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";

				String arcType = s.findInLine(expr);
				String startNode = s.findInLine(expr);
				String endNode = s.findInLine(expr);
				List<String> fact = Arrays.asList(arcType, startNode, endNode);
				logger.debug(fact);
				facts.add(fact);

				if (s.findInLine(expr) != null) {
					logger.error("Found non-triple in file: " + line);
					System.exit(1); //TODO Remove
				}
				s.close();
			}
		}

		List<String> factsText =
			FileUtil.collectionToString(facts);
		logger.debug("Printing stored facts...");
		logger.debug(String.join("\n", factsText));
		
		return facts;
	}
}