package edu.usc.softarch.arcade.facts.driver;

import java.util.List;

import edu.usc.softarch.arcade.clustering.ConcernClusterArchitecture;
import edu.usc.softarch.arcade.facts.ConcernCluster;

public class ConcernClusterRsf {
	private static boolean containsClusterWithName(
			ConcernClusterArchitecture clusters, String clusterName) {
		for (ConcernCluster cluster : clusters) {
			if (cluster.getName().equals(clusterName)) {
				return true;
			}
		}
		return false;
	}
	
	public static ConcernClusterArchitecture extractConcernClustersFromRsfFile(String rsfFilename) {
		RsfReader.loadRsfDataFromFile(rsfFilename);
		Iterable<List<String>> clusterFacts = RsfReader.filteredRoutineFacts;
		ConcernClusterArchitecture clusters = new ConcernClusterArchitecture();
		for (List<String> fact : clusterFacts) {
			String clusterName = fact.get(1).trim();
			String element = fact.get(2).trim();
			if (containsClusterWithName(clusters,clusterName)) {
				for (ConcernCluster cluster : clusters) {
					if (cluster.getName().equals(clusterName)) {
						cluster.addEntity(element);
					}
				}
			}
			else {
				ConcernCluster newCluster = new ConcernCluster();
				newCluster.setName(clusterName);
				newCluster.addEntity(element);
				clusters.add(newCluster);
			}
		}
		return clusters;
	}
}
