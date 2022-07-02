package edu.usc.softarch.arcade.metrics.decay;

import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.Set;

public class ArchitecturalStability {
	public static double computeStability(String archPath, String depsPath)
			throws IOException {
		return computeStability(
			ReadOnlyArchitecture.readFromRsf(archPath), depsPath);
	}

	public static double computeStability(
			ReadOnlyArchitecture arch, String depsPath) throws IOException {
		return computeStability(arch.buildGraph(depsPath));
	}

	public static double computeStability(
			SimpleDirectedGraph<String, DefaultEdge> graph) {
		double stabilitySum = 0;

		for (String vertex : graph.vertexSet()) {
			Set<DefaultEdge> incomingEdges = graph.incomingEdgesOf(vertex);
			Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(vertex);
			double denom = incomingEdges.size() + outgoingEdges.size();

			if (denom !=0)
				stabilitySum += incomingEdges.size() / denom;
		}

		return stabilitySum / graph.vertexSet().size();
	}
}
