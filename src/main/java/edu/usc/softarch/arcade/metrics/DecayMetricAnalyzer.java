package edu.usc.softarch.arcade.metrics;

import java.io.IOException;

import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.metrics.decay.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.usc.softarch.arcade.util.FileUtil;

public class DecayMetricAnalyzer {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		String clusterPath = FileUtil.tildeExpandPath(args[0]);
		String depsPath = FileUtil.tildeExpandPath(args[1]);

		run(clusterPath, depsPath);
	}

	public static double[] run(String clusterPath, String depsPath)
			throws IOException {
		SimpleDirectedGraph<String, DefaultEdge> graph =
			ReadOnlyArchitecture.readFromRsf(clusterPath)
				.buildGraph(depsPath);
		double[] result = new double[4];

		result[0] = RatioCohesiveInteractions.detectRci(graph);
		result[1] = TwoWayPairRatio.computeTwoWayPairRatio(graph);
		result[2] = ArchitecturalStability.computeStability(graph);
		result[3] = ModularizationQuality.computeMqRatio(clusterPath, depsPath);

		return result;
	}
	//endregion
}
