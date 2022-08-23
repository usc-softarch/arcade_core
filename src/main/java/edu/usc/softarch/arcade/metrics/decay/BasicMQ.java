package edu.usc.softarch.arcade.metrics.decay;

import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import edu.usc.softarch.util.LabeledEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;

public class BasicMQ {
	public static double computeBasicMq(
			String archPath, String depsPath) throws IOException {
		return computeBasicMq(ReadOnlyArchitecture.readFromRsf(archPath), depsPath);
	}

	public static double computeBasicMq(
			ReadOnlyArchitecture arch, String depsPath) throws IOException {
		SimpleDirectedGraph<String, LabeledEdge> graph =
			arch.buildFullGraph(depsPath);
		return computeBasicMq(arch, graph);
	}

	public static double computeBasicMq(ReadOnlyArchitecture arch,
			SimpleDirectedGraph<String, LabeledEdge> graph) {
		double intraConnectivity =
			IntraConnectivity.computeIntraConnectivity(arch, graph);
		double interConnectivity =
			InterConnectivity.computeInterConnectivity(arch, graph);

		return computeBasicMq(intraConnectivity, interConnectivity);
	}

	public static double computeBasicMq(
			double intraConnectivity, double interConnectivity) {
		return intraConnectivity - interConnectivity;
	}
}
