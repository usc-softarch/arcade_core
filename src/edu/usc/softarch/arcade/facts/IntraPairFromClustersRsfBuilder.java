package edu.usc.softarch.arcade.facts;

import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.usc.softarch.arcade.facts.driver.RsfReader;
import edu.usc.softarch.arcade.util.DebugUtil;

public class IntraPairFromClustersRsfBuilder {
	static Logger logger = Logger.getLogger(IntraPairFromClustersRsfBuilder.class);
	
	public static HashSet<HashSet<String>>  buildIntraPairsFromClustersRsf(String rsfFilename) {
		List<List<String>> facts = RsfReader.extractFactsFromRSF(rsfFilename);
		TreeMap<String, HashSet<String>> clusterMap = buildClusterMapFromRsfFile(facts);
		TreeMap<String, HashSet<HashSet<String>>> clusterIntraPairsMap = buildClusterIntraPairsMap(clusterMap);
		HashSet<HashSet<String>> allIntraPairs = buildAllIntraPairs(clusterIntraPairsMap);
		
		return allIntraPairs;
	}

	private static HashSet<HashSet<String>> buildAllIntraPairs(
			TreeMap<String, HashSet<HashSet<String>>> clusterIntraPairsMap) {
		HashSet<HashSet<String>> allIntraPairs = new HashSet<HashSet<String>>();
		for (String clusterNumber : clusterIntraPairsMap.keySet()) {
			for (HashSet<String> intraPair : clusterIntraPairsMap.get(clusterNumber)) {
				allIntraPairs.add(intraPair);
			}
		}
		return allIntraPairs;
	}

	private static TreeMap<String, HashSet<HashSet<String>>> buildClusterIntraPairsMap(
			TreeMap<String, HashSet<String>> clusterMap) {
		TreeMap<String, HashSet<HashSet<String>>> clusterIntraPairsMap = new TreeMap<String, HashSet<HashSet<String>>>();
		for (String currClusterNumber : clusterMap.keySet()) {
			HashSet<String> elements = clusterMap.get(currClusterNumber);
			
			for (String element1 : elements) {
				for (String element2 : elements) {
					/*if(element1.equals(element2)) {
						continue;
					}*/
					if (clusterIntraPairsMap.containsKey(currClusterNumber)) {
						HashSet<HashSet<String>> intraPairs = clusterIntraPairsMap.get(currClusterNumber);
						HashSet<String> intraPair = new HashSet<String>();
						intraPair.add(element1);
						intraPair.add(element2);
						//DebugUtil.checkIntraPairSize(intraPair,element1,element2);
						intraPairs.add(intraPair);
					}
					else {
						HashSet<HashSet<String>> intraPairs = new HashSet<HashSet<String>>();
						HashSet<String> intraPair = new HashSet<String>();
						intraPair.add(element1);
						intraPair.add(element2);
						intraPairs.add(intraPair);
						//DebugUtil.checkIntraPairSize(intraPair,element1,element2);
						clusterIntraPairsMap.put(currClusterNumber, intraPairs);
					}
					
				}
			}
		}
		
		logger.debug("Printing intrapairs for clusters from rsf file...");
		for (String currClusterNumber : clusterMap.keySet()) {
			HashSet<HashSet<String>> intraPairs = clusterIntraPairsMap.get(currClusterNumber);
			logger.debug("Intrapairs for cluster number: " + currClusterNumber);
			for (HashSet<String> intraPair : intraPairs) {
				logger.debug("\t" + intraPair);
			}
		}
		
		return clusterIntraPairsMap;
	}

	private static TreeMap<String, HashSet<String>> buildClusterMapFromRsfFile(
			List<List<String>> facts) {
		TreeMap<String,HashSet<String>> clusterMap = new TreeMap<String,HashSet<String>>();
		
		for (List<String> fact : facts) {
			String currClusterId = fact.get(1);
			String entity = fact.get(2);
			if (clusterMap.containsKey(currClusterId)) {
				HashSet<String> elements = clusterMap.get(currClusterId);
				elements.add(entity);
			}
			else {
				HashSet<String> elements = new HashSet<String>();
				elements.add(entity);
				clusterMap.put(currClusterId, elements);
			}
		}
		
		logger.debug("Printing clusters obtained from clusters rsf file...");
		for (String currClusterNumber : clusterMap.keySet()) {
			HashSet<String> elements = clusterMap.get(currClusterNumber);
			logger.debug("Current cluster number: " + currClusterNumber);
			for (String element : elements) {
				logger.debug("\t" + element);
			}
		}
		return clusterMap;
	}
}
