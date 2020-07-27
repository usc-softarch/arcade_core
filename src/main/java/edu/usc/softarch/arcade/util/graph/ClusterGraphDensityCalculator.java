package edu.usc.softarch.arcade.util.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;

import edu.usc.softarch.arcade.clustering.util.ClusterUtil;
import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class ClusterGraphDensityCalculator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String depsFilename = args[0];
		String clustersFilename = args[1];
		
		RsfReader.loadRsfDataFromFile(depsFilename);
		List<List<String>> depFacts = RsfReader.unfilteredFacts;
		
		RsfReader.loadRsfDataFromFile(clustersFilename);
		List<List<String>> clusterFacts = RsfReader.unfilteredFacts;
		
		Map<String,Set<String>> clusterMap = ClusterUtil.buildClusterMap(clusterFacts);
		
		Set<List<String>> edges = ClusterUtil.buildClusterEdges(clusterMap, depFacts);
		
		int numEdges = edges.size();
		int numVertices = clusterMap.keySet().size();
		
		double graphDensity = (double)numEdges / (double)(numVertices* (numVertices-1));
		
		System.out.println(Joiner.on("\n").join(edges));
		System.out.println("no. of edges: " + numEdges);
		System.out.println("no. of vertices: " + numVertices);
		System.out.println("graph density: " + graphDensity);

	}

}
