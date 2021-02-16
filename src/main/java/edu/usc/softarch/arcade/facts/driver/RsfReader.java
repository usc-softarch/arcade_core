package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.StopWatch;

public class RsfReader {
	private static Logger logger = LogManager.getLogger(RsfReader.class);
	public static Set<List<String>> untypedEdgesSet;
	public static Set<String> startNodesSet;
	public static List<List<String>> filteredRoutineFacts;
	public static List<String> filteredRoutines;
	public static Set<String> endNodesSet;
	public static Set<String> allNodesSet;
	public static List<List<String>> unfilteredFacts;
	
	public static List<List<String>> extractFactsFromRSF(String rsfFilename) {
		// List of facts extracted from RSF File
		List<List<String>> facts = new ArrayList<>();

		logger.debug("Attempting to read " + rsfFilename);
		
		try (BufferedReader in = new BufferedReader(new FileReader(rsfFilename))) {
			String line;

			while ((line = in.readLine()) != null) {
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
		} catch (IOException e) {
			e.printStackTrace(); //TODO Treat properly
		}
		return facts;
	}

	public static void loadRsfDataFromFile(String rsfFilename) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		unfilteredFacts = extractFactsFromRSF(rsfFilename);

		List<String> unfilteredFactsText =
			FileUtil.collectionToString(unfilteredFacts);
		logger.debug("Printing stored facts...");
		logger.debug(String.join("\n", unfilteredFactsText));
		
		filteredRoutineFacts = unfilteredFacts;

		List<List<String>> untypedEdges = convertFactsToUntypedEdges(unfilteredFacts);
		untypedEdgesSet = new HashSet<>(untypedEdges);

		List<String> untypedEdgesText = FileUtil.collectionToString(untypedEdges);
		logger.debug("Printing untyped edges....");
		logger.debug("number of untyped edges as list: " + untypedEdges.size());
		logger.debug("number of untyped edges as set: "	+ untypedEdgesSet.size());
		logger.debug(String.join("\n", untypedEdgesText));

		List<String> startNodesList = convertFactsToStartNodesList(unfilteredFacts);
		HashSet<String> rawStartNodesSet = new HashSet<>(startNodesList);

		List<String> rawStartNodesSetText =
			FileUtil.collectionToString(rawStartNodesSet);
		logger.debug("Printing raw start nodes...");
		logger.debug("number of raw start nodes: " + rawStartNodesSet.size());
		logger.debug(String.join("\n", rawStartNodesSetText));

		List<String> endNodesList = convertFactsToEndNodesList(unfilteredFacts);
		endNodesSet = new HashSet<>(endNodesList);
		
		List<String> endNodesSetText = FileUtil.collectionToString(endNodesSet);
		logger.debug("Printing end nodes...");
		logger.debug("number of end nodes: " + endNodesSet.size());
		logger.debug(String.join("\n", endNodesSetText));
		
		startNodesSet = new TreeSet<>(rawStartNodesSet);

		List<String> startNodesSetText = FileUtil.collectionToString(startNodesSet);
		logger.debug("Printing start nodes...");
		logger.debug("number of start nodes: " + startNodesSet.size());
		logger.debug(String.join("\n", startNodesSetText));
		
		allNodesSet = new HashSet<>(startNodesSet);
		allNodesSet.addAll(endNodesSet);
		
		stopWatch.stop();
		logger.debug("Elapsed time in milliseconds: " + stopWatch.getElapsedTime());
	}

	private static List<String> convertFactsToEndNodesList(
			List<List<String>> filteredFacts) {
		return filteredFacts.stream()
			.map((List<String> fact) -> fact.get(2))
			.collect(Collectors.toList());
	}

	private static List<String> convertFactsToStartNodesList(
			List<List<String>> filteredFacts) {
		return filteredFacts.stream()
			.map((List<String> fact) -> fact.get(1))
			.collect(Collectors.toList());
	}

	private static List<List<String>> convertFactsToUntypedEdges(
			List<List<String>> filteredFacts) {
		return filteredFacts.stream()
			.map((List<String> fact) -> Arrays.asList(fact.get(1), fact.get(2)))
			.collect(Collectors.toList());
	}
}