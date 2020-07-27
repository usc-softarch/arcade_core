package edu.usc.softarch.arcade.util.convert;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class AuthToFullyQualifiedAuthConverter {

	/**
	 * @param args
	 */

	public static void main(String[] args) {
		String clustersFilename = args[0];
		String depsRsfFilename = args[1];
		String fullyQualifiedGroundTruthFilename = args[2];

		RsfReader.loadRsfDataFromFile(depsRsfFilename);
		List<List<String>> depFacts = RsfReader.unfilteredFacts;

		RsfReader.loadRsfDataFromFile(clustersFilename);
		List<List<String>> clusterFacts = RsfReader.unfilteredFacts;

		Map<String,Set<String>> clusterMap = new HashMap<String,Set<String>>();

		for (List<String> fact : clusterFacts) {
			String clusterName = fact.get(1);
			String entity = fact.get(2);
			if (clusterMap.get(clusterName) == null) {
				Set<String> entities = new HashSet<String>();
				entities.add(entity);
				clusterMap.put(clusterName,entities);
			}
			else {
				Set<String> entities = clusterMap.get(clusterName);
				entities.add(entity);
				clusterMap.put(clusterName,entities);
			}
		}

		// maps an entity to all of the possible classes it may match
		Map<String,Set<String>> matchingClassesMap = new HashMap<String,Set<String>>();

		findMatchingClasses(depFacts, clusterMap, matchingClassesMap);
		
		System.out.println(Joiner.on("\n").withKeyValueSeparator(":").join(matchingClassesMap));
		
		Set<String> unmatchedEntities = new HashSet<String>();
		
		
		for (String clusterName : clusterMap.keySet()) {
			for (String entity : clusterMap.get(clusterName)) {
				if (matchingClassesMap.get(entity) == null) {
					unmatchedEntities.add(entity);
				}
			}
		}
		System.out.println("List of entities not a source or target:");
		System.out.println(Joiner.on("\n").join(unmatchedEntities));
		
		try {
			FileWriter out = new FileWriter(fullyQualifiedGroundTruthFilename);
			for (String clusterName : clusterMap.keySet()) {
				for (String entity : clusterMap.get(clusterName)) {
					for (String matchingClass : matchingClassesMap.get(entity)) {
						out.write("contain " + clusterName + " " + matchingClass + "\n");
					}
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	/**
	 * 
	 * Fills {@code matchingClassesMap} with all the classes that the entity key may refer to 
	 * 
	 * @param depFacts
	 * @param clusterMap
	 * @param matchingClassesMap
	 */
	private static void findMatchingClasses(List<List<String>> depFacts,
			Map<String, Set<String>> clusterMap,
			Map<String, Set<String>> matchingClassesMap) {
		for (String clusterName : clusterMap.keySet()) {
			for (String entity : clusterMap.get(clusterName)) {
				for (List<String> depFact : depFacts) {
					String source = depFact.get(1).trim();
					String target = depFact.get(2).trim();
					String sourceClassNameOnly = source.substring(source.lastIndexOf(".")+1).split("\\$")[0].trim();
					String targetClassNameOnly = target.substring(target.lastIndexOf(".")+1).split("\\$")[0].trim();

					if (entity.trim().equals(sourceClassNameOnly) || entity.trim().equals(targetClassNameOnly) ) {
						String matchingClass = null;
						if (entity.trim().equals(sourceClassNameOnly)) {
							matchingClass = source;
						}
						else {
							matchingClass = target;
						}


						if (matchingClassesMap.get(entity) == null) {
							Set<String> classes = new HashSet<String>();
							classes.add(matchingClass);
							matchingClassesMap.put(entity.trim(),classes);
						}
						else {
							Set<String> classes = matchingClassesMap.get(entity);
							classes.add(matchingClass);
							matchingClassesMap.put(entity.trim(), classes);
						}

					}
				}
			}
		}
	}

}
