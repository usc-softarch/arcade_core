package edu.usc.softarch.arcade.facts.dependencies;

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

public class RsfReader {
	public static Set<List<String>> untypedEdgesSet;
	public static Set<String> startNodesSet;
	public static Iterable<List<String>> filteredRoutineFacts;
	public static Set<String> endNodesSet;
	public static Set<String> allNodesSet;
	public static List<List<String>> unfilteredFaCtS;
	
	public static List<List<String>> extractFactsFromRSF(String rsfFilename) {
		// List of facts extracted from RSF File
		List<List<String>> facts = new ArrayList<>();

		try (BufferedReader in = new BufferedReader(new FileReader(rsfFilename))) {
			String line;

			while ((line = in.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;

				Scanner s = new Scanner(line);
				String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";

				String arcType = s.findInLine(expr);
				String startNode = s.findInLine(expr);
				String endNode = s.findInLine(expr);
				List<String> fact = Arrays.asList(arcType, startNode, endNode);
				facts.add(fact);

				if (s.findInLine(expr) != null) {
					throw new RuntimeException("Found non-triple in file: " + line);
				}
				s.close();
			}
		} catch (IOException e) {
			e.printStackTrace(); //TODO Treat properly
		}
		return facts;
	}

	public static void loadRsfDataFromFile(String rsfFilename) {
		unfilteredFaCtS = extractFactsFromRSF(rsfFilename);

		filteredRoutineFacts = unfilteredFaCtS;

		List<List<String>> untypedEdges = convertFactsToUntypedEdges(unfilteredFaCtS);
		untypedEdgesSet = new HashSet<>(untypedEdges);

		List<String> startNodesList = convertFactsToStartNodesList(unfilteredFaCtS);
		HashSet<String> rawStartNodesSet = new HashSet<>(startNodesList);

		List<String> endNodesList = convertFactsToEndNodesList(unfilteredFaCtS);
		endNodesSet = new HashSet<>(endNodesList);

		startNodesSet = new TreeSet<>(rawStartNodesSet);

		allNodesSet = new HashSet<>(startNodesSet);
		allNodesSet.addAll(endNodesSet);
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
