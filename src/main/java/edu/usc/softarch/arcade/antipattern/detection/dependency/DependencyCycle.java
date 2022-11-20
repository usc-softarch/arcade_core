package edu.usc.softarch.arcade.antipattern.detection.dependency;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DependencyCycle {
	public static SmellCollection detect(ReadOnlyArchitecture arch,
		String depsPath, double lengthThreshold) throws IOException {
		SimpleDirectedGraph<String, DefaultEdge> graph = arch.buildGraph(depsPath);

		return detect(graph, lengthThreshold);
	}

	public static SmellCollection detect(
			ReadOnlyArchitecture arch, String depsPath) throws IOException {
		SimpleDirectedGraph<String, DefaultEdge> graph = arch.buildGraph(depsPath);

		return detect(graph);
	}

	public static SmellCollection detect(
			SimpleDirectedGraph<String, DefaultEdge> graph) {
		return detect(graph, 2);
	}

	public static SmellCollection detect(
			SimpleDirectedGraph<String, DefaultEdge> graph, double lengthThreshold) {
		SmellCollection result = new SmellCollection();

		KosarajuStrongConnectivityInspector<String, DefaultEdge> inspector =
			new KosarajuStrongConnectivityInspector<>(graph);
		List<Set<String>> connectedSets = inspector.stronglyConnectedSets();

		for (Set<String> connectedSet : connectedSets)
			if (connectedSet.size() > lengthThreshold) {
				Smell bdc = new Smell(Smell.SmellType.bdc);
				bdc.addClusterCollection(connectedSet);
				result.add(bdc);
			}

		return result;
	}
}
