package edu.usc.softarch.arcade.metrics.decay;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.util.LabeledEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ArchitecturalStability {
	public static double computeStability(String archPath, String depsPath)
			throws IOException {
		return computeStability(
			ReadOnlyArchitecture.readFromRsf(archPath), depsPath);
	}

	public static double computeStability(
			ReadOnlyArchitecture arch, String depsPath) throws IOException {
		return computeStability(arch, arch.buildFullGraph(depsPath));
	}

	public static double computeStability(ReadOnlyArchitecture arch,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		double stabilitySum = 0;

		for (ReadOnlyCluster cluster : arch.values())
			stabilitySum += computeStability(cluster, graph);

		return stabilitySum / arch.values().size();
	}

	public static double computeStability(ReadOnlyCluster cluster,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		double fanIn = computeFanIn(cluster, graph);
		double fanOut = computeFanOut(cluster, graph);

		double denom = fanIn + fanOut;
		return denom !=0 ? fanOut / denom : 0;
	}

	public static double computeFanIn(ReadOnlyCluster cluster,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		return computeIncoming(cluster, graph).size();
	}

	public static double computeFanOut(ReadOnlyCluster cluster,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		return computeOutgoing(cluster, graph).size();
	}

	private static Set<LabeledEdge> computeIncoming(ReadOnlyCluster cluster,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		Set<LabeledEdge> incomingEdges = new HashSet<>();

		for (String entity : cluster.getEntities()) {
			Set<LabeledEdge> entityEdges = graph.edgesOf(entity);
			incomingEdges.addAll(entityEdges.stream()
				// The edge is connected to another cluster
				.filter(e -> e.label.equals("external"))
				// The edge is going into this cluster
				.filter(e -> cluster.getEntities().contains(graph.getEdgeTarget(e)))
				.collect(Collectors.toSet()));
		}

		return incomingEdges;
	}

	private static Set<LabeledEdge> computeOutgoing(ReadOnlyCluster cluster,
		SimpleDirectedGraph<String, LabeledEdge> graph) {
		Set<LabeledEdge> outgoingEdges = new HashSet<>();

		for (String entity : cluster.getEntities()) {
			Set<LabeledEdge> entityEdges = graph.edgesOf(entity);
			outgoingEdges.addAll(entityEdges.stream()
				// The edge is connected to another cluster
				.filter(e -> e.label.equals("external"))
				// The edge is going out of this cluster
				.filter(e -> cluster.getEntities().contains(graph.getEdgeSource(e)))
				.collect(Collectors.toSet()));
		}

		return outgoingEdges;
	}
}
