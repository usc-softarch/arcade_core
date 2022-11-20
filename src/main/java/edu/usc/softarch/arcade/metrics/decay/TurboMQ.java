package edu.usc.softarch.arcade.metrics.decay;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.util.LabeledEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TurboMQ {
	public static double computeTurboMq(
			String archPath, String depsPath) throws IOException {
		return computeTurboMq(ReadOnlyArchitecture.readFromRsf(archPath), depsPath);
	}

	public static double computeTurboMq(
			ReadOnlyArchitecture arch, String depsPath) throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> graph =
			arch.buildFullGraph(depsPath);
		return computeTurboMq(arch, graph);
	}

	public static double computeTurboMq(ReadOnlyArchitecture arch,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		Map<String, Double> clusterFactors = new HashMap<>();
		for (ReadOnlyCluster cluster : arch.values())
			clusterFactors.put(cluster.name, computeClusterFactor(cluster, graph));

		double mq = 0;
		for (Double cf : clusterFactors.values())
			mq += cf;
		return mq / arch.size();
	}

	public static double computeClusterFactor(ReadOnlyCluster cluster,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		Set<LabeledEdge> internalEdges = new HashSet<>();
		Set<LabeledEdge> externalEdges = new HashSet<>();

		for (String entity : cluster.getEntities()) {
			Set<LabeledEdge> edges = graph.edgesOf(entity);
			internalEdges.addAll(edges.stream()
				.filter(e -> e.label.equals("internal")).collect(Collectors.toSet()));
			externalEdges.addAll(edges.stream()
				.filter(e -> e.label.equals("external")).collect(Collectors.toSet()));
		}

		if (internalEdges.isEmpty())
			return 0.0;
		else
			return (2.0 * internalEdges.size()) /
				(2.0 * internalEdges.size() + externalEdges.size());
	}
}
