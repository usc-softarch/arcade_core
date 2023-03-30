package edu.usc.softarch.arcade.metrics.evolution;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.metrics.RenameFixer;
import edu.usc.softarch.arcade.util.CLI;
import edu.usc.softarch.arcade.util.McfpDriver;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WeightedEdgeA2a {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		Map<String, String> parsedArgs = CLI.parseArguments(args);
		run(parsedArgs.get("sourcersf"), parsedArgs.get("targetrsf"),
			parsedArgs.get("sourcedeps"), parsedArgs.get("targetdeps"));
	}

	public static double run(String sourceRsf, String targetRsf,
			String sourceDeps, String targetDeps) throws IOException {
		return (new WeightedEdgeA2a(
			sourceRsf, targetRsf, sourceDeps, targetDeps)).solve();
	}

	public static double run(File sourceRsf, File targetRsf,
			String sourceDeps, String targetDeps) throws IOException {
		return (new WeightedEdgeA2a(
			sourceRsf, targetRsf, sourceDeps, targetDeps)).solve();
	}

	public static double run(File sourceRsf, File targetRsf, String sourceDeps,
			String targetDeps, double simThreshold) throws IOException {
		return (new WeightedEdgeA2a(sourceRsf, targetRsf,
			sourceDeps, targetDeps, simThreshold)).solve();
	}

	public static double run(File sourceRsf, File targetRsf, String sourceDeps,
			String targetDeps, double simThreshold, McfpDriver driver)
			throws IOException {
		return (new WeightedEdgeA2a(sourceRsf, targetRsf,
			sourceDeps, targetDeps, simThreshold, driver)).solve();
	}

	public static double run(ReadOnlyArchitecture ra1, ReadOnlyArchitecture ra2,
			String sourceDeps, String targetDeps, double simThreshold,
			McfpDriver driver) throws IOException {
		return (new WeightedEdgeA2a(ra1, ra2, sourceDeps,
			targetDeps, simThreshold, driver)).solve();
	}
	//endregion

	//region ATTRIBUTES
	private double edgea2a;
	private final Cvg cvg;
	private final Map<String, String> matches;
	private final SimpleDirectedWeightedGraph<
		String, DefaultWeightedEdge> sourceGraph;
	private final SimpleDirectedWeightedGraph<
		String, DefaultWeightedEdge> targetGraph;
	//endregion

	//region CONSTRUCTORS
	public WeightedEdgeA2a(String sourceRsf, String targetRsf,
			String sourceDeps, String targetDeps) throws IOException {
		this(new File(sourceRsf), new File(targetRsf), sourceDeps, targetDeps);
	}

	public WeightedEdgeA2a(File sourceRsf, File targetRsf,
			String sourceDeps, String targetDeps) throws IOException {
		this(sourceRsf, targetRsf, sourceDeps, targetDeps, 0.66);
	}

	public WeightedEdgeA2a(File sourceRsf, File targetRsf, String sourceDeps,
			String targetDeps, double simThreshold) throws IOException {
		this.edgea2a = -1;
		ReadOnlyArchitecture sourceClusters =
			ReadOnlyArchitecture.readFromRsf(sourceRsf);
		ReadOnlyArchitecture targetClusters =
			ReadOnlyArchitecture.readFromRsf(targetRsf);
		RenameFixer.fix(sourceClusters, targetClusters);
		this.cvg = new Cvg(sourceClusters, targetClusters, simThreshold, 1.0);
		this.matches =
			new McfpDriver(sourceClusters, targetClusters).getMatchSet();
		this.sourceGraph = sourceClusters.buildWeightedGraph(sourceDeps, true);
		this.targetGraph = targetClusters.buildWeightedGraph(targetDeps, true);
	}

	public WeightedEdgeA2a(ReadOnlyArchitecture ra1, ReadOnlyArchitecture ra2,
			String sourceDeps, String targetDeps, double simThreshold,
			McfpDriver driver) throws IOException {
		this.edgea2a = -1;
		this.cvg = new Cvg(ra1, ra2, simThreshold, 1.0);
		this.matches = driver.getMatchSet();
		this.sourceGraph = ra1.buildWeightedGraph(sourceDeps, true);
		this.targetGraph = ra2.buildWeightedGraph(targetDeps, true);
	}

	public WeightedEdgeA2a(File sourceRsf, File targetRsf, String sourceDeps,
		String targetDeps, double simThreshold, McfpDriver driver)
		throws IOException {
		this.edgea2a = -1;
		ReadOnlyArchitecture sourceClusters =
			ReadOnlyArchitecture.readFromRsf(sourceRsf);
		ReadOnlyArchitecture targetClusters =
			ReadOnlyArchitecture.readFromRsf(targetRsf);
		RenameFixer.fix(sourceClusters, targetClusters);
		this.cvg = new Cvg(sourceClusters, targetClusters, simThreshold, 1.0);
		this.matches = driver.getMatchSet();
		this.sourceGraph = sourceClusters.buildWeightedGraph(sourceDeps, true);
		this.targetGraph = targetClusters.buildWeightedGraph(targetDeps, true);
	}
	//endregion

	//region PROCESSING
	private double numerator() {
		int result = 0;

		// Get the match sets from CVG to know which clusters to discard
		Set<ReadOnlyCluster> sourceMatches = this.cvg.getSourceMatches();
		Map<String, ReadOnlyCluster> sourceMatchMap = sourceMatches.stream()
			.collect(Collectors.toMap(c -> c.name, c -> c));
		Set<ReadOnlyCluster> targetMatches = this.cvg.getTargetMatches();
		Map<String, ReadOnlyCluster> targetMatchMap = targetMatches.stream()
			.collect(Collectors.toMap(c -> c.name, c -> c));

		Set<DefaultWeightedEdge> targetEdges =
			new HashSet<>(this.targetGraph.edgeSet());

		for (DefaultWeightedEdge sourceEdge : this.sourceGraph.edgeSet()) {
			// Get the names of the involved clusters in each versions
			String sourceC1Name = this.sourceGraph.getEdgeSource(sourceEdge);
			String sourceC2Name = this.sourceGraph.getEdgeTarget(sourceEdge);
			String targetC1Name = this.matches.get(sourceC1Name);
			String targetC2Name = this.matches.get(sourceC2Name);

			// Check if any of the involved clusters should be discarded
			if (!sourceMatchMap.containsKey(sourceC1Name)
					|| !sourceMatchMap.containsKey(sourceC2Name)
					|| !targetMatchMap.containsKey(targetC1Name)
					|| !targetMatchMap.containsKey(targetC2Name)) {
				result += sourceGraph.getEdgeWeight(sourceEdge);
				continue;
			}

			// Check if the edge still exists in the latter version
			DefaultWeightedEdge targetEdge =
				this.targetGraph.getEdge(targetC1Name, targetC2Name);
			if (targetEdge != null) {
				result += Math.abs(sourceGraph.getEdgeWeight(sourceEdge)
					- targetGraph.getEdgeWeight(targetEdge));
				targetEdges.remove(targetEdge);
			}
		}

		// Add all unmatched edges left in the latter version
		for (DefaultWeightedEdge targetEdge : targetEdges)
			result += targetGraph.getEdgeWeight(targetEdge);

		return result;
	}

	private double denominator() {
		double result = 0;

		for (DefaultWeightedEdge edge : this.sourceGraph.edgeSet())
			result += this.sourceGraph.getEdgeWeight(edge);
		for (DefaultWeightedEdge edge : this.targetGraph.edgeSet())
			result += this.targetGraph.getEdgeWeight(edge);

		return result;
	}

	public double solve() {
		if (this.edgea2a == -1) {
			double denominator = denominator();
			if (denominator != 0)
				this.edgea2a = (1 - numerator() / denominator) * 100;
			else
				throw new IllegalArgumentException(
					"One of the architectures was empty.");
		}
		return this.edgea2a;
	}
	//endregion
}