package edu.usc.softarch.arcade.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.softarch.arcade.facts.dependencies.RsfReader;

/**
 * @author joshua
 */
public class ClusterUtil {
	public static Map<String, Set<String>> buildDependenciesMap(String depsRsfFilename) {
		RsfReader.loadRsfDataFromFile(depsRsfFilename);
		Iterable<List<String>> depFacts = RsfReader.filteredRoutineFacts;
		
		Map<String,Set<String>> depMap = new HashMap<>();
		
		for (List<String> fact : depFacts) {
			String source = fact.get(1).trim();
			String target = fact.get(2).trim();
			Set<String> dependencies = null;
			if (depMap.containsKey(source)) {
				dependencies = depMap.get(source);
			}
			else {
				dependencies = new HashSet<>();
			}
			dependencies.add(target);
			depMap.put(source, dependencies);
		}
		return depMap;
	}
}
