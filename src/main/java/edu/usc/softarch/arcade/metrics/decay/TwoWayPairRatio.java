package edu.usc.softarch.arcade.metrics.decay;

import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TwoWayPairRatio {
	public static double computeTwoWayPairRatio(String archPath, String depsPath)
			throws IOException {
		return computeTwoWayPairRatio(
			ReadOnlyArchitecture.readFromRsf(archPath), depsPath);
	}

	public static double computeTwoWayPairRatio(
			ReadOnlyArchitecture arch, String depsPath) throws IOException {
		return computeTwoWayPairRatio(arch.buildGraph(depsPath));
	}

	public static double computeTwoWayPairRatio(
			SimpleDirectedGraph<String, DefaultEdge> graph) {
		return detectTwoWayDeps(graph)
			/ (combinations(graph.vertexSet().size()));
	}

	public static double detectTwoWayDeps(
			SimpleDirectedGraph<String, DefaultEdge> graph) {
		Set<Map.Entry<String, String>> twoWayPairs = new HashSet<>();

		Set<DefaultEdge> edges = graph.edgeSet();
		for (DefaultEdge edge : edges) {
			String sourceCluster = graph.getEdgeSource(edge);
			String targetCluster = graph.getEdgeTarget(edge);
			// If the inverse edge exists...
			if (graph.containsEdge(targetCluster, sourceCluster)) {
				Map.Entry<String, String> pair =
					new AbstractMap.SimpleEntry<>(sourceCluster, targetCluster);
				Map.Entry<String, String> inversePair =
					new AbstractMap.SimpleEntry<>(targetCluster, sourceCluster);
				// ... and wasn't already counted
				if (!twoWayPairs.contains(pair) && !twoWayPairs.contains(inversePair))
					twoWayPairs.add(pair);
			}
		}

		return twoWayPairs.size();
	}

	private static long combinations(int n) {
		long coeff = 1;

		for (int i = n - 1; i <= n; i++)
			coeff *= i;
		for (int i = 1; i <= 2; i++)
			coeff /= i;

		return coeff;
	}
}
