package edu.usc.softarch.arcade.metrics.decay;

import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.ReadOnlyCluster;
import edu.usc.softarch.util.LabeledEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class IntraConnectivity {
	public static double computeIntraConnectivity(
			String archPath, String depsPath) throws IOException {
		return computeIntraConnectivity(
			ReadOnlyArchitecture.readFromRsf(archPath), depsPath);
	}

	public static double computeIntraConnectivity(
			ReadOnlyArchitecture arch, String depsPath) throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> graph =
			arch.buildFullGraph(depsPath);
		return computeIntraConnectivity(arch, graph);
	}

	public static double computeIntraConnectivity(ReadOnlyArchitecture arch,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		double result = 0.0;

		for (ReadOnlyCluster cluster : arch.values()) {
			Set<LabeledEdge> internalEdges = new HashSet<>();

			for (String entity : cluster.getEntities()) {
				Set<LabeledEdge> edges = graph.edgesOf(entity);
				internalEdges.addAll(edges.stream()
					.filter(e -> e.label.equals("internal")).collect(Collectors.toSet()));
			}

			result += internalEdges.size() / Math.pow(cluster.size(), 2);
		}

		return result / arch.size();
	}
}
