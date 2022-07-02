package edu.usc.softarch.arcade.metrics.decay;

import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.ReadOnlyCluster;
import edu.usc.softarch.util.LabeledEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModularizationQuality {
	public static double computeMqRatio(
			String archPath, String depsPath) throws IOException {
		return computeMqRatio(ReadOnlyArchitecture.readFromRsf(archPath), depsPath);
	}

	public static double computeMqRatio(
			ReadOnlyArchitecture arch, String depsPath) throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> graph =
			arch.buildFullGraph(depsPath);

		Map<String, Double> clusterFactors = new HashMap<>();
		for (ReadOnlyCluster cluster : arch.values()) {
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
				clusterFactors.put(cluster.name, 0.0);
			else {
				double cf = (2.0 * internalEdges.size()) /
					(2.0 * internalEdges.size() + externalEdges.size());
				clusterFactors.put(cluster.name, cf);
			}
		}

		double mq = 0;
		for (Double cf : clusterFactors.values())
			mq += cf;
		return mq / arch.size();
	}
}
