package edu.usc.softarch.arcade.facts.design;

import edu.usc.softarch.util.EnhancedHashSet;
import edu.usc.softarch.util.EnhancedSet;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class McfpDriver {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		McfpDriver driver = new McfpDriver(args[0], args[1]);

		System.out.println(driver.changeSet);
	}
	//endregion

	//region ATTRIBUTES
	List<DefaultWeightedEdge> changeSet;
	//endregion

	//region CONSTRUCTORS
	public McfpDriver(String path1, String path2) throws IOException {
		Map<String, EnhancedSet<String>> arch1 = readArchitectureRsf(path1);
		Map<String, EnhancedSet<String>> arch2 = readArchitectureRsf(path2);

		balanceArchitectures(arch1, arch2);
		this.changeSet = solve(arch1, arch2);
	}
	//endregion

	//region PROCESSING
	private void balanceArchitectures(
		Map<String, EnhancedSet<String>> arch1,
		Map<String, EnhancedSet<String>> arch2) {
		int dummyCount = Math.abs(arch1.size() - arch2.size());
		Map<String, EnhancedSet<String>> smallerArch =
			arch1.size() < arch2.size() ? arch1 : arch2;

		for (int i = 0; i < dummyCount; i++)
			smallerArch.put("dummy" + i, new EnhancedHashSet<>());
	}

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
	//endregion

	//region SERIALIZATION
	private Map<String, EnhancedSet<String>> readArchitectureRsf(String path)
			throws IOException {
		Map<String, EnhancedSet<String>> architecture = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
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
