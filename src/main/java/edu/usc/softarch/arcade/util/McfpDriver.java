package edu.usc.softarch.arcade.util;

import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Minimum-cost flow problem. Serves as a driver for resolving MCFP using
 * the JGraphT library, which in turn provides the input for the Change
 * Analyzer component of RecovAr.
 */
public class McfpDriver {
	//region ATTRIBUTES
	private final List<DefaultWeightedEdge> changeSet;
	//endregion

	//region CONSTRUCTORS
	public McfpDriver(String arch1Path, String arch2Path) throws IOException {
		this(new File(arch1Path), new File(arch2Path));
	}

	public McfpDriver(File arch1, File arch2) throws IOException {
		this(readArchitectureRsf(arch1), readArchitectureRsf(arch2));
	}

	public McfpDriver(Map<String, EnhancedSet<String>> arch1,
			Map<String, EnhancedSet<String>> arch2) {
		this.changeSet = solve(arch1, arch2);
	}
	//endregion

	//region ACCESSORS
	public List<DefaultWeightedEdge> getChangeSet() {
		return new ArrayList<>(this.changeSet);	}
	//endregion

	//region PROCESSING
	private Graph<String, DefaultWeightedEdge> makeGraph(
		Map<String, EnhancedSet<String>> arch1,
		Map<String, EnhancedSet<String>> arch2) {
		// Instantiate graph
		Graph<String, DefaultWeightedEdge> graph =
			new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		// Create source and sink vertices
		graph.addVertex("source");
		graph.addVertex("sink");

		// Set firstPass so edges to sink are only created once
		boolean firstPass = true;

		for (Map.Entry<String, EnhancedSet<String>> cluster1 : arch1.entrySet()) {
			// Create vertex for the source cluster
			String vertex1 = "source_" + cluster1.getKey();
			graph.addVertex(vertex1);

			//Create edge from source to new vertex1
			Graphs.addEdgeWithVertices(graph, "source", vertex1, 0);

			for (Map.Entry<String, EnhancedSet<String>> cluster2 : arch2.entrySet()) {
				// Calculate cost as the symmetric difference of the two clusters
				int cost =
					cluster1.getValue().symmetricDifference(cluster2.getValue()).size();

				// Create vertex for the target cluster
				String vertex2 = "target_" + cluster2.getKey();
				if (firstPass) {
					graph.addVertex(vertex2);
					// Create edge to sink if it's the first pass
					Graphs.addEdgeWithVertices(graph, vertex2, "sink", 0);
				}

				// Create edge between the clusters
				Graphs.addEdgeWithVertices(graph, vertex1, vertex2, cost);
			}
			// End first pass
			firstPass = false;
		}

		return graph;
	}

	private List<DefaultWeightedEdge> solve(
			Map<String, EnhancedSet<String>> arch1,
			Map<String, EnhancedSet<String>> arch2) {
		MinimumCostFlowProblem<String, DefaultWeightedEdge> problem =
			new MinimumCostFlowProblem.MinimumCostFlowProblemImpl<>(
				// First argument is the problem graph itself
				makeGraph(arch1, arch2),
				// Second argument is the function of supply and demand
				(String v) -> {
					if (v.equals("source")) return arch1.size();
					if (v.equals("sink")) return -arch2.size();
					return 0;
				},
				// Third argument is the function of capacity
				e -> 1);
		CapacityScalingMinimumCostFlow<String, DefaultWeightedEdge> mcfAlgorithm =
			new CapacityScalingMinimumCostFlow<>();
		mcfAlgorithm.getMinimumCostFlow(problem);

		return mcfAlgorithm.getFlowMap().entrySet().stream()
			// Remove all edges that were unselected by the algorithm
			.filter(e -> e.getValue() != 0)
			// Remove all edges from the dummy source
			.filter(e -> !problem.getGraph().getEdgeSource(e.getKey()).equals("source"))
			// Remove all edges from the dummy target
			.filter(e -> !problem.getGraph().getEdgeTarget(e.getKey()).equals("sink"))
			// Get only the edge information
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());
	}

	public static Map<String, EnhancedSet<String>> readArchitectureRsf(File file)
		throws IOException {
		Map<String, EnhancedSet<String>> architecture = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] splitLine = line.split(" ");
				architecture.putIfAbsent(splitLine[1], new EnhancedHashSet<>());
				EnhancedSet<String> cluster = architecture.get(splitLine[1]);
				cluster.add(splitLine[2]);
			}
		}

		return architecture;
	}
	//endregion
}
