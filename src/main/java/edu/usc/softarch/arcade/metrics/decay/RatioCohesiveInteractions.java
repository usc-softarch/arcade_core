package edu.usc.softarch.arcade.metrics.decay;

import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.Set;

public class RatioCohesiveInteractions {
	public static double detectRci(String archPath, String depsPath)
			throws IOException {
		return detectRci(ReadOnlyArchitecture.readFromRsf(archPath), depsPath);
	}

	public static double detectRci(ReadOnlyArchitecture arch, String depsPath)
			throws IOException {
		return detectRci(arch.buildGraph(depsPath));
	}

	public static double detectRci(
			SimpleDirectedGraph<String, DefaultEdge> graph) {
		Set<DefaultEdge> edges = graph.edgeSet();
		Set<String> vertices = graph.vertexSet();

		double potentialEdgeCount = vertices.size() * (vertices.size() - 1);
		return edges.size() / potentialEdgeCount;
	}
}
