package edu.usc.softarch.arcade.facts.driver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Joiner;

public class GroundTruthValidator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String clustersFilename = args[0];
		String depsRsfFilename = args[1];
		String packageSelectExpr = args[2];
		
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
		
		Map<String,Set<String>> matchingClassesMap = new HashMap<String,Set<String>>();
		Map<String,Set<String>> errMatchClassesMap = new HashMap<String,Set<String>>();
		
		findMatchingClasses(depFacts, clusterMap, matchingClassesMap);
		
		for (Entry<String,Set<String>> entry : matchingClassesMap.entrySet()) {
			if (entry.getValue().size() == 0 || entry.getValue().size() > 1) {
				errMatchClassesMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		System.out.println("Printing all entries...");
		System.out.println(Joiner.on("\n").withKeyValueSeparator(":").join(matchingClassesMap));
		
		System.out.println("Printing erroneous entries...");
		System.out.println(Joiner.on("\n").withKeyValueSeparator(":").join(errMatchClassesMap));
		

	}

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
							matchingClass = sourceClassNameOnly;
						}
						else {
							matchingClass = targetClassNameOnly;
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
