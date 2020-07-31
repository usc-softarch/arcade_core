package edu.usc.softarch.arcade.facts;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class IntraPairFromClustersRsfBuilder {
	static Logger logger = Logger.getLogger(IntraPairFromClustersRsfBuilder.class);
	
	public static Set<Set<String>>  buildIntraPairsFromClustersRsf(String rsfFilename) {
		List<List<String>> facts = RsfReader.extractFactsFromRSF(rsfFilename);
		Map<String, Set<String>> clusterMap = buildClusterMapFromRsfFile(facts);
		Map<String, Set<Set<String>>> clusterIntraPairsMap = buildClusterIntraPairsMap(clusterMap);
		Set<Set<String>> allIntraPairs = buildAllIntraPairs(clusterIntraPairsMap);
		
		return allIntraPairs;
	}

	private static Set<Set<String>> buildAllIntraPairs(
			Map<String, Set<Set<String>>> clusterIntraPairsMap) {
		Set<Set<String>> allIntraPairs = new HashSet<>();
		for (String clusterNumber : clusterIntraPairsMap.keySet()) {
			for (Set<String> intraPair : clusterIntraPairsMap.get(clusterNumber)) {
				allIntraPairs.add(intraPair);
			}
		}
		return allIntraPairs;
	}

	private static Map<String, Set<Set<String>>> buildClusterIntraPairsMap(
			Map<String, Set<String>> clusterMap) {
		Map<String, Set<Set<String>>> clusterIntraPairsMap = new TreeMap<>();
		for (String currClusterNumber : clusterMap.keySet()) {
			Set<String> elements = clusterMap.get(currClusterNumber);
			for (String element1 : elements) {
				for (String element2 : elements) {
					if (clusterIntraPairsMap.containsKey(currClusterNumber)) {
						Set<Set<String>> intraPairs = clusterIntraPairsMap.get(currClusterNumber);
						Set<String> intraPair = new HashSet<>();
						intraPair.add(element1);
						intraPair.add(element2);
						intraPairs.add(intraPair);
					}
					else {
						Set<Set<String>> intraPairs = new HashSet<>();
						Set<String> intraPair = new HashSet<>();
						intraPair.add(element1);
						intraPair.add(element2);
						intraPairs.add(intraPair);
						clusterIntraPairsMap.put(currClusterNumber, intraPairs);
					}
				}
			}
		}
		
		logger.debug("Printing intrapairs for clusters from rsf file...");
		for (String currClusterNumber : clusterMap.keySet()) {
			Set<Set<String>> intraPairs = clusterIntraPairsMap.get(currClusterNumber);
			logger.debug("Intrapairs for cluster number: " + currClusterNumber);
			for (Set<String> intraPair : intraPairs) {
				logger.debug("\t" + intraPair);
			}
		}
		
		return clusterIntraPairsMap;
	}

	private static Map<String, Set<String>> buildClusterMapFromRsfFile(
			List<List<String>> facts) {
		Map<String,Set<String>> clusterMap = new TreeMap<>();
		
		for (List<String> fact : facts) {
			String currClusterId = fact.get(1);
			String entity = fact.get(2);
			if (clusterMap.containsKey(currClusterId)) {
				Set<String> elements = clusterMap.get(currClusterId);
				elements.add(entity);
			}
			else {
				Set<String> elements = new HashSet<>();
				elements.add(entity);
				clusterMap.put(currClusterId, elements);
			}
		}
		
		logger.debug("Printing clusters obtained from clusters rsf file...");
		for (String currClusterNumber : clusterMap.keySet()) {
			Set<String> elements = clusterMap.get(currClusterNumber);
			logger.debug("Current cluster number: " + currClusterNumber);
			for (String element : elements) {
				logger.debug("\t" + element);
			}
		}
		return clusterMap;
	}
}
