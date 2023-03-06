package edu.usc.softarch.arcade.metrics.evolution;

import edu.usc.softarch.arcade.clustering.data.ReadOnlyArchitecture;
import edu.usc.softarch.arcade.clustering.data.ReadOnlyCluster;
import edu.usc.softarch.arcade.util.McfpDriver;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EdgeA2a {
	//region PUBLIC INTERFACE
	public static double run(String sourceRsf, String targetRsf,
			String sourceDeps, String targetDeps) throws IOException {
		return (new EdgeA2a(sourceRsf, targetRsf, sourceDeps, targetDeps)).solve();
	}

	public static double run(File sourceRsf, File targetRsf,
			String sourceDeps, String targetDeps) throws IOException {
		try {
			return (new EdgeA2a(sourceRsf, targetRsf, sourceDeps, targetDeps)).solve();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Architectures "
				+ sourceRsf.getAbsolutePath() + " and "
				+ targetRsf.getAbsolutePath() + " were empty.", e);
		}
	}

	public static double run(File sourceRsf, File targetRsf, String sourceDeps,
			String targetDeps, double simThreshold) throws IOException {
		return (new EdgeA2a(sourceRsf, targetRsf,
			sourceDeps, targetDeps, simThreshold)).solve();
	}

	public static double run(File sourceRsf, File targetRsf, String sourceDeps,
			String targetDeps, double simThreshold, McfpDriver driver)
			throws IOException {
		return (new EdgeA2a(sourceRsf, targetRsf,
			sourceDeps, targetDeps, simThreshold, driver)).solve();
	}
	//endregion

	//region ATTRIBUTES
	private double edgea2a;
	private final Cvg cvg;
	private final Map<String, String> matches;
	private final SimpleDirectedGraph<String, DefaultEdge> sourceGraph;
	private final SimpleDirectedGraph<String, DefaultEdge> targetGraph;
	//endregion

	//region CONSTRUCTORS
	public EdgeA2a(String sourceRsf, String targetRsf,
			String sourceDeps, String targetDeps) throws IOException {
		this(new File(sourceRsf), new File(targetRsf), sourceDeps, targetDeps);
	}

	public EdgeA2a(File sourceRsf, File targetRsf,
			String sourceDeps, String targetDeps) throws IOException {
		this(sourceRsf, targetRsf, sourceDeps, targetDeps, 0.66);
	}

	public EdgeA2a(File sourceRsf, File targetRsf, String sourceDeps,
			String targetDeps, double simThreshold) throws IOException {
		this.edgea2a = -1;
		ReadOnlyArchitecture sourceClusters =
			ReadOnlyArchitecture.readFromRsf(sourceRsf);
		ReadOnlyArchitecture targetClusters =
			ReadOnlyArchitecture.readFromRsf(targetRsf);
		this.cvg = new Cvg(sourceClusters, targetClusters, simThreshold, 1.0);
		this.matches =
			new McfpDriver(sourceClusters, targetClusters).getMatchSet();
		this.sourceGraph = sourceClusters.buildGraph(sourceDeps);
		this.targetGraph = targetClusters.buildGraph(targetDeps);
	}

	public EdgeA2a(File sourceRsf, File targetRsf, String sourceDeps,
			String targetDeps, double simThreshold, McfpDriver driver)
			throws IOException {
		this.edgea2a = -1;
		ReadOnlyArchitecture sourceClusters =
			ReadOnlyArchitecture.readFromRsf(sourceRsf);
		ReadOnlyArchitecture targetClusters =
			ReadOnlyArchitecture.readFromRsf(targetRsf);
		this.cvg = new Cvg(sourceClusters, targetClusters, simThreshold, 1.0);
		this.matches = driver.getMatchSet();
		this.sourceGraph = sourceClusters.buildGraph(sourceDeps);
		this.targetGraph = targetClusters.buildGraph(targetDeps);
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

		Set<DefaultEdge> targetEdges = new HashSet<>(this.targetGraph.edgeSet());

		for (DefaultEdge sourceEdge : this.sourceGraph.edgeSet()) {
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
				result++;
				continue;
			}

			// Check if the edge still exists in the latter version
			DefaultEdge targetEdge =
				this.targetGraph.getEdge(targetC1Name, targetC2Name);
			if (targetEdge != null)
				targetEdges.remove(targetEdge);
		}

		// Add all unmatched edges left in the latter version
		result += targetEdges.size();

		return result;
	}

	private double denominator() {
		return this.sourceGraph.edgeSet().size()
			+ this.targetGraph.edgeSet().size();
	}

	public double solve() {
		if (this.edgea2a == -1) {
			double denominator = denominator();
			if (denominator != 0)
				this.edgea2a = (1 - numerator() / denominator) * 100;
			else
				throw new IllegalArgumentException(
					"Both architectures were empty.");
		}
		return this.edgea2a;
	}
	//endregion
}
