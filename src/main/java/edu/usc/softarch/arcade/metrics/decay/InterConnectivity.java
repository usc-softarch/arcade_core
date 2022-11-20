package edu.usc.softarch.arcade.metrics.decay;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.util.LabeledEdge;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterConnectivity {
	public static double computeInterConnectivity(
			String archPath, String depsPath) throws IOException {
		return computeInterConnectivity(
			ReadOnlyArchitecture.readFromRsf(archPath), depsPath);
	}

	public static double computeInterConnectivity(
			ReadOnlyArchitecture arch, String depsPath) throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> graph =
			arch.buildFullGraph(depsPath);
		return computeInterConnectivity(arch, graph);
	}

	public static double computeInterConnectivity(ReadOnlyArchitecture arch,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		double result = 0.0;
		List<ReadOnlyCluster> clusters = new ArrayList<>(arch.values());

		for (int i = 0; i < arch.size(); i++) {
			ReadOnlyCluster clusterLow = clusters.get(i);

			for (int j = i + 1; j < arch.size(); j++) {
				ReadOnlyCluster clusterHigh = clusters.get(j);
				Collection<String> entitiesHigh = clusterHigh.getEntities();
				Set<LabeledEdge> externalEdges = new HashSet<>();

				for (String entityLow : clusterLow.getEntities()) {
					// Get all edges that touch the entity from the low index cluster
					Set<LabeledEdge> edges = graph.edgesOf(entityLow);
					externalEdges.addAll(edges.stream()
						.filter(e -> e.label.equals("external"))
						// The other end of the edge is an entity from high index cluster
						.filter(e -> entitiesHigh.contains(graph.getEdgeTarget(e))
							|| entitiesHigh.contains(graph.getEdgeSource(e)))
						.collect(Collectors.toSet()));
				}

				result += externalEdges.size() /
					(2.0 * clusterLow.size() * clusterHigh.size());
			}
		}

		return result / ((arch.size() * (arch.size() - 1.0)) / 2);
	}

	public static DescriptiveStatistics computeInterConnectivity(
			ReadOnlyCluster cluster, ReadOnlyArchitecture arch,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		List<Double> results = new ArrayList<>();
		List<ReadOnlyCluster> clusters = new ArrayList<>(arch.values());
		clusters.remove(cluster);

		for (ReadOnlyCluster otherCluster : clusters) {
			Collection<String> entitiesHigh = otherCluster.getEntities();
			Set<LabeledEdge> externalEdges = new HashSet<>();

			for (String entityLow : cluster.getEntities()) {
				// Get all edges that touch the entity from the low index cluster
				Set<LabeledEdge> edges = graph.edgesOf(entityLow);
				externalEdges.addAll(edges.stream()
					.filter(e -> e.label.equals("external"))
					// The other end of the edge is an entity from high index cluster
					.filter(e -> entitiesHigh.contains(graph.getEdgeTarget(e))
						|| entitiesHigh.contains(graph.getEdgeSource(e)))
					.collect(Collectors.toSet()));
			}

			results.add(externalEdges.size() /
				(2.0 * cluster.size() * otherCluster.size()));
		}

		// Need to unbox the list because DescriptiveStatistics is dumb
		double[] resultsArray = Stream.of(results.toArray(new Double[0]))
			.mapToDouble(Double::doubleValue).toArray();
		return new DescriptiveStatistics(resultsArray);
	}
}
