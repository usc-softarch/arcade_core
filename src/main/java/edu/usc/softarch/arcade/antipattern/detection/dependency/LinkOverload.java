package edu.usc.softarch.arcade.antipattern.detection.dependency;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.clustering.ReadOnlyArchitecture;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.util.Arrays;

public class LinkOverload {
	public static SmellCollection detect(ReadOnlyArchitecture arch,
			String depsPath, double overloadFactor) throws IOException {
		SimpleDirectedGraph<String, DefaultEdge> graph = arch.buildGraph(depsPath);

		return detect(graph, overloadFactor);
	}

	public static SmellCollection detect(
			ReadOnlyArchitecture arch, String depsPath) throws IOException {
		SimpleDirectedGraph<String, DefaultEdge> graph = arch.buildGraph(depsPath);

		return detect(graph);
	}

	public static SmellCollection detect(
			SimpleDirectedGraph<String, DefaultEdge> graph) {
		return detect(graph, 1.5);
	}

	public static SmellCollection detect(
			SimpleDirectedGraph<String, DefaultEdge> graph, double overloadFactor) {
		SmellCollection result = new SmellCollection();

		// Load the in and out degrees of all vertices into arrays
		double[] inDegrees = graph.vertexSet().stream()
			.mapToDouble(graph::inDegreeOf).toArray();
		double[] outDegrees = graph.vertexSet().stream()
			.mapToDouble(graph::outDegreeOf).toArray();

		// Add them together
		double[] linkDegrees = new double[inDegrees.length];
		for (int i = 0; i < linkDegrees.length; i++)
			linkDegrees[i] = inDegrees[i] + outDegrees[i];

		// Get means
		double[] means = new double[3];
		means[0] = Arrays.stream(inDegrees).sum() / inDegrees.length;
		means[1] = Arrays.stream(outDegrees).sum() / outDegrees.length;
		means[2] = Arrays.stream(linkDegrees).sum() / linkDegrees.length;

		// Get standard deviations
		double[] stdev = new double[3];
		StandardDeviation stdDev = new StandardDeviation();
		stdev[0] = stdDev.evaluate(inDegrees);
		stdev[1] = stdDev.evaluate(outDegrees);
		stdev[2] = stdDev.evaluate(linkDegrees);

		// Check each cluster against the average
		for (String cluster : graph.vertexSet()) {
			int inDegree = graph.inDegreeOf(cluster);
			int outDegree = graph.outDegreeOf(cluster);

			boolean inOverload = inDegree >	means[0] + overloadFactor * stdev[0];
			boolean outOverload = outDegree >	means[1] + overloadFactor * stdev[1];
			boolean linkOverload = inDegree + outDegree >
				means[2] + overloadFactor * stdev[2];

			if (inOverload || outOverload || linkOverload) {
				Smell buo = new Smell(Smell.SmellType.buo);
				buo.addCluster(cluster);
				result.add(buo);
			}
		}

		return result;
	}
}
